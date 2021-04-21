package com.android_dev.cleanairspaces.repositories.ui_based

import com.android_dev.cleanairspaces.persistence.local.models.dao.MapDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.SearchSuggestionsDataDao
import com.android_dev.cleanairspaces.persistence.local.models.dao.WatchedLocationHighLightsDao
import com.android_dev.cleanairspaces.persistence.local.models.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapDataRepo
@Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val mapDataDao: MapDataDao,
    private val searchSuggestionsDataDao: SearchSuggestionsDataDao,
    private val watchedLocationHighLightsDao: WatchedLocationHighLightsDao
) {

    private val TAG = MapDataRepo::class.java.simpleName


    fun getSearchSuggestions(query: String): Flow<List<SearchSuggestionsData>> {
        return searchSuggestionsDataDao.getSearchSuggestions(
            query = "%$query%"
        )
    }

    fun getMapDataFlow() = mapDataDao.getMapDataFlow()
    fun getWatchedLocationHighLights() = watchedLocationHighLightsDao.getWatchedLocationHighLights()

    suspend fun refreshMapData() {
        val newMapData = getMapData()
        mapDataDao.insertAll(newMapData)
        val allMapData = mapDataDao.getAllMapData()
        //outdoor locations search
        searchSuggestionsDataDao.deleteAllOutDoorSearchSuggestions()
        var locationsCounter = 0
        for ((i, mapData) in allMapData.withIndex()) {
            //populate with actual API data NOT mapData
            val binder = "${mapData.lat}${mapData.lon}"
            searchSuggestionsDataDao.insertSuggestion(
                SearchSuggestionsData(
                    actualDataTag = binder,
                    isForOutDoorLoc = true,
                    nameToDisplay = "out door loc $i"
                )
            )
            val found = watchedLocationHighLightsDao.checkIfIsWatchedLocation(binder) > 0
            if (!found) {
                //update
                watchedLocationHighLightsDao.insertLocation(
                    WatchedLocationHighLights(
                        actualDataTag = binder,
                        lat = mapData.lat,
                        lon = mapData.lon,
                        pm_outdoor = if (i % 2 != 0) mapData.pm25 else 10.0,
                        pm_indoor = if (i % 2 == 0) mapData.pm25 else null,
                        name = "out door loc $i",
                        logo = "",
                        location_area = dummyLocations[locationsCounter],
                        indoor_co2 = testCo2s.random(),
                        indoor_humidity = testHumidities.random(),
                        indoor_temperature = testTemperatures.random() ,
                        indoor_voc = testVCos.random(),
                        energyMonth = if (i % 2 == 0) 136.0 else null,
                        energyMax = if (i % 2 == 0) 200000.120 else null,
                        isIndoorLoc = (i % 2 == 0)
                    )
                )
                locationsCounter++
                if (locationsCounter == 6)
                    locationsCounter = 0
            }
        }
    }

    fun watchALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        coroutineScope.launch(Dispatchers.IO) {
            watchedLocationHighLightsDao.insertLocation(watchedLocationHighLight)
        }
    }

    fun stopWatchingALocation(watchedLocationHighLight: WatchedLocationHighLights) {
        coroutineScope.launch(Dispatchers.IO) {
            watchedLocationHighLightsDao.deleteWatchedLocationHighLights(watchedLocationHighLight)
        }
    }


    fun getLastDaysHistory(dataTag: String): Flow<List<LocationHistoryThreeDays>> {
        TODO("Not yet implemented")
    }

    fun getLastWeekHistory(dataTag: String): Flow<List<LocationHistoryWeek>> {
        TODO("Not yet implemented")
    }

    fun getLastMonthHistory(dataTag: String): Flow<List<LocationHistoryMonth>> {
        TODO("Not yet implemented")
    }

    fun getLastTimeUpdatedHistory(dataTag: String): Long? {
        TODO("Not yet implemented")
    }



    /******************** DUMMY MAPA DATA API **********/
    private fun getMapData(): ArrayList<MapData> {
        return arrayListOf(
            MapData(
                lat = DummyMapData.SHANGHAI.mapData.lat,
                lon = DummyMapData.SHANGHAI.mapData.lon,
                pm25 = DummyMapData.SHANGHAI.mapData.pm25
            ),
            MapData(
                lat = DummyMapData.BEIJING.mapData.lat,
                lon = DummyMapData.BEIJING.mapData.lon,
                pm25 = DummyMapData.BEIJING.mapData.pm25
            ),
            MapData(
                lat = DummyMapData.SUZHOU.mapData.lat,
                lon = DummyMapData.SUZHOU.mapData.lon,
                pm25 = DummyMapData.SUZHOU.mapData.pm25
            ),
            MapData(
                lat = DummyMapData.HANGZHOU.mapData.lat,
                lon = DummyMapData.HANGZHOU.mapData.lon,
                pm25 = DummyMapData.HANGZHOU.mapData.pm25
            ),
            MapData(
                lat = DummyMapData.GUANGZHOU.mapData.lat,
                lon = DummyMapData.GUANGZHOU.mapData.lon,
                pm25 = DummyMapData.GUANGZHOU.mapData.pm25
            ),
            MapData(
                lat = DummyMapData.ZHUHAI.mapData.lat,
                lon = DummyMapData.ZHUHAI.mapData.lon,
                pm25 = DummyMapData.ZHUHAI.mapData.pm25
            )
        )
    }


}

val dummyLocations =
    listOf<String>("Shanghai", "Beijing", "Suzhou", "Hangzhou", "Guangzhou", "Zhuhai")

enum class DummyMapData(val mapData: MapData, val status: String) {
    SHANGHAI(MapData(lat = 31.224361, lon = 121.469170, pm25 = 10.1), status = "good"),
    BEIJING(MapData(lat = 39.916668, lon = 116.383331, pm25 = 26.0), status = "moderate"),
    SUZHOU(MapData(lat = 31.299999, lon = 120.599998, pm25 = 55.0), status = "gUnhealthy"),
    HANGZHOU(MapData(lat = 30.250000, lon = 120.166664, pm25 = 130.10), status = "unhealthy"),
    GUANGZHOU(MapData(lat = 23.128994, lon = 113.253250, pm25 = 360.0), status = "vUnhealthy"),
    ZHUHAI(MapData(lat = 22.27694, lon = 113.56778, pm25 = 360.0), status = "hazardous")
}

val testTemperatures = listOf<Double>(15.0, 18.0, 26.0, 30.0, 22.0 )
val testCo2s = listOf<Double>(200.0, 800.14, 1000.55)
val testHumidities = listOf<Double>( 34.3, 36.1, 55.2, 72.0,80.0)
val testVCos = listOf<Double>(0.22, 0.60 ,0.90 )