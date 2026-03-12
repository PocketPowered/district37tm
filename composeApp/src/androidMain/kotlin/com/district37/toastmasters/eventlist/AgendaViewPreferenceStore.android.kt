package com.district37.toastmasters.eventlist

import android.content.Context

private const val PREFERENCES_NAME = "agenda_view_preferences"
private const val SELECTED_VIEW_KEY = "selected_view_mode"

actual class AgendaViewPreferenceStore actual constructor(context: Any?) {
    private val sharedPreferences = requireNotNull(context as? Context)
        .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    actual fun getSelectedAgendaViewMode(): AgendaViewMode {
        val savedValue = sharedPreferences.getString(SELECTED_VIEW_KEY, null)
        return AgendaViewMode.entries.firstOrNull { it.name == savedValue }
            ?: AgendaViewMode.EVENT_LIST
    }

    actual fun setSelectedAgendaViewMode(mode: AgendaViewMode) {
        sharedPreferences.edit()
            .putString(SELECTED_VIEW_KEY, mode.name)
            .apply()
    }
}
