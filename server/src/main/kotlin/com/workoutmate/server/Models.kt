package com.workoutmate.server

import kotlinx.serialization.Serializable

/** 앱이 보내는 내 정보 */
@Serializable
data class RoutineRequest(
    val goal: String,
    val level: String,
    val daysPerWeek: Int,
    val minutes: Int,
    val equipment: String,
    val note: String = "",
)

// ---- Claude가 돌려주는 루틴 (앱으로 그대로 전달) ----

@Serializable
data class RoutineItem(
    val name: String,
    val part: String,   // Chest / Back / Legs / Shoulders / Arms / Core / Cardio
    val sets: Int,
    val reps: Int,
    val weight: Float,
)

@Serializable
data class PlanRoutine(val name: String, val day: Int, val items: List<RoutineItem>)

@Serializable
data class RoutinePlan(val routines: List<PlanRoutine>, val tips: List<String>)

@Serializable
data class ErrorDto(val message: String)
