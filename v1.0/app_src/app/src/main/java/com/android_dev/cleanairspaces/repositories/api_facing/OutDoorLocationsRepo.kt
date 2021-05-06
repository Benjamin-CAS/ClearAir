package com.android_dev.cleanairspaces.repositories.api_facing

import com.android_dev.cleanairspaces.persistence.api.responses.*
import com.android_dev.cleanairspaces.persistence.api.services.OutDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.local.models.dao.MapDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.SearchSuggestionsDataDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.L_TIME_KEY
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.PAYLOAD_KEY
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutDoorLocationsRepo
@Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val mapDataDao: MapDataDao,
    private val searchSuggestionsDataDao: SearchSuggestionsDataDao,
    private val outDoorLocationApiService: OutDoorLocationApiService,
    private val myLogger: MyLogger
) {

    private val TAG = OutDoorLocationsRepo::class.java.simpleName

    private fun getOtherOutDoorLocationsResponseCallback(): Callback<OutDoorLocationResponse> {
        return object : Callback<OutDoorLocationResponse> {
            override fun onResponse(
                call: Call<OutDoorLocationResponse>,
                response: Response<OutDoorLocationResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            val locations = responseBody!!.data
                            if (locations.isNullOrEmpty()) {
                                myLogger.logThis(
                                    TAG,
                                    "getOtherOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                myLogger.logThis(
                                    TAG,
                                    "getOtherOutDoorLocationsResponseCallback()",
                                    "total ${locations.size}"
                                )
                                mapOutDoorLocationsToMapData(otherLocations = locations)
                            }
                        } catch (e: Exception) {
                            myLogger.logThis(
                                TAG,
                                "getOtherOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        myLogger.logThis(
                            TAG,
                            "getOtherOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<OutDoorLocationResponse>, e: Throwable) {
                myLogger.logThis(
                    TAG,
                    "getOtherOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun getAmericaOutDoorLocationsResponseCallback(): Callback<List<OutDoorLocationAmerica>> {
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
                                myLogger.logThis(
                                    TAG,
                                    "getAmericaOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                myLogger.logThis(
                                    TAG,
                                    "getAmericaOutDoorLocationsResponseCallback()",
                                    "total ${responseBody.size}"
                                )
                                mapOutDoorLocationsToMapData(
                                    usLocations = responseBody
                                )
                            }
                        } catch (e: Exception) {
                            myLogger.logThis(
                                TAG,
                                "getAmericaOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        myLogger.logThis(
                            TAG,
                            "getAmericaOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationAmerica>>, e: Throwable) {
                myLogger.logThis(
                    TAG,
                    "getAmericaOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }

    private fun getTaiwanOutDoorLocationsResponseCallback(): Callback<List<OutDoorLocationTaiwan>> {
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
                                myLogger.logThis(
                                    TAG,
                                    "getTaiwanOutDoorLocationsResponseCallback()",
                                    "locations in response are null or empty"
                                )
                            } else {
                                myLogger.logThis(
                                    TAG,
                                    "getTaiwanOutDoorLocationsResponseCallback()",
                                    "total ${responseBody.size}"
                                )
                                mapOutDoorLocationsToMapData(
                                    taiwanLocations = responseBody
                                )
                            }
                        } catch (e: Exception) {
                            myLogger.logThis(
                                TAG,
                                "getTaiwanOutDoorLocationsResponseCallback()",
                                "exception ${e.message}",
                                e
                            )
                        }
                    }
                    else -> {
                        myLogger.logThis(
                            TAG,
                            "getTaiwanOutDoorLocationsResponseCallback()",
                            "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationTaiwan>>, e: Throwable) {
                myLogger.logThis(
                    TAG,
                    "getTaiwanOutDoorLocationsResponseCallback()",
                    "OnFailure-exception ${e.message}"
                )
            }
        }
    }


    suspend fun refreshOutDoorLocations() {
        //refreshing out door locations
        val otherLocationsResponse = outDoorLocationApiService.fetchOtherOutDoorLocations()
        otherLocationsResponse.enqueue(getOtherOutDoorLocationsResponseCallback())

        //refreshing american locations
        val usLocationsResponse = outDoorLocationApiService.fetchAmericanOutDoorLocations()
        usLocationsResponse.enqueue(getAmericaOutDoorLocationsResponseCallback())

        /* TODO try after issue with coordinates is fixed -- refreshing taiwan locations
        val taiwanLocationsResponse = outDoorLocationApiService.fetchTaiwanOutDoorLocations()
        taiwanLocationsResponse.enqueue(getTaiwanOutDoorLocationsResponseCallback()) */


        //refreshing outdoor location details -- more detailed locations
        val rand10 = (0..10)
        val timeStamp =
            (rand10.random().toString() + System.currentTimeMillis().toString()).replace(" ", "")
        val data = JsonObject()
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForOutdoorLocation(
            timeStamp = timeStamp
        )
        data.addProperty(L_TIME_KEY, timeStamp)
        data.addProperty(PAYLOAD_KEY, pl)
        val request = outDoorLocationApiService.fetchOutDoorLocationsExtraDetails(
            data = data
        )
        request.enqueue(getOutdoorLocationsDetails())

    }

    private fun getOutdoorLocationsDetails(): Callback<OutDoorDetailsLocationResponse> {
        return object : Callback<OutDoorDetailsLocationResponse> {
            override fun onResponse(
                call: Call<OutDoorDetailsLocationResponse>,
                response: Response<OutDoorDetailsLocationResponse>
            ) {

                val responseBody = response.body()
                try {
                    if (responseBody!!.data.isNotEmpty()) {
                        mapOutDoorLocationsToMapData(
                            outDoorExtraDetailed = responseBody.data
                        )
                    }

                } catch (exc: java.lang.Exception) {
                    myLogger.logThis(
                        TAG,
                        "getOutdoorLocationsDetails",
                        "failed response - $response responseBody - $responseBody exception ${exc.message}",
                        exc
                    )
                }
            }

            override fun onFailure(call: Call<OutDoorDetailsLocationResponse>, t: Throwable) {
                myLogger.logThis(
                    TAG, "getOutdoorLocationsDetails() -> onFailure()", "${t.message}"
                )
            }
        }
    }


    private fun mapOutDoorLocationsToMapData(
        taiwanLocations: List<OutDoorLocationTaiwan>? = null,
        usLocations: List<OutDoorLocationAmerica>? = null,
        otherLocations: List<OutDoorLocationsOther>? = null,
        outDoorExtraDetailed: List<OutDoorDetailedLocationData>? = null
    ) {
        coroutineScope.launch(Dispatchers.IO) {

            try {
                //update map data
                var dataType: String = ""
                val mapDataList = ArrayList<MapData>()
                val searchData = ArrayList<SearchSuggestionsData>()
                when {
                    taiwanLocations != null -> {
                        for (location in taiwanLocations) {
                            val taiwanData = MapData(
                                lat = location.lat.toDouble(),
                                lon = location.lon.toDouble(),
                                pm25 = location.pm2p5.toDouble(),
                                actualDataTag = "taiwan${location.lat}${location.lon}"
                            )
                            mapDataList.add(taiwanData)
                            myLogger.logThis(
                                TAG,
                                "mapOutDoorLocationsToMapData() -> Taiwan",
                                "tag ${taiwanData.actualDataTag} lat ${taiwanData.lat} lon ${taiwanData.lon} getALatLon ${taiwanData.getAMapLocationLatLng()} aLatLon ${
                                    LatLng(
                                        taiwanData.lat,
                                        taiwanData.lon
                                    )
                                }"
                            )
                        }
                        dataType = "taiwan"
                    }

                    usLocations != null -> {
                        for (location in usLocations) {
                            mapDataList.add(
                                MapData(
                                    lat = location.sta_lat.toDouble(),
                                    lon = location.sta_lon.toDouble(),
                                    pm25 = location.pm2p5.toDouble(),
                                    actualDataTag = "us${location.sta_lat}${location.sta_lon}"
                                )
                            )
                        }
                        dataType = "usa"
                    }

                    outDoorExtraDetailed != null -> {
                        //This info is not used in the map--
                        for (location in outDoorExtraDetailed) {
                            val tag = "${location.company_id}${location.location_id}"
                            searchData.add(
                                SearchSuggestionsData(
                                    actualDataTag = tag,
                                    nameToDisplay = location.location_name,
                                    location_id = location.location_id,
                                    monitor_id = "",
                                    company_id = location.company_id,
                                    isForOutDoorLoc = true,
                                    isForMonitor = false,
                                    isForIndoorLoc = false,
                                    lat = location.lat.toDouble(),
                                    lon = location.lon.toDouble(),
                                )
                            )
                        }
                        dataType = "outdoor-detailed"

                    }

                    otherLocations != null -> {
                        for (location in otherLocations) {
                            mapDataList.add(
                                MapData(
                                    actualDataTag = "other${location.lat}${location.lon}",
                                    lat = location.lat.toDouble(),
                                    lon = location.lon.toDouble(),
                                    pm25 = location.reading.toDouble()
                                )
                            )
                        }
                        dataType = "other"
                    }
                }

                if (mapDataList.isNotEmpty()) {
                    mapDataDao.insertAll(mapDataList)
                }

                if (searchData.isNotEmpty()) {
                    searchSuggestionsDataDao.deleteAllOutDoorSearchSuggestions()
                    searchSuggestionsDataDao.insertSuggestions(searchData)
                }
                myLogger.logThis(
                    TAG,
                    "mapOutDoorLocationsToMapData()",
                    "success $dataType ${mapDataList.size} mapped data"
                )

            } catch (e: java.lang.Exception) {
                myLogger.logThis(TAG, "mapOutDoorLocationsToMapData()", "exception ${e.message}", e)
            }
        }

    }


}