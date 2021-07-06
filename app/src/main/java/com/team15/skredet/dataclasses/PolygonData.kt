package com.team15.skredet.dataclasses

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

data class PolygonData(
    val Id: Int,
    val name: String,
    val Coordinates: List<LatLng>,
    var dangerLevel: String,
    var polygon: Polygon?,
    var poloptions: PolygonOptions?,
    var polygonCentre: LatLng,
    var marker: Marker?
)
