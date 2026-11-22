package com.workoutmate.app.data

/** 운동 추가할 때 고를 수 있는 기본 종목 목록 */
data class ExerciseDef(val name: String, val part: BodyPart, val defaultWeight: Float, val defaultReps: Int)

object Exercises {
    val all = listOf(
        ExerciseDef("벤치프레스", BodyPart.Chest, 40f, 10),
        ExerciseDef("인클라인 덤벨 프레스", BodyPart.Chest, 14f, 10),
        ExerciseDef("체스트 플라이", BodyPart.Chest, 20f, 12),
        ExerciseDef("푸시업", BodyPart.Chest, 0f, 15),
        ExerciseDef("데드리프트", BodyPart.Back, 60f, 8),
        ExerciseDef("랫풀다운", BodyPart.Back, 40f, 10),
        ExerciseDef("바벨 로우", BodyPart.Back, 40f, 10),
        ExerciseDef("시티드 로우", BodyPart.Back, 35f, 12),
        ExerciseDef("풀업", BodyPart.Back, 0f, 8),
        ExerciseDef("스쿼트", BodyPart.Legs, 50f, 10),
        ExerciseDef("레그 프레스", BodyPart.Legs, 80f, 12),
        ExerciseDef("런지", BodyPart.Legs, 10f, 12),
        ExerciseDef("레그 컬", BodyPart.Legs, 25f, 12),
        ExerciseDef("카프 레이즈", BodyPart.Legs, 30f, 15),
        ExerciseDef("오버헤드 프레스", BodyPart.Shoulders, 25f, 10),
        ExerciseDef("사이드 레터럴 레이즈", BodyPart.Shoulders, 6f, 15),
        ExerciseDef("페이스 풀", BodyPart.Shoulders, 15f, 15),
        ExerciseDef("바벨 컬", BodyPart.Arms, 20f, 12),
        ExerciseDef("해머 컬", BodyPart.Arms, 10f, 12),
        ExerciseDef("트라이셉스 푸시다운", BodyPart.Arms, 20f, 12),
        ExerciseDef("딥스", BodyPart.Arms, 0f, 10),
        ExerciseDef("플랭크(초)", BodyPart.Core, 0f, 60),
        ExerciseDef("크런치", BodyPart.Core, 0f, 20),
        ExerciseDef("행잉 레그 레이즈", BodyPart.Core, 0f, 12),
        ExerciseDef("러닝(분)", BodyPart.Cardio, 0f, 20),
        ExerciseDef("사이클(분)", BodyPart.Cardio, 0f, 20),
        ExerciseDef("줄넘기(분)", BodyPart.Cardio, 0f, 10),
    )

    fun find(name: String): ExerciseDef? = all.firstOrNull { it.name == name }
}
