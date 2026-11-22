package com.workoutmate.app.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.DAY_LABELS
import com.workoutmate.app.data.Exercises
import com.workoutmate.app.data.Routine
import com.workoutmate.app.data.RoutineApi
import com.workoutmate.app.data.RoutineApiException
import com.workoutmate.app.data.RoutineItem
import com.workoutmate.app.data.RoutinePlan
import com.workoutmate.app.data.RoutineRequest
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.components.AppButton
import com.workoutmate.app.ui.components.AppCard
import com.workoutmate.app.ui.components.AppTextField
import com.workoutmate.app.ui.components.ButtonKind
import com.workoutmate.app.ui.components.Chevron
import com.workoutmate.app.ui.components.ChevronDirection
import com.workoutmate.app.ui.components.ExercisePickerSheet
import com.workoutmate.app.ui.components.LineIcon
import com.workoutmate.app.ui.components.LineIconKind
import com.workoutmate.app.ui.components.NumberDialog
import com.workoutmate.app.ui.components.PlayfulProgressRing
import com.workoutmate.app.ui.components.SelectChip
import com.workoutmate.app.ui.components.Stepper
import com.workoutmate.app.ui.components.TopBar
import com.workoutmate.app.ui.components.fmt
import com.workoutmate.app.ui.theme.AppTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.max

// ================= 루틴 목록 =================

@Composable
fun RoutinesScreen(
    onEdit: (Long?) -> Unit,
    onAi: () -> Unit,
    onStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val routines = WorkoutRepository.routines.sortedWith(compareBy({ if (it.day == 0) 8 else it.day }, { it.name }))

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "루틴",
                style = MaterialTheme.typography.headlineMedium,
                color = c.textPrimary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            )
        }
        item {
            // AI 루틴 만들기
            AppCard(face = c.primarySoft, edge = c.primarySoft) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(c.primary),
                        contentAlignment = Alignment.Center,
                    ) { LineIcon(LineIconKind.Spark, c.onPrimary, Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AI로 루틴 만들기", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                        Text("목표와 운동 가능한 날만 알려 주세요", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
                    }
                }
                Spacer(Modifier.height(14.dp))
                AppButton("시작하기", onClick = onAi, height = 46.dp, modifier = Modifier.fillMaxWidth())
            }
        }
        items(routines, key = { it.id }) { r ->
            AppCard(
                modifier = Modifier.clickable { onEdit(r.id) },
                contentPadding = PaddingValues(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (r.day == 0) "자유" else DAY_LABELS[r.day],
                        style = MaterialTheme.typography.labelLarge,
                        color = if (r.day == LocalDate.now().dayOfWeek.value) c.primary else c.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(c.fill)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(r.name, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                    Chevron(ChevronDirection.Right, c.textDisabled, Modifier.size(16.dp), stroke = 2.dp)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    r.items.joinToString(" · ") { "${it.name} ${it.sets}×${it.reps}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textSecondary,
                    maxLines = 2,
                )
                Spacer(Modifier.height(12.dp))
                AppButton(
                    "오늘 이 루틴으로 운동",
                    onClick = {
                        WorkoutRepository.startRoutine(r, LocalDate.now().toString())
                        onStarted()
                    },
                    kind = ButtonKind.Weak,
                    height = 42.dp,
                    leading = LineIconKind.Play,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            AppButton(
                "새 루틴 직접 만들기",
                onClick = { onEdit(null) },
                kind = ButtonKind.Secondary,
                leading = LineIconKind.Plus,
                height = 48.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ================= 루틴 편집 =================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoutineEditScreen(routineId: Long?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val original = routineId?.let { WorkoutRepository.findRoutine(it) }
    var name by remember { mutableStateOf(original?.name ?: "") }
    var day by remember { mutableStateOf(original?.day ?: 0) }
    val items = remember { mutableStateListOf<RoutineItem>().apply { addAll(original?.items.orEmpty()) } }
    var picking by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Pair<Int, String>?>(null) } // (종목, 필드)

    Column(modifier.fillMaxSize()) {
        TopBar(onBack, title = if (original == null) "새 루틴" else "루틴 수정")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard {
                Text("이름", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                Spacer(Modifier.height(6.dp))
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "예) 상체 A")
                Spacer(Modifier.height(14.dp))
                Text("요일", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { d -> SelectChip(DAY_LABELS[d], selected = day == d) { day = d } }
                    SelectChip("자유", selected = day == 0) { day = 0 }
                }
            }

            items.forEachIndexed { i, item ->
                AppCard(contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = c.textTertiary)
                        Spacer(Modifier.width(10.dp))
                        Text(item.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, modifier = Modifier.weight(1f))
                        Text(
                            "빼기",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textTertiary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { items.removeAt(i) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LabeledStepper("세트", "${item.sets}", Modifier.weight(1f),
                            onMinus = { items[i] = item.copy(sets = max(1, item.sets - 1)) },
                            onPlus = { items[i] = item.copy(sets = item.sets + 1) },
                            onTap = { editing = i to "sets" })
                        LabeledStepper("회", "${item.reps}", Modifier.weight(1f),
                            onMinus = { items[i] = item.copy(reps = max(1, item.reps - 1)) },
                            onPlus = { items[i] = item.copy(reps = item.reps + 1) },
                            onTap = { editing = i to "reps" })
                        LabeledStepper("kg", item.weight.fmt(), Modifier.weight(1f),
                            onMinus = { items[i] = item.copy(weight = max(0f, item.weight - 2.5f)) },
                            onPlus = { items[i] = item.copy(weight = item.weight + 2.5f) },
                            onTap = { editing = i to "weight" })
                    }
                }
            }

            AppButton(
                "운동 추가",
                onClick = { picking = true },
                kind = ButtonKind.Weak,
                leading = LineIconKind.Plus,
                height = 46.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            if (original != null) {
                Text(
                    "이 루틴 삭제",
                    style = MaterialTheme.typography.labelLarge,
                    color = c.bad,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            WorkoutRepository.deleteRoutine(original.id)
                            onBack()
                        }
                        .padding(12.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
        Box(Modifier.background(c.background).padding(16.dp)) {
            AppButton(
                "저장",
                onClick = {
                    WorkoutRepository.saveRoutine(
                        Routine(original?.id ?: 0L, name.trim().ifEmpty { "내 루틴" }, day, items.toList()),
                    )
                    onBack()
                },
                enabled = items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (picking) {
        ExercisePickerSheet(
            onPick = { n, part ->
                val def = Exercises.find(n)
                items.add(RoutineItem(n, part, 3, def?.defaultReps ?: 10, def?.defaultWeight ?: 0f))
                picking = false
            },
            onDismiss = { picking = false },
        )
    }

    editing?.let { (i, field) ->
        val item = items.getOrNull(i) ?: return@let
        NumberDialog(
            title = when (field) { "sets" -> "세트 수"; "reps" -> "횟수"; else -> "무게 (kg)" },
            initial = when (field) { "sets" -> "${item.sets}"; "reps" -> "${item.reps}"; else -> item.weight.fmt() },
            decimal = field == "weight",
            onDismiss = { editing = null },
            onConfirm = { v ->
                items[i] = when (field) {
                    "sets" -> item.copy(sets = max(1, v.toInt()))
                    "reps" -> item.copy(reps = max(1, v.toInt()))
                    else -> item.copy(weight = max(0f, v))
                }
            },
        )
    }
}

@Composable
private fun LabeledStepper(
    label: String,
    value: String,
    modifier: Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onTap: () -> Unit,
) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppTheme.colors.textTertiary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        Stepper(value, onMinus, onPlus, onTap, Modifier.fillMaxWidth())
    }
}

// ================= AI 루틴 =================

private val GOALS = listOf("근력", "근비대", "다이어트", "체력")
private val LEVELS = listOf("초보", "중급", "고급")
private val MINUTES = listOf(30, 45, 60, 90)
private val EQUIPMENT = listOf("헬스장", "덤벨", "맨몸")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiRoutineScreen(onBack: () -> Unit, onSaved: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    var goal by remember { mutableStateOf("근비대") }
    var level by remember { mutableStateOf("초보") }
    var days by remember { mutableStateOf(3) }
    var minutes by remember { mutableStateOf(60) }
    var equipment by remember { mutableStateOf("헬스장") }
    var note by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var plan by remember { mutableStateOf<RoutinePlan?>(null) }

    Column(modifier.fillMaxSize()) {
        TopBar(onBack, title = "AI 루틴")

        when {
            loading -> Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoadingRing()
                Spacer(Modifier.height(20.dp))
                Text("루틴을 짜고 있어요", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text("목표와 수준에 맞춰 종목과 세트를 고르는 중", style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
            }

            plan != null -> {
                val p = plan!!
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("이렇게 짜 봤어요", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary, modifier = Modifier.padding(start = 4.dp))
                    p.routines.forEach { r ->
                        AppCard(contentPadding = PaddingValues(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    DAY_LABELS.getOrElse(r.day) { "자유" },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = c.primary,
                                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.primarySoft).padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(r.name, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                            }
                            Spacer(Modifier.height(10.dp))
                            r.items.forEach { item ->
                                Row(Modifier.padding(vertical = 3.dp)) {
                                    Text(item.name, style = MaterialTheme.typography.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
                                    Text(
                                        "${item.sets}×${item.reps}" + if (item.weight > 0f) " · ${item.weight.fmt()}kg" else "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.textSecondary,
                                    )
                                }
                            }
                        }
                    }
                    if (p.tips.isNotEmpty()) {
                        AppCard(face = c.fillWeak) {
                            Text("팁", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                            Spacer(Modifier.height(6.dp))
                            p.tips.forEach { Text("· $it", style = MaterialTheme.typography.bodySmall, color = c.textSecondary, modifier = Modifier.padding(vertical = 2.dp)) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row(Modifier.background(c.background).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppButton("다시 만들기", onClick = { plan = null }, kind = ButtonKind.Weak, modifier = Modifier.weight(1f))
                    AppButton(
                        "루틴 저장",
                        onClick = {
                            p.routines.forEach { r -> WorkoutRepository.saveRoutine(Routine(0L, r.name, r.day, r.items)) }
                            onSaved()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            else -> {
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FormBlock("목표") { ChipRow(GOALS, goal) { goal = it } }
                    FormBlock("운동 경험") { ChipRow(LEVELS, level) { level = it } }
                    FormBlock("일주일에 며칠") { ChipRow((2..6).map { "${it}일" }, "${days}일") { days = it.removeSuffix("일").toInt() } }
                    FormBlock("한 번에 운동 시간") { ChipRow(MINUTES.map { "${it}분" }, "${minutes}분") { minutes = it.removeSuffix("분").toInt() } }
                    FormBlock("운동 장소 · 도구") { ChipRow(EQUIPMENT, equipment) { equipment = it } }
                    FormBlock("참고할 점 (선택)") {
                        AppTextField(value = note, onValueChange = { note = it }, placeholder = "예) 허리가 약해요, 엉덩이 위주로", minLines = 2)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Column(Modifier.background(c.background).padding(16.dp)) {
                    error?.let {
                        Text(it, style = MaterialTheme.typography.labelMedium, color = c.bad, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
                    }
                    AppButton(
                        "루틴 만들기",
                        leading = LineIconKind.Spark,
                        onClick = {
                            error = null
                            loading = true
                            scope.launch {
                                try {
                                    plan = RoutineApi.generate(RoutineRequest(goal, level, days, minutes, equipment, note))
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: RoutineApiException) {
                                    error = e.message
                                } catch (e: Exception) {
                                    error = "루틴을 만들지 못했어요. 다시 시도해 주세요."
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingRing() {
    val t = rememberInfiniteTransition(label = "ai")
    val p by t.animateFloat(0.05f, 0.95f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "p")
    Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        PlayfulProgressRing(progress = p, modifier = Modifier.size(120.dp), thickness = 10.dp)
        LineIcon(LineIconKind.Spark, AppTheme.colors.primary, Modifier.size(36.dp))
    }
}

@Composable
private fun FormBlock(title: String, content: @Composable () -> Unit) {
    AppCard(contentPadding = PaddingValues(18.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = AppTheme.colors.textPrimary)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { o -> SelectChip(o, selected = o == selected) { onSelect(o) } }
    }
}
