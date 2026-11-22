package com.workoutmate.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.workoutmate.app.data.db.AppDatabase
import com.workoutmate.app.data.db.RoutineEntity
import com.workoutmate.app.data.db.WorkoutDao
import com.workoutmate.app.data.db.WorkoutLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

/**
 * 운동 기록과 루틴.
 * 화면은 메모리 목록을 바로 보고, 바뀔 때마다 폰 안의 Room DB에도 저장합니다.
 */
object WorkoutRepository {
    val logs = mutableStateListOf<WorkoutLog>()
    val routines = mutableStateListOf<Routine>()

    private var dao: WorkoutDao? = null
    private val io = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }
    private val ids = AtomicLong(System.currentTimeMillis() * 1000)

    private fun newId() = ids.incrementAndGet()

    suspend fun load(context: Context) {
        val d = AppDatabase.get(context).workoutDao().also { dao = it }

        logs.clear()
        logs.addAll(d.logs().map { it.toLog() })
        routines.clear()
        routines.addAll(d.routines().map { it.toRoutine() })
    }

    // ---- 운동 기록 ----

    fun logsOn(date: String): List<WorkoutLog> = logs.filter { it.date == date }.sortedBy { it.order }

    /** 종목 추가: 지난번에 했던 무게·횟수를 그대로 가져오고, 처음이면 기본값 3세트 */
    fun suggestedSets(name: String, date: String): List<SetLog> {
        val last = logs.lastOrNull { it.name == name && it.date != date }
        return last?.sets?.map { it.copy(done = false) }
            ?: Exercises.find(name)?.let { def -> List(3) { SetLog(def.defaultWeight, def.defaultReps) } }
            ?: List(3) { SetLog(0f, 10) }
    }

    /** 지난번 기록이 있으면 그 무게·횟수를 이어서 씀 */
    fun lastTime(name: String, date: String): WorkoutLog? = logs.lastOrNull { it.name == name && it.date != date }

    fun addExercise(date: String, name: String, part: BodyPart, sets: List<SetLog> = suggestedSets(name, date)) {
        val order = (logsOn(date).maxOfOrNull { it.order } ?: -1) + 1
        save(WorkoutLog(newId(), date, name, part, order, sets))
    }

    fun findLog(id: Long): WorkoutLog? = logs.firstOrNull { it.id == id }

    fun updateSets(id: Long, sets: List<SetLog>) {
        val log = logs.firstOrNull { it.id == id } ?: return
        save(log.copy(sets = sets))
    }

    fun removeLog(id: Long) {
        logs.removeAll { it.id == id }
        io.launch { dao?.deleteLog(id) }
    }

    /** 루틴을 오늘 기록으로 옮겨서 시작 */
    fun startRoutine(routine: Routine, date: String) {
        var order = (logsOn(date).maxOfOrNull { it.order } ?: -1) + 1
        routine.items.forEach { item ->
            save(WorkoutLog(newId(), date, item.name, item.part, order++, List(item.sets) { SetLog(item.weight, item.reps) }))
        }
    }

    private fun save(log: WorkoutLog) {
        val i = logs.indexOfFirst { it.id == log.id }
        if (i >= 0) logs[i] = log else logs.add(log)
        io.launch { dao?.upsertLog(log.toEntity()) }
    }

    // ---- 루틴 ----

    fun routineFor(day: Int): Routine? = routines.firstOrNull { it.day == day }

    fun findRoutine(id: Long): Routine? = routines.firstOrNull { it.id == id }

    /** 새 루틴이면 id 0으로 넘기면 됩니다 */
    fun saveRoutine(routine: Routine): Routine {
        val r = if (routine.id == 0L) routine.copy(id = newId()) else routine
        val i = routines.indexOfFirst { it.id == r.id }
        if (i >= 0) routines[i] = r else routines.add(r)
        io.launch { dao?.upsertRoutine(r.toEntity()) }
        return r
    }

    fun deleteRoutine(id: Long) {
        routines.removeAll { it.id == id }
        io.launch { dao?.deleteRoutine(id) }
    }

    // ---- 통계 ----

    /** 날짜 → 그날 완료한 볼륨(kg) */
    fun volumeByDate(): Map<String, Float> =
        logs.groupBy { it.date }.mapValues { (_, list) -> list.sumOf { it.volume.toDouble() }.toFloat() }

    /** 완료한 세트가 하나라도 있는 날 */
    fun workoutDates(): Set<String> = logs.filter { it.doneSets > 0 }.map { it.date }.toSet()

    /** 연속 운동일: 오늘(또는 어제)부터 거꾸로 하루도 안 빠지고 운동한 날 수 */
    fun streak(today: LocalDate = LocalDate.now()): Int {
        val days = workoutDates()
        var d = if (today.toString() in days) today else today.minusDays(1)
        var n = 0
        while (d.toString() in days) {
            n++
            d = d.minusDays(1)
        }
        return n
    }

    // ---- DB 변환 ----

    private fun WorkoutLog.toEntity() =
        WorkoutLogEntity(id, date, name, part.name, order, json.encodeToString(sets))

    private fun WorkoutLogEntity.toLog() = WorkoutLog(
        id = id,
        date = date,
        name = name,
        part = BodyPart.entries.firstOrNull { it.name == part } ?: BodyPart.Core,
        order = sortOrder,
        sets = json.decodeFromString<List<SetLog>>(setsJson),
    )

    private fun Routine.toEntity() = RoutineEntity(id, name, day, json.encodeToString(items))

    private fun RoutineEntity.toRoutine() =
        Routine(id, name, day, json.decodeFromString<List<RoutineItem>>(itemsJson))
}
