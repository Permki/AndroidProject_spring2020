package com.team15.skredet.dataclasses

data class AvalancheProblem(
    val AvalCauseId: Int,
    val AvalCauseName: String,
    val AvalProbabilityId: Int,
    val AvalProbabilityName: String,
    val AvalPropagationId: Int,
    val AvalPropagationName: String,
    val AvalTriggerSimpleId: Int,
    val AvalTriggerSimpleName: String,
    val AvalancheExtId: Int,
    val AvalancheExtName: String,
    val AvalancheProblemId: Int,
    val AvalancheProblemTypeId: Int,
    val AvalancheProblemTypeName: String,
    val AvalancheTypeId: Int,
    val AvalancheTypeName: String,
    val DestructiveSizeExtId: Int,
    val DestructiveSizeExtName: String,
    val ExposedHeight1: Int,
    val ExposedHeight2: Int,
    val ExposedHeightFill: Int,
    val ValidExpositions: String
)