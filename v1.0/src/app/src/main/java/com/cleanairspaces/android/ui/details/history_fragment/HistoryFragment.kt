package com.cleanairspaces.android.ui.details.history_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    companion object {
        private val TAG = HistoryFragment::class.java.simpleName
    }

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDetailsViewModel by activityViewModels()

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
        viewModel.observeLocationDaysHistory().observe(
                viewLifecycleOwner, Observer { list ->
            list?.let {
                updateDaysHistoryChart(it)
            }
        }
        )

        viewModel.observeLocationWeekHistory().observe(
                viewLifecycleOwner, Observer { list ->
            list?.let {
                updateWeekHistoryChart(it)
            }
        }
        )

        viewModel.observeLocationMonthHistory().observe(
                viewLifecycleOwner, Observer { list ->
            list?.let {
                updateMonthHistoryChart(it)
            }
        }
        )
    }


    private fun updateDaysHistoryChart(daysHistory: List<LocationHistoryThreeDays>) {
        try {
            val entries = ArrayList<Entry>()
            for ((i, day) in daysHistory.withIndex()) {
                if(day.reading_comp != null)
                    entries.add(Entry(i.toFloat(), day.reading_comp!!.toFloat()))
            }
            val daysHistoryDataSet = LineDataSet(entries, LocationHistoryThreeDays.responseKey)
            daysHistoryDataSet.setDrawValues(false)
            daysHistoryDataSet.setDrawFilled(true)
            daysHistoryDataSet.lineWidth = 3f
            daysHistoryDataSet.fillColor = R.color.green
            daysHistoryDataSet.fillAlpha = R.color.yellow

            binding.daysChart.apply {
                xAxis.labelRotationAngle = 0f
                data = LineData(daysHistoryDataSet)
                axisRight.isEnabled = false
                xAxis.axisMaximum = 1 + 0.1f
                setTouchEnabled(true)
                setPinchZoom(true)
                description.text = "Days"
                setNoDataText("Loading data ...")
                animateX(1000, Easing.EaseInExpo)
            }

        }catch (e : Exception){
            MyLogger.logThis(
                    TAG, "updateDaysHistoryChart()",
                    "new data ${daysHistory.size} in size ${e.message}",
                    e
            )
        }
    }

    private fun updateWeekHistoryChart(weekHistory: List<LocationHistoryWeek>) {
        try {
            val entries = ArrayList<Entry>()
            for ((i, day) in weekHistory.withIndex()) {
                if(day.reading_comp != null)
                    entries.add(Entry(i.toFloat(), day.reading_comp!!.toFloat()))
            }
            val daysHistoryDataSet = LineDataSet(entries, LocationHistoryThreeDays.responseKey)
            daysHistoryDataSet.setDrawValues(false)
            daysHistoryDataSet.setDrawFilled(true)
            daysHistoryDataSet.lineWidth = 3f
            daysHistoryDataSet.fillColor = R.color.green
            daysHistoryDataSet.fillAlpha = R.color.yellow

            binding.weekChart.apply {
                xAxis.labelRotationAngle = 0f
                data = LineData(daysHistoryDataSet)
                axisRight.isEnabled = false
                xAxis.axisMaximum = 1 + 0.1f
                setTouchEnabled(true)
                setPinchZoom(true)
                description.text = "Week"
                setNoDataText("Loading data ...")
                animateX(1000, Easing.EaseInExpo)
            }

        }catch (e : Exception){
            MyLogger.logThis(
                    TAG, "updateWeekHistoryChart()",
                    "new data ${weekHistory.size} in size ${e.message}",
                    e
            )
        }
    }

    private fun updateMonthHistoryChart(monthHistory: List<LocationHistoryMonth>) {
        try {
            val entries = ArrayList<Entry>()
            for ((i, day) in monthHistory.withIndex()) {
                if(day.reading_comp != null)
                    entries.add(Entry(i.toFloat(), day.reading_comp!!.toFloat()))
            }
            val daysHistoryDataSet = LineDataSet(entries, LocationHistoryThreeDays.responseKey)
            daysHistoryDataSet.setDrawValues(false)
            daysHistoryDataSet.setDrawFilled(true)
            daysHistoryDataSet.lineWidth = 3f
            daysHistoryDataSet.fillColor = R.color.green
            daysHistoryDataSet.fillAlpha = R.color.yellow

            binding.monthChart.apply {
                xAxis.labelRotationAngle = 0f
                data = LineData(daysHistoryDataSet)
                axisRight.isEnabled = false
                xAxis.axisMaximum = 1 + 0.1f
                setTouchEnabled(true)
                setPinchZoom(true)
                description.text = "Month"
                setNoDataText("Loading data ...")
                animateX(1000, Easing.EaseInExpo)
            }

        }catch (e : Exception){
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