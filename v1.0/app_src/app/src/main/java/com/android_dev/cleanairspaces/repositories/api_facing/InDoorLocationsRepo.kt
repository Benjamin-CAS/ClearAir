package com.android_dev.cleanairspaces.repositories.api_facing

import com.android_dev.cleanairspaces.persistence.api.responses.IndoorLocations
import com.android_dev.cleanairspaces.persistence.api.responses.IndoorLocationsResponse
import com.android_dev.cleanairspaces.persistence.api.services.InDoorLocationApiService
import com.android_dev.cleanairspaces.persistence.local.models.dao.SearchSuggestionsDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.SearchSuggestionsData
import com.android_dev.cleanairspaces.utils.CasEncDecQrProcessor
import com.android_dev.cleanairspaces.utils.MyLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InDoorLocationsRepo
@Inject constructor(
        private val coroutineScope: CoroutineScope,
        private val searchSuggestionsDataDao: SearchSuggestionsDataDao,
        private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao,
        private val inDoorLocationsApiService: InDoorLocationApiService) {

    private val TAG = IndoorLocationsResponse::class.java.simpleName

    suspend fun refreshInDoorLocations() {
        val timeStamp = System.currentTimeMillis().toString()
        val pl = CasEncDecQrProcessor.getEncryptedEncodedPayloadForIndoorLocation(timeStamp = timeStamp)
        val indoorLocationsResponse = inDoorLocationsApiService.fetchInDoorLocations(pl = pl)
        indoorLocationsResponse.enqueue(getInDoorLocationsResponseCallback())
    }

    private fun mapIndoorLocationsToSearchableData(indoorLocations: List<IndoorLocations>) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val searchData = ArrayList<SearchSuggestionsData>()
                for (location in indoorLocations) {
                    val tag = location.company_id
                    /** TODO later
                    searchData.add(
                            SearchSuggestionsData(
                                    actualDataTag = tag,
                                    isForOutDoorLoc = false,
                                    nameToDisplay = location.name_en,
                                    location_id = "",
                                    monitor_id = "",
                                    company_id = location.company_id,
                                    isForMonitor = false,
                                    isForIndoorLoc = true,
                                    is_secure = location.secure.toInt() == 1
                            )) **/
                }
                searchSuggestionsDataDao.deleteAllInDoorSearchSuggestions()
                searchSuggestionsDataDao.insertSuggestions(searchData)
                MyLogger.logThis(TAG, "saveInDoorLocations()", "saved ${indoorLocations.size} locations")
            } catch (e: java.lang.Exception) {
                MyLogger.logThis(TAG, "saveInDoorLocations()", "received ${indoorLocations.size} locations failed exc ${e.message}", e)
            }
        }
    }


    private fun getInDoorLocationsResponseCallback(): Callback<IndoorLocationsResponse> {
        return object : Callback<IndoorLocationsResponse> {
            override fun onResponse(
                    call: Call<IndoorLocationsResponse>,
                    response: Response<IndoorLocationsResponse>
            ) {
                when {
                    response.code() == 200 -> {
                        val responseBody = response.body()
                        try {
                            if (responseBody == null || responseBody.data.isNullOrEmpty()) {
                                MyLogger.logThis(
                                        TAG,
                                        "getInDoorLocationsResponseCallback()",
                                        "response body is null or empty",
                                )
                            } else {
                                mapIndoorLocationsToSearchableData(
                                        responseBody.data
                                )
                            }
                        } catch (e: Exception) {
                            MyLogger.logThis(
                                    TAG,
                                    "getInDoorLocationsResponseCallback()",
                                    "exception ${e.message}",
                                    e
                            )
                        }
                    }
                    else -> {
                        MyLogger.logThis(
                                TAG,
                                "getInDoorLocationsResponseCallback()",
                                "response code not 200, $response"
                        )
                    }
                }
            }

            override fun onFailure(call: Call<IndoorLocationsResponse>, t: Throwable) {
                MyLogger.logThis(TAG, "getInDoorLocationsResponseCallback() - OnFailure()->", "exc ${t.message}")
            }

        }
    }

}