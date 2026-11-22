package com.workoutmate.app.data

import kotlinx.serialization.Serializable

/** 운동 부위 */
enum class BodyPart(val label: String) {
    Chest("가슴"),
    Back("등"),
    Legs("하체"),
    Shoulders("어깨"),
    Arms("팔"),
    Core("코어"),
    Cardio("유산소"),
}

/** 한 세트: 무게(kg) × 횟수. 맨몸 운동은 무게 0 */
@Serializable
data class SetLog(val weight: Float, val reps: Int, val done: Boolean = false)

/** 그날 한 종목의 기록 */
data class WorkoutLog(
    val id: Long,
    val date: String,          // yyyy-MM-dd
    val name: String,
    val part: BodyPart,
    val order: Int,
    val sets: List<SetLog>,
) {
    /** 완료한 세트의 볼륨 (무게 × 횟수 합) */
    val volume: Float get() = sets.filter { it.done }.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
    val doneSets: Int get() = sets.count { it.done }
}

/** 루틴 안의 한 종목 (목표 세트·횟수·무게) */
@Serializable
data class RoutineItem(
    val name: String,
    val part: BodyPart,
    val sets: Int,
    val reps: Int,
    val weight: Float,
)

/** 요일별 운동 루틴. day: 1=월 … 7=일, 0=요일 없음 */
data class Routine(
    val id: Long,
    val name: String,
    val day: Int,
    val items: List<RoutineItem>,
)

val DAY_LABELS = listOf("없음", "월", "화", "수", "목", "금", "토", "일")
