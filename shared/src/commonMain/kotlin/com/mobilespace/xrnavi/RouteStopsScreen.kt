package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import kotlin.math.abs
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun RouteStopsScreen(
    origin: String,
    destination: String,
    stops: List<RouteStop>,
    notice: String,
    canSave: Boolean,
    onAdd: () -> Unit,
    onReplace: (String) -> Unit,
    onRemove: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onToggleTheme: () -> Unit
) {
    AuthBackHandler(onCancel)
    val editorScrollState = rememberScrollState()
    LaunchedEffect(stops) { editorScrollState.scrollTo(0) }
    Column(modifier = Modifier.fillMaxSize().imePadding().exportTestTags()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Скасувати",
                modifier = Modifier.clickable(role = Role.Button, onClick = onCancel).padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Зупинки маршруту",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Тема",
                modifier = Modifier.clickable(role = Role.Button, onClick = onToggleTheme).padding(8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().testTag("route-stops-content").verticalScroll(editorScrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                "Початок · лише для перегляду",
                origin.ifBlank { "Не обрано" },
                modifier = Modifier.testTag("route-stops-origin")
            )
            StatusCard(
                "Призначення · лише для перегляду",
                destination.ifBlank { "Не обрано" },
                modifier = Modifier.testTag("route-stops-destination")
            )
            Text(
                "Проміжні зупинки: ${stops.size}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (stops.isEmpty()) {
                StatusCard(
                    "Проміжних зупинок немає",
                    "Додайте підтверджену адресу з локального демопошуку або «Обраного»."
                )
            } else {
                stops.forEachIndexed { index, stop ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("route-stop-${stop.stopId}"),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Зупинка ${index + 1}" +
                                    stop.placeName?.let { " · $it" }.orEmpty(),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(stop.address, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                stop.detail,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    RouteStopAction("Вище", index > 0) { onMoveUp(stop.stopId) }
                                    RouteStopAction("Нижче", index < stops.lastIndex) {
                                        onMoveDown(stop.stopId)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    RouteStopAction("Замінити адресу") {
                                        onReplace(stop.stopId)
                                    }
                                    RouteStopAction("Видалити") {
                                        onRemove(stop.stopId)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            RouteStopAction("Додати зупинку", onClick = onAdd)
            if (notice.isNotBlank()) StatusCard("Зупинки маршруту", notice)
            Text(
                "Збереження не будує багатоточковий маршрут і не запускає навігацію.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RouteStopAction("Скасувати", modifier = Modifier.weight(1f), onClick = onCancel)
            RouteStopAction(
                "Зберегти зупинки",
                enabled = canSave,
                modifier = Modifier.weight(1f),
                onClick = onSave
            )
        }
    }
}
