package com.android_dev.cleanairspaces.views.adapters.view_holders

import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.AirConditionerItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.bumptech.glide.Glide

class AirConditionerViewHolder(private val binding:AirConditionerItemBinding):RecyclerView.ViewHolder(binding.root) {
    fun bind(
        listener: WatchedItemsActionListener,
        airConditioner:AirConditionerEntity,
        displayFav: Boolean = true
    ){
        Log.e(TAG, "bind: 可以了 $airConditioner")
        if (displayFav) {
            val watchIndicator = if (airConditioner.watchAirConditioner) {
                R.drawable.ic_fav
            } else {
                R.drawable.ic_not_fav
            }
            Glide.with(itemView.context)
                .load(watchIndicator)
                .into(binding.favoriteDevice)
        }
        binding.favoriteDevice.setOnClickListener {
            listener.onClickToggleWatchAirConditioner(airConditioner)
        }
        binding.apply {
            toggleGroupOperationMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when(checkedId){
                    R.id.inside_zone_btn -> {
                        Log.e(TAG, "bind: $isChecked")
                        fanBtn.isClickable = false
                        coolingBtn.isClickable = false
                        heatingBtn.isClickable = false
                        autoBtn.isClickable = false
                        fanModeManualBtn.isClickable = false
                        fanModeAutoBtn.isClickable = false
                        offBtn.isClickable = false
                        LowBtn.isClickable = false
                        MediumBtn.isClickable = false
                        heightBtn.isClickable = false
                        editTargetTemp.isVisible = false
                    }
                    R.id.independent_btn -> {
                        Log.e(TAG, "bind: $isChecked")
                        fanBtn.isClickable = true
                        coolingBtn.isClickable = true
                        heatingBtn.isClickable = true
                        autoBtn.isClickable = true
                        fanModeManualBtn.isClickable = true
                        fanModeAutoBtn.isClickable = true
                        offBtn.isClickable = true
                        LowBtn.isClickable = true
                        MediumBtn.isClickable = true
                        heightBtn.isClickable = true
                        editTargetTemp.isVisible = true
                    }
                }
            }
            airConditioner.zoneModeSelect()?.let { toggleGroupOperationMode.check(it) }
            toggleBtnAcMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when(checkedId){
                    R.id.fan_btn -> {

                    }
                    R.id.cooling_btn -> {

                    }
                    R.id.heating_btn -> {

                    }
                    R.id.auto_btn -> {

                    }
                }
            }
            airConditioner.lastModeSelect()?.let { toggleBtnAcMode.check(it) }
            toggleBtnFanMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when(checkedId){
                    R.id.fan_mode_manual_btn -> {
                        offBtn.isClickable = true
                        LowBtn.isClickable = true
                        MediumBtn.isClickable = true
                        heightBtn.isClickable = true
                    }
                    R.id.fan_mode_auto_btn -> {
                        offBtn.isClickable = false
                        LowBtn.isClickable = false
                        MediumBtn.isClickable = false
                        heightBtn.isClickable = false
                    }
                }
            }
            airConditioner.lastAutoSelect()?.let { toggleBtnFanMode.check(it) }
            toggleBtnSpeed.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when(checkedId){
                    R.id.off_btn -> {

                    }
                    R.id.Low_btn -> {

                    }
                    R.id.Medium_btn -> {

                    }
                    R.id.height_btn -> {

                    }
                }
            }
            airConditioner.lastFanSelect()?.let { toggleBtnSpeed.check(it) }
            addBtn.setOnClickListener {
                var targetNum = targetTemp.text.toString().toFloat()
                targetNum += 0.5f
                if (targetNum in 12f..40f){
                    targetTemp.text = targetNum.toString()
                }

            }
            reduceBtn.setOnClickListener {
                var targetNum = targetTemp.text.toString().toFloat()
                targetNum -= 0.5f
                if (targetNum in 12f..40f){
                    targetTemp.text = targetNum.toString()
                }
            }
            deviceNameConditioner.text = airConditioner.devName
            deviceMacConditioner.text = airConditioner.mac
            deviceTypeConditioner.text = airConditioner.deviceType
            currentTemp.text = airConditioner.current
            targetTemp.text = airConditioner.target
        }
    }
    companion object {
        const val TAG = "AirConditionerViewHolder"
    }
}