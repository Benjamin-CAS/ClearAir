package com.cleanairspaces.android.ui.details.history_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.cleanairspaces.android.databinding.FragmentHistoryBinding
import com.cleanairspaces.android.models.entities.LocationHistoryMonth
import com.cleanairspaces.android.models.entities.LocationHistoryThreeDays
import com.cleanairspaces.android.models.entities.LocationHistoryWeek
import com.cleanairspaces.android.ui.details.LocationDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

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
                        drawDaysHistoryChart(it)
                    }
             }
        )

        viewModel.observeLocationWeekHistory().observe(
                viewLifecycleOwner, Observer { list ->
            list?.let {
                drawWeekHistoryChart(it)
            }
        }
        )

        viewModel.observeLocationMonthHistory().observe(
                viewLifecycleOwner, Observer { list ->
            list?.let {
                drawMonthHistoryChart(it)
            }
        }
        )
    }


    private fun drawDaysHistoryChart(daysHistory: List<LocationHistoryThreeDays>) {
        
    }

    private fun drawWeekHistoryChart(weekHistory: List<LocationHistoryWeek>) {

    }

    private fun drawMonthHistoryChart(monthHistory: List<LocationHistoryMonth>) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}