package com.team15.skredet

import android.app.Activity
import android.graphics.Color
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import com.team15.skredet.dataclasses.PolygonData
import java.util.*

/**
 * The viewmodel contains all publicly shared variables and especially all LiveData used by observers
 * @author Permki
 */
class SharedViewModel : ViewModel() {

    lateinit var main: Activity
    lateinit var myMap: GoogleMap
    lateinit var profileFavoriteIcon: ImageButton
    lateinit var detailFavoriteIcon: ImageButton
    var pushWarningUp = true
    var pushWarningDown = true
    var updateDetails = MutableLiveData<Boolean>()
    var startFragId = R.id.nav_home
    var tempNumDays = 7
    var biggestNum = 7
    var update = MutableLiveData<Boolean>()
    var updateSeekbarSize = MutableLiveData<Boolean>()
    var fragmentList = hashMapOf<Int, Fragment>()
    var fragmentTitles = hashMapOf<Int, String>()
    var noHamburgerFrags = mutableListOf<Int>()
    var colorMap = MutableLiveData<Boolean>()
    var updateFavorites = MutableLiveData<Boolean>()
    var updatedTime = MutableLiveData<String>()
    val futurePredictionSize = 3 //set prediction. standard for NVE == 3
    var numDays = MutableLiveData(7)
    val maxNumDays = 30
    var currentDate = GregorianCalendar()
    var currentDaynumber = 0
    var currentRegionId = 0
    var animationDelay = 200L
    var animate = MutableLiveData<Boolean>()
    var animating = false
    var currentDangerLevel = ""
    var currentRegionName = MutableLiveData("")
    var zoomLevel = 4.2F
    var borderThickness = 4F
    val leftShiftDisplayOffset = MutableLiveData(0.8) //used to moving camera to the side of marker
    var favoritesList = hashMapOf<Int, String>()
    val polygonList = hashMapOf<Int, PolygonData>()
    var mainInfoMap = hashMapOf<Int, AllRegionsDetailsData>()
    var currentCoordinates = MutableLiveData<LatLng>()
    var mapType = MutableLiveData<Int>()
    var fragStack = Stack<Fragment>()
    var internet = MutableLiveData(true)
    var showWelcomePage = true
    var infoPageShown = MutableLiveData<Boolean>()
    var updateFailureOrigin = "Main"

    var mapTypePicker = hashMapOf(
        "Hybrid" to GoogleMap.MAP_TYPE_HYBRID, "Terreng" to GoogleMap.MAP_TYPE_TERRAIN,
        "Satelitt" to GoogleMap.MAP_TYPE_SATELLITE, "Vanlig" to GoogleMap.MAP_TYPE_NORMAL
    )

    var colorPicker = hashMapOf(
        "null" to 0, "0" to 0,
        "1" to Color.argb(127, 0, 255, 0),
        "2" to Color.argb(127, 242, 124, 39),
        "3" to Color.argb(127, 242, 39, 39),
        "4" to Color.argb(180, 242, 39, 39),
        "5" to Color.BLACK
    )

    /*Collection of variables used in the background process, depending on what type of
     notification the user wants and how often */
    var checkApiIntervalminutes: Long = 15

    /*When the application closes, the user wil get a notification stating that the application
    Is running in the background, this value decides if the user wants to see it every time
    He/she closes it */
    var showClosingNotification = true

    //Show closing notification only once per run
    var closingNotificationIsShowedDuringThisRun = false

    var deleteUserData = MutableLiveData(false)

    fun getLongText() = currentCoordinates.value!!.longitude.toString()
    fun getLatText() = currentCoordinates.value!!.latitude.toString()
    fun setColorOnHeart() {
        setColorOnHeart(currentRegionId)
    }

    fun addToFavorites() {
        addToFavorites(currentRegionId, currentDangerLevel)
    }

    fun addToFavorites(id: Int, dangerLevel: String) {
        if (favoritesList.contains(id)) {
            main.getString(R.string.removed_favorite).toast(main)
            id.let { favoritesList.remove(it) }
        } else {
            main.getString(R.string.added_favorite).toast(main)
            favoritesList[id] = dangerLevel
        }
        updateFavorites.value = true
        setColorOnHeart(id)
    }


    /**
     * If in favoriteslist, set color on heart in detailfrag and mapfrag
     * @author Permki & Sarekhs
     * @param id -> Id of region to be displayed in RegionDetails
     */
    private fun setColorOnHeart(id: Int) {
        if (favoritesList.contains(id) && id == currentRegionId) {
            detailFavoriteIcon.colorBlack()
            profileFavoriteIcon.colorBlack()
        } else {
            detailFavoriteIcon.removeColor()
            profileFavoriteIcon.removeColor()
        }
    }

    /**
     * Sets current information and triggers RegionDetails to update and MainActivity to switch to RegionDetails
     * @author Permki & Sarekhs
     * @param id -> Id of region to be displayed in RegionDetails
     */
    fun openDetailFragment(id: Int) {
        currentRegionId = id
        currentCoordinates.value = polygonList[id]?.polygonCentre
        currentRegionName.value = polygonList[id]?.name
        updateDetails.value = true
    }


    /**
     * Recolors all polygons if needed. Checks whether the user has unchecked colors, and then checks
     * if the colorsetting for each polygon has changed
     * @author Permki
     */
    fun recolorPolygons() {
        var dangerLevel = "0"
        for (pol in polygonList.values) {
            if (colorMap.value == true) dangerLevel =
                mainInfoMap[pol.Id]!!.AvalancheWarningList[currentDaynumber].DangerLevel
            if (dangerLevel != pol.dangerLevel) {
                pol.polygon?.remove()
                pol.dangerLevel = dangerLevel
                colorPicker[dangerLevel]?.let { pol.poloptions?.fillColor(it) }
                pol.polygon = myMap.addPolygon(pol.poloptions)
            }
        }
    }
}