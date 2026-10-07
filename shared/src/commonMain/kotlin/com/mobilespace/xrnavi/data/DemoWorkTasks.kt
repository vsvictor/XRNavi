package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.WorkTask

/** Read-only examples from the mockups, not tasks downloaded or accepted from an organization. */
class DemoWorkTasks {
    val current = WorkTask("DL-204", "Львів, вул. Городоцька, 359")
    val offered = WorkTask("DL-205", "Київ, термінал Північ")
    fun find(id: String?) = listOf(current, offered).find { it.id == id }
}
