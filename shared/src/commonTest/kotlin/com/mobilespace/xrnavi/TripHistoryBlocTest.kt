package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class TripHistoryBlocTest {
    @Test
    fun filterIsRetained() {
        val bloc = TripHistoryBloc()
        bloc.dispatch(TripHistoryEvent.Filter(HistoryFilter.Work))
        assertEquals(HistoryFilter.Work, bloc.state.value.filter)
    }
}
