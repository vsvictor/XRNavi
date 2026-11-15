package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddVehicleDropdown(
    value: String,
    placeholder: String,
    options: List<Pair<String, String>>,
    expanded: Boolean,
    enabled: Boolean,
    tag: String,
    onValueChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onSelected: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    selection: TextRange = TextRange(value.length),
    onSelectionChanged: (TextRange) -> Unit = {},
    inputEffect: AddVehicleInputEffect? = null,
    onInputEffectHandled: (AddVehicleInputEffect) -> Unit = {},
    optionDividers: Boolean = false,
    optionTrailingContent: (@Composable (String) -> Unit)? = null,
    readOnly: Boolean = false,
    popupMaxHeight: Dp? = 240.dp,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(enabled, inputEffect) {
        if (enabled && inputEffect != null) {
            when (inputEffect) {
                AddVehicleInputEffect.FocusMake -> {
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
                AddVehicleInputEffect.HideKeyboard -> keyboard?.hide()
            }
            onInputEffectHandled(inputEffect)
        }
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().testTag(tag),
    ) {
        OutlinedTextField(
            value = TextFieldValue(value, selection),
            onValueChange = {
                if (it.text != value) onValueChange(it.text)
                onSelectionChanged(it.selection)
            },
            enabled = enabled,
            readOnly = readOnly,
            singleLine = !readOnly,
            placeholder = { Text(placeholder) },
            trailingIcon = {
                IconButton(
                    onClick = { onExpandedChange(!expanded) },
                    enabled = enabled,
                ) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            },
            modifier = Modifier.fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) },
        )
        if (readOnly) {
            Box(
                Modifier.matchParentSize()
                    .semantics { contentDescription = value.ifBlank { placeholder } }
                    .clickable(enabled = enabled, role = Role.Button) { onExpandedChange(!expanded) },
            )
        }
        DropdownMenu(
            expanded = enabled && expanded && options.isNotEmpty(),
            onDismissRequest = { onExpandedChange(false) },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.width(maxWidth).then(
                popupMaxHeight?.let { Modifier.heightIn(max = it) } ?: Modifier,
            ),
        ) {
            options.forEachIndexed { index, (id, name) ->
                if (optionDividers && index > 0) HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(name) },
                    trailingIcon = optionTrailingContent?.let { content -> { content(id) } },
                    onClick = { onSelected(id) },
                    enabled = enabled,
                    modifier = Modifier.testTag("$tag-option-$id"),
                )
            }
        }
    }
}
