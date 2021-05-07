package com.android_dev.cleanairspaces.persistence.api.responses

data class HistoryDataUnEncrypted(
        var date_reading: String?,
        var avg_reading: Double?,//indoor
        var avg_tvoc: String?,
        var avg_co2: Double?,
        var avg_temperature: Double?,
        var avg_humidity: Double?,
        var reading_comp: Double?,//outdoor
)