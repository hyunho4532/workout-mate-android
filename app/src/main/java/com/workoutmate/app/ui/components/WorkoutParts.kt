package com.workoutmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workoutmate.app.data.BodyPart
import com.workoutmate.app.data.Exercises
import com.workoutmate.app.ui.theme.AppTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor

/** 40.0 → "40", 42.5 → "42.5" */
fun Float.fmt(): String = if (this % 1f == 0f) toInt().toString() else "%.1f".format(this)

/** 1,240 처럼 천 단위 쉼표 */
fun Float.grouped(): String = "%,d".format(toInt())

/** − 값 + 로 숫자를 바꾸는 작은 조절기. 가운데 숫자를 누르면 직접 입력 */
@Composable
fun Stepper(
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onTapValue: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val c = AppTheme.colors
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(c.fillWeak),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(32.dp).clickable(enabled = enabled, onClick = onMinus),
            contentAlignment = Alignment.Center,
        ) { LineIcon(LineIconKind.Minus, c.textTertiary, Modifier.size(14.dp)) }
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) c.textPrimary else c.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled, onClick = onTapValue)
                .padding(vertical = 6.dp),
        )
        Box(
            Modifier.size(32.dp).clickable(enabled = enabled, onClick = onPlus),
            contentAlignment = Alignment.Center,
        ) { LineIcon(LineIconKind.Plus, c.textTertiary, Modifier.size(14.dp)) }
    }
}

/** 숫자 직접 입력 창 */
@Composable
fun NumberDialog(
    title: String,
    initial: String,
    decimal: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    val c = AppTheme.colors
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = c.textPrimary) },
        text = {
            AppTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() || (decimal && it == '.') } },
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                text.toFloatOrNull()?.let(onConfirm)
                onDismiss()
            }) { Text("확인", color = c.primary, style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소", color = c.textSecondary, style = MaterialTheme.typography.labelLarge) }
        },
    )
}

/**
 * 운동 검색: 맨 위 검색바 + 아래 목록 하나.
 * 부위 이름(예: "가슴")으로 검색해도 되고, 목록에 없으면 입력한 이름 그대로 고를 수 있어요.
 * 운동 추가 페이지와 루틴 편집의 시트에서 같이 씁니다.
 */
@Composable
fun ExerciseSearch(
    onPick: (String, BodyPart) -> Unit,
    modifier: Modifier = Modifier,
    listModifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val q = query.replace(" ", "")
    val results = if (q.isEmpty()) Exercises.all
    else Exercises.all.filter { it.name.replace(" ", "").contains(q, ignoreCase = true) || it.part.label.contains(q) }
    val exact = Exercises.all.any { it.name.replace(" ", "") == q }

    Column(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(c.surface)
                .border(1.dp, c.divider, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LineIcon(LineIconKind.Search, c.textTertiary, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("운동 이름이나 부위 검색", style = MaterialTheme.typography.bodyLarge, color = c.textDisabled)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.textPrimary),
                    cursorBrush = SolidColor(c.primary),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
            }
            if (query.isNotEmpty()) {
                Text(
                    "지우기",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.textTertiary,
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable { query = "" }.padding(4.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(listModifier.fillMaxWidth()) {
            if (query.isNotBlank() && !exact) {
                item(key = "custom") {
                    PickerRow("'${query.trim()}' 추가", "직접 입력", highlight = true) { onPick(query.trim(), BodyPart.Other) }
                }
            }
            items(results, key = { it.name }) { def ->
                PickerRow(def.name, def.part.label) { onPick(def.name, def.part) }
            }
        }
    }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
}

/** 루틴 편집에서 쓰는 시트 버전 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(onPick: (String, BodyPart) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppTheme.colors.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        ExerciseSearch(
            onPick = onPick,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
            listModifier = Modifier.heightIn(min = 200.dp, max = 460.dp),
        )
    }
}

@Composable
private fun PickerRow(title: String, subtitle: String, highlight: Boolean = false, onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (highlight) c.primary else c.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
    }
}
