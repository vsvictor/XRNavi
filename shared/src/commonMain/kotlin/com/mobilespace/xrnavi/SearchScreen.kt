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
internal fun SearchScreen(
    demoMode: Boolean,
    origin: String,
    destination: String,
    vehicleClass: String,
    selectedVehicle: DemoVehicleProfile?,
    target: SearchTarget,
    query: String,
    status: SearchStatus,
    vehicleMode: String,
    savedPlaceKeys: Set<String>,
    onVehicleModeChange: (String) -> Unit,
    onTargetChange: (SearchTarget) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    onPickMap: () -> Unit,
    onSelectPlace: (DemoPlace) -> Unit,
    onSavePlace: (DemoPlace) -> SavedPlaceAddStatus,
    onOpenSavedPlaces: (SavedPlace?) -> Unit,
    onSectionChange: (DemoSection) -> Unit,
    onToggleTheme: () -> Unit
) {
    val screenViewModel: SearchScreenViewModel = viewModel { SearchScreenViewModel() }
    val screenBloc = screenViewModel.bloc
    val screenState by screenBloc.state.collectAsStateWithLifecycle()
    DisposableEffect(screenBloc) {
        onDispose { screenBloc.dispatch(SearchScreenEvent.Reset) }
    }
    val matchingPlaces = if (query.isBlank()) {
        demoPlaces.take(3)
    } else {
        demoPlaces.filter {
            it.title.contains(query.trim(), ignoreCase = true) ||
                it.detail.contains(query.trim(), ignoreCase = true)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductWordmark(
                modifier = Modifier
                    .semantics { contentDescription = "Скасувати пошук" }
                    .clickable(role = Role.Button, onClick = onBack)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (demoMode) "Демо-адреси" else "Пошук",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Тема",
                    modifier = Modifier
                        .clickable(role = Role.Button, onClick = onToggleTheme)
                        .padding(4.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AddressSearchCard(
                origin = origin,
                destination = destination,
                target = target,
                query = query,
                onTargetChange = onTargetChange,
                onQueryChange = onQueryChange,
                onSearch = onSearch
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "Обране · збережене локально",
                    modifier = Modifier
                        .clickable(role = Role.Button) { onOpenSavedPlaces(null) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
            }
            if (screenState.saveNotice.isNotBlank()) {
                StatusCard("Обране", screenState.saveNotice)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        !demoMode -> "ПОШУК НЕ ПІДКЛЮЧЕНО"
                        query.isBlank() -> "НЕЩОДАВНІ АДРЕСИ · ДЕМО"
                        else -> "ЗНАЙДЕНІ АДРЕСИ · ДЕМО"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (status == SearchStatus.Results && demoMode) {
                    Text(
                        "${matchingPlaces.size} результат${if (matchingPlaces.size == 1) "" else "и"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            when (status) {
                SearchStatus.Idle -> {
                    if (demoMode) {
                        matchingPlaces.take(2).forEach { place ->
                            SearchPlaceRow(
                                place = place,
                                recent = true,
                                isSaved = place.toSavedPlace().key in savedPlaceKeys,
                                onClick = { onSelectPlace(place) },
                                onSave = {
                                    when (onSavePlace(place)) {
                                        SavedPlaceAddStatus.Added ->
                                            screenBloc.dispatch(SearchScreenEvent.PlaceSaved)
                                        SavedPlaceAddStatus.Duplicate ->
                                            screenBloc.dispatch(SearchScreenEvent.DuplicatePlace(place))
                                        SavedPlaceAddStatus.Invalid ->
                                            screenBloc.dispatch(SearchScreenEvent.InvalidPlace)
                                    }
                                }
                            )
                        }
                    } else {
                        StatusCard(
                            title = "Адреси недоступні",
                            message = "Пошуковий провайдер не налаштований. Введений текст не надсилається."
                        )
                    }
                }

                SearchStatus.Loading -> AsyncState(
                    title = "Шукаємо адресу…",
                    message = "Пошук демонстраційних адрес",
                    loading = true
                )

                SearchStatus.Empty -> AsyncState(
                    title = "Нічого не знайдено",
                    message = "У локальному демо-наборі немає відповідної адреси."
                )

                SearchStatus.Error -> AsyncState(
                    title = "Пошук не підключено",
                    message = "Geocoding provider відсутній. Запит залишився лише в цьому екрані.",
                    actionLabel = "Повторити",
                    onAction = onSearch
                )

                SearchStatus.Results -> {
                    if (!demoMode) {
                        AsyncState(
                            title = "Живі результати недоступні",
                            message = "Цей екран не виконує мережевий пошук."
                        )
                    } else if (matchingPlaces.isEmpty()) {
                        AsyncState(
                            title = "Нічого не знайдено",
                            message = "Змініть запит або виберіть адресу з нещодавніх."
                        )
                    } else {
                        matchingPlaces.forEach { place ->
                            SearchPlaceRow(
                                place = place,
                                recent = false,
                                isSaved = place.toSavedPlace().key in savedPlaceKeys,
                                onClick = { onSelectPlace(place) },
                                onSave = {
                                    when (onSavePlace(place)) {
                                        SavedPlaceAddStatus.Added ->
                                            screenBloc.dispatch(SearchScreenEvent.PlaceSaved)
                                        SavedPlaceAddStatus.Duplicate ->
                                            screenBloc.dispatch(SearchScreenEvent.DuplicatePlace(place))
                                        SavedPlaceAddStatus.Invalid ->
                                            screenBloc.dispatch(SearchScreenEvent.InvalidPlace)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            SearchMapPicker(onClick = onPickMap)
            SearchVehicleCard(
                vehicleClass = vehicleClass,
                vehicle = selectedVehicle,
                mode = vehicleMode,
                onModeChange = onVehicleModeChange
            )
        }
        BottomNavigation(DemoSection.Map, onSectionChange)
    }
    screenState.duplicatePlace?.let { duplicate ->
        AlertDialog(
            onDismissRequest = { screenBloc.dispatch(SearchScreenEvent.DismissDuplicate) },
            title = { Text("Адресу вже збережено") },
            text = { Text("${duplicate.address} уже є в обраному. Відкрити список або змінити ярлик?") },
            confirmButton = {
                TextButton(onClick = {
                    screenBloc.dispatch(SearchScreenEvent.DismissDuplicate)
                    onOpenSavedPlaces(null)
                }) { Text("Відкрити обране") }
            },
            dismissButton = {
                TextButton(onClick = {
                    screenBloc.dispatch(SearchScreenEvent.DismissDuplicate)
                    onOpenSavedPlaces(duplicate)
                }) { Text("Перейменувати") }
            }
        )
    }
}
