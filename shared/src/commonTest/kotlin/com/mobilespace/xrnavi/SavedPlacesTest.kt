package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SavedPlacesTest {
    @Test
    fun addsConfirmedPlacesAndFindsDuplicatesWithoutMergingDistinctPlaces() {
        val first = addSavedPlace(emptyList(), demoPlaces[0])
        assertEquals(SavedPlaceAddStatus.Added, first.status)
        assertEquals(1, first.places.size)
        assertEquals(demoPlaces[0].title, first.places.single().address)

        val duplicate = addSavedPlace(first.places, demoPlaces[0].copy(title = " ПЛОЩА РИНОК, 1 "))
        assertEquals(SavedPlaceAddStatus.Duplicate, duplicate.status)
        assertEquals(first.places, duplicate.places)
        assertEquals(first.places.single(), duplicate.existing)

        val sameAddressDifferentLocation = addSavedPlace(first.places, demoPlaces[2])
        assertEquals(SavedPlaceAddStatus.Added, sameAddressDifferentLocation.status)
        assertEquals(2, sameAddressDifferentLocation.places.size)
    }

    @Test
    fun renamingAndDeletionAreAddressKeyedAndRejectBlankLabels() {
        val first = addSavedPlace(emptyList(), demoPlaces[0]).places.single()
        val second = addSavedPlace(listOf(first), demoPlaces[1]).places.last()
        val longLabel = "Улюблене місце для тривалих поїздок у центральній частині Львова біля Ратуші"

        val renamed = renameSavedPlace(listOf(first, second), first.key, longLabel)
        assertNotNull(renamed)
        assertEquals(longLabel, renamed.first().label)
        assertEquals(first.address, renamed.first().address)
        assertEquals(first.detail, renamed.first().detail)
        assertEquals(second, renamed.last())
        assertNull(renameSavedPlace(renamed, first.key, "  "))

        assertEquals(listOf(second), removeSavedPlace(renamed, first.key))
    }

    @Test
    fun similarAddressPrefixesRemainDistinctAndUnchangedByLabelEdits() {
        val first = addSavedPlace(emptyList(), demoPlaces[0])
        val second = addSavedPlace(first.places, demoPlaces[1])
        assertEquals(SavedPlaceAddStatus.Added, second.status)
        assertEquals("площа Ринок, 1", second.places[0].address)
        assertEquals("площа Ринок, 10", second.places[1].address)
        assertEquals(2, second.places.map { it.key }.distinct().size)

        val renamed = renameSavedPlace(
            second.places,
            second.places[0].key,
            "Довгий ярлик, який не змінює підтверджену адресу"
        )
        assertNotNull(renamed)
        assertEquals(second.places[0].address, renamed[0].address)
        assertEquals(second.places[1], renamed[1])
    }

    @Test
    fun localEncodingRestoresListAndRejectsMalformedOrDuplicateRecords() {
        val places = demoPlaces.take(2).map { it.toSavedPlace() }
        assertEquals(
            SavedPlacesDecodeResult.Success(places),
            decodeSavedPlaces(encodeSavedPlaces(places))
        )
        assertEquals(SavedPlacesDecodeResult.Success(emptyList()), decodeSavedPlaces(null))
        assertEquals(SavedPlacesDecodeResult.Invalid, decodeSavedPlaces("corrupt"))
        val duplicate = listOf(places.first(), places.first())
        assertEquals(
            SavedPlacesDecodeResult.Invalid,
            decodeSavedPlaces(encodeSavedPlaces(duplicate))
        )
    }

    @Test
    fun selectingSavedAddressOnlyChangesActiveTargetAndKeepsPreviewWhenUnchanged() {
        val saved = demoPlaces[0].toSavedPlace()
        val addresses = AddressSearchState(
            origin = demoPlaces[1].title,
            destination = saved.address,
            target = SearchTarget.Destination,
            query = "discard on selection"
        )
        val sameAddress = selectSavedPlaceAddress(addresses, saved, AppScreen.Preview)
        assertFalse(sameAddress.changed)
        assertEquals(AppScreen.Preview, sameAddress.screen)
        assertEquals(addresses.origin, sameAddress.addresses.origin)
        assertEquals(saved.address, sameAddress.addresses.destination)
        assertEquals("", sameAddress.addresses.query)

        val originTarget = addresses.copy(target = SearchTarget.Origin)
        val changedOrigin = selectSavedPlaceAddress(originTarget, demoPlaces[3].toSavedPlace(), AppScreen.Preview)
        assertTrue(changedOrigin.changed)
        assertEquals(AppScreen.Map, changedOrigin.screen)
        assertEquals(demoPlaces[3].title, changedOrigin.addresses.origin)
        assertEquals(saved.address, changedOrigin.addresses.destination)
    }

    @Test
    fun cancelAndFavoriteManagementDoNotChangeConfirmedAddresses() {
        val addresses = AddressSearchState(
            origin = demoPlaces[0].title,
            destination = demoPlaces[1].title,
            target = SearchTarget.Origin,
            query = "draft"
        )
        val cancelled = addresses.cancel()
        assertEquals(addresses.origin, cancelled.origin)
        assertEquals(addresses.destination, cancelled.destination)

        val saved = addSavedPlace(emptyList(), demoPlaces[2]).places
        val renamed = renameSavedPlace(saved, saved.single().key, "Робота")
        assertNotNull(renamed)
        val removed = removeSavedPlace(renamed, renamed.single().key)
        assertTrue(removed.isEmpty())
        assertEquals(addresses.origin, cancelled.origin)
        assertEquals(addresses.destination, cancelled.destination)
    }
}
