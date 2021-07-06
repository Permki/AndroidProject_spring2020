package com.team15.skredet.dataclasses

data class MountainWeather(
    val CloudCoverId: Int,
    val CloudCoverName: String,
    val Comment: String,
    val LastSavedTime: String,
    val MeasurementTypes: List<MeasurementType>
)