package com.team15.skredet.dataclasses

import com.google.android.gms.maps.model.LatLng

class Destinations {

    companion object {

        fun getDestination(name: String): LatLng? {
            return destinations.get(name)?.get(0)
                ?.let { LatLng(it, destinations.get(name)?.get(1)!!) }
        }

        private val destinations: HashMap<String, Array<Double>> = hashMapOf(
            "Oslo" to arrayOf(59.911491, 10.757933),
            "Trondheim" to arrayOf(63.446827, 10.421906),
            "Sentrum" to arrayOf(65.0, 19.0)
        )
    }
}