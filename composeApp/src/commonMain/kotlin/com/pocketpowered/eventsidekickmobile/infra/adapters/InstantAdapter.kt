package com.district37.toastmasters.infra.adapters

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import kotlinx.datetime.Instant

/**
 * Apollo adapter for kotlinx.datetime.Instant scalar type
 * Handles serialization/deserialization of Instant values from GraphQL
 */
object InstantAdapter : Adapter<Instant> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Instant {
        val dateString = reader.nextString()!!
        return if (dateString.contains('T') && !dateString.contains('Z') && !dateString.contains('+')) {
            // Handle format like "2024-12-01T23:00:00" by appending UTC timezone
            Instant.parse("${dateString}Z")
        } else {
            Instant.parse(dateString)
        }
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Instant) {
        writer.value(value.toString())
    }
}
