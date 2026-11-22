package com.workoutmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
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

/** 운동 고르기 시트: 부위로 거르고, 목록에서 고르거나 직접 입력 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisePickerSheet(onPick: (String, BodyPart) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    var part by remember { mutableStateOf<BodyPart?>(null) }
    var custom by remember { mutableStateOf("") }
    val list = Exercises.all.filter { part == null || it.part == part }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = c.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text("운동 추가", style = MaterialTheme.typography.titleLarge, color = c.textPrimary)
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SelectChip("전체", selected = part == null) { part = null }
                BodyPart.entries.forEach { p -> SelectChip(p.label, selected = part == p) { part = p } }
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(Modifier.heightIn(max = 360.dp)) {
                items(list, key = { it.name }) { def ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPick(def.name, def.part) }
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val badge = AppTheme.badgeFor(def.part.label)
                        Box(
                            Modifier.size(8.dp).clip(CircleShape).background(badge.strong),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(def.name, style = MaterialTheme.typography.bodyLarge, color = c.textPrimary, modifier = Modifier.weight(1f))
                        Text(def.part.label, style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("목록에 없으면 직접 입력", style = MaterialTheme.typography.labelMedium, color = c.textTertiary)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppTextField(
                    value = custom,
                    onValueChange = { custom = it },
                    placeholder = "예) 케이블 크로스오버",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                AppButton(
                    "추가",
                    onClick = { onPick(custom.trim(), part ?: BodyPart.Core) },
                    enabled = custom.isNotBlank(),
                    height = 48.dp,
                    modifier = Modifier.width(76.dp),
                )
            }
        }
    }
}
