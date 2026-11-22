package com.workoutmate.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.lifecycleScope
import com.workoutmate.app.data.RestTimer
import com.workoutmate.app.data.WorkoutRepository
import com.workoutmate.app.ui.AppRoot
import com.workoutmate.app.ui.theme.WorkoutMateTheme
import com.workoutmate.app.ui.theme.ThemeMode
import com.workoutmate.app.ui.theme.ThemeSettings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeSettings.load(this)
        RestTimer.load(this)
        // 폰 안의 DB에서 운동 기록과 루틴 불러오기
        lifecycleScope.launch { WorkoutRepository.load(applicationContext) }
        enableEdgeToEdge()
        setContent {
            val dark = when (ThemeSettings.mode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            DisposableEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose {}
            }
            WorkoutMateTheme(darkTheme = dark) {
                AppRoot()
            }
        }
    }
}
