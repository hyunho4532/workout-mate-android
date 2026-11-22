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
import com.workoutmate.app.ui.screens.AddExerciseScreen
import com.workoutmate.app.ui.screens.AiRoutineScreen
import com.workoutmate.app.ui.screens.ExerciseDetailScreen
import com.workoutmate.app.ui.screens.ProfileScreen
import com.workoutmate.app.ui.screens.RoutineEditScreen
import com.workoutmate.app.ui.screens.RoutinesScreen
import com.workoutmate.app.ui.screens.StatsScreen
import com.workoutmate.app.ui.screens.TodayScreen
import com.workoutmate.app.ui.theme.AppTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.input.pointer.pointerInput

/** 아래 탭 화면 */
enum class Tab { Today, Routines, Stats, Profile }

/** 탭 위에 덮어 씌우는 페이지. 뒤로가기를 누르면 하나씩 닫혀요. */
sealed interface Page {
    data class RoutineEdit(val id: Long?) : Page
    data object AiRoutine : Page
    data object AddExercise : Page
    data class ExerciseDetail(val logId: Long) : Page
}

/** 페이지 스택의 한 칸. depth로 열리는지(깊어짐) 닫히는지(얕아짐) 구분해 애니메이션 방향을 정해요. */
private data class Entry(val page: Page?, val depth: Int)

@Composable
fun AppRoot() {
    val c = AppTheme.colors
    var tab by remember { mutableStateOf(Tab.Today) }
    val pages = remember { mutableStateListOf<Page>() }

    fun open(page: Page) { pages.add(page) }
    fun close() { if (pages.isNotEmpty()) pages.removeAt(pages.lastIndex) }

    // 뒤로가기: 열린 페이지가 있으면 닫고, 다른 탭이면 '오늘'로, 오늘 탭이면 앱 종료
    BackHandler(enabled = pages.isNotEmpty() || tab != Tab.Today) {
        if (pages.isNotEmpty()) close() else tab = Tab.Today
    }

    Box(Modifier.fillMaxSize()) {
        // 1) 바닥: 탭 화면 + 하단 탭
        Scaffold(
            containerColor = c.background,
            bottomBar = { BottomBar(tab) { tab = it } },
        ) { inner ->
            val modifier = Modifier.padding(inner)
            when (tab) {
                Tab.Today -> TodayScreen(
                    onAdd = { open(Page.AddExercise) },
                    onOpen = { open(Page.ExerciseDetail(it)) },
                    modifier = modifier,
                )
                Tab.Routines -> RoutinesScreen(
                    onEdit = { open(Page.RoutineEdit(it)) },
                    onAi = { open(Page.AiRoutine) },
                    onStarted = { tab = Tab.Today },
                    modifier = modifier,
                )
                Tab.Stats -> StatsScreen(modifier)
                Tab.Profile -> ProfileScreen(modifier)
            }
        }

        // 2) 위: 열린 페이지가 오른쪽에서 밀려 들어와 탭 화면을 덮어요
        AnimatedContent(
            targetState = Entry(pages.lastOrNull(), pages.size),
            transitionSpec = {
                if (targetState.depth > initialState.depth) {
                    slideInHorizontally(tween(280)) { it } togetherWith
                        slideOutHorizontally(tween(280)) { -it / 4 } + fadeOut(tween(280))
                } else {
                    slideInHorizontally(tween(280)) { -it / 4 } togetherWith
                        slideOutHorizontally(tween(280)) { it }
                }.using(SizeTransform(clip = false))
            },
            contentKey = { it.page to it.depth },
            label = "pages",
        ) { entry ->
            val page = entry.page ?: return@AnimatedContent
            Box(
                Modifier
                    .fillMaxSize()
                    .background(c.background)
                    // 페이지 바깥 터치가 아래 탭 화면으로 새지 않게
                    .pointerInput(Unit) { detectTapGestures { } }
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                when (page) {
                    is Page.RoutineEdit -> RoutineEditScreen(page.id, onBack = { close() })
                    Page.AiRoutine -> AiRoutineScreen(onBack = { close() }, onSaved = { close() })
                    Page.AddExercise -> AddExerciseScreen(onBack = { close() }, onAdded = { close() })
                    is Page.ExerciseDetail -> ExerciseDetailScreen(page.logId, onBack = { close() })
                }
            }
        }
    }
}

@Composable
private fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().background(c.surface).navigationBarsPadding()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))
        Row(Modifier.fillMaxWidth().height(64.dp)) {
            BottomItem("오늘", NavIconKind.Today, current == Tab.Today, Modifier.weight(1f)) { onSelect(Tab.Today) }
            BottomItem("루틴", NavIconKind.Routine, current == Tab.Routines, Modifier.weight(1f)) { onSelect(Tab.Routines) }
            BottomItem("통계", NavIconKind.Stats, current == Tab.Stats, Modifier.weight(1f)) { onSelect(Tab.Stats) }
            BottomItem("프로필", NavIconKind.Profile, current == Tab.Profile, Modifier.weight(1f)) { onSelect(Tab.Profile) }
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
