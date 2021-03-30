package com.cleanairspaces.android.models.repository

import com.cleanairspaces.android.models.api.OutDoorLocationsApiService
import com.cleanairspaces.android.models.api.responses.OutDoorLocationAmerica
import com.cleanairspaces.android.models.api.responses.OutDoorLocationsOtherResponse
import com.cleanairspaces.android.models.api.responses.OutDoorLocationTaiwan
import com.cleanairspaces.android.models.api.responses.OutDoorLocationsOther
import com.cleanairspaces.android.models.dao.OutDoorLocationsDao
import com.cleanairspaces.android.models.entities.LocationAreas
import com.cleanairspaces.android.models.entities.OutDoorLocations
import com.cleanairspaces.android.utils.DEFAULT_LOCATION_EN_NAME
import com.cleanairspaces.android.utils.MyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutDoorLocationsRepo
@Inject constructor(
        private val outDoorLocationsApiService: OutDoorLocationsApiService,
        private val coroutineScope: CoroutineScope,
        private val outDoorLocationsDao: OutDoorLocationsDao
){
    private val TAG = OutDoorLocationsRepo::class.java.simpleName

        fun  getOutDoorLocationsFlow() = outDoorLocationsDao.getOutDoorLocationsFlow()

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
                                MyLogger.logThis(TAG,"getOtherOutDoorLocationsResponseCallback()", "locations in response are null or empty" )
                            } else {
                                MyLogger.logThis(TAG,"getOtherOutDoorLocationsResponseCallback()", "total ${locations.size}")
                                saveOutDoorLocations(locations, location_area = LocationAreas.OTHER)
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(TAG,"getOtherOutDoorLocationsResponseCallback()", "exception ${e.message}", e)
                        }
                    }
                    else -> {
                        MyLogger.logThis(TAG,"getOtherOutDoorLocationsResponseCallback()", "response code not 200, $response" )
                    }
                }
            }

            override fun onFailure(call: Call<OutDoorLocationsOtherResponse>, e: Throwable) {
                MyLogger.logThis(TAG,"getOtherOutDoorLocationsResponseCallback()", "OnFailure-exception ${e.message}")
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
                                MyLogger.logThis(TAG,"getAmericaOutDoorLocationsResponseCallback()", "locations in response are null or empty" )
                            } else {
                                MyLogger.logThis(TAG,"getAmericaOutDoorLocationsResponseCallback()", "total ${responseBody.size}")
                                saveOutDoorLocations(responseBody, location_area = LocationAreas.AMERICA)
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(TAG,"getAmericaOutDoorLocationsResponseCallback()", "exception ${e.message}", e)
                        }
                    }
                    else -> {
                        MyLogger.logThis(TAG,"getAmericaOutDoorLocationsResponseCallback()", "response code not 200, $response" )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationAmerica>>, e: Throwable) {
                MyLogger.logThis(TAG,"getAmericaOutDoorLocationsResponseCallback()", "OnFailure-exception ${e.message}")
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
                                MyLogger.logThis(TAG,"getTaiwanOutDoorLocationsResponseCallback()", "locations in response are null or empty" )
                            } else {
                                MyLogger.logThis(TAG,"getTaiwanOutDoorLocationsResponseCallback()", "total ${responseBody.size}")
                                saveOutDoorLocations(responseBody, location_area = LocationAreas.TAIWAN)
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(TAG,"getTaiwanOutDoorLocationsResponseCallback()", "exception ${e.message}", e)
                        }
                    }
                    else -> {
                        MyLogger.logThis(TAG,"getTaiwanOutDoorLocationsResponseCallback()", "response code not 200, $response" )
                    }
                }
            }

            override fun onFailure(call: Call<List<OutDoorLocationTaiwan>>, e: Throwable) {
                MyLogger.logThis(TAG,"getTaiwanOutDoorLocationsResponseCallback()", "OnFailure-exception ${e.message}")
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
    
    fun saveOutDoorLocations(locations : List<Any>, location_area : LocationAreas){
        coroutineScope.launch {
            when (location_area) {
                LocationAreas.AMERICA -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationAmerica
                        val outDoorLocations = OutDoorLocations(
                            pm2p5 = location.pm2p5,
                            lon = location.sta_lon,
                            lat = location.sta_lat,
                            location_area = LocationAreas.AMERICA
                        )
                        outDoorLocationsDao.insertOutDoorLocation(outDoorLocations)

                    }
                }
                LocationAreas.TAIWAN -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationTaiwan
                        val outDoorLocations = OutDoorLocations(
                                pm2p5 = location.pm2p5,
                                lon = location.lon,
                                lat = location.lat,
                                location_area = LocationAreas.TAIWAN
                        )
                        outDoorLocationsDao.insertOutDoorLocation(outDoorLocations)
                    }

                }
                LocationAreas.OTHER -> {
                    val locationsList = locations as List<*>
                    for (loc in locationsList) {
                        val location = loc as OutDoorLocationsOther
                        val outDoorLocations = OutDoorLocations(
                                location_id = location.location_id,
                                monitor_id = location.monitor_id,
                                name_en = location.name_en,
                                reading = location.reading,
                                date_reading = location.date_reading,
                                lon = location.lon,
                                lat = location.lat,
                                location_area = LocationAreas.OTHER
                        )
                        outDoorLocationsDao.insertOutDoorLocation(outDoorLocations)
                    }
                }
            }
        }
    }
}