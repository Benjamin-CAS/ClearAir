package com.cleanairspaces.android.ui.details.history_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.cleanairspaces.android.R
import com.cleanairspaces.android.databinding.FragmentHistoryBinding
import com.cleanairspaces.android.models.entities.LocationHistoryMonth
import com.cleanairspaces.android.models.entities.LocationHistoryThreeDays
import com.cleanairspaces.android.models.entities.LocationHistoryWeek
import com.cleanairspaces.android.ui.details.LocationDetailsViewModel
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.UNSET_PARAM_VAL
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HistoryFragment : Fragment() {

    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
    }

    private var graphParamSelected: TextView? = null
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDetailsViewModel by activityViewModels()

    private val openSansLight by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.open_sans_condensed_light)
    }

    private val openSans by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.open_sans)
    }


    private val greenColor by lazy { ContextCompat.getColor(requireContext(), R.color.green) }
    private val yellowColor by lazy { ContextCompat.getColor(requireContext(), R.color.yellow) }
    private val redColor by lazy { ContextCompat.getColor(requireContext(), R.color.red) }
    private val blackColor by lazy { ContextCompat.getColor(requireContext(), R.color.black) }

    private val checkIcon by lazy { ContextCompat.getDrawable(requireContext(), R.drawable.ic_blue_check) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setParametersClickListeners()
        setSelectedParamView(binding.aqiTv)
        styleBarChart(binding.daysChart)
        styleBarChart(binding.weekChart)
        styleBarChart(binding.monthChart)

        viewModel.observeLocationDetails().observe(viewLifecycleOwner, Observer {
            if (!viewModel.graphParamsSet) {
                //we only want to listen once
                viewModel.graphParamsSet = true
                val showIndoor = (it.indoorPmValue != UNSET_PARAM_VAL)
                toggleIndoorParameters(show = showIndoor)
                observeHistory() //TODO not correct ---
            }
        })

    }

    private fun observeHistory(){
           viewModel.observeLocationDaysHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                updateDaysHistoryChart(it)
            }
        }
        )

        viewModel.observeLocationWeekHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                updateWeekHistoryChart(it)
            }
        }
        )

        viewModel.observeLocationMonthHistory().observe(
                viewLifecycleOwner, { list ->
            list?.let {
                updateMonthHistoryChart(it)
            }
        }
        )
    }

    /*********** graph controls ***********/
    private fun setParametersClickListeners(){
        binding.apply {
            aqiTv.setOnClickListener {
                setSelectedParamView(aqiTv)
            }
            tvocTv.setOnClickListener {
                setSelectedParamView(tvocTv)
            }
            tmpTv.setOnClickListener {
                setSelectedParamView(tmpTv)
            }
            humidityTv.setOnClickListener {
                setSelectedParamView(humidityTv)
            }
            co2Tv.setOnClickListener {
                setSelectedParamView(co2Tv)
            }
            outdoorsTv.setOnClickListener {
                setSelectedParamView(outdoorsTv)
            }
        }
    }
    private fun clearSelectedParamView(){
            graphParamSelected?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
    }

    private fun setSelectedParamView(selectedTv: TextView) {
        clearSelectedParamView()
        selectedTv.setCompoundDrawablesWithIntrinsicBounds(null, null, checkIcon, null)
        graphParamSelected = selectedTv
    }

    private fun toggleIndoorParameters(show : Boolean){
        binding.apply {
            pmCard.isVisible = show
            tmpCard.isVisible = show
            co2Card.isVisible = show
            humidityCard.isVisible = show
            tvocCard.isVisible = show
        }
    }



    /********** bar chart **********/
    private fun styleBarChart(barChart: BarChart) {
        barChart.apply {
            axisRight.isEnabled = false
            axisLeft.isEnabled = false

            val legendGood = LegendEntry()
            legendGood.label = getString(R.string.good_air_status_txt)
            legendGood.formColor = greenColor

            val legendModerate = LegendEntry()
            legendModerate.label = getString(R.string.moderate_air_status_txt)
            legendModerate.formColor = yellowColor

            val legendBad = LegendEntry()
            legendBad.label = getString(R.string.danger_txt)
            legendBad.formColor = redColor

            legend.apply {
                typeface = openSans
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                yOffset = 0f
                setCustom(listOf(legendGood, legendModerate, legendBad))
            }

            xAxis.apply {
                labelRotationAngle = 0f
                setDrawGridLines(false)
                axisMaximum = 4.0f
                axisMinimum = 0.0f
                position = XAxis.XAxisPosition.BOTTOM
                typeface = openSansLight
                textSize = 12f
            }

            setNoDataTextTypeface(openSansLight)


            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false
            setNoDataText(getString(R.string.loading_graph_data))
            setNoDataTextTypeface(openSans)
            setNoDataTextColor(blackColor)
            animateX(1000, Easing.EaseInExpo)
        }
    }

    private fun styleDataSet(barDataSet: BarDataSet, valColorMap: ArrayList<Int>) {
        barDataSet.apply {
            //color of the bar
            colors = valColorMap
            //Setting the size of the legend box
            formSize = 10f
            //showing the value of the bar, default true if not set
            setDrawValues(true)
            //setting the text size of the value of the bar
            valueTextSize = 12f
            valueTextColor = blackColor
            valueTypeface = openSansLight

        }
    }

    private fun updateDaysHistoryChart(daysHistory: List<LocationHistoryThreeDays>) {
        try {
            val title = getString(R.string.last_seven_two_hours)
            val entries = ArrayList<BarEntry>()
            val trial = arrayOf(10, 20, 30, 40, 50) //Y AXIS
            val valColorMap = ArrayList<Int>()
            for ((i, day) in trial.withIndex()) {
                val entry = BarEntry(i.toFloat(), day.toFloat())
                entries.add(entry)
                when {
                    day <= 20 -> valColorMap.add(greenColor)
                    day in 21..40 -> valColorMap.add(yellowColor)
                    else -> valColorMap.add(redColor)
                }
            }
            val daysHistoryDataSet = MyBarDataSet(entries, title)
            styleDataSet(daysHistoryDataSet, valColorMap)

            binding.daysChart.apply {
                data = BarData(daysHistoryDataSet)
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "updateDaysHistoryChart()",
                    "new data ${daysHistory.size} in size ${e.message}",
                    e
            )
        }
    }

    private fun updateWeekHistoryChart(weekHistory: List<LocationHistoryWeek>) {
        try {
            val title = getString(R.string.last_week_lbl)
            val entries = ArrayList<BarEntry>()
            val trial = arrayOf(10, 20, 30, 40, 50) //Y AXIS
            val valColorMap = ArrayList<Int>()
            for ((i, day) in trial.withIndex()) {
                val entry = BarEntry(i.toFloat(), day.toFloat())
                entries.add(entry)
                when {
                    day <= 20 -> valColorMap.add(greenColor)
                    day in 21..40 -> valColorMap.add(yellowColor)
                    else -> valColorMap.add(redColor)
                }
            }
            val daysHistoryDataSet = MyBarDataSet(entries, title)
            styleDataSet(daysHistoryDataSet, valColorMap)

            binding.weekChart.apply {
                data = BarData(daysHistoryDataSet)
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "updateWeekHistoryChart()",
                    "new data ${weekHistory.size} in size ${e.message}",
                    e
            )
        }
    }

    private fun updateMonthHistoryChart(monthHistory: List<LocationHistoryMonth>) {
        try {
            val title = getString(R.string.last_thirty_lbl)
            val entries = ArrayList<BarEntry>()
            val trial = arrayOf(10, 20, 30, 40, 50) //Y AXIS
            val valColorMap = ArrayList<Int>()
            for ((i, day) in trial.withIndex()) {
                val entry = BarEntry(i.toFloat(), day.toFloat())
                entries.add(entry)
                when {
                    day <= 20 -> valColorMap.add(greenColor)
                    day in 21..40 -> valColorMap.add(yellowColor)
                    else -> valColorMap.add(redColor)
                }
            }
            val daysHistoryDataSet = MyBarDataSet(entries, title)
            styleDataSet(daysHistoryDataSet, valColorMap)

            binding.monthChart.apply {
                data = BarData(daysHistoryDataSet)
                notifyDataSetChanged()
            }
        } catch (e: Exception) {
            MyLogger.logThis(
                    TAG, "updateMonthHistoryChart()",
                    "new data ${monthHistory.size} in size ${e.message}",
                    e
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

internal class MyBarDataSet(yVals: List<BarEntry?>?,
                            label: String?) : BarDataSet(yVals, label) {
    override fun getEntryIndex(e: BarEntry?): Int {
        return super.getEntryIndex(e)
    }

    override fun getColor(index: Int): Int {
        MyLogger.logThis("coloring bar", "called", "$index")
        return mColors[index]
    }
}
