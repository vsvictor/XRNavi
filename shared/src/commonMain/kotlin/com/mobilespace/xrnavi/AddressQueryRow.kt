package com.mobilespace.xrnavi

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

@Composable
internal fun AddressQueryRow(
    target: SearchTarget,
    active: Boolean,
    value: String,
    placeholder: String,
    onActivate: () -> Unit,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    showSearch: Boolean,
    focusRequester: FocusRequester
) {
    val dark = isDarkXrnaviTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (active) {
                    Modifier
                } else {
                    Modifier.clickable(role = Role.Button) {
                        onActivate()
                    }
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (target == SearchTarget.Origin && value.isBlank() && !active) {
            Box(
                Modifier
                    .padding(start = 1.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            )
        } else {
            FigmaIcon(
                light = if (target == SearchTarget.Origin) {
                    Res.drawable.search_origin_light
                } else {
                    Res.drawable.search_pin_light
                },
                dark = if (target == SearchTarget.Origin) {
                    Res.drawable.search_origin_dark
                } else {
                    Res.drawable.search_pin_dark
                },
                modifier = Modifier
                    .padding(start = if (target == SearchTarget.Origin) 1.dp else 0.dp)
                    .size(if (target == SearchTarget.Origin) 12.dp else 14.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            if (active) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = if (target == SearchTarget.Origin) {
                                "Початкова адреса"
                            } else {
                                "Адреса призначення"
                            }
                        },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isBlank()) {
                                Text(
                                    placeholder,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            } else {
                Text(
                    text = value.ifBlank { placeholder },
                    fontSize = 13.sp,
                    fontWeight = if (value.isBlank()) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1
                )
            }
        }
        if (active && value.isNotBlank()) {
            Text(
                "×",
                modifier = Modifier
                    .semantics { contentDescription = "Очистити пошуковий запит" }
                    .clickable(role = Role.Button) { onValueChange("") }
                    .padding(horizontal = 8.dp),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (showSearch && value.isNotBlank()) {
            FigmaIcon(
                light = Res.drawable.search_search_dark,
                dark = Res.drawable.search_search_dark,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(role = Role.Button, onClick = onSearch),
                contentDescription = "Шукати адресу",
                tint = if (dark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
            )
        }
        if (target == SearchTarget.Origin && !active) {
            FigmaIcon(
                light = Res.drawable.search_swap_light,
                dark = Res.drawable.search_swap_light,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp),
                contentDescription = "Початкова точка"
            )
        }
    }
}
