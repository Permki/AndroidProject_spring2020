package com.team15.skredet.externalFiles

data class StoredUserData(
    //val name: String,
    //val POIList: MutableList<Pair<String, Float>>
    val daysToShow: Int,
    val colorMap: Boolean,
    val favoritesList: MutableList<Pair<Int, String>>,
    val showAvalancheIncrease: Boolean,
    val showAvalancheDecrease: Boolean,
    val check_API_IntervalMinutes: Long,
    val showClosingNotification: Boolean,
    val showWelcomePage: Boolean,
    val mapType: Int
    //val polygonList: MutableList<PolygonData>

)