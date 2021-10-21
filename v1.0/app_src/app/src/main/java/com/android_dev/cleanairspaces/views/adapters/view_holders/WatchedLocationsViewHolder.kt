package com.android_dev.cleanairspaces.views.adapters.view_holders

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.WatchedLocationItemBinding
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.persistence.local.models.ui_models.formatWatchedHighLightsData
import com.android_dev.cleanairspaces.views.adapters.action_listeners.WatchedItemsActionListener
import com.bumptech.glide.Glide

class WatchedLocationsViewHolder(private val binding: WatchedLocationItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        location: WatchedLocationHighLights,
        locationListener: WatchedItemsActionListener,
        aqiIndex: String?
    ) {
        val ctx = itemView.context
        binding.apply {
            val uiData = formatWatchedHighLightsData(ctx = ctx, location = location, aqiIndex = aqiIndex)
            locationNameTv.text = uiData.locationName
            Glide.with(ctx)
                .load(uiData.logo)
                .placeholder(R.drawable.clean_air_spaces_logo_name)
                .into(locationLogoIv)
            updatedTv.text = uiData.updated
            locationAreaTv.text = uiData.locationArea
            if (uiData.hasOutDoorData) {
                outdoorTv.isVisible = true
                outdoorPointsTv.text = uiData.outDoorPmValue.toString()
                outdoorStatusIndicatorTv.text = ctx.getString(uiData.outDoorAqiStatus!!.lbl)
                outdoorStatusIndicatorIv.setImageResource(uiData.outDoorAqiStatus.status_bar_res)
                outdoorPmIndexTv.text = uiData.aqiIndexStr

            } else {

            }
            //if we have indoor pm
            if (uiData.hasInDoorData) {
                indoorTv.isVisible = true
                indoorPointsTv.text = uiData.indoorPmValue.toString()
                indoorStatusIndicatorTv.text = ctx.getString(uiData.indoorAQIStatus!!.lbl)
                indoorStatusIndicatorIv.setImageResource(
                    uiData.indoorAQIStatus.status_bar_res
                )
                itemCard.setCardBackgroundColor(
                    ContextCompat.getColor(ctx, uiData.indoorAQIStatus.backGroundColorRes)
                )
                indoorPmIndexTv.text = uiData.aqiIndexStr
            } else {
                //hide data
                indoorTv.isVisible = true
                indoorTv.text = ""
                indoorPointsTv.text = ""
                indoorStatusIndicatorTv.text = ""
                indoorPmIndexTv.text = ""
                itemCard.setCardBackgroundColor(uiData.defaultBgColor)
            }
            itemView.setOnClickListener {
                locationListener.onClickWatchedLocation(location)
                Log.e(TAG, "itemViewSetOnClickListener: $location")
            }
        }
    }
    companion object{
        const val TAG = "Adapter"
    }
}