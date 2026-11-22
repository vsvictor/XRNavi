package com.mobilespace.xrnavi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
internal fun VehiclePhotoSlider(
    state: VehiclePhotosState,
    dispatch: (VehiclePhotosEvent) -> Unit,
    imageHeight: Dp,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().testTag("vehicle-photos")) {
        key(state.query, state.selectionKey) {
            val dark = isDarkXrnaviTheme()
            val pager = rememberPagerState(
                initialPage = state.page.coerceIn(0, state.pageCount - 1),
                pageCount = { state.pageCount },
            )
            LaunchedEffect(pager) {
                snapshotFlow { pager.currentPage }.distinctUntilChanged().collect {
                    dispatch(VehiclePhotosEvent.PageChanged(it))
                }
            }
            Box(Modifier.fillMaxWidth().height(imageHeight)) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize().testTag("vehicle-photo-pager"),
                ) { index ->
                    val photo = state.photoAtPage(index)
                    if (index == 0) {
                        SignInHero(
                            modifier = Modifier.fillMaxSize().testTag("vehicle-photo-original"),
                            dark = dark,
                            horizontalLayout = false,
                            image = defaultVehiclePhoto(dark),
                            showOverlayContent = false,
                            reducedGradient = true,
                        )
                    } else if (photo != null) {
                        val context = LocalPlatformContext.current
                        val imageLoader = remember(context) { ImageLoader.Builder(context).build() }
                        DisposableEffect(imageLoader) { onDispose { imageLoader.shutdown() } }
                        Box(
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            if (photo.url in state.failedImages) {
                                Column(Modifier.align(Alignment.Center)) {
                                    Text("Не вдалося завантажити фото.")
                                    TextButton(onClick = {
                                        dispatch(
                                            VehiclePhotosEvent.RetryImage(
                                                photo.url
                                            )
                                        )
                                    }) {
                                        Text("Повторити")
                                    }
                                }
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(photo.url)
                                        .httpHeaders(
                                            NetworkHeaders.Builder()
                                                .set(
                                                    "User-Agent",
                                                    "XRNavi/1.0 (https://github.com/vsvictor/XRNavi)"
                                                )
                                                .build()
                                        ).build(),
                                    imageLoader = imageLoader,
                                    contentDescription = photo.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onError = { dispatch(VehiclePhotosEvent.ImageFailed(photo.url)) },
                                )
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(signInHeroGradient(dark, horizontal = false))
                                )
                            }
                        }
                    }
                }
                overlay()
            }
            state.photoAtPage(pager.currentPage)?.let { photo ->
                val uri = LocalUriHandler.current
                Text(
                    "${photo.title} · ${photo.author} · Wikimedia Commons",
                    Modifier.clickable { uri.openUri(photo.sourceUrl) }
                        .testTag("vehicle-photo-source"),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    photo.license,
                    Modifier.clickable { uri.openUri(photo.licenseUrl) }
                        .testTag("vehicle-photo-license"),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            if (state.saveError) {
                Text("Не вдалося зберегти вибране фото.", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { dispatch(VehiclePhotosEvent.RetrySave) }) { Text("Повторити") }
            }
        }
    }
}
