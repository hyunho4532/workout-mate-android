package com.workoutmate.app.ui.components

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.Clock
import com.workoutmate.app.data.RestTimer
import com.workoutmate.app.data.SetLog
import com.workoutmate.app.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * 세트 편집표: 세트마다 무게·횟수 조절, (선택) 완료 체크, 세트 추가/빼기.
 * showCheck=false면 운동 추가 페이지처럼 계획만 잡을 때 씁니다.
 */
@Composable
fun SetEditor(
    sets: List<SetLog>,
    onChange: (List<SetLog>) -> Unit,
    showCheck: Boolean,
    onSetDone: () -> Unit = {},
) {
    val c = AppTheme.colors
    var editing by remember { mutableStateOf<Pair<Int, Boolean>?>(null) } // (세트, 무게인지)
    fun update(i: Int, set: SetLog) = onChange(sets.toMutableList().also { it[i] = set })

    Column {
        Row(Modifier.padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("세트", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.width(36.dp))
            Text("kg", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.weight(1f).padding(start = 40.dp))
            Text("회", style = MaterialTheme.typography.labelSmall, color = c.textTertiary, modifier = Modifier.weight(1f).padding(start = 40.dp))
            if (showCheck) Spacer(Modifier.width(48.dp))
        }
        sets.forEachIndexed { i, set ->
            Spacer(Modifier.height(8.dp))
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
                if (showCheck) {
                    Spacer(Modifier.width(8.dp))
                    CheckButton(done = set.done) {
                        val nowDone = !set.done
                        update(i, set.copy(done = nowDone))
                        if (nowDone) onSetDone()
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppButton(
                "세트 추가",
                onClick = { onChange(sets + (sets.lastOrNull() ?: SetLog(0f, 10)).copy(done = false)) },
                kind = ButtonKind.Weak,
                height = 40.dp,
                leading = LineIconKind.Plus,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                "세트 빼기",
                onClick = { onChange(sets.dropLast(1)) },
                enabled = sets.size > 1,
                kind = ButtonKind.Weak,
                height = 40.dp,
                leading = LineIconKind.Minus,
                modifier = Modifier.weight(1f),
            )
        }
    }

    editing?.let { (i, isWeight) ->
        val set = sets.getOrNull(i) ?: return@let
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
        LineIcon(LineIconKind.Check, if (done) Color.White else c.textDisabled, Modifier.size(18.dp))
    }
}

/**
 * 세트 묶음 요약: 같은 무게·횟수끼리 묶어서 "40kg × 10 · 3세트"처럼.
 * 완료한 세트는 초록, 아직인 세트는 회색으로 칩마다 표시합니다.
 */
fun groupSets(sets: List<SetLog>): List<Pair<SetLog, Int>> {
    val groups = mutableListOf<Pair<SetLog, Int>>()
    sets.forEach { s ->
        val key = s.copy(done = false)
        val last = groups.lastOrNull()
        if (last != null && last.first == key) groups[groups.lastIndex] = key to last.second + 1
        else groups.add(key to 1)
    }
    return groups
}

/** 세트를 체크하면 나타나는 휴식 타이머 막대 */
@Composable
fun RestTimerBar() {
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
