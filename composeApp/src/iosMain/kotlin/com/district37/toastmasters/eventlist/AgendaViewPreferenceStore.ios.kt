package com.district37.toastmasters.eventlist

import platform.Foundation.NSUserDefaults

private const val SELECTED_VIEW_KEY = "agenda_selected_view_mode"

actual class AgendaViewPreferenceStore actual constructor(context: Any?) {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual fun getSelectedAgendaViewMode(): AgendaViewMode {
        val savedValue = userDefaults.stringForKey(SELECTED_VIEW_KEY)
        return AgendaViewMode.entries.firstOrNull { it.name == savedValue }
            ?: AgendaViewMode.EVENT_LIST
    }

    actual fun setSelectedAgendaViewMode(mode: AgendaViewMode) {
        userDefaults.setObject(mode.name, SELECTED_VIEW_KEY)
    }
}
