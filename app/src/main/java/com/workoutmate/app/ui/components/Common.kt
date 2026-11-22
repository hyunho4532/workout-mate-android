package com.workoutmate.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.workoutmate.app.ui.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** 누르면 통통 튀듯 작아지는 클릭 */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed && enabled) 0.93f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounce",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/**
 * 플레이풀 UI의 핵심: 아래에 진한 '두께(lip)'가 깔린 입체 블록.
 * 누르면 윗면이 두께만큼 내려가서 실제 버튼을 누르는 느낌이 납니다.
 */
@Composable
fun Chunky(
    face: Color,
    lip: Color,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    depth: Dp = 4.dp,
    border: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val push by animateDpAsState(
        if (pressed && enabled && onClick != null) depth else 0.dp,
        spring(dampingRatio = 0.55f, stiffness = 900f),
        label = "push",
    )
    Box(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            } else Modifier,
        ),
        propagateMinConstraints = true,
    ) {
        // 두께
        Box(Modifier.matchParentSize().padding(top = depth).clip(shape).background(lip))
        // 윗면
        Box(
            Modifier
                .padding(bottom = depth)
                .offset { IntOffset(0, push.roundToPx()) }
                .clip(shape)
                .background(face)
                .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier),
            contentAlignment = contentAlignment,
            content = content,
        )
    }
}

/** 테두리와 두께가 있는 둥근 카드 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    face: Color = AppTheme.colors.surface,
    edge: Color = AppTheme.colors.divider,
    content: @Composable ColumnScope.() -> Unit,
) {
    Chunky(
        face = face,
        lip = edge,
        border = edge,
        shape = RoundedCornerShape(20.dp),
        depth = 0.dp,
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart,
    ) {
        Column(Modifier.fillMaxWidth().padding(contentPadding), content = content)
    }
}

enum class ButtonKind { Primary, Accent, Secondary, Weak }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    kind: ButtonKind = ButtonKind.Primary,
    height: Dp = 54.dp,
    leading: LineIconKind? = null,
) {
    val c = AppTheme.colors
    val (face, lip, fg) = when {
        !enabled -> Triple(c.fill, c.divider, c.textDisabled)
        kind == ButtonKind.Primary -> Triple(c.primary, c.primaryDark, c.onPrimary)
        kind == ButtonKind.Accent -> Triple(c.accent, c.accentDark, c.onAccent)
        kind == ButtonKind.Secondary -> Triple(c.primarySoft, c.primarySoft, c.primary)
        else -> Triple(c.surface, c.divider, c.textSecondary)
    }
    Chunky(
        face = face,
        lip = lip,
        border = if (kind == ButtonKind.Weak && enabled) c.divider else null,
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        depth = 3.dp,
        modifier = modifier.height(height + 3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                LineIcon(leading, fg, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                style = if (height >= 52.dp) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                else MaterialTheme.typography.labelLarge,
                color = fg,
            )
        }
    }
}

/** 상단 바: 뒤로가기 화살표 + (선택) 제목 */
@Composable
fun TopBar(onBack: () -> Unit, title: String? = null) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Chunky(
            face = c.surface,
            lip = c.divider,
            border = c.divider,
            shape = CircleShape,
            depth = 0.dp,
            onClick = onBack,
            modifier = Modifier.size(42.dp),
        ) {
            Chevron(ChevronDirection.Left, c.textPrimary, Modifier.size(20.dp), stroke = 2.dp)
        }
        if (title != null) {
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary)
        }
    }
}

enum class ChevronDirection { Left, Right, Down }

/** 아이콘 라이브러리 없이 그리는 꺾쇠 화살표 */
@Composable
fun Chevron(direction: ChevronDirection, color: Color, modifier: Modifier = Modifier.size(20.dp), stroke: Dp = 2.dp) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            when (direction) {
                ChevronDirection.Left -> { moveTo(w * 0.62f, h * 0.2f); lineTo(w * 0.32f, h * 0.5f); lineTo(w * 0.62f, h * 0.8f) }
                ChevronDirection.Right -> { moveTo(w * 0.38f, h * 0.2f); lineTo(w * 0.68f, h * 0.5f); lineTo(w * 0.38f, h * 0.8f) }
                ChevronDirection.Down -> { moveTo(w * 0.2f, h * 0.38f); lineTo(w * 0.5f, h * 0.66f); lineTo(w * 0.8f, h * 0.38f) }
            }
        }
        drawPath(path, color, style = Stroke(stroke.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

enum class NavIconKind { Today, Routine, Stats, Profile }

/** 하단 탭 아이콘 (오늘, 루틴, 통계, 프로필) */
@Composable
fun NavIcon(kind: NavIconKind, color: Color, cutout: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        when (kind) {
            NavIconKind.Today -> {
                // 덤벨: 가운데 손잡이 + 양쪽 원판
                drawLine(color, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.75f, h * 0.5f), w * 0.12f, StrokeCap.Round)
                drawRoundRect(color, Offset(w * 0.12f, h * 0.24f), Size(w * 0.16f, h * 0.52f), CornerRadius(w * 0.05f))
                drawRoundRect(color, Offset(w * 0.72f, h * 0.24f), Size(w * 0.16f, h * 0.52f), CornerRadius(w * 0.05f))
                drawRoundRect(color, Offset(w * 0.02f, h * 0.34f), Size(w * 0.12f, h * 0.32f), CornerRadius(w * 0.04f))
                drawRoundRect(color, Offset(w * 0.86f, h * 0.34f), Size(w * 0.12f, h * 0.32f), CornerRadius(w * 0.04f))
            }
            NavIconKind.Routine -> {
                // 체크리스트
                drawRoundRect(color, Offset(w * 0.14f, h * 0.08f), Size(w * 0.72f, h * 0.84f), CornerRadius(w * 0.16f))
                val sw = w * 0.09f
                drawLine(cutout, Offset(w * 0.3f, h * 0.34f), Offset(w * 0.38f, h * 0.42f), sw, StrokeCap.Round)
                drawLine(cutout, Offset(w * 0.38f, h * 0.42f), Offset(w * 0.5f, h * 0.28f), sw, StrokeCap.Round)
                drawLine(cutout, Offset(w * 0.3f, h * 0.62f), Offset(w * 0.7f, h * 0.62f), sw, StrokeCap.Round)
                drawLine(cutout, Offset(w * 0.3f, h * 0.76f), Offset(w * 0.56f, h * 0.76f), sw, StrokeCap.Round)
            }
            NavIconKind.Stats -> {
                // 막대 그래프
                val bw = w * 0.2f
                drawRoundRect(color, Offset(w * 0.1f, h * 0.5f), Size(bw, h * 0.4f), CornerRadius(w * 0.05f))
                drawRoundRect(color, Offset(w * 0.4f, h * 0.24f), Size(bw, h * 0.66f), CornerRadius(w * 0.05f))
                drawRoundRect(color, Offset(w * 0.7f, h * 0.38f), Size(bw, h * 0.52f), CornerRadius(w * 0.05f))
            }
            NavIconKind.Profile -> {
                drawCircle(color, radius = w * 0.2f, center = Offset(w * 0.5f, h * 0.32f))
                drawRoundRect(
                    color,
                    topLeft = Offset(w * 0.14f, h * 0.58f),
                    size = Size(w * 0.72f, h * 0.34f),
                    cornerRadius = CornerRadius(w * 0.2f),
                )
            }
        }
    }
}

enum class TagTone { Grey, Blue, Yellow }

/** 작은 라벨 (객관식, 보통 등) */
@Composable
fun Tag(text: String, tone: TagTone = TagTone.Grey) {
    val c = AppTheme.colors
    val (bg, fg) = when (tone) {
        TagTone.Grey -> c.fill to c.textSecondary
        TagTone.Blue -> c.primarySoft to c.primary
        TagTone.Yellow -> c.accentSoft to c.accentDark
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** 고르는 칩: 평평한 알약, 고르면 먹색으로 채워집니다 */
@Composable
fun SelectChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    val bg by animateColorAsState(if (selected) c.textPrimary else c.surface, label = "chipBg")
    val fg by animateColorAsState(if (selected) c.surface else c.textSecondary, label = "chipFg")
    val line by animateColorAsState(if (selected) c.textPrimary else c.divider, label = "chipLine")
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = fg,
        modifier = Modifier
            .bounceClick(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, line, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

/** 둥근 입력칸, 누르면 보라 테두리 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val c = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val border by animateColorAsState(if (focused) c.primary else c.divider, label = "fieldBorder")
    val textStyle = MaterialTheme.typography.bodyLarge
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        singleLine = minLines == 1 && keyboardOptions != KeyboardOptions.Default,
        textStyle = textStyle.copy(color = if (enabled) c.textPrimary else c.textSecondary),
        cursorBrush = SolidColor(c.primary),
        interactionSource = interaction,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(c.fillWeak)
                    .border(1.dp, border, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                if (value.isEmpty()) Text(placeholder, style = textStyle, color = c.textDisabled)
                inner()
            }
        },
    )
}

/** 카드 안 목록 한 줄: 과목 색 배지 + 제목/설명 + 화살표 */
@Composable
fun ListRow(
    title: String,
    subtitle: String,
    badge: String,
    onClick: () -> Unit,
) {
    val c = AppTheme.colors
    val color = AppTheme.badgeFor(badge)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(color.soft),
            contentAlignment = Alignment.Center,
        ) {
            Text(badge, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color.strong)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.textPrimary, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textTertiary)
        }
        Chevron(ChevronDirection.Right, c.textDisabled, Modifier.size(16.dp), stroke = 2.dp)
    }
}

private class Piece(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val size: Float,
    val spin: Float,
    val round: Boolean,
)

/** 정답일 때 터지는 색종이. key가 바뀔 때마다 한 번 터집니다. */
@Composable
fun ConfettiBurst(key: Any, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val palette = listOf(c.primary, c.accent, c.good, c.bad, Color(0xFF4FB3FF), Color(0xFFFF8CC0))
    val pieces = remember(key) {
        List(48) {
            Piece(
                angle = (PI + Random.nextFloat() * PI).toFloat(), // 위쪽 반원으로 퍼짐
                speed = 0.45f + Random.nextFloat() * 0.75f,
                color = palette[it % palette.size],
                size = 7f + Random.nextFloat() * 7f,
                spin = Random.nextFloat() * 4f - 2f,
                round = Random.nextBoolean(),
            )
        }
    }
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { progress.animateTo(1f, tween(1500, easing = LinearEasing)) }
    if (progress.value >= 1f) return

    Canvas(modifier) {
        val t = progress.value
        val origin = Offset(size.width / 2f, size.height * 0.45f)
        val reach = size.width * 0.75f
        val alpha = (1f - t).coerceIn(0f, 1f)
        pieces.forEach { p ->
            val x = origin.x + cos(p.angle) * reach * p.speed * t
            val y = origin.y + sin(p.angle) * reach * p.speed * t + size.height * 1.1f * t * t
            val s = p.size * density / 2f
            if (p.round) {
                drawCircle(p.color.copy(alpha = alpha), radius = s * 0.6f, center = Offset(x, y))
            } else {
                rotate(p.spin * 360f * t, Offset(x, y)) {
                    drawRect(p.color.copy(alpha = alpha), topLeft = Offset(x - s, y - s / 2f), size = Size(s * 2f, s))
                }
            }
        }
    }
}

/**
 * 통통한 진행 바: 둥근 홈 안에 보라 막대가 스프링처럼 늘어나고,
 * 위쪽에 반짝이는 줄이 있어 젤리처럼 보입니다.
 */
@Composable
fun PlayfulProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
) {
    val c = AppTheme.colors
    val animated by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "progress",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(c.fill),
    ) {
        if (animated > 0.001f) {
            Box(
                Modifier
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
    }
}

/**
 * 캐릭터를 감싸는 통통한 원형 진행 링.
 * 굵은 둥근 선이 12시 방향부터 시계 방향으로 스프링처럼 차오르고, 끝에 반짝이는 점이 따라다닙니다.
 */
@Composable
fun PlayfulProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    thickness: Dp = 12.dp,
) {
    val c = AppTheme.colors
    val animated by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ring",
    )
    Canvas(modifier) {
        val stroke = thickness.toPx()
        val inset = stroke / 2f + 2.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val topLeft = Offset(inset, inset)
        // 바탕 링
        drawArc(c.fill, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        if (animated > 0.001f) {
            val sweep = 360f * animated
            drawArc(color, -90f, sweep, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            // 끝에 따라다니는 반짝 점
            val angle = Math.toRadians((-90f + sweep).toDouble())
            val r = arcSize.width / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val head = Offset(center.x + r * cos(angle).toFloat(), center.y + r * sin(angle).toFloat())
            drawCircle(Color.White, radius = stroke * 0.32f, center = head)
        }
    }
}

enum class LineIconKind { Camera, Image, Document, Sun, Moon, Auto, Retry, Plus, Edit, Trash, Minus, Check, Timer, Spark, Play }

/** 이모지 대신 쓰는 얇은 선 아이콘 */
@Composable
fun LineIcon(kind: LineIconKind, color: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = w * 0.085f
        val line = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (kind) {
            LineIconKind.Camera -> {
                drawRoundRect(color, Offset(w * 0.08f, h * 0.28f), Size(w * 0.84f, h * 0.58f), CornerRadius(w * 0.12f), style = line)
                drawLine(color, Offset(w * 0.36f, h * 0.18f), Offset(w * 0.64f, h * 0.18f), sw, StrokeCap.Round)
                drawCircle(color, w * 0.15f, Offset(w * 0.5f, h * 0.57f), style = line)
            }
            LineIconKind.Image -> {
                drawRoundRect(color, Offset(w * 0.08f, h * 0.14f), Size(w * 0.84f, h * 0.72f), CornerRadius(w * 0.12f), style = line)
                drawCircle(color, w * 0.07f, Offset(w * 0.34f, h * 0.38f))
                drawPath(Path().apply {
                    moveTo(w * 0.14f, h * 0.78f); lineTo(w * 0.42f, h * 0.52f); lineTo(w * 0.58f, h * 0.66f)
                    lineTo(w * 0.7f, h * 0.56f); lineTo(w * 0.88f, h * 0.74f)
                }, color, style = line)
            }
            LineIconKind.Document -> {
                drawPath(Path().apply {
                    moveTo(w * 0.22f, h * 0.08f); lineTo(w * 0.6f, h * 0.08f); lineTo(w * 0.8f, h * 0.28f)
                    lineTo(w * 0.8f, h * 0.92f); lineTo(w * 0.22f, h * 0.92f); close()
                }, color, style = line)
                drawLine(color, Offset(w * 0.36f, h * 0.52f), Offset(w * 0.66f, h * 0.52f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.36f, h * 0.7f), Offset(w * 0.58f, h * 0.7f), sw, StrokeCap.Round)
            }
            LineIconKind.Sun -> {
                drawCircle(color, w * 0.18f, Offset(w / 2, h / 2), style = line)
                for (k in 0 until 8) {
                    val a = Math.toRadians(k * 45.0)
                    val c1 = Offset(w / 2 + cos(a).toFloat() * w * 0.32f, h / 2 + sin(a).toFloat() * h * 0.32f)
                    val c2 = Offset(w / 2 + cos(a).toFloat() * w * 0.42f, h / 2 + sin(a).toFloat() * h * 0.42f)
                    drawLine(color, c1, c2, sw, StrokeCap.Round)
                }
            }
            LineIconKind.Moon -> {
                drawPath(Path().apply {
                    moveTo(w * 0.62f, h * 0.1f)
                    cubicTo(w * 0.2f, h * 0.12f, w * 0.12f, h * 0.7f, w * 0.48f, h * 0.88f)
                    cubicTo(w * 0.7f, h * 0.98f, w * 0.9f, h * 0.82f, w * 0.92f, h * 0.62f)
                    cubicTo(w * 0.6f, h * 0.72f, w * 0.38f, h * 0.4f, w * 0.62f, h * 0.1f)
                    close()
                }, color, style = line)
            }
            LineIconKind.Auto -> {
                drawCircle(color, w * 0.38f, Offset(w / 2, h / 2), style = line)
                drawArc(color, 90f, 180f, true, Offset(w * 0.12f, h * 0.12f), Size(w * 0.76f, h * 0.76f))
            }
            LineIconKind.Retry -> {
                drawArc(color, -60f, 290f, false, Offset(w * 0.16f, h * 0.16f), Size(w * 0.68f, h * 0.68f), style = line)
                drawPath(Path().apply {
                    moveTo(w * 0.62f, h * 0.08f); lineTo(w * 0.8f, h * 0.24f); lineTo(w * 0.58f, h * 0.32f)
                }, color, style = line)
            }
            LineIconKind.Edit -> {
                drawPath(Path().apply {
                    moveTo(w * 0.16f, h * 0.84f); lineTo(w * 0.2f, h * 0.64f); lineTo(w * 0.64f, h * 0.2f)
                    lineTo(w * 0.8f, h * 0.36f); lineTo(w * 0.36f, h * 0.8f); close()
                }, color, style = line)
                drawLine(color, Offset(w * 0.56f, h * 0.28f), Offset(w * 0.72f, h * 0.44f), sw, StrokeCap.Round)
            }
            LineIconKind.Trash -> {
                drawLine(color, Offset(w * 0.14f, h * 0.24f), Offset(w * 0.86f, h * 0.24f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.38f, h * 0.1f), Offset(w * 0.62f, h * 0.1f), sw, StrokeCap.Round)
                drawPath(Path().apply {
                    moveTo(w * 0.24f, h * 0.24f); lineTo(w * 0.3f, h * 0.9f); lineTo(w * 0.7f, h * 0.9f); lineTo(w * 0.76f, h * 0.24f)
                }, color, style = line)
                drawLine(color, Offset(w * 0.43f, h * 0.42f), Offset(w * 0.43f, h * 0.72f), sw, StrokeCap.Round)
                drawLine(color, Offset(w * 0.57f, h * 0.42f), Offset(w * 0.57f, h * 0.72f), sw, StrokeCap.Round)
            }
            LineIconKind.Minus -> {
                drawLine(color, Offset(w * 0.18f, h / 2), Offset(w * 0.82f, h / 2), sw * 1.2f, StrokeCap.Round)
            }
            LineIconKind.Check -> {
                drawPath(Path().apply {
                    moveTo(w * 0.18f, h * 0.52f); lineTo(w * 0.42f, h * 0.74f); lineTo(w * 0.84f, h * 0.28f)
                }, color, style = Stroke(sw * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            LineIconKind.Timer -> {
                drawCircle(color, w * 0.36f, Offset(w / 2, h * 0.56f), style = line)
                drawLine(color, Offset(w * 0.4f, h * 0.08f), Offset(w * 0.6f, h * 0.08f), sw, StrokeCap.Round)
                drawLine(color, Offset(w / 2, h * 0.56f), Offset(w / 2, h * 0.36f), sw, StrokeCap.Round)
            }
            LineIconKind.Spark -> {
                drawPath(Path().apply {
                    moveTo(w * 0.5f, h * 0.08f); quadraticTo(w * 0.56f, h * 0.44f, w * 0.92f, h * 0.5f)
                    quadraticTo(w * 0.56f, h * 0.56f, w * 0.5f, h * 0.92f)
                    quadraticTo(w * 0.44f, h * 0.56f, w * 0.08f, h * 0.5f)
                    quadraticTo(w * 0.44f, h * 0.44f, w * 0.5f, h * 0.08f); close()
                }, color)
            }
            LineIconKind.Play -> {
                drawPath(Path().apply {
                    moveTo(w * 0.3f, h * 0.18f); lineTo(w * 0.82f, h * 0.5f); lineTo(w * 0.3f, h * 0.82f); close()
                }, color)
            }
            LineIconKind.Plus -> {
                drawLine(color, Offset(w / 2, h * 0.18f), Offset(w / 2, h * 0.82f), sw * 1.2f, StrokeCap.Round)
                drawLine(color, Offset(w * 0.18f, h / 2), Offset(w * 0.82f, h / 2), sw * 1.2f, StrokeCap.Round)
            }
        }
    }
}
