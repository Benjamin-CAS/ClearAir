package com.android_dev.cleanairspaces.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.MonitorItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatMonitorData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsIndoorExtras
import com.bumptech.glide.Glide

class MonitorsAdapter(private val monitorListener: OnClickItemListener) :
        RecyclerView.Adapter<MonitorsAdapter.MonitorsAdapterViewHolder>() {

    private val monitorList = ArrayList<MonitorDetails>()

    private var aqiIndex: String? = null

    interface OnClickItemListener {
        fun onClickWatchedMonitor(monitor: MonitorDetails)
        fun onSwipeToDeleteMonitor(monitor: MonitorDetails)
    }

    class MonitorsAdapterViewHolder(private val binding: MonitorItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
        fun bind(
                monitor: MonitorDetails,
                monitorListener: OnClickItemListener,
                aqiIndex: String?
        ) {
            val ctx = itemView.context
            binding.apply {
                val isWatchingIndicatorIcon = if(monitor.watch_location){
                    R.drawable.ic_eye
                }else{
                    R.drawable.ic_add
                }
                Glide.with(ctx)
                        .load(isWatchingIndicatorIcon)
                        .into(isWatchingIndicator)
                val uiPmData =
                        formatMonitorData(ctx = ctx, monitor = monitor, aqiIndex = aqiIndex)
                indoorName.text = uiPmData.locationName
                pmVal.text = uiPmData.indoorPmValue.toString()
                inDoorPmValue.text = uiPmData.indoorPmValue.toString()
                uiPmData.indoorAQIStatus?.status_bar_res?.let {
                    indoorStatusIndicatorTv.background =
                            ContextCompat.getDrawable(ctx, it)

                    indoorStatusIndicatorTv.setText(uiPmData.indoorAQIStatus.lbl)
                    val uiExtraData = formatWatchedHighLightsIndoorExtras(
                            ctx = ctx,
                            co2Lvl = monitor.indoor_co2,
                            vocLvl = monitor.indoor_tvoc,
                            tmpLvl = monitor.indoor_temperature,
                            humidLvl = monitor.indoor_humidity,
                            inDoorAqiStatus = uiPmData.indoorAQIStatus,
                            addUnitsInVal = false
                    )
                  tmpVal.text = uiExtraData.tmpLvlTxt
                  humidVal.text = uiExtraData.humidLvlTxt
                  tvocVal.text =  uiExtraData.vocLvlTxt
                  co2Val.text =   uiExtraData.co2LvlTxt
                  updatedOnTv.text = monitor.getUpdatedOnFormatted()
                }
                itemView.setOnClickListener { monitorListener.onClickWatchedMonitor(monitor) }

            }
        }
    }


    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): MonitorsAdapterViewHolder {
        val binding =
                MonitorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonitorsAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonitorsAdapterViewHolder, position: Int) {
        val monitor = monitorList[position]
        holder.bind(monitor, monitorListener, aqiIndex)
    }

    fun setWatchedMonitorsList(monitorList: List<MonitorDetails>) {
        this.monitorList.clear()
        this.monitorList.addAll(monitorList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return monitorList.size
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String?) {
        this.aqiIndex = selectedAQIIndex
        notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        val monitor = monitorList[adapterPosition]
        monitorListener.onSwipeToDeleteMonitor(monitor)
    }
}