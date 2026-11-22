package com.workoutmate.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.workoutmate.app.ui.components.NavIcon
import com.workoutmate.app.ui.components.NavIconKind
import com.workoutmate.app.ui.screens.AiRoutineScreen
import com.workoutmate.app.ui.screens.ProfileScreen
import com.workoutmate.app.ui.screens.RoutineEditScreen
import com.workoutmate.app.ui.screens.RoutinesScreen
import com.workoutmate.app.ui.screens.StatsScreen
import com.workoutmate.app.ui.screens.TodayScreen
import com.workoutmate.app.ui.theme.AppTheme

/** 화면 목록. 라이브러리 없이 간단한 상태로 전환합니다. */
sealed interface Screen {
    data object Today : Screen
    data object Routines : Screen
    data object Stats : Screen
    data object Profile : Screen
    data class RoutineEdit(val id: Long?) : Screen
    data object AiRoutine : Screen
}

private val TABS = listOf(Screen.Today, Screen.Routines, Screen.Stats, Screen.Profile)

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf<Screen>(Screen.Today) }

    fun goBack() {
        screen = when (screen) {
            is Screen.RoutineEdit, Screen.AiRoutine -> Screen.Routines
            else -> Screen.Today
        }
    }
    BackHandler(enabled = screen != Screen.Today) { goBack() }

    Scaffold(
        containerColor = AppTheme.colors.background,
        bottomBar = { if (screen in TABS) BottomBar(screen) { screen = it } },
    ) { inner ->
        val modifier = Modifier.padding(inner)
        when (val s = screen) {
            Screen.Today -> TodayScreen(modifier)
            Screen.Routines -> RoutinesScreen(
                onEdit = { screen = Screen.RoutineEdit(it) },
                onAi = { screen = Screen.AiRoutine },
                onStarted = { screen = Screen.Today },
                modifier = modifier,
            )
            Screen.Stats -> StatsScreen(modifier)
            Screen.Profile -> ProfileScreen(modifier)
            is Screen.RoutineEdit -> RoutineEditScreen(s.id, onBack = { goBack() }, modifier = modifier)
            Screen.AiRoutine -> AiRoutineScreen(onBack = { goBack() }, onSaved = { screen = Screen.Routines }, modifier = modifier)
        }
    }
}

@Composable
private fun BottomBar(current: Screen, onSelect: (Screen) -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Row(Modifier.fillMaxWidth().height(64.dp)) {
            BottomItem("오늘", NavIconKind.Today, current == Screen.Today, Modifier.weight(1f)) { onSelect(Screen.Today) }
            BottomItem("루틴", NavIconKind.Routine, current == Screen.Routines, Modifier.weight(1f)) { onSelect(Screen.Routines) }
            BottomItem("통계", NavIconKind.Stats, current == Screen.Stats, Modifier.weight(1f)) { onSelect(Screen.Stats) }
            BottomItem("프로필", NavIconKind.Profile, current == Screen.Profile, Modifier.weight(1f)) { onSelect(Screen.Profile) }
        }
    }
}

@Composable
private fun BottomItem(label: String, icon: NavIconKind, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    val color by animateColorAsState(if (selected) c.textPrimary else c.textDisabled, label = "tab")
    Column(
        modifier.fillMaxHeight().clickable(interactionSource = null, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(if (selected) c.fill else Color.Transparent)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            NavIcon(icon, color = color, cutout = if (selected) c.fill else c.surface)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
