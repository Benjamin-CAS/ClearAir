package com.android_dev.cleanairspaces.views.fragments.details_tabbed.devices

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentDevicesBinding
import com.android_dev.cleanairspaces.persistence.api.mqtt.CasMqttClient
import com.android_dev.cleanairspaces.persistence.api.mqtt.DeviceUpdateMqttMessage
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.VerticalSpaceItemDecoration
import com.android_dev.cleanairspaces.utils.myTxt
import com.android_dev.cleanairspaces.views.adapters.DevicesAdapter
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DevicesFragment : Fragment(), DevicesAdapter.OnClickItemListener {

    companion object {
        private val TAG = DevicesFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!
    private val devicesAdapter = DevicesAdapter(this)

    private val viewModel: DevicesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeWatchedLocationWithAqi().observe(viewLifecycleOwner, {
            it?.let {
                viewModel.aqiIndex = it.aqiIndex
                updateGenDetails(it.watchedLocationHighLights)
                observeDevicesForLoc(
                    actualDataTag = it.watchedLocationHighLights.actualDataTag,
                    aqiIndex = it.aqiIndex
                )
            }
        })
        binding.searchDevicesBtn.setOnClickListener {
            fetchDevices()
        }

        binding.foundDevices.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
            )
            adapter = devicesAdapter
            addItemDecoration(VerticalSpaceItemDecoration(30))
        }

        viewModel.observeDeviceLoading().observe(viewLifecycleOwner, {
            it?.let { loadingState ->

                when(loadingState) {
                    DevicesLoadingState.IDLE -> {
                        toggleProgressVisibility(false)
                    }
                    DevicesLoadingState.LOADING -> {
                        toggleProgressVisibility(true)
                    }
                    DevicesLoadingState.LOADED_SUCCESS -> {
                        toggleProgressVisibility(false)
                    }
                    DevicesLoadingState.LOADED_SUCCESS_EMPTY -> {
                        toggleProgressVisibility(false)
                        Toast.makeText(
                            requireContext(),
                            R.string.no_devices_found,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    DevicesLoadingState.LOADING_FAILED -> {
                        toggleProgressVisibility(false)
                        Toast.makeText(
                            requireContext(),
                            R.string.invalid_device_credentials,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })

        viewModel.getMqttMessage().observe(
            viewLifecycleOwner, Observer {
               it?.let{ newMsg ->
                    connectAndPublish(newMsg)
                }
            }
        )
    }

    private fun fetchDevices() {
        val username = binding.userName.myTxt(binding.userName)
        val password = binding.password.myTxt(binding.password)
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                R.string.device_credentials_required,
                Toast.LENGTH_LONG
            ).show()
        } else {
            viewModel.fetchDevicesForLocation(username, password)
        }

    }

    private fun toggleProgressVisibility(isLoadingDevices: Boolean) {
        binding.apply {
            progressCircular.isVisible = isLoadingDevices
            searchDevicesBtn.isEnabled = !isLoadingDevices
        }
    }

    private fun observeDevicesForLoc(
        actualDataTag: String,
        aqiIndex: String?
    ) {
        viewModel.observeDevicesForLocation(locationsTag = actualDataTag)
            .observe(viewLifecycleOwner, {
                it?.let {
                    binding.progressCircular.isVisible = false

                    if (it.isNotEmpty())
                        toggleCredentialsFormVisibility(show = false)
                    devicesAdapter.updateSelectedAqiIndex(aqiIndex)
                    devicesAdapter.setWatchedDevicesList(it)
                }
            })
    }

    private fun updateGenDetails(currentlyWatchedLocationHighLights: WatchedLocationHighLights) {
        val logoURL = currentlyWatchedLocationHighLights.getFullLogoUrl()

        lifecycleScope.launch(Dispatchers.IO) {
            myLogger.logThis(
                tag = LogTags.USER_ACTION_OPEN_SCREEN,
                from = TAG,
                msg = "viewing devices for ${currentlyWatchedLocationHighLights.name}"
            )
        }
        binding.apply {
            if (logoURL.isNotBlank()) {
                locationLogo.isVisible = true
                Glide.with(requireContext())
                    .load(logoURL)
                    .into(locationLogo)
            }
            locationNameTv.text = if (currentlyWatchedLocationHighLights.name.isNotBlank())
                currentlyWatchedLocationHighLights.name
            else currentlyWatchedLocationHighLights.location_area
            locationNameTv.isSelected = true
        }
        toggleCredentialsFormVisibility(show = true)
    }

    private fun toggleCredentialsFormVisibility(show: Boolean) {
        binding.apply {
            userName.isVisible = show
            password.isVisible = show
            searchDevicesBtn.isVisible = show
        }
    }


    /*************************** DEVICE RELATED ACTIONS ********************/
    override fun onClickToggleWatchDevice(device: DevicesDetails) {
        viewModel.watchThisDevice(device, watchDevice = !device.watch_device)
    }

    override fun onSwipeToDeleteDevice(device: DevicesDetails) {
        //do nothing --
    }

    override fun onToggleFreshAir(device: DevicesDetails, status : String) {
      viewModel.onToggleFreshAir(device, status)
    }

    override fun onToggleFanSpeed(device: DevicesDetails, status : String, speed: String?) {
        viewModel.onToggleFanSpeed(device, status, speed)
    }

    override fun onToggleMode(device: DevicesDetails, toMode: String) {
        viewModel.onToggleMode(device, toMode)
    }

    override fun onToggleDuctFit(device: DevicesDetails, status : String) {
        viewModel.onToggleDuctFit(device, status)
    }


    /************ MQTT **************/
    @Inject lateinit var casMqttClient: CasMqttClient
    private fun connectAndPublish(deviceUpdateMqttMessage: DeviceUpdateMqttMessage) {
        casMqttClient.connectAndPublish(deviceUpdateMqttMessage)
        viewModel.setMqttStatus(null) //clear
        viewModel.refreshDevicesAfterDelay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        casMqttClient.disconnect()
    }

}