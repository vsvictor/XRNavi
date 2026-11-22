package com.mobilespace.xrnavi

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.mustang_thumbnail_dark
import xrnavi.shared.generated.resources.mustang_thumbnail_light

@OptIn(ExperimentalCoroutinesApi::class)
class VehiclePhotosTest {
    @Test
    fun defaultPhotoMatchesTheme() {
        assertEquals(Res.drawable.mustang_thumbnail_dark, defaultVehiclePhoto(true))
        assertEquals(Res.drawable.mustang_thumbnail_light, defaultVehiclePhoto(false))
    }

    private val query = VehiclePhotoQuery("Ford", "Fusion", 2017)
    private val photo = VehiclePhoto(
        "https://upload.wikimedia.org/car.jpg",
        "https://commons.wikimedia.org/wiki/File:Ford.jpg",
        "Author", "CC BY 4.0", "https://creativecommons.org/licenses/by/4.0", "Ford.jpg",
    )

    @Test
    fun originalImageIsFirstPageInAllSearchStates() {
        val states = listOf(
            VehiclePhotosState(),
            VehiclePhotosState(query = query, loading = true),
            VehiclePhotosState(query = query, error = "network error"),
            VehiclePhotosState(query = query, photos = emptyList()),
        )
        states.forEach {
            assertEquals(1, it.pageCount)
            assertEquals(null, it.photoAtPage(0))
        }
        val found = VehiclePhotosState(query = query, photos = listOf(photo))
        assertEquals(2, found.pageCount)
        assertEquals(null, found.photoAtPage(0))
        assertEquals(photo, found.photoAtPage(1))
        assertEquals(null, found.photoAtPage(2))
    }

    @Test
    fun searchEncodesParametersAndKeepsOnlyRelevantLicensedPhotos() = runTest {
        val client = HttpClient(MockEngine { request ->
            assertEquals("Ford Fusion 2017 filetype:bitmap", request.url.parameters["gsrsearch"])
            assertEquals("6", request.url.parameters["gsrnamespace"])
            assertTrue(request.headers["User-Agent"].orEmpty().contains("XRNavi"))
            respond(
                """{"query":{"pages":[
                    {"index":2,"title":"File:Ford Fusion 2017 front.jpg","imageinfo":[{"mime":"image/jpeg","thumburl":"https://thumb.wikimedia.org/a.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:a.jpg","extmetadata":{"Artist":{"value":"<a href='x'>Jane &amp; John</a>"},"LicenseShortName":{"value":"CC BY 4.0"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by/4.0"}}}]},
                    {"index":1,"title":"File:Ford Fusion 2017 side.jpg","imageinfo":[{"mime":"image/jpeg","url":"https://upload.wikimedia.org/b.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:b.jpg","extmetadata":{"Artist":{"value":"Alice"},"LicenseShortName":{"value":"CC0"},"LicenseUrl":{"value":"https://creativecommons.org/publicdomain/zero/1.0"}}}]},
                    {"index":3,"title":"File:Ford Fusion 2017.jpg","imageinfo":[{"mime":"image/jpeg","url":"https://upload.wikimedia.org/c.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:c.jpg","extmetadata":{"Artist":{"value":"A"},"LicenseShortName":{"value":"CC BY-SA 4.0"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by-sa/4.0"}}}]},
                    {"index":4,"title":"File:Ford Focus 2017.jpg","imageinfo":[{"mime":"image/jpeg","url":"https://upload.wikimedia.org/d.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:d.jpg","extmetadata":{"Artist":{"value":"A"},"LicenseShortName":{"value":"CC BY 4.0"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by/4.0"}}}]},
                    {"index":5,"title":"File:Ford Fusion 2017.jpg","imageinfo":[{"mime":"image/jpeg","url":"https://evil.example/e.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:e.jpg","extmetadata":{"Artist":{"value":"A"},"LicenseShortName":{"value":"CC BY 4.0"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by/4.0"}}}]},
                    {"index":6,"title":"File:1965 Ford Fusion.jpg","imageinfo":[{"mime":"image/jpeg","url":"https://upload.wikimedia.org/f.jpg","descriptionurl":"https://commons.wikimedia.org/wiki/File:f.jpg","extmetadata":{"ImageDescription":{"value":"Photographed in 2017"},"Artist":{"value":"A"},"LicenseShortName":{"value":"CC BY 4.0"},"LicenseUrl":{"value":"https://creativecommons.org/licenses/by/4.0"}}}]}
                ]}}""",
                HttpStatusCode.OK,
            )
        })
        try {
            val result = WikimediaVehiclePhotoService(client).search(query)
            assertEquals(listOf("Alice", "Jane & John"), result.map { it.author })
            assertEquals(listOf("CC0", "CC BY 4.0"), result.map { it.license })
        } finally {
            client.close()
        }
    }

    @Test
    fun apiErrorsAreNotEmptyResults() {
        assertFailsWith<IllegalStateException> {
            parseWikimediaPhotos("""{"error":{"code":"ratelimited"}}""", query)
        }
        assertFailsWith<IllegalStateException> {
            parseWikimediaPhotos("""{"query":"invalid"}""", query)
        }
        assertEquals(emptyList(), parseWikimediaPhotos("""{"batchcomplete":true}""", query))
    }

    @Test
    fun selectionCancelsPreviousSearchAndCachesSuccessfulResults() = runTest {
        val first = CompletableDeferred<List<VehiclePhoto>>()
        val secondQuery = VehiclePhotoQuery("Ford", "Fiesta", 2018)
        var calls = 0
        val service = object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> {
                calls++
                return if (query == this@VehiclePhotosTest.query) first.await() else listOf(photo)
            }
        }
        val bloc = VehiclePhotosBloc(service, backgroundScope)
        bloc.dispatch(VehiclePhotosEvent.Open(query))
        runCurrent()
        assertTrue(bloc.state.value.loading)
        bloc.dispatch(VehiclePhotosEvent.Open(secondQuery))
        runCurrent()
        first.complete(emptyList())
        runCurrent()
        assertEquals(secondQuery, bloc.state.value.query)
        assertEquals(listOf(photo), bloc.state.value.photos)
        bloc.dispatch(VehiclePhotosEvent.Open(null))
        bloc.dispatch(VehiclePhotosEvent.Open(secondQuery))
        runCurrent()
        assertEquals(2, calls)
        assertFalse(bloc.state.value.loading)
        assertEquals(listOf(photo), bloc.state.value.photos)
    }

    @Test
    fun failureIsRetainedAndRetryPerformsAnotherSearch() = runTest {
        var calls = 0
        val bloc = VehiclePhotosBloc(object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> {
                calls++
                if (calls == 1) error("network")
                return listOf(photo)
            }
        }, backgroundScope)
        bloc.dispatch(VehiclePhotosEvent.Open(query))
        runCurrent()
        assertTrue(bloc.state.value.error.isNotEmpty())
        bloc.dispatch(VehiclePhotosEvent.Retry)
        runCurrent()
        assertEquals(2, calls)
        assertEquals(listOf(photo), bloc.state.value.photos)
        assertEquals("", bloc.state.value.error)
    }

    @Test
    fun pageSelectionIncludesOriginalAndResetsForNewQuery() = runTest {
        val bloc = VehiclePhotosBloc(object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> =
                listOf(photo)
        }, backgroundScope)
        bloc.dispatch(VehiclePhotosEvent.Open(query))
        runCurrent()
        assertEquals(0, bloc.state.value.page)
        bloc.dispatch(VehiclePhotosEvent.PageChanged(1))
        assertEquals(1, bloc.state.value.page)
        bloc.dispatch(VehiclePhotosEvent.PageChanged(2))
        assertEquals(1, bloc.state.value.page)
        bloc.dispatch(VehiclePhotosEvent.PageChanged(0))
        assertEquals(0, bloc.state.value.page)
        bloc.dispatch(VehiclePhotosEvent.PageChanged(1))
        bloc.dispatch(VehiclePhotosEvent.Open(VehiclePhotoQuery("Ford", "Fiesta", 2018)))
        runCurrent()
        assertEquals(0, bloc.state.value.page)
    }

    @Test
    fun selectedPhotoPersistsPerAccountAndVehicleAndRestoresWithoutSearchResults() = runTest {
        val storage = MemorySecureSessionStorage()
        val selections = VehiclePhotoSelections(storage)
        val service = object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> =
                listOf(photo)
        }
        val bloc = VehiclePhotosBloc(service, backgroundScope, selections)
        bloc.dispatch(VehiclePhotosEvent.Open(query, "account", "vehicle"))
        runCurrent()
        bloc.dispatch(VehiclePhotosEvent.PageChanged(1))
        assertEquals(
            photo,
            VehiclePhotoSelections(storage).selections.value["account"]?.get("vehicle")
        )
        val reopened = VehiclePhotosBloc(object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> = emptyList()
        }, backgroundScope, VehiclePhotoSelections(storage))
        reopened.dispatch(VehiclePhotosEvent.Open(query, "account", "vehicle"))
        runCurrent()
        assertEquals(photo, reopened.state.value.photoAtPage(reopened.state.value.page))
        reopened.dispatch(VehiclePhotosEvent.Open(query, "other-account", "vehicle"))
        assertEquals(0, reopened.state.value.page)
        reopened.dispatch(VehiclePhotosEvent.Open(query, "account", "other-vehicle"))
        assertEquals(0, reopened.state.value.page)
        reopened.dispatch(VehiclePhotosEvent.Open(query, "account", "vehicle"))
        reopened.dispatch(VehiclePhotosEvent.PageChanged(0))
        assertEquals(
            null,
            VehiclePhotoSelections(storage).selections.value["account"]?.get("vehicle")
        )
    }

    @Test
    fun draftSelectionMovesToCreatedVehicleAndSaveFailuresRemainRetryable() = runTest {
        var failWrites = true
        val backing = MemorySecureSessionStorage()
        val storage = object : SecureSessionStorage {
            override fun read(): String? = backing.read()
            override fun write(value: String): Boolean = !failWrites && backing.write(value)
            override fun clear() = backing.clear()
        }
        val selections = VehiclePhotoSelections(storage)
        val bloc = VehiclePhotosBloc(object : VehiclePhotoService {
            override suspend fun search(query: VehiclePhotoQuery): List<VehiclePhoto> =
                listOf(photo)
        }, backgroundScope, selections)
        bloc.dispatch(VehiclePhotosEvent.Open(query, "account", draft = true))
        runCurrent()
        bloc.dispatch(VehiclePhotosEvent.PageChanged(1))
        bloc.dispatch(VehiclePhotosEvent.Open(query, "account", "new-vehicle"))
        assertTrue(bloc.state.value.saveError)
        assertEquals(photo, bloc.state.value.photoAtPage(bloc.state.value.page))
        failWrites = false
        bloc.dispatch(VehiclePhotosEvent.RetrySave)
        assertFalse(bloc.state.value.saveError)
        assertEquals(
            photo,
            VehiclePhotoSelections(storage).selections.value["account"]?.get("new-vehicle")
        )
    }
}
