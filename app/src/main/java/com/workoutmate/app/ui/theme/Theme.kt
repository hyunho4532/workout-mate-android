package com.workoutmate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * 색상: 따뜻한 바탕 + 먹색 글자, 주요 동작은 에너지 오렌지, 포인트는 노랑.
 * 버튼 아래 '두께'(lip)는 얇게만 써서 누르는 느낌만 남깁니다.
 */
@Immutable
data class AppColors(
    val background: Color,
    val surface: Color,
    val fill: Color,
    val fillWeak: Color,
    val divider: Color,       // 카드 테두리
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val primary: Color,
    val primaryDark: Color,   // 주요 버튼의 두께
    val primarySoft: Color,
    val onPrimary: Color,
    val accent: Color,
    val accentDark: Color,
    val accentSoft: Color,
    val onAccent: Color,
    val good: Color,
    val goodDark: Color,
    val goodSoft: Color,
    val bad: Color,
    val badDark: Color,
    val badSoft: Color,
    val isDark: Boolean,
)

private val LightAppColors = AppColors(
    background = Color(0xFFF6F4EF),   // 종이색
    surface = Color(0xFFFFFFFF),
    fill = Color(0xFFEFECE6),
    fillWeak = Color(0xFFF9F7F3),
    divider = Color(0xFFE5E1D9),
    textPrimary = Color(0xFF17181C),  // 먹색
    textSecondary = Color(0xFF4A4D55),
    textTertiary = Color(0xFF8B8E95),
    textDisabled = Color(0xFFC0C2C7),
    primary = Color(0xFFF2551D),      // 에너지 오렌지
    primaryDark = Color(0xFFC2410C),
    primarySoft = Color(0xFFFDEDE5),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFFF2B705),       // 연필 노랑
    accentDark = Color(0xFFB98A00),
    accentSoft = Color(0xFFFCF3D6),
    onAccent = Color(0xFF241C00),
    good = Color(0xFF1E9E5A),
    goodDark = Color(0xFF15773F),
    goodSoft = Color(0xFFE5F4EB),
    bad = Color(0xFFE5484D),
    badDark = Color(0xFFB4343A),
    badSoft = Color(0xFFFCEAEA),
    isDark = false,
)

private val DarkAppColors = AppColors(
    background = Color(0xFF121316),
    surface = Color(0xFF1B1C20),
    fill = Color(0xFF26282D),
    fillWeak = Color(0xFF202226),
    divider = Color(0xFF2F3137),
    textPrimary = Color(0xFFECEDEF),
    textSecondary = Color(0xFFB9BCC3),
    textTertiary = Color(0xFF82858D),
    textDisabled = Color(0xFF55585F),
    primary = Color(0xFFFF7A45),
    primaryDark = Color(0xFFD9541F),
    primarySoft = Color(0xFF3A2219),
    onPrimary = Color(0xFFFFFFFF),
    accent = Color(0xFFF5C33B),
    accentDark = Color(0xFFC79A1C),
    accentSoft = Color(0xFF39311A),
    onAccent = Color(0xFF241C00),
    good = Color(0xFF3CC27A),
    goodDark = Color(0xFF5ED494),
    goodSoft = Color(0xFF16301F),
    bad = Color(0xFFF06A6E),
    badDark = Color(0xFFF58A8D),
    badSoft = Color(0xFF3A1E21),
    isDark = true,
)

/** 과목마다 다른 색 배지 (연한 바탕, 진한 글자) — 채도를 낮춘 차분한 색 */
@Immutable
data class Badge(val soft: Color, val strong: Color)

private val LightBadges = listOf(
    Badge(Color(0xFFE9EDFB), Color(0xFF2B4ACB)), // 잉크
    Badge(Color(0xFFF8E9E3), Color(0xFFB4532F)), // 테라코타
    Badge(Color(0xFFE3F1EA), Color(0xFF247A55)), // 세이지
    Badge(Color(0xFFE6EFF5), Color(0xFF2F6A8F)), // 슬레이트 블루
    Badge(Color(0xFFF7F0DA), Color(0xFF8C6A00)), // 머스터드
    Badge(Color(0xFFF1E8F2), Color(0xFF7E4A87)), // 플럼
)

private val DarkBadges = listOf(
    Badge(Color(0xFF1F2748), Color(0xFF9DB0FF)),
    Badge(Color(0xFF3A2620), Color(0xFFF0A182)),
    Badge(Color(0xFF173028), Color(0xFF7FD1A8)),
    Badge(Color(0xFF1B2A35), Color(0xFF8DC1E3)),
    Badge(Color(0xFF332B16), Color(0xFFE8C766)),
    Badge(Color(0xFF2E2131), Color(0xFFD3A3DB)),
)

private val LocalAppColors = staticCompositionLocalOf { LightAppColors }
private val LocalBadges = staticCompositionLocalOf { LightBadges }

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    /** 같은 글자(과목명 등)는 항상 같은 색 */
    @Composable @ReadOnlyComposable
    fun badgeFor(key: String): Badge {
        val list = LocalBadges.current
        return list[abs(key.hashCode()) % list.size]
    }
}

private fun style(size: Int, line: Int, weight: FontWeight) = TextStyle(
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = (-0.2).sp,
)

// 제목은 굵게, 본문은 담백하게. 자간을 살짝 좁혀 단단한 인상
private val AppTypography = Typography(
    headlineMedium = style(26, 34, FontWeight.Bold).copy(letterSpacing = (-0.5).sp),
    headlineSmall = style(22, 30, FontWeight.Bold).copy(letterSpacing = (-0.4).sp),
    titleLarge = style(19, 27, FontWeight.Bold),
    titleMedium = style(16, 24, FontWeight.SemiBold),
    titleSmall = style(15, 22, FontWeight.SemiBold),
    bodyLarge = style(16, 25, FontWeight.Normal),
    bodyMedium = style(14, 22, FontWeight.Normal),
    bodySmall = style(13, 19, FontWeight.Normal),
    labelLarge = style(15, 20, FontWeight.SemiBold),
    labelMedium = style(13, 18, FontWeight.Medium),
    labelSmall = style(12, 16, FontWeight.SemiBold),
)

@Composable
fun WorkoutMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val c = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primarySoft,
        onPrimaryContainer = c.primary,
        background = c.background,
        onBackground = c.textPrimary,
        surface = c.surface,
        onSurface = c.textPrimary,
        surfaceVariant = c.fill,
        onSurfaceVariant = c.textTertiary,
        outline = c.divider,
        outlineVariant = c.divider,
        error = c.bad,
    )
    CompositionLocalProvider(
        LocalAppColors provides c,
        LocalBadges provides if (darkTheme) DarkBadges else LightBadges,
    ) {
        MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
    }
}
