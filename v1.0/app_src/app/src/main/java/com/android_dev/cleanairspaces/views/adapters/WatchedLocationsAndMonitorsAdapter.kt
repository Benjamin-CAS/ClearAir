package com.android_dev.cleanairspaces.views.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.databinding.WatchedLocationItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.android_dev.cleanairspaces.views.adapters.view_holders.DevicesAdapterViewHolder
import com.android_dev.cleanairspaces.views.adapters.view_holders.WatchedLocationsViewHolder

class WatchedLocationsAndDevicesAdapter(
    private val listener: WatchedItemsActionListener,
    private val displayFav: Boolean = true) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DEVICES = 1
        const val VIEW_TYPE_LOCATIONS = 2
        private val TAG = WatchedLocationsAndDevicesAdapter::class.java.simpleName
    }

    private val dataset = ArrayList<LocationMonitorWrapper>()
    private val devicesList = ArrayList<DevicesDetails>()
    private val locationsList = ArrayList<WatchedLocationHighLights>()

    private var aqiIndex: String? = null


    fun setWatchedDevicesList(deviceList: List<DevicesDetails>) {
        this.devicesList.clear()
        this.devicesList.addAll(deviceList)
        changeDataset()
    }


    fun setWatchedLocationsList(locationList: List<WatchedLocationHighLights>) {
        this.locationsList.clear()
        this.locationsList.addAll(locationList)
        changeDataset()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun changeDataset() {
        dataset.clear()
        for (newLoc in locationsList)
            dataset.add(
                LocationMonitorWrapper(
                    locationHighLights = newLoc
                )
            )
        for (newMonitor in devicesList)
            dataset.add(
                LocationMonitorWrapper(
                    device = newMonitor
                )
            )

        notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        val item = dataset[adapterPosition]
        item.device?.let {
            listener.onSwipeToDeleteDevice(it)
        }
        item.locationHighLights?.let {
            listener.onSwipeToDeleteLocation(it)
        }
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        this.aqiIndex = selectedAQIIndex
        notifyDataSetChanged()
    }


    /************** Recycler view Holder *******/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == VIEW_TYPE_DEVICES) DevicesAdapterViewHolder(
            DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ) else WatchedLocationsViewHolder(
            WatchedLocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val item = dataset[position]
            Log.e(TAG, "onBindViewHolder: $dataset")
            if (item.device != null) {
                (holder as DevicesAdapterViewHolder).bind(
                    device = item.device,
                    deviceListener = listener,
                    aqiIndex = aqiIndex,
                    displayFav = displayFav
                )
            } else {
                Log.e(TAG, "onBindViewHolder: ${item.device}")
                (holder as WatchedLocationsViewHolder).bind(
                    location = item.locationHighLights!!,
                    locationListener = listener,
                    aqiIndex = aqiIndex
                )
            }
        } catch (exc: Exception) {
            Log.e(TAG, "${exc.message}", exc)
        }
    }

    override fun getItemCount() = dataset.size

    override fun getItemViewType(position: Int) = when {
        dataset[position].device != null -> VIEW_TYPE_DEVICES
        else -> VIEW_TYPE_LOCATIONS
    }
    data class LocationMonitorWrapper(
        val device: DevicesDetails? = null,
        val locationHighLights: WatchedLocationHighLights? = null
    )

}