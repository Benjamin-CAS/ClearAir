package com.android_dev.cleanairspaces.views.fragments.monitor_details

import android.os.Parcelable
import com.android_dev.cleanairspaces.persistence.local.models.entities.MonitorDetails
import kotlinx.parcelize.Parcelize

@Parcelize
data class MonitorDetailsAqiWrapper(
    val monitorDetails: MonitorDetails,
    val aqiIndex: String?
) : Parcelable
