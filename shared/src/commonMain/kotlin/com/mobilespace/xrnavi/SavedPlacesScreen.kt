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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
internal fun SavedPlacesScreen(
    places: List<SavedPlace>,
    initialEditKey: String?,
    allowAddressSelection: Boolean,
    allowDestinationUse: Boolean,
    notice: String,
    onBack: () -> Unit,
    onSelectAddress: (SavedPlace) -> Unit,
    onUseAsDestination: (SavedPlace) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (SavedPlace) -> Unit,
    onToggleTheme: () -> Unit
) {
    val screenViewModel: SavedPlacesScreenViewModel = viewModel { SavedPlacesScreenViewModel() }
    val screenBloc = screenViewModel.bloc
    val screenState by screenBloc.state.collectAsStateWithLifecycle()
    DisposableEffect(screenBloc) {
        onDispose { screenBloc.dispatch(SavedPlacesScreenEvent.Reset) }
    }
    val renameFocusRequester = remember { FocusRequester() }
    LaunchedEffect(initialEditKey, places) {
        val initial = places.firstOrNull { it.key == initialEditKey }
        screenBloc.dispatch(SavedPlacesScreenEvent.SelectInitialEdit(initial))
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Назад",
                modifier = Modifier.clickable(role = Role.Button, onClick = onBack)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
            ProductWordmark(Modifier.weight(1f))
            Text(
                "Тема",
                modifier = Modifier.clickable(role = Role.Button, onClick = onToggleTheme)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                Modifier.widthIn(max = 760.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Збережені місця",
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    "Особисте обране · локально в деморежимі",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                StatusCard(
                    "Локальне збереження",
                    "Місця зберігаються лише на цьому пристрої. Хмарну синхронізацію не підключено."
                )
                if (notice.isNotBlank()) StatusCard("Збереження", notice)
                if (places.isEmpty()) {
                    StatusCard(
                        "Поки немає збережених місць",
                        "Додавайте підтверджені адреси з результатів демопошуку. Нещодавні результати не додаються автоматично."
                    )
                } else {
                    places.forEach { place ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().testTag("saved-place-${place.key}"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    place.label,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!place.label.equals(place.address, ignoreCase = true)) {
                                    Text(
                                        place.address,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    place.detail,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (allowAddressSelection) Text(
                                        "Вибрати адресу",
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            onSelectAddress(place)
                                        }.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                    if (allowDestinationUse) Text(
                                        "Використати як призначення",
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            onUseAsDestination(place)
                                        }.padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        "Перейменувати ярлик",
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            screenBloc.dispatch(SavedPlacesScreenEvent.Edit(place))
                                        }.padding(vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        "Видалити",
                                        modifier = Modifier.clickable(role = Role.Button) {
                                            screenBloc.dispatch(SavedPlacesScreenEvent.RequestDelete(place))
                                        }.padding(vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                StatusCard(
                    "Організація",
                    "Корпоративні місця ще не підключено. Особисте обране не означає членство чи доступ до автопарку."
                )
                Text(
                    "Ярлики «Дім» і «Робота» можна задати лише вручну; адреси не заповнюються автоматично.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    screenState.editingPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { screenBloc.dispatch(SavedPlacesScreenEvent.CancelEdit) },
            title = { Text("Перейменувати ярлик") },
            text = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                LaunchedEffect(place.key) {
                    renameFocusRequester.requestFocus()
                    dialogKeyboardController?.show()
                }
                Column(
                    Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(place.address)
                    BasicTextField(
                        value = screenState.editedLabel,
                        onValueChange = {
                            screenBloc.dispatch(SavedPlacesScreenEvent.EditLabel(it))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                dialogFocusManager.clearFocus(force = true)
                                dialogKeyboardController?.hide()
                            }
                        ),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.fillMaxWidth().background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ).padding(12.dp).focusRequester(renameFocusRequester)
                            .testTag("saved-place-label-input")
                    )
                }
            },
            confirmButton = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                TextButton(
                    enabled = screenState.editedLabel.isNotBlank(),
                    onClick = {
                        dialogFocusManager.clearFocus(force = true)
                        dialogKeyboardController?.hide()
                        onRename(place.key, screenState.editedLabel)
                        screenBloc.dispatch(SavedPlacesScreenEvent.SaveEdit)
                    }
                ) { Text("Зберегти") }
            },
            dismissButton = {
                val dialogFocusManager = LocalFocusManager.current
                val dialogKeyboardController = LocalSoftwareKeyboardController.current
                TextButton(onClick = {
                    dialogFocusManager.clearFocus(force = true)
                    dialogKeyboardController?.hide()
                    screenBloc.dispatch(SavedPlacesScreenEvent.CancelEdit)
                }) { Text("Скасувати") }
            }
        )
    }
    screenState.deletingPlace?.let { place ->
        AlertDialog(
            onDismissRequest = { screenBloc.dispatch(SavedPlacesScreenEvent.CancelDelete) },
            title = { Text("Видалити з обраного?") },
            text = { Text("${place.label} · ${place.address}") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(place)
                    screenBloc.dispatch(SavedPlacesScreenEvent.ConfirmDelete)
                }) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(
                    onClick = { screenBloc.dispatch(SavedPlacesScreenEvent.CancelDelete) }
                ) { Text("Скасувати") }
            }
        )
    }
}
