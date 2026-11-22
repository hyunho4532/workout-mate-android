package com.workoutmate.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 세트 사이 휴식 타이머.
 * 세트를 완료로 체크하면 시작돼요. 화면 아래 막대가 남은 시간을 보여 줍니다.
 */
object RestTimer {
    /** 끝나는 시각 (System.currentTimeMillis 기준). null이면 쉬는 중 아님 */
    var endAt by mutableStateOf<Long?>(null)
        private set
    var totalSeconds by mutableIntStateOf(90)
        private set

    /** 프로필에서 고르는 기본 휴식 시간 */
    var defaultSeconds by mutableIntStateOf(90)
        private set

    fun start(seconds: Int = defaultSeconds) {
        totalSeconds = seconds
        endAt = System.currentTimeMillis() + seconds * 1000L
    }

    fun addSeconds(seconds: Int) {
        val end = endAt ?: return
        totalSeconds += seconds
        endAt = end + seconds * 1000L
    }

    fun stop() {
        endAt = null
    }

    fun load(context: Context) {
        defaultSeconds = context.getSharedPreferences("settings", Context.MODE_PRIVATE).getInt("rest_seconds", 90)
    }

    fun setDefault(context: Context, seconds: Int) {
        defaultSeconds = seconds
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putInt("rest_seconds", seconds).apply()
    }
}

/** 화면이 1초마다 다시 그려지도록 쓰는 현재 시각 */
object Clock {
    var now by mutableLongStateOf(System.currentTimeMillis())
}
