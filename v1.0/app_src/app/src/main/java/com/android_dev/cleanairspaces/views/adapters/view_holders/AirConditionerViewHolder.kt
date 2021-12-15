package com.android_dev.cleanairspaces.views.adapters.view_holders

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.AirConditionerItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.AirConditionerEntity
import com.android_dev.cleanairspaces.utils.airConditionerId
import com.android_dev.cleanairspaces.views.adapters.action_listeners.ManagerBtnClick
import com.android_dev.cleanairspaces.views.adapters.action_listeners.OnRepeatAirConditionerListener
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.bumptech.glide.Glide

class AirConditionerViewHolder(private val binding: AirConditionerItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private var opMode = ""
    private var acMode = ""
    private var fanMode = ""
    private var currentMode = ""
    private var targetTempNum = ""

    @SuppressLint("Recycle")
    fun bind(
        listener: WatchedItemsActionListener,
        airConditioner: AirConditionerEntity,
        displayFav: Boolean = true,
        onRepeatAirConditionerListener: OnRepeatAirConditionerListener,
        managerBtnClick: ManagerBtnClick
    ) {
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
        binding.constraintLayouts.isVisible = false
        binding.dataDetail.isVisible = true
        binding.managerBtn.setOnClickListener {
            managerBtnClick.managerBtnClickListener(airConditioner)?.let {
                Log.e(TAG, "constraintLayouts: $it")
                val anim = if (it) ObjectAnimator.ofFloat(
                    binding.constraintLayouts,
                    "alpha",
                    1f,
                    0f
                ) else ObjectAnimator.ofFloat(binding.constraintLayouts, "alpha", 0f, 1f)
                anim.duration = 1000
                anim.start()
                binding.constraintLayouts.isVisible = !it
                binding.dataDetail.isVisible = it
            }
        }
        binding.apply {
            currentTempText.text = airConditioner.current
            targetTempText.text = airConditioner.target
            toggleGroupOperationMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
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
                        opMode = "2"
                        onRepeatAirConditionerListener.cancelAirConditioner(airConditionerId)
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
                        opMode = "1"
                    }
                }
            }
            airConditioner.zoneModeSelect()?.let {
                toggleGroupOperationMode.check(it.btnId)
                opMode = it.num
                binding.operationModeText.text = it.text
            }
            toggleBtnAcMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.fan_btn -> {
                        acMode = "0"
                    }
                    R.id.cooling_btn -> {
                        acMode = "1"
                    }
                    R.id.heating_btn -> {
                        acMode = "2"
                    }
                    R.id.auto_btn -> {
                        acMode = "3"
                    }
                }
            }
            airConditioner.lastModeSelect()?.let {
                toggleBtnAcMode.check(it.btnId)
                acMode = it.num
                binding.acModeText.text = it.text
            }
            toggleBtnFanMode.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.fan_mode_manual_btn -> {
                        offBtn.isClickable = true
                        LowBtn.isClickable = true
                        MediumBtn.isClickable = true
                        heightBtn.isClickable = true
                        fanMode = "1"
                    }
                    R.id.fan_mode_auto_btn -> {
                        offBtn.isClickable = false
                        LowBtn.isClickable = false
                        MediumBtn.isClickable = false
                        heightBtn.isClickable = false
                        fanMode = "2"
                    }
                }
            }
            airConditioner.lastAutoSelect()?.let {
                toggleBtnFanMode.check(it.btnId)
                fanMode = it.num
                binding.fanModeText.text = it.text
            }
            toggleBtnSpeed.addOnButtonCheckedListener { group, checkedId, isChecked ->
                when (checkedId) {
                    R.id.off_btn -> {
                        currentMode = "0"
                    }
                    R.id.Low_btn -> {
                        currentMode = "1"
                    }
                    R.id.Medium_btn -> {
                        currentMode = "2"
                    }
                    R.id.height_btn -> {
                        currentMode = "3"
                    }
                }
            }
            airConditioner.lastFanSelect()?.let {
                toggleBtnSpeed.check(it.btnId)
                currentMode = it.num
                binding.currentFanSpeedText.text = it.text
            }
            addBtn.setOnClickListener {
                var targetNum = targetTemp.text.toString().toFloat()
                targetNum += 0.5f
                if (targetNum in 12f..40f) {
                    targetTemp.text = targetNum.toString()
                    targetTempNum = targetNum.toString()
                }
            }
            reduceBtn.setOnClickListener {
                var targetNum = targetTemp.text.toString().toFloat()
                targetNum -= 0.5f
                if (targetNum in 12f..40f) {
                    targetTemp.text = targetNum.toString()
                    targetTempNum = targetNum.toString()
                }
            }
            submitCancelBtn.setOnClickListener {
                onRepeatAirConditionerListener.cancelAirConditioner(airConditionerId)
                managerBtnClick.managerBtnClickListener(airConditioner)?.let {
                    Log.e(TAG, "constraintLayouts: $it")
                    val anim = if (it) ObjectAnimator.ofFloat(
                        binding.constraintLayouts,
                        "alpha",
                        1f,
                        0f
                    ) else ObjectAnimator.ofFloat(binding.constraintLayouts, "alpha", 0f, 1f)
                    anim.duration = 1000
                    anim.start()
                    binding.constraintLayouts.isVisible = !it
                    binding.dataDetail.isVisible = it
                }
            }
            submitBtn.setOnClickListener {
                val message =
                    "${currentMode}${opMode}${fanMode}${acMode}00${targetTempNum.replace(".", "")}"
                if (message.isNotBlank()) onRepeatAirConditionerListener.submitAirConditioner(
                    airConditionerId,
                    airConditioner.mac.trim(),
                    message
                )
            }
            deviceNameConditioner.text = airConditioner.devName
            deviceMacConditioner.text = airConditioner.mac
            deviceTypeConditioner.text = airConditioner.deviceType
            currentTemp.text = airConditioner.current
            targetTemp.text = airConditioner.target
            targetTempNum = airConditioner.target
        }
    }

    companion object {
        const val TAG = "AirConditionerViewHolder"
    }
}