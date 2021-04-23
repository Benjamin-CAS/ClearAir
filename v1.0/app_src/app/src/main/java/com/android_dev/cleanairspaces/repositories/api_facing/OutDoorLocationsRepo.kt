package com.android_dev.cleanairspaces.repositories.api_facing

import com.android_dev.cleanairspaces.persistence.api.responses.*
import com.android_dev.cleanairspaces.persistence.api.services.OutDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.local.models.dao.MapDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.SearchSuggestionsDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.MapData
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.persistence.local.models.entities.WatchedLocationHighLights
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.L_TIME_KEY
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.PAYLOAD_KEY
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
        private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        private val outDoorLocationApiService: OutDoorLocationApiService) {

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
                                mapOutDoorLocationsToMapData(otherLocations = locations)
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

            override fun onFailure(call: Call<OutDoorLocationResponse>, e: Throwable) {
                MyLogger.logThis(
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
                                mapOutDoorLocationsToMapData(
                                        usLocations = responseBody
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
                                mapOutDoorLocationsToMapData(
                                        taiwanLocations = responseBody
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

    private fun mapOutDoorLocationsToMapData(
            taiwanLocations: List<OutDoorLocationTaiwan>? = null,
            usLocations: List<OutDoorLocationAmerica>? = null,
            otherLocations: List<OutDoorLocationsOther>? = null,
            inDoorExtraDetailed: List<OutDoorDetailedLocationData>? = null) {
        coroutineScope.launch(Dispatchers.IO) {

            try {
                //update map data
                val mapDataList = ArrayList<MapData>()
                val searchData = ArrayList<SearchSuggestionsData>()
                when {
                    taiwanLocations != null -> {
                        for (location in taiwanLocations) {
                            mapDataList.add(MapData(
                                    lat = location.lat.toDouble(),
                                    lon = location.lon.toDouble(),
                                    pm25 = location.pm2p5.toDouble(),
                                    actualDataTag = ""
                            ))
                        }
                    }

                    usLocations != null -> {
                        for (location in usLocations) {
                            mapDataList.add(MapData(
                                    lat = location.sta_lat.toDouble(),
                                    lon = location.sta_lon.toDouble(),
                                    pm25 = location.pm2p5.toDouble(),
                                    actualDataTag = ""
                            ))
                        }
                    }

                    inDoorExtraDetailed != null -> {
                        //TODO? more data & map data
                        for (location in inDoorExtraDetailed) {
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
                                    ))
                        }

                    }

                    otherLocations != null -> {
                        for (location in otherLocations) {
                            val tag = "${location.location_id}${location.monitor_id}"
                            mapDataList.add(MapData(
                                    actualDataTag = tag,
                                    lat = location.lat.toDouble(),
                                    lon = location.lon.toDouble(),
                                    pm25 = location.reading.toDouble()
                            ))
                            //update watched location
                            val matchingLocations = watchedLocationHighLightsDao.checkIfIsWatchedLocation(tag)
                            val found = matchingLocations.isNotEmpty()
                            if (found) {
                                val foundLocation = matchingLocations[0]
                                //update preserve pwd & username data
                                watchedLocationHighLightsDao.insertLocation(
                                        WatchedLocationHighLights(
                                                actualDataTag = tag,
                                                lat = location.lat.toDouble(),
                                                lon = location.lon.toDouble(),
                                                pm_outdoor = location.reading.toDouble(),
                                                pm_indoor = foundLocation.pm_indoor,
                                                name = location.name_en,
                                                logo = foundLocation.logo,
                                                location_area = foundLocation.location_area,
                                                indoor_co2 = foundLocation.indoor_co2,
                                                indoor_humidity = foundLocation.indoor_humidity,
                                                indoor_temperature = foundLocation.indoor_temperature,
                                                indoor_voc = foundLocation.indoor_voc,
                                                energyMonth = foundLocation.energyMonth,
                                                energyMax = foundLocation.energyMax,
                                                isIndoorLoc = false,
                                                compId = foundLocation.compId,
                                                locId = location.location_id,
                                                monitorId = location.monitor_id,
                                                lastRecPwd = foundLocation.lastRecPwd,
                                                lastRecUsername = foundLocation.lastRecUsername
                                        )
                                )
                            }
                        }
                    }
                }

                if (mapDataList.isNotEmpty()) {
                    mapDataDao.deleteAll()
                    mapDataDao.insertAll(mapDataList)
                }

                if (searchData.isNotEmpty()) {
                    searchSuggestionsDataDao.deleteAllOutDoorSearchSuggestions()
                    searchSuggestionsDataDao.insertSuggestions(searchData)
                }
                MyLogger.logThis(TAG, "mapOutDoorLocationsToMapData()", "success")

            } catch (e: java.lang.Exception) {
                MyLogger.logThis(TAG, "mapOutDoorLocationsToMapData()", "exception ${e.message}", e)
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

        //refreshing taiwan locations
        val taiwanLocationsResponse = outDoorLocationApiService.fetchTaiwanOutDoorLocations()
        taiwanLocationsResponse.enqueue(getTaiwanOutDoorLocationsResponseCallback())

        val timeStamp  = System.currentTimeMillis().toString()
        val data = JsonObject()
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForOutdoorLocation(
                timeStamp = timeStamp
        )
        data.addProperty(L_TIME_KEY, timeStamp)
        data.addProperty(PAYLOAD_KEY, pl)
        val request =  outDoorLocationApiService.fetchOutDoorLocationsExtraDetails(
                data = data
        )
        request.enqueue(object  : Callback<OutDoorDetailsLocationResponse>{
            override fun onResponse(call: Call<OutDoorDetailsLocationResponse>, response: Response<OutDoorDetailsLocationResponse>) {

                val responseBody = response.body()
                try{
                   if (responseBody!!.data.isNotEmpty()){
                       mapOutDoorLocationsToMapData(
                               inDoorExtraDetailed = responseBody!!.data
                       )
                   }

                }catch (exc : java.lang.Exception){
                    MyLogger.logThis(
                            TAG, "failed $responseBody", "${exc.message}", exc
                    )
                }
            }

            override fun onFailure(call: Call<OutDoorDetailsLocationResponse>, t: Throwable) {
                MyLogger.logThis(
                        TAG, "failed retrofit", "${t.message}"
                )
            }


        })

    }

}