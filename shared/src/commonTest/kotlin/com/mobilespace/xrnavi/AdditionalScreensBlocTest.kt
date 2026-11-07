package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdditionalScreensBlocTest {
    @Test
    fun returningToAnExistingDestinationDoesNotAccumulateNavigationCycles() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleCatalog))
        repeat(10) {
            bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleModel))
            bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleReview))
            bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleCatalog))
            assertTrue(bloc.state.value.backStack.isEmpty())
        }
        bloc.dispatch(AdditionalScreensEvent.Back)
        assertNull(bloc.state.value.screen)
    }

    @Test
    fun vehicleDraftIsLocalAndRetainedAcrossNavigation() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleReview))
        bloc.dispatch(AdditionalScreensEvent.VehicleHeight("4,0"))
        bloc.dispatch(AdditionalScreensEvent.VehicleLength("16.5"))
        bloc.dispatch(AdditionalScreensEvent.VehicleMass("40"))
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleCatalog))
        bloc.dispatch(AdditionalScreensEvent.Back)
        assertEquals("4,0", bloc.state.value.vehicleHeight)
        assertEquals("16.5", bloc.state.value.vehicleLength)
        assertEquals("40", bloc.state.value.vehicleMass)
    }

    @Test
    fun everyScreenOpensAndBackRestoresThePreviousScreen() {
        AdditionalScreen.entries.forEach { screen ->
            val bloc = AdditionalScreensBloc()
            bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.Privacy))
            bloc.dispatch(AdditionalScreensEvent.Open(screen))
            assertEquals(screen, bloc.state.value.screen)
            bloc.dispatch(AdditionalScreensEvent.Back)
            assertEquals(
                if (screen == AdditionalScreen.Privacy) null else AdditionalScreen.Privacy,
                bloc.state.value.screen
            )
        }
    }

    @Test
    fun bottomNavigationClosesTheEntireStackAndEmitsOneSectionTransition() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.Privacy))
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.DeleteAccount))
        bloc.dispatch(AdditionalScreensEvent.Section(DemoSection.Settings))
        assertNull(bloc.state.value.screen)
        assertTrue(bloc.state.value.backStack.isEmpty())
        assertEquals(DemoSection.Settings, bloc.state.value.sectionTarget)
        bloc.dispatch(AdditionalScreensEvent.SectionHandled)
        assertNull(bloc.state.value.sectionTarget)
    }

    @Test
    fun deletionNeverReportsSuccessOrDeletesAnythingEvenAfterConsent() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.DeleteAccount))
        bloc.dispatch(AdditionalScreensEvent.DeleteAccount)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Підтвердьте"))
        bloc.dispatch(AdditionalScreensEvent.DeletionConsent(true))
        bloc.dispatch(AdditionalScreensEvent.DeleteAccount)
        assertEquals(AdditionalScreen.DeleteAccount, bloc.state.value.screen)
        assertTrue(bloc.state.value.notice.orEmpty().contains("не видалено"))
        bloc.dispatch(AdditionalScreensEvent.Back)
        assertFalse(bloc.state.value.deletionConsent)
    }

    @Test
    fun invitationRequiresCodeAndConsentAndNeverClaimsMembership() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.Invitation))
        bloc.dispatch(AdditionalScreensEvent.AcceptInvitation)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Введіть код"))
        bloc.dispatch(AdditionalScreensEvent.InvitationCode("EXAMPLE"))
        bloc.dispatch(AdditionalScreensEvent.InvitationConsent(true))
        bloc.dispatch(AdditionalScreensEvent.AcceptInvitation)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Членство в організації не змінено"))
        bloc.dispatch(AdditionalScreensEvent.InvitationCode("OTHER"))
        assertFalse(bloc.state.value.invitationConsent)
    }

    @Test
    fun localSelectionsAreKeptAcrossDetailNavigation() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Model("Focus"))
        bloc.dispatch(AdditionalScreensEvent.Year("2023"))
        bloc.dispatch(AdditionalScreensEvent.Trim("EcoBoost · 2.3"))
        bloc.dispatch(AdditionalScreensEvent.History(HistoryFilter.Work))
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleModel))
        bloc.dispatch(AdditionalScreensEvent.Open(AdditionalScreen.VehicleReview))
        bloc.dispatch(AdditionalScreensEvent.Back)
        assertEquals("Focus", bloc.state.value.model)
        assertEquals("2023", bloc.state.value.year)
        assertEquals("EcoBoost · 2.3", bloc.state.value.trim)
        assertEquals(HistoryFilter.Work, bloc.state.value.historyFilter)
    }

    @Test
    fun allUnavailableActionsProduceExplicitNotice() {
        val bloc = AdditionalScreensBloc()
        bloc.dispatch(AdditionalScreensEvent.Unavailable("Завантаження мап"))
        assertNotNull(bloc.state.value.notice)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Дію не виконано"))
        bloc.dispatch(AdditionalScreensEvent.DismissNotice)
        assertNull(bloc.state.value.notice)
    }
}
