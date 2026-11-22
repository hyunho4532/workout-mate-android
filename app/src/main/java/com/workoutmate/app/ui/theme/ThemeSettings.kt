package com.workoutmate.app.ui.theme

import android.content.Context
import com.workoutmate.app.ui.components.LineIconKind
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 화면 모드: 폰 설정 따라가기 / 항상 라이트 / 항상 다크 */
enum class ThemeMode(val label: String, val icon: LineIconKind) {
    System("자동", LineIconKind.Auto),
    Light("라이트", LineIconKind.Sun),
    Dark("다크", LineIconKind.Moon);

    fun next(): ThemeMode = entries[(ordinal + 1) % entries.size]
}

/** 고른 화면 모드를 기억해 두는 곳 (앱을 다시 켜도 유지) */
object ThemeSettings {
    private const val PREFS = "settings"
    private const val KEY = "theme_mode"

    var mode by mutableStateOf(ThemeMode.System)
        private set

    fun load(context: Context) {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        mode = ThemeMode.entries.firstOrNull { it.name == saved } ?: ThemeMode.System
    }

    fun set(context: Context, newMode: ThemeMode) {
        mode = newMode
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, newMode.name).apply()
    }
}
