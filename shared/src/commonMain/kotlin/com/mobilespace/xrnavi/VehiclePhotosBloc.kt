package com.mobilespace.xrnavi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class VehiclePhotosState(
    val query: VehiclePhotoQuery? = null,
    val loading: Boolean = false,
    val photos: List<VehiclePhoto> = emptyList(),
    val error: String = "",
    val page: Int = 0,
    val failedImages: Set<String> = emptySet(),
    val saveError: Boolean = false,
    val selectionKey: String? = null,
    val accountId: String? = null,
) {
    val pageCount: Int get() = photos.size + 1
    fun photoAtPage(page: Int): VehiclePhoto? = photos.getOrNull(page - 1)
}

internal sealed interface VehiclePhotosEvent {
    data class Open(
        val query: VehiclePhotoQuery?,
        val accountId: String? = null,
        val vehicleId: String? = null,
        val draft: Boolean = false,
    ) : VehiclePhotosEvent

    data object Retry : VehiclePhotosEvent
    data object RetrySave : VehiclePhotosEvent
    data class PageChanged(val page: Int) : VehiclePhotosEvent
    data class ImageFailed(val url: String) : VehiclePhotosEvent
    data class RetryImage(val url: String) : VehiclePhotosEvent
}

internal class VehiclePhotosBloc(
    private val service: VehiclePhotoService,
    private val scope: CoroutineScope,
    private val selections: VehiclePhotoSelections = VehiclePhotoSelections(
        MemorySecureSessionStorage()
    ),
) {
    private val mutableState = MutableStateFlow(VehiclePhotosState())
    val state = mutableState.asStateFlow()
    private val cache = mutableMapOf<VehiclePhotoQuery, List<VehiclePhoto>>()
    private var job: Job? = null
    private var request: Any? = null
    private var context = VehiclePhotosEvent.Open(null)
    private var selectedPhoto: VehiclePhoto? = null

    fun dispatch(event: VehiclePhotosEvent) {
        when (event) {
            is VehiclePhotosEvent.Open -> {
                if (event == context) return
                job?.cancel()
                request = null
                val oldContext = context
                val transferDraft = oldContext.draft && event.vehicleId != null &&
                        oldContext.accountId == event.accountId && oldContext.query == event.query
                if (transferDraft) {
                    val saved = event.accountId != null &&
                            selections.select(event.accountId, event.vehicleId, selectedPhoto)
                    mutableState.value = mutableState.value.copy(saveError = !saved)
                } else if (oldContext.accountId != event.accountId ||
                    oldContext.vehicleId != event.vehicleId || oldContext.draft != event.draft ||
                    (event.vehicleId == null && oldContext.query != event.query)
                ) {
                    selectedPhoto = if (event.accountId != null && event.vehicleId != null)
                        selections.selections.value[event.accountId]?.get(event.vehicleId) else null
                }
                context = event
                val query = event.query
                val saveError = if (transferDraft) mutableState.value.saveError else false
                val initial = selectedPhoto?.let(::listOf).orEmpty()
                mutableState.value = VehiclePhotosState(
                    query = query,
                    photos = initial,
                    page = if (selectedPhoto == null) 0 else 1,
                    saveError = saveError,
                    selectionKey = if (event.draft) "draft" else event.vehicleId,
                    accountId = event.accountId,
                )
                if (query != null) {
                    val cached = cache[query]
                    if (cached != null) updatePhotos(cached)
                    else load(query)
                }
            }

            VehiclePhotosEvent.Retry -> mutableState.value.query?.let { load(it) }
            VehiclePhotosEvent.RetrySave -> {
                val accountId = context.accountId
                val vehicleId = context.vehicleId
                val saved = accountId != null && vehicleId != null &&
                        selections.select(accountId, vehicleId, selectedPhoto)
                mutableState.value = mutableState.value.copy(saveError = !saved)
            }

            is VehiclePhotosEvent.PageChanged -> if (event.page in 0 until mutableState.value.pageCount) {
                val photo = mutableState.value.photoAtPage(event.page)
                if (photo != selectedPhoto) {
                    selectedPhoto = photo
                    val accountId = context.accountId
                    val vehicleId = context.vehicleId
                    val saved = if (vehicleId == null) true
                    else accountId != null && selections.select(accountId, vehicleId, photo)
                    mutableState.value =
                        mutableState.value.copy(page = event.page, saveError = !saved)
                } else mutableState.value = mutableState.value.copy(page = event.page)
            }

            is VehiclePhotosEvent.ImageFailed -> {
                if (mutableState.value.photos.any { it.url == event.url }) {
                    mutableState.value = mutableState.value.copy(
                        failedImages = mutableState.value.failedImages + event.url,
                    )
                }
            }

            is VehiclePhotosEvent.RetryImage -> {
                mutableState.value = mutableState.value.copy(
                    failedImages = mutableState.value.failedImages - event.url,
                )
            }
        }
    }

    private fun load(query: VehiclePhotoQuery) {
        job?.cancel()
        val identity = Any()
        request = identity
        mutableState.value = mutableState.value.copy(loading = true, error = "")
        job = scope.launch {
            try {
                val photos = service.search(query)
                if (request !== identity) return@launch
                if (cache.size >= 20) cache.remove(cache.keys.first())
                cache[query] = photos
                updatePhotos(photos)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (request === identity) {
                    mutableState.value = mutableState.value.copy(
                        loading = false,
                        error = "Не вдалося завантажити фото. Повторіть."
                    )
                }
            }
        }
    }

    private fun updatePhotos(photos: List<VehiclePhoto>) {
        val selected = selectedPhoto
        val results =
            if (selected != null) listOf(selected) + photos.filterNot { it.url == selected.url } else photos
        val page = if (selected == null) 0 else results.indexOfFirst { it.url == selected.url } + 1
        mutableState.value = mutableState.value.copy(loading = false, photos = results, page = page)
    }
}
