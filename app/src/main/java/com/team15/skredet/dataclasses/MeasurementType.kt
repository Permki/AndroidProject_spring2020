package com.team15.skredet.dataclasses

data class MeasurementType(
    val Id: Int,
    val MeasurementSubTypes: List<MeasurementSubType>,
    val Name: String,
    val SortOrder: Int
)