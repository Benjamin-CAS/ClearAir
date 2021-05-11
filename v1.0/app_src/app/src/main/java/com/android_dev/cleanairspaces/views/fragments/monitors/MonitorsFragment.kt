package com.android_dev.cleanairspaces.views.fragments.monitors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentMonitorsBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.VerticalSpaceItemDecoration
import com.android_dev.cleanairspaces.utils.myTxt
import com.android_dev.cleanairspaces.views.adapters.MonitorsAdapter
import com.android_dev.cleanairspaces.views.fragments.details.LocationDetailsViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonitorsFragment : Fragment(), MonitorsAdapter.OnClickItemListener {

    companion object {
        private val TAG = MonitorsFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger

    private var _binding: FragmentMonitorsBinding? = null
    private val binding get() = _binding!!
    private val monitorsAdapter = MonitorsAdapter(this)

    private val viewModel: LocationDetailsViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMonitorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeWatchedLocation().observe(viewLifecycleOwner, {
            it?.let {
                updateGenDetails(it)
                observeMonitorsForLoc(it.actualDataTag)
            }
        })
        binding.searchMonitorsBtn.setOnClickListener {
            fetchMonitors()
        }

        binding.foundMonitors.apply {
            layoutManager = LinearLayoutManager(
                    requireContext(),
                    RecyclerView.VERTICAL,
                    false
            )
            adapter = monitorsAdapter
            addItemDecoration(VerticalSpaceItemDecoration(30))
        }

    }

    private fun fetchMonitors(){
            val username = binding.userName.myTxt(binding.userName)
            val password = binding.password.myTxt(binding.password)
            if (username.isNullOrBlank() || password.isNullOrBlank()) {
                Toast.makeText(
                        requireContext(),
                        R.string.monitor_credentials_required,
                        Toast.LENGTH_LONG
                ).show()
            } else {
                binding.apply {
                    progressCircular.isVisible = true
                    searchMonitorsBtn.isEnabled = false
                }
                viewModel.fetchMonitorsForLocation(username, password)
            }

    }

    private fun observeMonitorsForLoc(actualDataTag: String) {
        viewModel.observeMonitorsForLocation(locationsTag = actualDataTag).observe(viewLifecycleOwner, {
            it?.let {
                monitorsAdapter.updateSelectedAqiIndex(viewModel.aqiIndex)
                monitorsAdapter.setWatchedMonitorsList(it)
                binding.progressCircular.isVisible = false
            }
        })
    }

    private fun updateGenDetails(currentlyWatchedLocationHighLights: WatchedLocationHighLights) {
        val logoURL = currentlyWatchedLocationHighLights.getFullLogoUrl()
        myLogger.logThis(
                tag = LogTags.USER_ACTION_OPEN_SCREEN,
                from = TAG,
                msg = "viewing ${currentlyWatchedLocationHighLights.name}"
        )
        binding.apply {
            if (logoURL.isNotBlank()) {
                locationLogo.isVisible = true
                Glide.with(requireContext())
                        .load(logoURL)
                        .into(locationLogo)
            }
            locationNameTv.text = if(currentlyWatchedLocationHighLights.location_area.isNotBlank())
                currentlyWatchedLocationHighLights.location_area
            else currentlyWatchedLocationHighLights.name
            locationNameTv.isSelected = true
            val isSecure =currentlyWatchedLocationHighLights.is_secure
            userName.isVisible = isSecure
            password.isVisible = isSecure
            searchMonitorsBtn.isVisible = isSecure
            if (!isSecure){
                //password and username are not required
                binding.apply {
                    progressCircular.isVisible = true
                }
                viewModel.fetchMonitorsForLocation(username="", password = "")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClickWatchedMonitor(monitor: MonitorDetails) {
      if (!monitor.watch_location){
          viewModel.watchThisMonitor(monitor)
      }else{
          //todo show history --if is watched
          val i = 0
      }
    }

    override fun onSwipeToDeleteMonitor(monitor: MonitorDetails) {
       //do nothing --
    }
}