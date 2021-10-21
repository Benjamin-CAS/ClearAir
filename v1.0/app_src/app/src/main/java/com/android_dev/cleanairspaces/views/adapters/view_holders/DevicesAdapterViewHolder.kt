package com.android_dev.cleanairspaces.views.adapters.view_holders

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.utils.DevicesTypes
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.bumptech.glide.Glide

class DevicesAdapterViewHolder(private val binding: DeviceItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        device: DevicesDetails,
        deviceListener: WatchedItemsActionListener,
        aqiIndex: String?,
        displayFav: Boolean = true
    ) {
        Log.e(TAG, "bind:${device.status}")
        val ctx = itemView.context
        val deviceStatus = device.getStatusColor()
        DevicesTypes.getDeviceInfoByType(device.device_type)?.let { typeInfo ->
            val titleNStatus = device.dev_name + " " + device.mac.trim().takeLast(4) + " | " +
                    ctx.getString(deviceStatus.statusTxt)
            binding.apply {
                deviceName.text = titleNStatus
                deviceTypeVal.text = ctx.getString(typeInfo.deviceTitleRes)
            }
            displayDeviceSettings(ctx, device, typeInfo, binding)
            setClickListeners(binding, deviceListener, device, typeInfo)
        }

        binding.deviceCard.setCardBackgroundColor(
            ContextCompat.getColor(
                ctx, deviceStatus.bgColorRes
            )
        )
//        if (device.status == IS_OFF){
//            binding.deviceCard.setBackgroundColor(
//                ContextCompat.getColor(
//                    ctx, R.color.cas_blue
//                )
//            )
//        }else{
//            binding.deviceCard.setBackgroundColor(
//                ContextCompat.getColor(
//                    ctx, R.color.background_good
//                )
//            )
//        }

        if (displayFav) {
            binding.watchDevice.isVisible = true
            val watchIndicator = if (device.watch_device) {
                R.drawable.ic_fav
            } else {
                R.drawable.ic_not_fav
            }
            Glide.with(ctx)
                .load(watchIndicator)
                .into(binding.watchDevice)
        } else {
            binding.watchDevice.isVisible = false
        }

        binding.watchDevice.setOnClickListener {
            deviceListener.onClickToggleWatchDevice(device = device)
        }
    }

    private fun setClickListeners(
        binding: DeviceItemBinding,
        deviceListener: WatchedItemsActionListener,
        device: DevicesDetails,
        typeInfo: DevicesTypes.DeviceInfo
    ) {
        val ctx = itemView.context
        binding.apply {
            modeManualBtn.setOnClickListener {
                deviceListener.onToggleMode(device, toMode = MANUAL)
            }

            modeAutoBtn.setOnClickListener {
                deviceListener.onToggleMode(device, toMode = AUTO)
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
            }

            fanBasicOffBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device, status = OFF_STATUS
                )
            }
            fanBasicOnBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device, status = ON_STATUS
                )
            }

            fanOffBtn.setOnClickListener {
                binding.deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.cas_blue
                    )
                )
                deviceListener.onToggleFanSpeed(
                    device, status = OFF_STATUS
                )
            }
            fanLowBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device,
                    status = ON_STATUS,
                    speed = if (typeInfo.hasExtendedFanCalibrations) TURBO_LOW_SPEED else LOW_SPEED
                )
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
                Log.e(TAG, "setClickListeners: 点击了 + typeInfo:${device.status}")
            }
            fanMedBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device,
                    status = ON_STATUS,
                    speed = if (typeInfo.hasExtendedFanCalibrations) TURBO_MED_SPEED else MED_SPEED
                )
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
            }
            fanHighBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device,
                    status = ON_STATUS,
                    speed = if (typeInfo.hasExtendedFanCalibrations) TURBO_HIGH_SPEED else HIGH_SPEED
                )
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
            }

            fanSleepBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device, status = ON_STATUS, speed = TURBO_SLEEP_SPEED
                )
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
            }

            fanTurboBtn.setOnClickListener {
                deviceListener.onToggleFanSpeed(
                    device, status = ON_STATUS, speed = TURBO_FULL_SPEED
                )
                deviceCard.setBackgroundColor(
                    ContextCompat.getColor(
                        it.context, R.color.background_good
                    )
                )
            }

            ductfitOnBtn.setOnClickListener {
                deviceListener.onToggleDuctFit(
                    device, status = ON_STATUS
                )
            }
            ductfitOffBtn.setOnClickListener {
                deviceListener.onToggleDuctFit(
                    device, status = OFF_STATUS
                )
            }

            freshAirOnBtn.setOnClickListener {
                deviceListener.onToggleFreshAir(
                    device, status = ON_STATUS
                )
            }
            freshAirOffBtn.setOnClickListener {
                deviceListener.onToggleFreshAir(
                    device, status = OFF_STATUS
                )
            }
        }
    }

    private fun displayDeviceSettings(
        ctx: Context,
        device: DevicesDetails,
        typeInfo: DevicesTypes.DeviceInfo,
        binding: DeviceItemBinding
    ) {
        binding.apply {
            //display mode
            val activeBg = ContextCompat.getDrawable(ctx, R.drawable.cas_filled_blue)
            val inActiveBg = ContextCompat.getDrawable(ctx, R.drawable.cas_outline_blue)
            val whiteColor = ContextCompat.getColor(ctx, R.color.white)
            val blueColor = ContextCompat.getColor(ctx, R.color.cas_blue)

            deviceModeLbl.isVisible = typeInfo.hasMode
            modeManualBtn.isVisible = typeInfo.hasMode
            modeAutoBtn.isVisible = typeInfo.hasMode
            if (device.isModeAuto()) {
                modeAutoBtn.background = activeBg
                modeAutoBtn.setTextColor(whiteColor)
                modeManualBtn.background = inActiveBg
                modeManualBtn.setTextColor(blueColor)
            } else {
                modeManualBtn.background = activeBg
                modeAutoBtn.background = inActiveBg
                modeManualBtn.setTextColor(whiteColor)
                modeAutoBtn.setTextColor(blueColor)
            }

            //display fan off low med and high
            val hasFanCalib = typeInfo.hasFanCalibrations
            deviceFanCalibLbl.isVisible = hasFanCalib
            fanOffBtn.isVisible = hasFanCalib
            fanLowBtn.isVisible = hasFanCalib
            fanMedBtn.isVisible = hasFanCalib
            fanHighBtn.isVisible = hasFanCalib
            if (device.isFanHigh()) {
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = activeBg
                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(whiteColor)
            } else if (device.isFanMed()) {
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = activeBg
                fanHighBtn.background = inActiveBg
                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(whiteColor)
                fanHighBtn.setTextColor(blueColor)

            } else if (device.isFanLow()) {
                Log.e(TAG, "displayDeviceSettings: 应该为蓝色")
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = activeBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(whiteColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)

            } else if (!device.isFanOn()) {
                fanOffBtn.background = activeBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanOffBtn.setTextColor(whiteColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)
            }


            //display fan off slow low med high and turbo
            if(!hasFanCalib){
            val hasFanExt =  typeInfo.hasExtendedFanCalibrations
            deviceFanCalibLbl.isVisible = hasFanExt
            fanOffBtn.isVisible = hasFanExt
            fanSleepBtn.isVisible = hasFanExt
            fanLowBtn.isVisible = hasFanExt
            fanMedBtn.isVisible = hasFanExt
            fanHighBtn.isVisible = hasFanExt
            fanTurboBtn.isVisible = hasFanExt
            if (device.isTurboFanSleep()) {
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanTurboBtn.background = inActiveBg
                fanSleepBtn.background = activeBg
                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)
                fanTurboBtn.setTextColor(blueColor)
                fanSleepBtn.setTextColor(whiteColor)

            } else if (device.isTurboFanTurbo()) {
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanTurboBtn.background = activeBg
                fanSleepBtn.background = inActiveBg

                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)
                fanTurboBtn.setTextColor(whiteColor)
                fanSleepBtn.setTextColor(blueColor)

            } else if (device.isTurboFanHigh()) {

                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = activeBg
                fanTurboBtn.background = inActiveBg
                fanSleepBtn.background = inActiveBg

                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(whiteColor)
                fanTurboBtn.setTextColor(blueColor)
                fanSleepBtn.setTextColor(blueColor)

            } else if (device.isTurboFanMed()) {

                fanOffBtn.background = inActiveBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = activeBg
                fanHighBtn.background = inActiveBg
                fanTurboBtn.background = inActiveBg
                fanSleepBtn.background = inActiveBg

                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(whiteColor)
                fanHighBtn.setTextColor(blueColor)
                fanTurboBtn.setTextColor(blueColor)
                fanSleepBtn.setTextColor(blueColor)


            } else if (device.isTurboFanLow()) {
                fanOffBtn.background = inActiveBg
                fanLowBtn.background = activeBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanTurboBtn.background = inActiveBg
                fanSleepBtn.background = inActiveBg
                fanOffBtn.setTextColor(blueColor)
                fanLowBtn.setTextColor(whiteColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)
                fanTurboBtn.setTextColor(blueColor)
                fanSleepBtn.setTextColor(blueColor)


            } else if (!device.isTurboFanOn()) {

                fanOffBtn.background = activeBg
                fanLowBtn.background = inActiveBg
                fanMedBtn.background = inActiveBg
                fanHighBtn.background = inActiveBg
                fanTurboBtn.background = inActiveBg
                fanSleepBtn.background = inActiveBg

                fanOffBtn.setTextColor(whiteColor)
                fanLowBtn.setTextColor(blueColor)
                fanMedBtn.setTextColor(blueColor)
                fanHighBtn.setTextColor(blueColor)
                fanTurboBtn.setTextColor(blueColor)
                fanSleepBtn.setTextColor(blueColor)

            }
        }


            //display basic fan settings
            if(!hasFanCalib && !typeInfo.hasExtendedFanCalibrations) {
                val hasBasicFanSettings = typeInfo.hasFanBasic
                deviceFanBasicLbl.isVisible = hasBasicFanSettings
                fanBasicOffBtn.isVisible = hasBasicFanSettings
                fanBasicOnBtn.isVisible = hasBasicFanSettings
                if (device.isFanOn()) {
                    fanBasicOffBtn.background = inActiveBg
                    fanBasicOnBtn.background = activeBg
                    fanBasicOffBtn.background = inActiveBg
                    fanBasicOnBtn.setTextColor(whiteColor)
                    fanBasicOffBtn.setTextColor(blueColor)
                } else {
                    fanBasicOffBtn.background = activeBg
                    fanBasicOnBtn.background = inActiveBg
                    fanBasicOffBtn.setTextColor(whiteColor)
                    fanBasicOnBtn.setTextColor(blueColor)
                }
            }


            //fresh air status
            val displayFA = typeInfo.hasFreshAir
            deviceFreshAirLbl.isVisible = displayFA
            freshAirOnBtn.isVisible = displayFA
            freshAirOffBtn.isVisible = displayFA
            if (device.isFreshAirOn()) {
                freshAirOnBtn.background = activeBg
                freshAirOffBtn.background = inActiveBg
                freshAirOnBtn.setTextColor(whiteColor)
                freshAirOffBtn.setTextColor(blueColor)
            } else {
                freshAirOnBtn.background = activeBg
                freshAirOffBtn.background = inActiveBg
                freshAirOnBtn.setTextColor(whiteColor)
                freshAirOffBtn.setTextColor(blueColor)
            }

            //display ductFit status
            val showDuctFit = if (displayFA) false else typeInfo.hasDuctFit
            deviceDuctFitLbl.isVisible = showDuctFit
            ductfitOnBtn.isVisible = showDuctFit
            ductfitOffBtn.isVisible = showDuctFit
            if (device.isDuctFitOn()) {
                ductfitOnBtn.background = activeBg
                ductfitOffBtn.background = inActiveBg
                ductfitOnBtn.setTextColor(whiteColor)
                ductfitOffBtn.setTextColor(blueColor)
            } else {
                ductfitOnBtn.background = inActiveBg
                ductfitOffBtn.background = activeBg
                ductfitOnBtn.setTextColor(blueColor)
                ductfitOffBtn.setTextColor(whiteColor)
            }
        }
    }
    companion object{
        const val TAG = "DevicesAdapterViewHolder"
    }
}