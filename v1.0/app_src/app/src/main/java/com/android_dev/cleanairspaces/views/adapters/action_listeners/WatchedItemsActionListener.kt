package com.android_dev.cleanairspaces.views.adapters.action_listeners

import com.android_dev.cleanairspaces.persistence.local.models.entities.DevicesDetails
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights

interface WatchedItemsActionListener {
    fun onClickToggleWatchDevice(device: DevicesDetails)
    fun onToggleFreshAir(device: DevicesDetails, status: String)
    fun onToggleFanSpeed(device: DevicesDetails, status: String, speed: String? = null)
    fun onToggleMode(device: DevicesDetails, toMode: String)
    fun onToggleDuctFit(device: DevicesDetails, status: String)
    fun onClickWatchedLocation(locationHighLights: WatchedLocationHighLights)
    fun onSwipeToDeleteLocation(locationHighLights: WatchedLocationHighLights)
    fun onSwipeToDeleteDevice(device: DevicesDetails)
}