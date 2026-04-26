package com.district37.toastmasters.database

class DevSettingsRepository(database: TMDatabase) {
    private val queries = database.devSettingsQueries

    fun getConferenceOverrideId(): Long? {
        return queries.getConferenceOverrideId().executeAsOneOrNull()?.conference_override_id
    }

    fun setConferenceOverrideId(id: Long) {
        queries.setConferenceOverrideId(id)
    }

    fun clearConferenceOverrideId() {
        queries.clearConferenceOverrideId()
    }
}
