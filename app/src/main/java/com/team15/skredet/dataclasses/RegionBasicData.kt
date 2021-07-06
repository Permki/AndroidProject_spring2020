package com.team15.skredet.dataclasses

data class RegionBasicData(
    val CountyList: List<County>,
    val Id: Int,
    val MunicipalityList: List<Municipality>,
    val Name: String,
    val Polygon: List<String>,
    val TypeId: Int,
    val TypeName: String
)