package com.district37.toastmasters.eventlist

enum class AgendaViewMode {
    EVENT_LIST,
    SCHEDULE
}

expect class AgendaViewPreferenceStore(context: Any? = null) {
    fun getSelectedAgendaViewMode(): AgendaViewMode

    fun setSelectedAgendaViewMode(mode: AgendaViewMode)
}
