package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AdditionalNoticeBlocTest {
    @Test
    fun allUnavailableActionsProduceExplicitNotice() {
        val bloc = AdditionalNoticeBloc()
        bloc.dispatch(AdditionalNoticeEvent.Unavailable("Завантаження мап"))
        assertNotNull(bloc.state.value.notice)
        assertTrue(bloc.state.value.notice.orEmpty().contains("Дію не виконано"))
        bloc.dispatch(AdditionalNoticeEvent.Dismiss)
        assertNull(bloc.state.value.notice)
    }
}
