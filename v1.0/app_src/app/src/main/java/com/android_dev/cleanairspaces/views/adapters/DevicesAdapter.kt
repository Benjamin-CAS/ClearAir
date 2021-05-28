package com.android_dev.cleanairspaces.views.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.DeviceItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import com.android_dev.cleanairspaces.utils.DevicesTypes
import com.bumptech.glide.Glide

class DevicesAdapter(private val deviceListener: OnClickItemListener) :
    RecyclerView.Adapter<DevicesAdapter.DevicesAdapterViewHolder>() {

    private val deviceList = ArrayList<DevicesDetails>()

    private var aqiIndex: String? = null

    interface OnClickItemListener {
        fun onClickToggleWatchDevice(device: DevicesDetails)
        fun onSwipeToDeleteDevice(device: DevicesDetails)
        fun onToggleFreshAir(device: DevicesDetails, status: String)
        fun onToggleFanSpeed(device: DevicesDetails, status: String, speed: String? = null)
        fun onToggleMode(device: DevicesDetails, toMode: String)
        fun onToggleDuctFit(device: DevicesDetails, status: String)
    }

    class DevicesAdapterViewHolder(private val binding: DeviceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            device: DevicesDetails,
            deviceListener: OnClickItemListener,
            aqiIndex: String?
        ) {
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
                    ctx, deviceStatus.colorRes
                )
            )

            val watchIndicator = if(device.watch_device){
                R.drawable.ic_fav
            }else{
                R.drawable.ic_not_fav
            }
            Glide.with(ctx)
                .load(watchIndicator)
                .into(binding.watchDevice)

            binding.watchDevice.setOnClickListener{
               deviceListener.onClickToggleWatchDevice(device = device)
            }
        }

        private fun setClickListeners(
            binding: DeviceItemBinding,
            deviceListener: OnClickItemListener,
            device: DevicesDetails,
            typeInfo: DevicesTypes.DeviceInfo
        ) {
            binding.apply {

                modeManualBtn.setOnClickListener {
                    deviceListener.onToggleMode(device, toMode = MANUAL)
                }

                modeAutoBtn.setOnClickListener {
                    deviceListener.onToggleMode(device, toMode = AUTO)
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
                    deviceListener.onToggleFanSpeed(
                        device, status = OFF_STATUS
                    )
                }
                fanLowBtn.setOnClickListener {
                    deviceListener.onToggleFanSpeed(
                        device, status = ON_STATUS, speed =  if (typeInfo.hasExtendedFanCalibrations) TURBO_SLOW_SPEED else LOW_SPEED
                    )
                }
                fanMedBtn.setOnClickListener {
                    deviceListener.onToggleFanSpeed(
                        device, status = ON_STATUS, speed = if (typeInfo.hasExtendedFanCalibrations) TURBO_MED_SPEED else MED_SPEED
                    )
                }
                fanHighBtn.setOnClickListener {
                    deviceListener.onToggleFanSpeed(
                        device, status = ON_STATUS, speed = if (typeInfo.hasExtendedFanCalibrations) TURBO_HIGH_SPEED else HIGH_SPEED
                    )
                }

                fanSlowBtn.setOnClickListener {
                    deviceListener.onToggleFanSpeed(
                        device, status = ON_STATUS, speed =  TURBO_SLOW_SPEED
                    )
                }

                fanTurboBtn.setOnClickListener {
                    deviceListener.onToggleFanSpeed(
                        device, status = ON_STATUS, speed =  TURBO_FULL_SPEED
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
                val hasFanExt = typeInfo.hasExtendedFanCalibrations
                deviceFanCalibLbl.isVisible = hasFanExt
                fanOffBtn.isVisible = hasFanExt
                fanSlowBtn.isVisible = hasFanExt
                fanLowBtn.isVisible = hasFanExt
                fanMedBtn.isVisible = hasFanExt
                fanHighBtn.isVisible = hasFanExt
                fanTurboBtn.isVisible = hasFanExt
                if (device.isTurboFanSlow()) {

                    fanOffBtn.background = inActiveBg
                    fanLowBtn.background = inActiveBg
                    fanMedBtn.background = inActiveBg
                    fanHighBtn.background = inActiveBg
                    fanTurboBtn.background = inActiveBg
                    fanSlowBtn.background = activeBg

                    fanOffBtn.setTextColor(blueColor)
                    fanLowBtn.setTextColor(blueColor)
                    fanMedBtn.setTextColor(blueColor)
                    fanHighBtn.setTextColor(blueColor)
                    fanTurboBtn.setTextColor(blueColor)
                    fanSlowBtn.setTextColor(whiteColor)

                } else if (device.isTurboFanTurbo()) {
                    fanOffBtn.background = inActiveBg
                    fanLowBtn.background = inActiveBg
                    fanMedBtn.background = inActiveBg
                    fanHighBtn.background = inActiveBg
                    fanTurboBtn.background = activeBg
                    fanSlowBtn.background = inActiveBg

                    fanOffBtn.setTextColor(blueColor)
                    fanLowBtn.setTextColor(blueColor)
                    fanMedBtn.setTextColor(blueColor)
                    fanHighBtn.setTextColor(blueColor)
                    fanTurboBtn.setTextColor(whiteColor)
                    fanSlowBtn.setTextColor(blueColor)

                } else if (device.isTurboFanHigh()) {

                    fanOffBtn.background = inActiveBg
                    fanLowBtn.background = inActiveBg
                    fanMedBtn.background = inActiveBg
                    fanHighBtn.background = activeBg
                    fanTurboBtn.background = inActiveBg
                    fanSlowBtn.background = inActiveBg

                    fanOffBtn.setTextColor(blueColor)
                    fanLowBtn.setTextColor(blueColor)
                    fanMedBtn.setTextColor(blueColor)
                    fanHighBtn.setTextColor(whiteColor)
                    fanTurboBtn.setTextColor(blueColor)
                    fanSlowBtn.setTextColor(blueColor)

                } else if (device.isTurboFanMed()) {

                    fanOffBtn.background = inActiveBg
                    fanLowBtn.background = inActiveBg
                    fanMedBtn.background = activeBg
                    fanHighBtn.background = inActiveBg
                    fanTurboBtn.background = inActiveBg
                    fanSlowBtn.background = inActiveBg

                    fanOffBtn.setTextColor(blueColor)
                    fanLowBtn.setTextColor(blueColor)
                    fanMedBtn.setTextColor(whiteColor)
                    fanHighBtn.setTextColor(blueColor)
                    fanTurboBtn.setTextColor(blueColor)
                    fanSlowBtn.setTextColor(blueColor)


                } else if (device.isTurboFanLow()) {

                    fanOffBtn.background = inActiveBg
                    fanLowBtn.background = activeBg
                    fanMedBtn.background = inActiveBg
                    fanHighBtn.background = inActiveBg
                    fanTurboBtn.background = inActiveBg
                    fanSlowBtn.background = inActiveBg

                    fanOffBtn.setTextColor(blueColor)
                    fanLowBtn.setTextColor(whiteColor)
                    fanMedBtn.setTextColor(blueColor)
                    fanHighBtn.setTextColor(blueColor)
                    fanTurboBtn.setTextColor(blueColor)
                    fanSlowBtn.setTextColor(blueColor)


                } else if (!device.isTurboFanOn()) {

                    fanOffBtn.background = activeBg
                    fanLowBtn.background = inActiveBg
                    fanMedBtn.background = inActiveBg
                    fanHighBtn.background = inActiveBg
                    fanTurboBtn.background = inActiveBg
                    fanSlowBtn.background = inActiveBg

                    fanOffBtn.setTextColor(whiteColor)
                    fanLowBtn.setTextColor(blueColor)
                    fanMedBtn.setTextColor(blueColor)
                    fanHighBtn.setTextColor(blueColor)
                    fanTurboBtn.setTextColor(blueColor)
                    fanSlowBtn.setTextColor(blueColor)

                }


                //display basic fan settings
                val hasBasicFanSettings = !hasFanCalib && typeInfo.hasFanBasic
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