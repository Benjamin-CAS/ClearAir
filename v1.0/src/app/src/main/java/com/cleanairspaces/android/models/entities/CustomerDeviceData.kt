package com.cleanairspaces.android.models.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cleanairspaces.android.utils.BASE_URL
import kotlinx.parcelize.Parcelize

@Entity(
        tableName = "customer_device_data",
        indices = [androidx.room.Index(value = ["company_id", "location_id"], unique = true)]
)
@Parcelize
data class CustomerDeviceData(
        @PrimaryKey(autoGenerate = false)
        var device_id: String,
        val company_id: String,
        val location_id: String,
        @ColumnInfo(name = "company_name")
        val company: String,
        val location: String,
        val dev_name: String,
        val isSecure: Boolean,
        @ColumnInfo(name = "logo_file_name")
        val logo: String,
        val reference_mon: String,
        var isMyDeviceData: Boolean = false,
        var monitor_id: String?,
        var type: String?,
) : Parcelable {

    fun getFullLogoUrl(): String {
        return LOGO_BASE_URL + logo
    }

    fun getFullDeviceLogoUrl(deviceLogoName: String): String {
        return LOGO_BASE_URL + deviceLogoName
    }

    companion object {
        const val RESPONSE_MONITOR_TYPE_KEY: String = "type"
        const val RESPONSE_KEY = "customer"
        private const val LOGO_BASE_URL = "${BASE_URL}assets/images/logo/"
    }
}
