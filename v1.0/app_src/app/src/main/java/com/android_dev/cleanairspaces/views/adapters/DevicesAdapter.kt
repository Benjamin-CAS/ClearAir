package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.utils.DevicesTypes

class DevicesAdapter(private val deviceListener: OnClickItemListener) :
    RecyclerView.Adapter<DevicesAdapter.DevicesAdapterViewHolder>() {

    private var isForIndoorLoc: Boolean = true
    private val deviceList = ArrayList<DevicesDetails>()

    private var aqiIndex: String? = null

    interface OnClickItemListener {
        fun onClickWatchedDevice(device: DevicesDetails)
        fun onSwipeToDeleteDevice(device: DevicesDetails)
    }

    class DevicesAdapterViewHolder(private val binding: DeviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            device: DevicesDetails,
            deviceListener: OnClickItemListener,
            aqiIndex: String?,
            isForIndoorLoc: Boolean
        ) {
            val ctx = itemView.context
            binding.apply {
                DevicesTypes.getDeviceInfoByType(device.device_type)?.let { typeInfo ->
                    deviceTypeVal.setText(
                        typeInfo.deviceTitleRes
                    )
                }
                deviceName.text = device.dev_name
                if (device.isOn()) {

                }
            }

        }
    }


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
        holder.bind(device, deviceListener, aqiIndex, isForIndoorLoc)
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

    fun updateLocationType(indoorLoc: Boolean) {
        this.isForIndoorLoc = indoorLoc
        notifyDataSetChanged()
    }
}