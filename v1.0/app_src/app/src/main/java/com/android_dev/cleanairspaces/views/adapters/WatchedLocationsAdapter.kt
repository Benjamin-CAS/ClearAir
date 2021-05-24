package com.android_dev.cleanairspaces.views.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.WatchedLocationItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsData
import com.bumptech.glide.Glide

class WatchedLocationsAdapter(private val locationListener: OnClickItemListener) :
    RecyclerView.Adapter<WatchedLocationsAdapter.WatchedLocationsViewHolder>() {

    private val locationList = ArrayList<WatchedLocationHighLights>()

    private var aqiIndex: String? = null

    interface OnClickItemListener {
        fun onClickWatchedLocation(location: WatchedLocationHighLights)
        fun onSwipeToDeleteLocation(location: WatchedLocationHighLights)
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
                    indoorTv.isVisible = false
                }
                itemView.setOnClickListener { locationListener.onClickWatchedLocation(location) }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WatchedLocationsViewHolder {
        val binding =
            WatchedLocationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WatchedLocationsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchedLocationsViewHolder, position: Int) {
        val location = locationList[position]
        holder.bind(location, locationListener, aqiIndex)
    }

    fun setWatchedLocationsList(locationList: List<WatchedLocationHighLights>) {
        this.locationList.clear()
        this.locationList.addAll(locationList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    fun updateSelectedAqiIndex(selectedAQIIndex: String) {
        this.aqiIndex = selectedAQIIndex
        notifyDataSetChanged()
    }

    fun removeAt(adapterPosition: Int) {
        val location = locationList[adapterPosition]
        locationListener.onSwipeToDeleteLocation(location)
    }
}