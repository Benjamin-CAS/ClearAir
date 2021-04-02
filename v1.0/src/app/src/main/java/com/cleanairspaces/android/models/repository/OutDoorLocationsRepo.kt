package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.OutDoorLocationsApiService
import com.cleanairspaces.android.models.api.QrScannedItemsApiService
import com.cleanairspaces.android.models.api.responses.*
import com.cleanairspaces.android.models.dao.OutDoorLocationsDao
import com.cleanairspaces.android.models.entities.LocationAreas
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.utils.L_TIME_KEY
import com.cleanairspaces.android.utils.MyLogger
import com.cleanairspaces.android.utils.PAYLOAD_KEY
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
class OutDoorLocationsRepo
@Inject constructor(
    private val outDoorLocationsApiService: OutDoorLocationsApiService,
    private val qrScannedItemsApiService: QrScannedItemsApiService,
    private val coroutineScope: CoroutineScope,
    private val outDoorLocationsDao: OutDoorLocationsDao
){
    private val TAG = OutDoorLocationsRepo::class.java.simpleName

        fun  getOutDoorLocationsLive() = outDoorLocationsDao.getOutDoorLocationsLive()

    private fun getOtherOutDoorLocationsResponseCallback() : Callback<OutDoorLocationsOtherResponse> {
        return object : Callback<OutDoorLocationsOtherResponse> {
            override fun onResponse(
                call: Call<OutDoorLocationsOtherResponse>,
                response: Response<OutDoorLocationsOtherResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            val locations = responseBody!!.data
                            if (locations.isNullOrEmpty()) {
                                MyLogger.logThis(
                                    TAG,
                                    "getOtherOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                MyLogger.logThis(
                                    TAG,
                                    "getOtherOutDoorLocationsResponseCallback()",
                                    "total ${locations.size}"
                                )
                                saveOutDoorLocations(locations, location_area = LocationAreas.OTHER)
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getOtherOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getOtherOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<OutDoorLocationsOtherResponse>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getOtherOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

   private fun getAmericaOutDoorLocationsResponseCallback() : Callback<List<OutDoorLocationAmerica>> {
        return object : Callback<List<OutDoorLocationAmerica>> {
            override fun onResponse(
                call: Call<List<OutDoorLocationAmerica>>,
                response: Response<List<OutDoorLocationAmerica>>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody.isNullOrEmpty()) {
                                MyLogger.logThis(
                                    TAG,
                                    "getAmericaOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                MyLogger.logThis(
                                    TAG,
                                    "getAmericaOutDoorLocationsResponseCallback()",
                                    "total ${responseBody.size}"
                                )
                                saveOutDoorLocations(
                                    responseBody,
                                    location_area = LocationAreas.AMERICA
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getAmericaOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getAmericaOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationAmerica>>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getAmericaOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

   private fun getTaiwanOutDoorLocationsResponseCallback() : Callback<List<OutDoorLocationTaiwan>> {
        return object : Callback<List<OutDoorLocationTaiwan>> {
            override fun onResponse(
                call: Call<List<OutDoorLocationTaiwan>>,
                response: Response<List<OutDoorLocationTaiwan>>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody.isNullOrEmpty()) {
                                MyLogger.logThis(
                                    TAG,
                                    "getTaiwanOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                MyLogger.logThis(
                                    TAG,
                                    "getTaiwanOutDoorLocationsResponseCallback()",
                                    "total ${responseBody.size}"
                                )
                                saveOutDoorLocations(
                                    responseBody,
                                    location_area = LocationAreas.TAIWAN
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getTaiwanOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getTaiwanOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationTaiwan>>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getTaiwanOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }


    suspend fun refreshOutDoorLocations(){
        //refreshing out door locations
        val otherLocationsResponse = outDoorLocationsApiService.fetchOtherOutDoorLocations()
        otherLocationsResponse.enqueue(getOtherOutDoorLocationsResponseCallback())

        //refreshing american locations
        val usLocationsResponse = outDoorLocationsApiService.fetchAmericanOutDoorLocations()
        usLocationsResponse.enqueue(getAmericaOutDoorLocationsResponseCallback())

        //refreshing taiwan locations
        val taiwanLocationsResponse = outDoorLocationsApiService.fetchTaiwanOutDoorLocations()
        taiwanLocationsResponse.enqueue(getTaiwanOutDoorLocationsResponseCallback())

    }
    
    fun saveOutDoorLocations(locations: List<Any>, location_area: LocationAreas){
        coroutineScope.launch {
            outDoorLocationsDao.deleteAllLocations()
            val newOutDoorLocations = ArrayList<OutDoorLocations>()
            when (location_area) {
                LocationAreas.AMERICA -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationAmerica
                        val outDoorLocation = OutDoorLocations(
                            pm2p5 = location.pm2p5,
                            lon = location.sta_lon,
                            lat = location.sta_lat,
                            location_area = LocationAreas.AMERICA
                        )
                        newOutDoorLocations.add(outDoorLocation)

                    }
                }
                LocationAreas.TAIWAN -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationTaiwan
                        val outDoorLocation = OutDoorLocations(
                            pm2p5 = location.pm2p5,
                            lon = location.lon,
                            lat = location.lat,
                            location_area = LocationAreas.TAIWAN
                        )
                        newOutDoorLocations.add(outDoorLocation)
                    }

                }
                LocationAreas.OTHER -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationsOther
                        val outDoorLocation = OutDoorLocations(
                            location_id = location.location_id,
                            monitor_id = location.monitor_id,
                            name_en = location.name_en,
                            reading = location.reading,
                            date_reading = location.date_reading,
                            lon = location.lon,
                            lat = location.lat,
                            location_area = LocationAreas.OTHER
                        )
                        newOutDoorLocations.add(outDoorLocation)
                    }
                }
            }
            outDoorLocationsDao.insertOutDoorLocations(newOutDoorLocations)
        }
    }



    /************************ QR SCANNED ITEMS *********************/
    private fun getScannedDeviceQrResponseCallback() : Callback<Any> {
        return object : Callback<Any> {
            override fun onResponse(
                call: Call<Any>,
                response: Response<Any>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null) {
                                MyLogger.logThis(
                                    TAG,
                                    "getScannedDeviceQrResponseCallback()",
                                    "returned a null body"
                                )
                            } else {
                                MyLogger.logThis(
                                    TAG, "getScannedDeviceQrResponseCallback()",
                                            " in response body $responseBody"
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                TAG,
                                "getScannedDeviceQrResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                            TAG,
                            "getScannedDeviceQrResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<Any>, e: Throwable) {
                MyLogger.logThis(
                    TAG,
                    "getScannedDeviceQrResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    suspend fun fetchLocationFromScannedDeviceQr(base64Str: String, payLoadTimeStamp: String) {
        try {
           val data = JsonObject()
           data.addProperty(L_TIME_KEY, payLoadTimeStamp)
           data.addProperty(PAYLOAD_KEY, base64Str)
            val otherLocationsResponse = qrScannedItemsApiService.fetchScannedDeviceQrResponse(
                data = data
            )

            MyLogger.logThis(TAG, "fetchLocationFromScannedDeviceQr()", "passing data $data")
            otherLocationsResponse.enqueue(getScannedDeviceQrResponseCallback())
        }catch (e : Exception){
            MyLogger.logThis(TAG, "fetchLocationFromScannedDeviceQr()", "exc ${e.message}", e)
        }
    }
}