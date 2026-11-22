package com.workoutmate.server

import com.anthropic.errors.AnthropicServiceException
import com.anthropic.errors.RateLimitException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("workout-server")

fun main() {
    // 8787은 내신메이트 서버가 쓰니 8788
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8788
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    // 모의 모드: MOCK_MODE=true 이거나 API 키가 없으면 Claude 대신 샘플 루틴 (요금 0원)
    val mockMode = System.getenv("MOCK_MODE")?.lowercase() == "true" ||
        System.getenv("ANTHROPIC_API_KEY").isNullOrBlank()
    val generator = if (mockMode) null else RoutineGenerator()
    if (mockMode) log.warn("모의 모드로 실행 중: Claude를 호출하지 않고 샘플 루틴을 돌려줍니다.")
    else log.info("Claude(${RoutineGenerator.MODEL})로 루틴을 만듭니다.")

    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }

    routing {
        get("/health") { call.respondText(if (mockMode) "ok (mock)" else "ok") }

        post("/routine") {
            val req = runCatching { call.receive<RoutineRequest>() }.getOrNull()
            if (req == null || req.daysPerWeek !in 1..7 || req.minutes !in 10..240) {
                call.respond(HttpStatusCode.BadRequest, ErrorDto("입력 값을 확인해 주세요."))
                return@post
            }
            try {
                val plan = if (generator == null) {
                    delay(2_000)
                    MockRoutine.generate(req)
                } else {
                    withContext(Dispatchers.IO) { generator.generate(req) }
                }
                call.respond(plan)
            } catch (e: GenerationException) {
                call.respond(HttpStatusCode.UnprocessableEntity, ErrorDto(e.message ?: "루틴을 만들지 못했어요."))
            } catch (e: RateLimitException) {
                log.warn("rate limited", e)
                call.respond(HttpStatusCode.TooManyRequests, ErrorDto("지금 요청이 많아요. 잠시 후 다시 시도해 주세요."))
            } catch (e: AnthropicServiceException) {
                log.error("Claude API error", e)
                call.respond(HttpStatusCode.BadGateway, ErrorDto("루틴을 만드는 중 오류가 났어요. 다시 시도해 주세요."))
            } catch (e: Exception) {
                log.error("routine failed", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorDto("루틴을 만드는 중 오류가 났어요. 다시 시도해 주세요."))
            }
        }
    }
}
