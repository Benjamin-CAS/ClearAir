package com.android_dev.cleanairspaces.views.adapters.view_holders

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