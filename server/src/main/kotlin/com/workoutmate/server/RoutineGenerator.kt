package com.workoutmate.server

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.core.JsonValue
import com.anthropic.helpers.MessageAccumulator
import com.anthropic.models.messages.JsonOutputFormat
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.StopReason
import kotlinx.serialization.json.Json

/** Claude가 거절했거나 결과를 못 만든 경우 (사용자에게 보여 줄 메시지 포함) */
class GenerationException(message: String) : Exception(message)

class RoutineGenerator(
    // ANTHROPIC_API_KEY 환경변수에서 키를 읽습니다. 키는 서버에만 두세요.
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun generate(req: RoutineRequest): RoutinePlan {
        val params = MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(32000L)
            .system(SYSTEM_PROMPT)
            .addUserMessage(userPrompt(req))
            .outputConfig(
                OutputConfig.builder()
                    .effort(OutputConfig.Effort.MEDIUM)
                    .format(
                        JsonOutputFormat.builder()
                            .schema(JsonOutputFormat.Schema.builder().putAllAdditionalProperties(PLAN_SCHEMA).build())
                            .build(),
                    )
                    .build(),
            )
            // 안전 분류기가 거절하면 서버에서 다른 모델로 자동 재시도
            .putAdditionalHeader("anthropic-beta", "server-side-fallback-2026-07-01")
            .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"))
            .build()

        val accumulator = MessageAccumulator.create()
        client.messages().createStreaming(params).use { stream ->
            stream.stream().forEach { accumulator.accumulate(it) }
        }
        val message = accumulator.message()

        when (message.stopReason().orElse(null)) {
            StopReason.REFUSAL -> throw GenerationException("이 요청으로는 루틴을 만들 수 없어요. 내용을 바꿔 다시 시도해 주세요.")
            StopReason.MAX_TOKENS -> throw GenerationException("루틴이 너무 길어져서 끝까지 만들지 못했어요. 다시 시도해 주세요.")
            else -> Unit
        }
        val text = message.content().mapNotNull { it.text().orElse(null)?.text() }.lastOrNull()
            ?: throw GenerationException("루틴을 만들지 못했어요. 다시 시도해 주세요.")
        return json.decodeFromString(RoutinePlan.serializer(), text)
    }

    private fun userPrompt(r: RoutineRequest) = buildString {
        appendLine("아래 사람에게 맞는 일주일 운동 루틴을 짜 주세요.")
        appendLine("- 목표: ${r.goal}")
        appendLine("- 운동 경험: ${r.level}")
        appendLine("- 일주일에 ${r.daysPerWeek}일, 한 번에 ${r.minutes}분")
        appendLine("- 운동 장소·도구: ${r.equipment}")
        if (r.note.isNotBlank()) appendLine("- 참고할 점: ${r.note.trim()}")
    }

    companion object {
        const val MODEL = "claude-opus-5-5"

        private val SYSTEM_PROMPT = """
            당신은 경험 많은 퍼스널 트레이너입니다. 사용자의 목표, 경험, 운동 가능한 날과 시간, 도구에 맞춰 현실적으로 따라 할 수 있는 주간 루틴을 짭니다.

            지켜 주세요:
            - 루틴 수는 일주일 운동일 수와 같게 하고, day는 1(월)~7(일) 중 겹치지 않게, 쉬는 날이 고르게 끼도록 배치합니다.
            - 한 루틴은 정해진 운동 시간 안에 끝나도록 종목 4~6개로 구성합니다. 세트 사이 휴식도 시간에 포함해서 계산합니다.
            - 종목 이름은 한국 헬스장에서 흔히 쓰는 이름(예: 벤치프레스, 랫풀다운, 스쿼트)으로, part는 Chest/Back/Legs/Shoulders/Arms/Core/Cardio 중 하나로 씁니다.
            - weight는 kg 단위의 시작 무게입니다. 경험 수준에 맞게 보수적으로 잡고, 맨몸 운동은 0으로 둡니다. 플랭크처럼 시간 기반 종목은 reps에 초, 유산소는 reps에 분을 넣습니다.
            - 맨몸만 가능하면 기구가 필요한 종목은 넣지 않습니다. 부상이나 주의할 점이 있으면 그 부위에 부담이 큰 종목은 빼거나 대체합니다.
            - name은 "상체 A", "하체", "전신 B"처럼 짧게 짓습니다.
            - tips에는 이 루틴을 따라 할 때 도움이 되는 짧은 조언 2~4개를 존댓말로 씁니다 (휴식 시간, 무게 올리는 시점 등).
        """.trimIndent()

        private fun str() = mapOf("type" to "string")
        private fun int() = mapOf("type" to "integer")

        private val ITEM_SCHEMA = mapOf(
            "type" to "object",
            "additionalProperties" to false,
            "required" to listOf("name", "part", "sets", "reps", "weight"),
            "properties" to mapOf(
                "name" to str(),
                "part" to mapOf("type" to "string", "enum" to listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")),
                "sets" to int(),
                "reps" to int(),
                "weight" to mapOf("type" to "number"),
            ),
        )

        private val PLAN_SCHEMA: Map<String, JsonValue> = mapOf(
            "type" to "object",
            "additionalProperties" to false,
            "required" to listOf("routines", "tips"),
            "properties" to mapOf(
                "routines" to mapOf(
                    "type" to "array",
                    "items" to mapOf(
                        "type" to "object",
                        "additionalProperties" to false,
                        "required" to listOf("name", "day", "items"),
                        "properties" to mapOf(
                            "name" to str(),
                            "day" to int(),
                            "items" to mapOf("type" to "array", "items" to ITEM_SCHEMA),
                        ),
                    ),
                ),
                "tips" to mapOf("type" to "array", "items" to str()),
            ),
        ).mapValues { JsonValue.from(it.value) }
    }
}

/** 모의 모드: Claude 없이 샘플 루틴 (API 요금 0원) */
object MockRoutine {
    fun generate(req: RoutineRequest): RoutinePlan {
        val bodyweight = req.equipment == "맨몸"
        val reps = when (req.goal) { "근력" -> 5; "근비대" -> 10; else -> 15 }
        val upper = if (bodyweight) listOf(RoutineItem("푸시업", "Chest", 3, 15, 0f), RoutineItem("풀업", "Back", 3, 8, 0f))
        else listOf(RoutineItem("벤치프레스", "Chest", 4, reps, 40f), RoutineItem("랫풀다운", "Back", 4, reps, 40f), RoutineItem("오버헤드 프레스", "Shoulders", 3, reps, 25f))
        val lower = if (bodyweight) listOf(RoutineItem("런지", "Legs", 3, 12, 0f), RoutineItem("플랭크(초)", "Core", 3, 60, 0f))
        else listOf(RoutineItem("스쿼트", "Legs", 4, reps, 50f), RoutineItem("레그 컬", "Legs", 3, 12, 25f), RoutineItem("플랭크(초)", "Core", 3, 60, 0f))
        val days = listOf(1, 3, 5, 2, 4, 6).take(req.daysPerWeek.coerceIn(2, 6)).sorted()
        return RoutinePlan(
            routines = days.mapIndexed { i, d ->
                if (i % 2 == 0) PlanRoutine("[모의] 상체 ${'A' + i / 2}", d, upper) else PlanRoutine("[모의] 하체 ${'A' + i / 2}", d, lower)
            },
            tips = listOf("모의 모드로 만든 루틴이에요. API 키를 넣으면 Claude가 직접 짜 줘요."),
        )
    }
}
