package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.databinding.AirConditionerItemBinding
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.android_dev.cleanairspaces.views.adapters.view_holders.AirConditionerViewHolder
import com.android_dev.cleanairspaces.views.adapters.view_holders.DevicesAdapterViewHolder

class DevicesAdapter(private val deviceListener: WatchedItemsActionListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val deviceList = ArrayList<TestAirConditioner>()
    private val devicesDetails = ArrayList<DevicesDetails>()
    private var aqiIndex: String? = null
    private val airConditionerList = ArrayList<AirConditionerEntity>()
    fun setWatchedDevicesList(deviceList: List<DevicesDetails>) {
        this.devicesDetails.clear()
        this.devicesDetails.addAll(deviceList)
        changeData()
    }
    fun setAirConditionerList(conditionerList: List<AirConditionerEntity>){
        this.airConditionerList.clear()
        this.airConditionerList.addAll(conditionerList)
        changeData()
    }
    private fun changeData(){
        deviceList.clear()
        for(item in devicesDetails){
            deviceList.add(TestAirConditioner(details = item))
        }
        for (item in airConditionerList){
            deviceList.add(TestAirConditioner(airConditionerEntity = item))
        }
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when(viewType){
            DEVICES -> DevicesAdapterViewHolder(DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            AIR_CONDITIONER_ITEM -> AirConditionerViewHolder(AirConditionerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> EmptyViewHolder(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = deviceList[position]
        if (item.details != null){
            (holder as DevicesAdapterViewHolder).bind(item.details,deviceListener, aqiIndex)
        }
        if (item.airConditionerEntity != null){
            (holder as AirConditionerViewHolder).bind(airConditioner = item.airConditionerEntity,listener = deviceListener)
        }
    }

    override fun getItemCount() = deviceList.size

    override fun getItemViewType(position: Int) = when {
        deviceList[position].details != null -> DEVICES
        deviceList[position].airConditionerEntity != null -> AIR_CONDITIONER_ITEM
        else -> 10
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        this.aqiIndex = selectedAQIIndex
        notifyDataSetChanged()
    }
    fun removeAt(adapterPosition: Int) {
        val device = deviceList[adapterPosition]
        device.details?.let { deviceListener.onSwipeToDeleteDevice(it) }
    }
    data class TestAirConditioner(val airConditionerEntity:AirConditionerEntity? = null, val details: DevicesDetails? = null)
    companion object {
        const val AIR_CONDITIONER_ITEM = 1
        const val DEVICES = 2
        const val TAG = "DevicesAdapter"
    }
    inner class EmptyViewHolder(val view:View):RecyclerView.ViewHolder(view)
}