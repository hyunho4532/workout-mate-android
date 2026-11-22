package com.workoutmate.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.BodyPart
import com.workoutmate.app.data.RestTimer
import com.workoutmate.app.data.SetLog
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.components.AppButton
import com.workoutmate.app.ui.components.AppCard
import com.workoutmate.app.ui.components.ExerciseSearch
import com.workoutmate.app.ui.components.RestTimerBar
import com.workoutmate.app.ui.components.SetEditor
import com.workoutmate.app.ui.components.TopBar
import com.workoutmate.app.ui.components.fmt
import com.workoutmate.app.ui.components.groupSets
import com.workoutmate.app.ui.theme.AppTheme
import java.time.LocalDate

/**
 * 운동 추가 페이지.
 * 1) 검색해서 종목 고르기 → 2) 세트 계획(무게·횟수·세트 수) 잡고 추가
 */
@Composable
fun AddExerciseScreen(onBack: () -> Unit, onAdded: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val date = LocalDate.now().toString()
    var picked by remember { mutableStateOf<Pair<String, BodyPart>?>(null) }
    var sets by remember { mutableStateOf<List<SetLog>>(emptyList()) }

    val choice = picked
    if (choice == null) {
        Column(modifier.fillMaxSize()) {
            TopBar(onBack, title = "운동 추가")
            ExerciseSearch(
                onPick = { name, part ->
                    picked = name to part
                    sets = WorkoutRepository.suggestedSets(name, date)
                },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            )
        }
        return
    }

    val (name, part) = choice
    val last = WorkoutRepository.lastTime(name, date)
    Column(modifier.fillMaxSize()) {
        TopBar({ picked = null }, title = "세트 설정")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.padding(horizontal = 4.dp)) {
                PartTag(part)
                Spacer(Modifier.height(6.dp))
                Text(name, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
                if (last != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "지난번 · " + groupSets(last.sets).joinToString(", ") { (s, n) -> "${s.weight.fmt()}kg × ${s.reps} · ${n}세트" },
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textTertiary,
                    )
                }
            }
            AppCard(contentPadding = PaddingValues(16.dp)) {
                SetEditor(sets = sets, onChange = { sets = it }, showCheck = false)
            }
        }
        Box(Modifier.fillMaxWidth().background(c.background).padding(16.dp)) {
            AppButton(
                "${sets.size}세트 추가하기",
                onClick = {
                    WorkoutRepository.addExercise(date, name, part, sets)
                    onAdded()
                },
                enabled = sets.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** 종목 상세: 세트를 하나씩 체크하며 운동하고, 체크하면 휴식 타이머가 돌아요 */
@Composable
fun ExerciseDetailScreen(logId: Long, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val log = WorkoutRepository.findLog(logId)
    if (log == null) {
        LaunchedEffect(logId) { onBack() }
        return
    }

    Column(modifier.fillMaxSize()) {
        TopBar(onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    PartTag(log.part)
                    Spacer(Modifier.height(6.dp))
                    Text(log.name, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
                }
                Text(
                    "${log.doneSets} / ${log.sets.size} 세트",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (log.doneSets == log.sets.size) c.good else c.textSecondary,
                )
            }
            AppCard(contentPadding = PaddingValues(16.dp)) {
                SetEditor(
                    sets = log.sets,
                    onChange = { WorkoutRepository.updateSets(log.id, it) },
                    showCheck = true,
                    onSetDone = { RestTimer.start() },
                )
            }
            Text(
                "이 운동 삭제",
                style = MaterialTheme.typography.labelLarge,
                color = c.bad,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        WorkoutRepository.removeLog(log.id)
                        onBack()
                    }
                    .padding(12.dp),
            )
        }
        Column(Modifier.background(c.background).padding(16.dp)) {
            RestTimerBar()
            Spacer(Modifier.height(10.dp))
            AppButton("완료", onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PartTag(part: BodyPart) {
    val badge = AppTheme.badgeFor(part.label)
    Text(
        part.label,
        style = MaterialTheme.typography.labelSmall,
        color = badge.strong,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badge.soft)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
