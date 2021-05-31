package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.android_dev.cleanairspaces.views.adapters.view_holders.DevicesAdapterViewHolder

class DevicesAdapter(private val deviceListener: WatchedItemsActionListener) :
    RecyclerView.Adapter<DevicesAdapterViewHolder>() {

    private val deviceList = ArrayList<DevicesDetails>()

    private var aqiIndex: String? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DevicesAdapterViewHolder {
        val binding =
            DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DevicesAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DevicesAdapterViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device, deviceListener, aqiIndex)
    }

    fun setWatchedDevicesList(deviceList: List<DevicesDetails>) {
        this.deviceList.clear()
        this.deviceList.addAll(deviceList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        this.aqiIndex = selectedAQIIndex
        notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        val device = deviceList[adapterPosition]
        deviceListener.onSwipeToDeleteDevice(device)
    }

}