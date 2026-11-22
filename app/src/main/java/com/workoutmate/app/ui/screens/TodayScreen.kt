package com.workoutmate.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.WorkoutLog
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.components.AppButton
import com.workoutmate.app.ui.components.AppCard
import com.workoutmate.app.ui.components.Chevron
import com.workoutmate.app.ui.components.ChevronDirection
import com.workoutmate.app.ui.components.LineIcon
import com.workoutmate.app.ui.components.LineIconKind
import com.workoutmate.app.ui.components.PlayfulProgressBar
import com.workoutmate.app.ui.components.RestTimerBar
import com.workoutmate.app.ui.components.fmt
import com.workoutmate.app.ui.components.groupSets
import com.workoutmate.app.ui.components.grouped
import com.workoutmate.app.ui.theme.AppTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 오늘: 위에는 오늘 요약, 아래에는 종목별 세트 묶음.
 * 운동 추가와 세트 체크는 각각 다른 페이지에서 해요.
 */
@Composable
fun TodayScreen(onAdd: () -> Unit, onOpen: (Long) -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val date = today.toString()
    val logs = WorkoutRepository.logsOn(date)
    val todayRoutine = WorkoutRepository.routineFor(today.dayOfWeek.value)

    val totalSets = logs.sumOf { it.sets.size }
    val doneSets = logs.sumOf { it.doneSets }
    val volume = logs.sumOf { it.volume.toDouble() }.toFloat()

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 180.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                item {
                    Text(
                        "운동 목록",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textTertiary,
                        modifier = Modifier.padding(start = 4.dp, top = 10.dp),
                    )
                }
                items(logs, key = { it.id }) { log -> ExerciseSummary(log, onClick = { onOpen(log.id) }) }
            } else {
                item { EmptyToday(todayRoutine?.let { it.name to it.items.joinToString(" · ") { i -> i.name } }) { todayRoutine?.let { WorkoutRepository.startRoutine(it, date) } } }
                val others = WorkoutRepository.routines.filter { it.id != todayRoutine?.id }
                if (others.isNotEmpty()) {
                    item {
                        Text(
                            "다른 루틴으로 시작",
                            style = MaterialTheme.typography.labelMedium,
                            color = c.textTertiary,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp),
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
        }

        // 아래쪽: 운동 추가 버튼 + 휴식 타이머
        Column(
            Modifier.align(Alignment.BottomCenter).padding(16.dp),
            horizontalAlignment = Alignment.End,
        ) {
            FloatingActionButton(
                onClick = onAdd,
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

/** 오늘 기록이 없을 때: 오늘 요일 루틴이 있으면 바로 시작 */
@Composable
private fun EmptyToday(routine: Pair<String, String>?, onStart: () -> Unit) {
    val c = AppTheme.colors
    AppCard {
        if (routine != null) {
            Text("오늘 루틴", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
            Spacer(Modifier.height(4.dp))
            Text(routine.first, style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(routine.second, style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            Spacer(Modifier.height(16.dp))
            AppButton("루틴 시작", onClick = onStart, leading = LineIconKind.Play, modifier = Modifier.fillMaxWidth())
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

/**
 * 종목 한 줄 요약: 이름 + 진행도, 아래에 같은 무게·횟수끼리 묶은 세트와 세트별 완료 점.
 * 누르면 상세 페이지에서 세트를 체크해요.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseSummary(log: WorkoutLog, onClick: () -> Unit) {
    val c = AppTheme.colors
    val badge = AppTheme.badgeFor(log.part.label)
    val allDone = log.sets.isNotEmpty() && log.doneSets == log.sets.size

    AppCard(modifier = Modifier.clickable(onClick = onClick), contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(badge.strong))
            Spacer(Modifier.width(10.dp))
            Text(log.name, style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            Text(
                if (allDone) "완료" else "${log.doneSets}/${log.sets.size}",
                style = MaterialTheme.typography.labelLarge,
                color = if (allDone) c.good else c.textTertiary,
            )
            Spacer(Modifier.width(6.dp))
            Chevron(ChevronDirection.Right, c.textDisabled, Modifier.size(14.dp), stroke = 2.dp)
        }
        Spacer(Modifier.height(10.dp))
        // 세트 묶음: 40kg × 10 · 3세트
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            groupSets(log.sets).forEach { (set, count) ->
                Text(
                    (if (set.weight > 0f) "${set.weight.fmt()}kg × " else "") + "${set.reps}회 · ${count}세트",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(c.fillWeak)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        // 세트별 완료 점
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            log.sets.forEach { s ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (s.done) c.good else c.fill),
                )
            }
        }
    }
}
