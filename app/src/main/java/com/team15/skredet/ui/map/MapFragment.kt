package com.team15.skredet.ui.map

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.team15.skredet.*
import com.team15.skredet.dataclasses.CoordinateDetails
import com.team15.skredet.dataclasses.Destinations
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Creates maps and sets up listeners
 * @author Permki
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var myMap: GoogleMap
    private lateinit var viewModel: SharedViewModel
    private lateinit var currentMarker: Marker
    private var locationPermissionRequestCode = 1
    private var currentAdviceIndex = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    /**
     * Initializes by finding viewmodel, creating map and activating listeners
     * @author Permki
     * @param savedInstanceState
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        viewModel = activity?.run { ViewModelProvider(this)[SharedViewModel::class.java] }
            ?: throw Exception("Invalid Activity")
        viewModel.profileFavoriteIcon = favoriteButton
        super.onActivityCreated(savedInstanceState)
        map.onCreate(savedInstanceState)
        map.onResume()
        map.getMapAsync(this)
        retainInstance = true
        activateListeners()
        viewModel.currentDaynumber = viewModel.numDays.value!! - viewModel.futurePredictionSize
    }

    /**
     * Defines map, sets up Google Maps UI, activates listener for mapclicks and centres the map so that
     * the whole country is visible
     * @author Permki
     * @param googleMap - Standard for mapview
     */
    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        viewModel.myMap = myMap
        moveCameraTo("Sentrum")
        enableUIsettings()
        myMap.setOnMapClickListener { coordinate ->
            viewModel.currentCoordinates.value = coordinate
        }
        setUpMap()
    }

    /**
     * Checks the location permission (needed for Android OS), and defines maptype either default or user-
     * preserved setting
     * @author Permki
     */
    private fun setUpMap() {
        if (context?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
            return
        }
        myMap.isMyLocationEnabled = true
        if (viewModel.mapType.value == null) {
            myMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        } else myMap.mapType = viewModel.mapType.value!!
        viewModel.mapType.observe(
            viewLifecycleOwner,
            Observer { myMap.mapType = viewModel.mapType.value!! })

    }

    /**
     * Animates through the dataset obtained from API at present.
     * Starts by setting animating true in order to ensure only one animation may be run at a time
     * Sets date by manipulating the seekbar which is attached to to a listener that recolors and updates ui
     * @author Permki
     */
    private fun animate() = MainScope().launch {
        viewModel.animating = true
        val currentDay = viewModel.currentDaynumber
        for (currentDaynumber in 0 until viewModel.numDays.value!!) {
            seekBar.progress = currentDaynumber
            delay(viewModel.animationDelay)
        }
        seekBar.progress = currentDay
        viewModel.animating = false
    }

    /**
     * Uses the passed in coordinates to make an API-call for the amount of days reflected in the settings
     * The camera is zoomed in on the clicked coordinate, and the cardview is updated.
     * @author Permki
     * @param coord - the coordinate caught by the mapClickListener
     */
    private fun getCoordinateData() = MainScope().launch {
        card_view_map.slideRight()
        currentAdviceIndex = 0
        mapProgressbar.show()
        if (::currentMarker.isInitialized) currentMarker.remove()
        val dateString =
            getDate(viewModel.currentDaynumber - (viewModel.numDays.value!! - viewModel.futurePredictionSize))

        try {
            val response = Fuel.get(
                getString(R.string.by_coordinates_details_general).format(
                    viewModel.getLatText(),
                    viewModel.getLongText(),
                    dateString,
                    dateString
                )
            ).awaitString()
            viewModel.internet.value = true
            if (response == "null") {
                context?.let { getString(R.string.press_within_boundaries).toast(it) }
            } else {
                val element = Gson().fromJson(response, CoordinateDetails::class.java)
                if (element[0].DangerLevel != "null") {
                    viewModel.currentRegionId = element[0].RegionId
                    viewModel.currentRegionName.value = element[0].RegionName
                    updateCardview()
                    myMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                viewModel.currentCoordinates.value?.latitude!!,
                                viewModel.currentCoordinates.value!!.longitude + viewModel.leftShiftDisplayOffset.value!!
                            ), viewModel.zoomLevel + 3
                        )
                    )
                    currentMarker = myMap.placeMarkerOnMap(viewModel.currentCoordinates.value!!)
                    delay(200)
                    card_view_map.slideLeft()
                }
            }
        } catch (e: Exception) {
            viewModel.updateFailureOrigin = "Map"
            viewModel.internet.value = false
        }
        mapProgressbar.hide()
    }

    /**
     * Updates info on cardview
     * Selfexplanatory
     * @author Permki
     */
    private fun updateCardview() {
        currentAdviceIndex = 0
        val entry = viewModel.mainInfoMap[viewModel.currentRegionId]
        if (entry != null) {
            viewModel.currentDangerLevel =
                entry.AvalancheWarningList[viewModel.currentDaynumber].DangerLevel
            element_dangerlevel_map.text =
                getString(R.string.dangerlevel_map_text).format(viewModel.currentDangerLevel)
            element_region_name_map.text = viewModel.currentRegionName.value
            element_day.text =
                getDate(viewModel.currentDaynumber - (viewModel.numDays.value!! - viewModel.futurePredictionSize))
            if (viewModel.currentDangerLevel != "0") {
                element_description_map.text =
                    entry.AvalancheWarningList[viewModel.currentDaynumber].AvalancheAdvices[currentAdviceIndex].Text
                profilePic_map.show()
                emptyPic.hide()
                context?.let {
                    Glide.with(it)
                        .load(entry.AvalancheWarningList[viewModel.currentDaynumber].AvalancheAdvices[currentAdviceIndex].ImageUrl)
                        .into(profilePic_map)
                }
            } else {
                element_description_map.text = getString(R.string.no_dangers)
                profilePic_map.hide()
                emptyPic.show()
            }
            viewModel.colorPicker[viewModel.currentDangerLevel]?.let {
                element_dangerlevel_map.setBackgroundColor(
                    it
                )
            }
            viewModel.setColorOnHeart()
        }
    }

    /**
     * Used to move camera to predefined place
     * @author Permki
     * @param name -> name of location
     */
    private fun moveCameraTo(name: String) {
        myMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                Destinations.getDestination(name),
                viewModel.zoomLevel
            )
        )
    }

    /**
     * Selfexplanatory
     * @author Permki
     */
    private fun enableUIsettings() {
        myMap.uiSettings.isZoomControlsEnabled = true
        myMap.uiSettings.isMyLocationButtonEnabled = true
        myMap.uiSettings.isZoomGesturesEnabled = true
    }

    /**
     * Negates visibility of seekbar
     * @author Permki
     */
    private fun seekBarVisibilitySetter() {
        if (seekBar.isVisible) {
            seekBar.hide()
        } else {
            seekBar.show()
        }
    }

    /**
     * Selfexplanatory. Initializes all map-listeners
     * @author Permki
     */
    private fun activateListeners() {
        slider_text.text = getDate(viewModel.currentDaynumber)
        viewModel.colorMap.observe(viewLifecycleOwner, Observer { viewModel.recolorPolygons() })
        viewModel.currentCoordinates.observe(viewLifecycleOwner, Observer { getCoordinateData() })
        viewModel.animate.observe(viewLifecycleOwner, Observer { animate() })
        slider_text.setOnClickListener { seekBarVisibilitySetter() }
        favoriteButton.setOnClickListener { viewModel.addToFavorites() }
        info_close_button.setOnClickListener { card_view_map.slideRight() }
        openDetailedInfo.setOnClickListener { triggerDetailedFrag() }
        element_region_name_map.setOnClickListener { triggerDetailedFrag() }
        zoomOut.setOnClickListener {
            moveCameraTo("Sentrum")
            card_view_map.slideRight()
            if (::currentMarker.isInitialized) currentMarker.remove()
        }

        viewModel.updateSeekbarSize.observe(viewLifecycleOwner, Observer {
            seekBar.max = viewModel.numDays.value!! - 1
            seekBar.progress = viewModel.numDays.value!! - viewModel.futurePredictionSize
        })
        profilePic_map.setOnClickListener() {
            currentAdviceIndex++
            val entry = viewModel.mainInfoMap[viewModel.currentRegionId]
            if (entry != null) {
                if (entry.AvalancheWarningList.get(viewModel.currentDaynumber).AvalancheAdvices.size <= currentAdviceIndex) currentAdviceIndex =
                    0
                element_description_map.text =
                    entry.AvalancheWarningList[viewModel.currentDaynumber].AvalancheAdvices[currentAdviceIndex].Text
                context?.let {
                    Glide.with(it)
                        .load(entry.AvalancheWarningList[viewModel.currentDaynumber].AvalancheAdvices[currentAdviceIndex].ImageUrl)
                        .into(profilePic_map)
                }
                ObjectAnimator.ofFloat(profilePic_map, View.ALPHA, 0.2f, 1.0f).setDuration(1000)
                    .start()
            }
            pictureIndex.text = (currentAdviceIndex + 1).toString()
        }

        slider_text.text = getDate(viewModel.currentDaynumber)
        seekBar.max = viewModel.numDays.value!! - 1
        seekBar.progress = viewModel.numDays.value!! - viewModel.futurePredictionSize
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                viewModel.currentDaynumber = i
                slider_text.text =
                    getDate(viewModel.currentDaynumber - (viewModel.numDays.value!! - viewModel.futurePredictionSize))
                viewModel.recolorPolygons()
                updateCardview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    /**
     * Opens detailed frag.
     * @author Permki
     */
    private fun triggerDetailedFrag() {
        viewModel.currentDate = GregorianCalendar()
        //viewModel.currentDate.add(GregorianCalendar.DAY_OF_YEAR, viewModel.currentDaynumber - (viewModel.numDays.value!! - viewModel.futurePredictionSize))
        viewModel.currentDate.add(
            GregorianCalendar.DAY_OF_YEAR,
            viewModel.currentDaynumber - (viewModel.numDays.value!! - viewModel.futurePredictionSize)
        )
        viewModel.updateDetails.value = true
    }

    /**
     * Not in use as of now. Kept in case needed later on
     * @author Permki
     */
    override fun onMarkerClick(m: Marker?): Boolean {
        return true
    }
}
