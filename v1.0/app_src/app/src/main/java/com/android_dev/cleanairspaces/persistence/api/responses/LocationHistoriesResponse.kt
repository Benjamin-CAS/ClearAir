package com.android_dev.cleanairspaces.persistence.api.responses

import com.google.gson.annotations.Expose

data class LocationHistoriesResponse(
        @Expose
        val payload: String?,

        @Expose
        val code: String,

        @Expose
        val ltime: String?

) {
    companion object {
        const val monthResponseKey = "lastMonth"
        const val weekResponseKey = "lastWeek"
        const val daysResponseKey = "latest72h"
    }
}