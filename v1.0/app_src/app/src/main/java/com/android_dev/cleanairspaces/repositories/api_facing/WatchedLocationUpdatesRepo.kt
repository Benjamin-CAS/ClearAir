package com.android_dev.cleanairspaces.repositories.api_facing

import com.android_dev.cleanairspaces.persistence.api.responses.LocationDetails
import com.android_dev.cleanairspaces.persistence.api.responses.LocationDetailsResponse
import com.android_dev.cleanairspaces.persistence.api.services.AppApiService
import com.android_dev.cleanairspaces.persistence.api.services.LocationDetailsService
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchedLocationUpdatesRepo
@Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        private val locationDetailsService: LocationDetailsService
) {

    private val TAG = WatchedLocationUpdatesRepo::class.java.simpleName

    private val recentRequestsData = arrayListOf<JsonObject>()


    fun refreshWatchedLocationsData() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val myLocations = watchedLocationHighLightsDao.getWatchedLocationHighLightsOnce()
                for (aLocation in myLocations) {
                    val compId = aLocation.compId
                    val locId = aLocation.locId
                    val userName = aLocation.lastRecUsername
                    val userPassword = aLocation.lastRecPwd
                    val timeStamp = System.currentTimeMillis().toString() + aLocation.actualDataTag
                    val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForLocationDetails(
                            compId = compId,
                            locId = locId,
                            userName = userName,
                            userPassword = userPassword,
                            timeStamp = timeStamp,
                            isIndoorLoc = aLocation.isIndoorLoc
                    )

                    val data = JsonObject()
                    data.addProperty(L_TIME_KEY, timeStamp)
                    data.addProperty(PAYLOAD_KEY, pl)
                    val response = locationDetailsService.fetchDetailsForLocation(
                            data = data,
                            method = AppApiService.DEVICE_INFO_METHOD
                    )

                    // keep track of this request data ---
                    data.addProperty(COMP_ID_KEY, compId)
                    data.addProperty(LOC_ID_KEY, locId)
                    data.addProperty(USER_KEY, userName)
                    data.addProperty(PASSWORD_KEY, userPassword)
                    data.addProperty(API_LOCAL_DATA_BINDER_KEY, aLocation.actualDataTag)
                    recentRequestsData.add(data)
                    response.enqueue(
                            getWatchedLocationDetailsResponseCallback()
                    )
                }
                MyLogger.logThis(
                        TAG, "refreshWatchedLocationsData()", "refreshed ${myLocations.size} data"
                )
            } catch (exc: java.lang.Exception) {
                MyLogger.logThis(
                        TAG, "refreshWatchedLocationsData()", "failed exc ${exc.message}", exc
                )

            }
        }
    }

    private fun getWatchedLocationDetailsResponseCallback(): Callback<LocationDetailsResponse> {
        return object : Callback<LocationDetailsResponse> {
            override fun onResponse(
                    call: Call<LocationDetailsResponse>,
                    response: Response<LocationDetailsResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null || responseBody.payload.isNullOrBlank()) {
                                MyLogger.logThis(
                                        TAG,
                                        "getWatchedLocationDetailsResponseCallback()",
                                        "returned a null body"
                                )
                            } else {
                                val lTime = responseBody.ltime ?: "0"
                                unEncryptWatchedLocationDetailsPayload(
                                        payload = responseBody.payload,
                                        lTime = lTime
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                    TAG,
                                    "getWatchedLocationDetailsResponseCallback()",
                                    "exception ${e.message}",
                                    e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                                TAG,
                                "getWatchedLocationDetailsResponseCallback()",
                                "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<LocationDetailsResponse>, e: Throwable) {
                MyLogger.logThis(
                        TAG,
                        "getWatchedLocationDetailsResponseCallback()",
                        "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun unEncryptWatchedLocationDetailsPayload(payload: String, lTime: String) {
        try {
            val dataMatchingLTime =
                    recentRequestsData.filter { it.get(L_TIME_KEY).asString.equals(lTime) }
            if (dataMatchingLTime.isNullOrEmpty()) return
            val requestedData = dataMatchingLTime[0]
            recentRequestsData.remove(requestedData)
            val dataTag = requestedData.get(API_LOCAL_DATA_BINDER_KEY).asString
            if (dataTag.isNullOrBlank()) return

            val unEncryptedPayload: String =
                    CasEncDecQrProcessor.decodeApiResponse(payload)
            val unEncJson = JSONObject(unEncryptedPayload)
            val locationDetails =
                    Gson().fromJson(unEncJson.toString(), LocationDetails::class.java)

            if (locationDetails != null) {
                locationDetails.company_id = requestedData.get(COMP_ID_KEY).asString
                locationDetails.location_id = requestedData.get(LOC_ID_KEY).asString
                locationDetails.lastKnownUserName = requestedData.get(USER_KEY).asString
                locationDetails.lastKnownPassword = requestedData.get(PASSWORD_KEY).asString
                locationDetails.lastUpdated = System.currentTimeMillis()

                MyLogger.logThis(
                        TAG,
                        "unEncryptWatchedLocationDetailsPayload(payload: , lTime  : $lTime)",
                        "encrypted data $unEncJson for company ${locationDetails.company_id} & location ${locationDetails.location_id}",
                )
                updateWatchedLocationIfExists(locationDetails, dataTag)
            } else {
                MyLogger.logThis(
                        TAG, "unEncryptWatchedLocationDetailsPayload()",
                        "failed to decode my locations details to gSon ..encoded - $unEncJson"
                )

            }

        } catch (e: java.lang.Exception) {
            MyLogger.logThis(
                    TAG,
                    " unEncryptWatchedLocationDetailsPayload(payload: , lTime  : $lTime)",
                    "failed ${e.message}",
                    e
            )
        }
    }

    private fun updateWatchedLocationIfExists(locationDetails: LocationDetails, dataTag: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val foundDetails = watchedLocationHighLightsDao.checkIfIsWatchedLocation(dataTag)
            if (foundDetails.isNotEmpty()) {
                val existingData = foundDetails[0]
                val updatedData = WatchedLocationHighLights(
                        actualDataTag = dataTag,
                        lat = locationDetails.getLat(),
                        lon = locationDetails.getLon(),
                        pm_outdoor = locationDetails.outdoor.outdoor_pm.toDoubleOrNull(),
                        pm_indoor = locationDetails.indoor.indoor_pm.toDoubleOrNull(),
                        name = existingData.name,
                        logo = existingData.logo,
                        location_area = existingData.location_area,
                        indoor_co2 = locationDetails.indoor.indoor_co2.toDoubleOrNull(),
                        indoor_humidity = locationDetails.indoor.indoor_humidity.toDoubleOrNull(),
                        indoor_temperature = locationDetails.indoor.indoor_temperature.toDoubleOrNull(),
                        indoor_voc = locationDetails.indoor.indoor_voc.toDoubleOrNull(),
                        energyMonth = locationDetails.energy.month?.toDoubleOrNull(),
                        energyMax = locationDetails.energy.max?.toDoubleOrNull(),
                        isIndoorLoc = locationDetails.indoor.indoor_pm.isNotBlank(),
                        compId = existingData.compId,
                        locId = existingData.locId,
                        monitorId = existingData.monitorId,
                        lastRecPwd = existingData.lastRecPwd,
                        lastRecUsername = existingData.lastRecUsername
                )
             watchedLocationHighLightsDao.insertLocation(updatedData)
            }
        }
    }


}