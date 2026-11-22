package com.workoutmate.app.data

import com.workoutmate.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/** AI에게 루틴을 부탁할 때 보내는 내 정보 */
@Serializable
data class RoutineRequest(
    val goal: String,          // 근력 / 근비대 / 다이어트 / 체력
    val level: String,         // 초보 / 중급 / 고급
    val daysPerWeek: Int,
    val minutes: Int,          // 한 번에 운동할 시간
    val equipment: String,     // 헬스장 / 덤벨 / 맨몸
    val note: String = "",     // 부상, 집중하고 싶은 부위 등
)

/** AI가 짜 준 루틴 묶음 */
@Serializable
data class RoutinePlan(val routines: List<PlanRoutine>, val tips: List<String>)

@Serializable
data class PlanRoutine(val name: String, val day: Int, val items: List<RoutineItem>)

class RoutineApiException(message: String) : Exception(message)

object RoutineApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.MINUTES)
        .callTimeout(4, TimeUnit.MINUTES)
        .build()

    suspend fun generate(req: RoutineRequest): RoutinePlan {
        if (BuildConfig.LOCAL_MOCK) {
            delay(3_000) // 로딩 화면을 볼 수 있게
            return LocalRoutineMock.generate(req)
        }
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("${BuildConfig.SERVER_URL}/routine")
                .post(json.encodeToString(req).toRequestBody("application/json".toMediaType()))
                .build()
            try {
                http.newCall(request).execute().use { res ->
                    val body = res.body?.string().orEmpty()
                    if (!res.isSuccessful) {
                        val msg = runCatching { json.decodeFromString<ErrorBody>(body).message }.getOrNull()
                        throw RoutineApiException(msg ?: "루틴을 만들지 못했어요. (${res.code})")
                    }
                    json.decodeFromString<RoutinePlan>(body)
                }
            } catch (e: IOException) {
                throw RoutineApiException("서버에 연결할 수 없어요. 인터넷 연결을 확인해 주세요.")
            }
        }
    }

    @Serializable
    private data class ErrorBody(val message: String)
}

/**
 * 서버 없이 앱에서 만드는 샘플 루틴 (테스트용).
 * 주 몇 회인지에 따라 무분할 / 상하체 / 3분할로 나누고, 수준에 맞춰 세트와 무게를 조절해요.
 */
object LocalRoutineMock {
    fun generate(req: RoutineRequest): RoutinePlan {
        val scale = when (req.level) { "초보" -> 0.6f; "고급" -> 1.3f; else -> 1f }
        val bodyweightOnly = req.equipment == "맨몸"
        val reps = when (req.goal) { "근력" -> 5; "근비대" -> 10; else -> 15 }
        val sets = if (req.minutes <= 30) 3 else 4

        fun item(name: String, part: BodyPart): RoutineItem {
            val def = Exercises.find(name)
            val weight = if (bodyweightOnly) 0f else ((def?.defaultWeight ?: 0f) * scale / 2.5f).toInt() * 2.5f
            return RoutineItem(name, part, sets, if (part == BodyPart.Core || part == BodyPart.Cardio) def?.defaultReps ?: reps else reps, weight)
        }

        val upper = if (bodyweightOnly) listOf(item("푸시업", BodyPart.Chest), item("풀업", BodyPart.Back), item("딥스", BodyPart.Arms))
        else listOf(item("벤치프레스", BodyPart.Chest), item("바벨 로우", BodyPart.Back), item("오버헤드 프레스", BodyPart.Shoulders), item("바벨 컬", BodyPart.Arms))
        val lower = if (bodyweightOnly) listOf(item("런지", BodyPart.Legs), item("크런치", BodyPart.Core), item("플랭크(초)", BodyPart.Core))
        else listOf(item("스쿼트", BodyPart.Legs), item("레그 프레스", BodyPart.Legs), item("레그 컬", BodyPart.Legs), item("플랭크(초)", BodyPart.Core))
        val cardio = listOf(item(if (bodyweightOnly) "줄넘기(분)" else "러닝(분)", BodyPart.Cardio))

        val days = req.daysPerWeek.coerceIn(2, 6)
        val schedule = listOf(1, 3, 5, 2, 4, 6).take(days).sorted()
        val routines = schedule.mapIndexed { i, day ->
            when (days) {
                2, 3 -> PlanRoutine("전신 ${'A' + i}", day, upper.take(2) + lower.take(2) + if (req.goal == "다이어트") cardio else emptyList())
                else -> if (i % 2 == 0) PlanRoutine("상체 ${'A' + i / 2}", day, upper)
                else PlanRoutine("하체 ${'A' + i / 2}", day, lower + if (req.goal == "다이어트") cardio else emptyList())
            }
        }
        return RoutinePlan(
            routines = routines,
            tips = listOf(
                "[테스트 모드] 앱 안의 샘플로 만든 루틴이에요.",
                "세트 사이 휴식은 ${if (req.goal == "근력") "2~3분" else "60~90초"}이 적당해요.",
                "마지막 세트가 쉬워지면 무게를 2.5kg씩 올려 보세요.",
            ),
        )
    }
}
