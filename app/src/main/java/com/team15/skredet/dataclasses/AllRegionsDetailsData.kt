package com.team15.skredet.dataclasses

data class AllRegionsDetailsData(
    val AvalancheWarningList: List<AvalancheWarning>,
    val Id: Int,
    val Name: String,
    val TypeId: Int,
    val TypeName: String,
    var DangerLevel: String?
)