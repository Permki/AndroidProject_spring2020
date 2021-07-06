package com.team15.skredet

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.gson.Gson
import com.team15.skredet.dataclasses.AllRegionsDetailsData
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class MainTest {

    @Test
    fun testformat() {
        //Date start at year 1900, and month stats with 0
        val date = Date(120, 4, 21)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val time = formatter.format(date)
        val idealTime = "2020-05-21"
        println(date)
        assertEquals(idealTime, time)
    }

    @Test
    fun clearTest() {
        val fullMap = hashMapOf<Int, String>()
        val emptyMap = hashMapOf<Int, String>()

        fullMap[10] = "Norge"
        emptyMap[10] = "Norge"
        emptyMap.clear()

        assertNotEquals(fullMap, emptyMap)
    }

    @Test
    fun destinationsTest() {
        val destinations: HashMap<String, Array<Double>> = hashMapOf(
            "Oslo" to arrayOf(59.911491, 10.757933),
            "Trondheim" to arrayOf(63.446827, 10.421906),
            "Sentrum" to arrayOf(65.0, 19.0)
        )
        assertEquals(destinations["Oslo"], arrayOf(59.911491, 10.757933))
    }

    @Test
    private fun getData() = MainScope().launch {
        var infoList = hashMapOf<Int, AllRegionsDetailsData>()
        try {
            val response =
                Fuel.get("https://api01.nve.no/hydrology/forecast/avalanche/v5.0.1/api/RegionSummary/Detail/1/2020-05-26/2020-05-26")
                    .awaitString()
            if (response.isNotEmpty()) {
                for (element in Gson().fromJson(response, Array<AllRegionsDetailsData>::class.java)
                    .toMutableList())
                    infoList[element.Id] = element
                assert(true)
            }
        } catch (e: java.lang.Exception) {
            assert(false)
        }
    }

}