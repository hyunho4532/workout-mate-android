package com.workoutmate.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.Clock
import com.workoutmate.app.data.RestTimer
import com.workoutmate.app.data.SetLog
import com.workoutmate.app.data.WorkoutLog
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.components.AppButton
import com.workoutmate.app.ui.components.AppCard
import com.workoutmate.app.ui.components.ButtonKind
import com.workoutmate.app.ui.components.Chunky
import com.workoutmate.app.ui.components.ExercisePickerSheet
import com.workoutmate.app.ui.components.LineIcon
import com.workoutmate.app.ui.components.LineIconKind
import com.workoutmate.app.ui.components.NumberDialog
import com.workoutmate.app.ui.components.PlayfulProgressBar
import com.workoutmate.app.ui.components.Stepper
import com.workoutmate.app.ui.components.fmt
import com.workoutmate.app.ui.components.grouped
import com.workoutmate.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

/** 오늘: 오늘 할 운동을 세트별로 체크하고, 체크하면 휴식 타이머가 돌아요 */
@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val date = today.toString()
    val logs = WorkoutRepository.logsOn(date)
    val todayRoutine = WorkoutRepository.routineFor(today.dayOfWeek.value)

    var picking by remember { mutableStateOf(false) }
    val totalSets = logs.sumOf { it.sets.size }
    val doneSets = logs.sumOf { it.doneSets }
    val volume = logs.sumOf { it.volume.toDouble() }.toFloat()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
                    Text(
                        "${today.monthValue}월 ${today.dayOfMonth}일 ${today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textTertiary,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("오늘의 운동", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary)
                }
            }

            if (logs.isNotEmpty()) {
                item {
                    AppCard(contentPadding = PaddingValues(18.dp)) {
                        Row {
                            SummaryCell("볼륨", "${volume.grouped()}kg", Modifier.weight(1f))
                            SummaryCell("세트", "$doneSets / $totalSets", Modifier.weight(1f))
                            SummaryCell("종목", "${logs.size}", Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(14.dp))
                        PlayfulProgressBar(
                            progress = if (totalSets == 0) 0f else doneSets.toFloat() / totalSets,
                            color = if (totalSets > 0 && doneSets == totalSets) c.good else c.primary,
                        )
                    }
                }
            } else {
                item {
                    AppCard {
                        if (todayRoutine != null) {
                            Text("오늘 루틴", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                            Spacer(Modifier.height(4.dp))
                            Text(todayRoutine.name, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                todayRoutine.items.joinToString(" · ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                            )
                            Spacer(Modifier.height(16.dp))
                            AppButton(
                                "루틴 시작",
                                onClick = { WorkoutRepository.startRoutine(todayRoutine, date) },
                                leading = LineIconKind.Play,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            Text("아직 기록이 없어요", style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "오른쪽 아래 + 버튼으로 운동을 추가하거나, 루틴 탭에서 루틴을 시작하세요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textSecondary,
                            )
                        }
                    }
                }
                // 다른 루틴으로 바로 시작
                val others = WorkoutRepository.routines.filter { it.id != todayRoutine?.id }
                if (others.isNotEmpty()) {
                    item {
                        Text(
                            "다른 루틴으로 시작",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textTertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        )
                    }
                    items(others, key = { "r${it.id}" }) { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(c.surface)
                                .clickable { WorkoutRepository.startRoutine(r, date) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(r.name, style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                                Text("${r.items.size}종목", style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
                            }
                            LineIcon(LineIconKind.Play, c.primary, Modifier.size(16.dp))
                        }
                    }
                }
            }

            items(logs, key = { it.id }) { log -> ExerciseCard(log) }
        }

        // 아래쪽: 휴식 타이머 + 운동 추가 버튼
        Column(
            Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            // 운동 추가 플로팅 버튼
            FloatingActionButton(
                onClick = { picking = true },
                shape = CircleShape,
                containerColor = c.primary,
                contentColor = c.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
                modifier = Modifier.size(60.dp),
            ) {
                LineIcon(LineIconKind.Plus, c.onPrimary, Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            RestTimerBar()
        }
    }

    if (picking) {
        ExercisePickerSheet(
            onPick = { name, part ->
                WorkoutRepository.addExercise(date, name, part)
                picking = false
            },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun SummaryCell(label: String, value: String, modifier: Modifier) {
    val c = AppTheme.colors
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
    }
}

/** 한 종목 카드: 세트마다 무게·횟수 조절 + 완료 체크 */
@Composable
private fun ExerciseCard(log: WorkoutLog) {
    val c = AppTheme.colors
    val badge = AppTheme.badgeFor(log.part.label)
    // 직접 입력 창: (세트 번호, 무게인지)
    var editing by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }

    fun update(i: Int, set: SetLog) = WorkoutRepository.updateSets(log.id, log.sets.toMutableList().also { it[i] = set })

    AppCard(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(log.name, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                log.part.label,
                style = MaterialTheme.typography.labelSmall,
                color = badge.strong,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badge.soft)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                "삭제",
                style = MaterialTheme.typography.labelMedium,
                color = c.textTertiary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { WorkoutRepository.removeLog(log.id) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        // 머리줄
        Row(Modifier.padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("세트", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.width(36.dp))
            Text("kg", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.weight(1f).padding(start = 40.dp))
            Text("회", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.weight(1f).padding(start = 40.dp))
            Spacer(Modifier.width(48.dp))
        }
        log.sets.forEachIndexed { i, set ->
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(if (set.done) c.good.copy(alpha = 0.15f) else c.fill),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = if (set.done) c.good else c.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Stepper(
                    value = set.weight.fmt(),
                    onMinus = { update(i, set.copy(weight = max(0f, set.weight - 2.5f))) },
                    onPlus = { update(i, set.copy(weight = set.weight + 2.5f)) },
                    onTapValue = { editing = i to true },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(6.dp))
                Stepper(
                    value = "${set.reps}",
                    onMinus = { update(i, set.copy(reps = max(0, set.reps - 1))) },
                    onPlus = { update(i, set.copy(reps = set.reps + 1)) },
                    onTapValue = { editing = i to false },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                CheckButton(done = set.done) {
                    val nowDone = !set.done
                    update(i, set.copy(done = nowDone))
                    if (nowDone) RestTimer.start()
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                "세트 추가",
                onClick = {
                    val last = log.sets.lastOrNull() ?: SetLog(0f, 10)
                    WorkoutRepository.updateSets(log.id, log.sets + last.copy(done = false))
                },
                kind = ButtonKind.Weak,
                height = 40.dp,
                leading = LineIconKind.Plus,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                "세트 빼기",
                onClick = { WorkoutRepository.updateSets(log.id, log.sets.dropLast(1)) },
                enabled = log.sets.size > 1,
                kind = ButtonKind.Weak,
                height = 40.dp,
                leading = LineIconKind.Minus,
                modifier = Modifier.weight(1f),
            )
        }
    }

    editing?.let { (i, isWeight) ->
        val set = log.sets.getOrNull(i) ?: return@let
        NumberDialog(
            title = if (isWeight) "${i + 1}세트 무게 (kg)" else "${i + 1}세트 횟수",
            initial = if (isWeight) set.weight.fmt() else "${set.reps}",
            decimal = isWeight,
            onDismiss = { editing = null },
            onConfirm = { v -> update(i, if (isWeight) set.copy(weight = v) else set.copy(reps = v.toInt())) },
        )
    }
}

@Composable
private fun CheckButton(done: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    val face by animateColorAsState(if (done) c.good else c.surface, label = "checkFace")
    Chunky(
        face = face,
        lip = if (done) c.goodDark else c.divider,
        border = if (done) null else c.divider,
        shape = RoundedCornerShape(10.dp),
        depth = 2.dp,
        onClick = onClick,
        modifier = Modifier.size(40.dp, 42.dp),
    ) {
        LineIcon(LineIconKind.Check, if (done) androidx.compose.ui.graphics.Color.White else c.textDisabled, Modifier.size(18.dp))
    }
}

/** 세트를 체크하면 나타나는 휴식 타이머 막대 */
@Composable
private fun RestTimerBar() {
    val c = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val endAt = RestTimer.endAt

    // 쉬는 동안 0.25초마다 시각을 갱신, 끝나면 진동 한 번
    LaunchedEffect(endAt) {
        while (endAt != null) {
            Clock.now = System.currentTimeMillis()
            if (Clock.now >= endAt) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                RestTimer.stop()
                break
            }
            delay(250)
        }
    }

    AnimatedVisibility(
        visible = endAt != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
    ) {
        val remainingMs = ((endAt ?: 0L) - Clock.now).coerceAtLeast(0L)
        val remaining = ((remainingMs + 999) / 1000).toInt()
        val progress = 1f - remainingMs / (RestTimer.totalSeconds * 1000f)
        AppCard(face = c.textPrimary, edge = c.textPrimary, contentPadding = PaddingValues(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LineIcon(LineIconKind.Timer, c.surface, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("휴식", style = MaterialTheme.typography.labelLarge, color = c.surface.copy(alpha = 0.7f))
                Spacer(Modifier.width(8.dp))
                Text(
                    "%d:%02d".format(remaining / 60, remaining % 60),
                    style = MaterialTheme.typography.headlineSmall,
                    color = c.surface,
                    modifier = Modifier.weight(1f),
                )
                TimerChip("+30초") { RestTimer.addSeconds(30) }
                Spacer(Modifier.width(6.dp))
                TimerChip("건너뛰기") { RestTimer.stop() }
            }
            Spacer(Modifier.height(10.dp))
            PlayfulProgressBar(progress = progress, color = c.primary)
        }
    }
}

@Composable
private fun TimerChip(text: String, onClick: () -> Unit) {
    val c = AppTheme.colors
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = c.surface,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(c.surface.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}
