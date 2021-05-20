package com.android_dev.cleanairspaces.views.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.MonitorItemBinding
import com.android_dev.cleanairspaces.databinding.WatchedLocationItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatMonitorData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsData
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsIndoorExtras
import com.bumptech.glide.Glide
import java.lang.Exception

class WatchedLocationsAndMonitorsAdapter(private val listener: OnClickItemListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_MONITORS = 1
        const val VIEW_TYPE_LOCATIONS = 2
        private val TAG = WatchedLocationsAndMonitorsAdapter::class.java.simpleName
    }

    private val dataset = ArrayList<LocationMonitorWrapper>()
    private val monitorsList = ArrayList<MonitorDetails>()
    private val locationsList = ArrayList<WatchedLocationHighLights>()

    private var aqiIndex: String? = null

    interface OnClickItemListener {
        fun onClickWatchedMonitor(monitor: MonitorDetails)
        fun onSwipeToDeleteMonitor(monitor: MonitorDetails)
        fun onClickWatchedLocation(locationHighLights: WatchedLocationHighLights)
        fun onSwipeToDeleteLocation(locationHighLights: WatchedLocationHighLights)
    }

    class WatchedMonitorsViewHolder(private val binding: MonitorItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            monitor: MonitorDetails,
            listener: OnClickItemListener,
            aqiIndex: String?
        ) {
            val ctx = itemView.context
            binding.apply {
                isWatchingIndicator.isVisible = false
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
                    tmpVal.setTextColor(ContextCompat.getColor(ctx , uiPmData.tmpColor))
                    humidVal.text = uiExtraData.humidLvlTxt
                    humidVal.setTextColor(ContextCompat.getColor(ctx , uiPmData.humldColor))
                    tvocVal.text = uiExtraData.vocLvlTxt
                    tvocVal.setTextColor(ContextCompat.getColor(ctx , uiPmData.vocColor))
                    co2Val.text = uiExtraData.co2LvlTxt
                    co2Val.setTextColor(ContextCompat.getColor(ctx , uiPmData.co2Color))
                    updatedOnTv.text = monitor.getUpdatedOnFormatted()
                }
                itemView.setOnClickListener { listener.onClickWatchedMonitor(monitor) }
            }
        }
    }


    fun setWatchedMonitorsList(monitorList: List<MonitorDetails>) {
        this.monitorsList.clear()
        this.monitorsList.addAll(monitorList)
        changeDataset()
    }


    class WatchedLocationsViewHolder(private val binding: WatchedLocationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            location: WatchedLocationHighLights,
            locationListener: OnClickItemListener,
            aqiIndex: String?
        ) {
            val ctx = itemView.context
            binding.apply {
                val uiData =
                    formatWatchedHighLightsData(ctx = ctx, location = location, aqiIndex = aqiIndex)
                locationNameTv.text = uiData.locationName
                Glide.with(ctx)
                    .load(uiData.logo)
                    .placeholder(R.drawable.clean_air_spaces_logo_name)
                    .into(locationLogoIv)

                updatedTv.text = uiData.updated
                locationAreaTv.text = uiData.locationArea
                if (uiData.hasOutDoorData) {
                    outdoorTv.isVisible = true
                    itemCard.setCardBackgroundColor(
                        ContextCompat.getColor(ctx, uiData.outDoorAqiStatus!!.backGroundColorRes)
                    )
                    outdoorPointsTv.text = uiData.outDoorPmValue.toString()
                    outdoorStatusIndicatorTv.text = ctx.getString(uiData.outDoorAqiStatus.lbl)
                    outdoorStatusIndicatorIv.setImageResource(
                        uiData.outDoorAqiStatus.status_bar_res
                    )
                    outdoorPmIndexTv.text = uiData.aqiIndexStr

                } else {
                    itemCard.setCardBackgroundColor(
                        uiData.defaultBgColor
                    )

                }
                //if we have indoor pm
                if (uiData.hasInDoorData) {
                    indoorTv.isVisible = true
                    indoorPointsTv.text = uiData.indoorPmValue.toString()
                    indoorStatusIndicatorTv.text = ctx.getString(uiData.indoorAQIStatus!!.lbl)
                    indoorStatusIndicatorIv.setImageResource(
                        uiData.indoorAQIStatus.status_bar_res
                    )
                    indoorPmIndexTv.text = uiData.aqiIndexStr
                } else {
                    //hide data
                    indoorTv.isVisible = true
                    indoorTv.text = ""
                    indoorPointsTv.text = ""
                    indoorStatusIndicatorTv.text = ""
                    indoorPmIndexTv.text = ""
                }
                itemView.setOnClickListener { locationListener.onClickWatchedLocation(location) }
            }
        }
    }

    fun setWatchedLocationsList(locationList: List<WatchedLocationHighLights>) {
        this.locationsList.clear()
        this.locationsList.addAll(locationList)
        changeDataset()
    }

    private fun changeDataset(){
        dataset.clear()
        for (newLoc in locationsList)
            dataset.add(LocationMonitorWrapper(
                locationHighLights = newLoc
            ))
        for(newMonitor in monitorsList)
            dataset.add(
                LocationMonitorWrapper(
                    monitor = newMonitor
                )
            )
       notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        val item = dataset[adapterPosition]
        item.monitor?.let {
            listener.onSwipeToDeleteMonitor(it)
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
        if (viewType == VIEW_TYPE_MONITORS) {
            return WatchedMonitorsViewHolder(
                MonitorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
        return WatchedLocationsViewHolder(
            WatchedLocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            val item = dataset[position]
            if (item.monitor != null) {
                (holder as WatchedMonitorsViewHolder).bind(
                    monitor = item.monitor, listener = listener, aqiIndex = aqiIndex
                )
            } else {
                (holder as WatchedLocationsViewHolder).bind(
                    location = item.locationHighLights!!,
                    locationListener = listener,
                    aqiIndex = aqiIndex
                )
            }
        }catch (exc : Exception){
            Log.d(
                TAG, "${exc.message}", exc
            )
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            dataset[position].monitor != null -> VIEW_TYPE_MONITORS
            else -> VIEW_TYPE_LOCATIONS
        }
    }

    data class LocationMonitorWrapper(
        val monitor : MonitorDetails? = null,
        val locationHighLights: WatchedLocationHighLights? = null
    )

}