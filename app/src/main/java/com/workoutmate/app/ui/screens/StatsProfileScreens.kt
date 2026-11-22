package com.workoutmate.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workoutmate.app.BuildConfig
import com.workoutmate.app.data.RestTimer
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.components.AppCard
import com.workoutmate.app.ui.components.Chunky
import com.workoutmate.app.ui.components.LineIcon
import com.workoutmate.app.ui.components.SelectChip
import com.workoutmate.app.ui.components.grouped
import com.workoutmate.app.ui.theme.AppTheme
import com.workoutmate.app.ui.theme.ThemeMode
import com.workoutmate.app.ui.theme.ThemeSettings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// ================= 통계 =================

@Composable
fun StatsScreen(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val today = LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val week = (0..6).map { monday.plusDays(it.toLong()) }
    val volumes = WorkoutRepository.volumeByDate()
    val weekVolumes = week.map { volumes[it.toString()] ?: 0f }
    val workoutDates = WorkoutRepository.workoutDates()
    val weekDays = week.count { it.toString() in workoutDates }
    val allSets = WorkoutRepository.logs.sumOf { it.doneSets }
    val history = WorkoutRepository.logs
        .filter { it.doneSets > 0 }
        .groupBy { it.date }
        .toSortedMap(compareByDescending { it })
        .entries.take(14)

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("통계", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
        }
        item {
            AppCard {
                Text("이번 주 볼륨", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                Spacer(Modifier.height(2.dp))
                Text("${weekVolumes.sum().grouped()}kg", style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(16.dp))
                WeekChart(weekVolumes, todayIndex = today.dayOfWeek.value - 1)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("이번 주 운동", "${weekDays}일", Modifier.weight(1f))
                StatTile("연속 기록", "${WorkoutRepository.streak()}일", Modifier.weight(1f), highlight = true)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("총 운동일", "${workoutDates.size}일", Modifier.weight(1f))
                StatTile("완료한 세트", "$allSets", Modifier.weight(1f))
            }
        }
        if (history.isNotEmpty()) {
            item {
                Text("최근 기록", style = MaterialTheme.typography.titleMedium, color = c.textPrimary, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
            }
            items(history.toList(), key = { it.key }) { (date, logs) ->
                val d = LocalDate.parse(date)
                AppCard(contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${d.monthValue}/${d.dayOfMonth}", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            logs.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        Text("${logs.sumOf { it.volume.toDouble() }.toFloat().grouped()}kg", style = MaterialTheme.typography.labelLarge, color = c.primary)
                    }
                }
            }
        } else {
            item {
                Text(
                    "세트를 완료하면 여기에 기록이 쌓여요",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
            }
        }
    }
}

/** 월~일 막대 그래프 (오늘 막대만 진하게) */
@Composable
private fun WeekChart(values: List<Float>, todayIndex: Int) {
    val c = AppTheme.colors
    val maxV = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val grow by animateFloatAsState(1f, tween(600), label = "grow")
    Column {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            val slot = size.width / values.size
            val barW = slot * 0.46f
            values.forEachIndexed { i, v ->
                val h = if (v <= 0f) 4.dp.toPx() else (size.height * (v / maxV) * grow).coerceAtLeast(6.dp.toPx())
                drawRoundRect(
                    color = if (i == todayIndex) c.primary else if (v > 0f) c.primary.copy(alpha = 0.35f) else c.fill,
                    topLeft = Offset(slot * i + (slot - barW) / 2, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row {
            listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { i, d ->
                Text(
                    d,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == todayIndex) c.primary else c.textTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier, highlight: Boolean = false) {
    val c = AppTheme.colors
    AppCard(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, color = if (highlight) c.primary else c.textPrimary)
    }
}

// ================= 프로필 =================

private val REST_OPTIONS = listOf(60, 90, 120, 180)

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val context = LocalContext.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("프로필", style = MaterialTheme.typography.headlineMedium, color = c.textPrimary, modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))

        AppCard {
            Text("세트 사이 휴식", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text("세트를 체크하면 이 시간만큼 타이머가 돌아요", style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                REST_OPTIONS.forEach { s ->
                    SelectChip(if (s % 60 == 0) "${s / 60}분" else "${s / 60}분 ${s % 60}초", selected = RestTimer.defaultSeconds == s) {
                        RestTimer.setDefault(context, s)
                    }
                }
            }
        }

        AppCard {
            Text("화면 모드", style = MaterialTheme.typography.titleSmall, color = c.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text("자동은 폰 설정(다크/라이트)을 따라가요", style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val selected = ThemeSettings.mode == mode
                    val face by animateColorAsState(if (selected) c.primary else c.surface, label = "face")
                    val fg by animateColorAsState(if (selected) c.onPrimary else c.textSecondary, label = "fg")
                    Chunky(
                        face = face,
                        lip = if (selected) c.primaryDark else c.divider,
                        border = if (selected) null else c.divider,
                        shape = RoundedCornerShape(14.dp),
                        depth = 3.dp,
                        onClick = { ThemeSettings.set(context, mode) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            LineIcon(mode.icon, fg, Modifier.size(22.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(mode.label, style = MaterialTheme.typography.labelLarge, color = fg)
                        }
                    }
                }
            }
        }

        AppCard(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("앱 버전", style = MaterialTheme.typography.titleSmall, color = c.textPrimary, modifier = Modifier.weight(1f))
                Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.labelLarge, color = c.textTertiary)
            }
            if (BuildConfig.LOCAL_MOCK) {
                Spacer(Modifier.height(8.dp))
                Text("테스트 모드 · AI 루틴은 앱 안의 샘플로 만들어져요", style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
