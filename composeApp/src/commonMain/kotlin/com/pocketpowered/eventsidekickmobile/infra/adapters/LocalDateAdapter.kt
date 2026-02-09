package com.district37.toastmasters.infra.adapters

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import kotlinx.datetime.LocalDate

/**
 * Apollo adapter for kotlinx.datetime.LocalDate scalar type
 * Handles serialization/deserialization of LocalDate values from GraphQL
 *
 * LocalDate is serialized as ISO-8601 date string (YYYY-MM-DD)
 */
object LocalDateAdapter : Adapter<LocalDate> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): LocalDate {
        val dateString = reader.nextString()!!
        return LocalDate.parse(dateString)
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: LocalDate) {
        writer.value(value.toString())
    }
}
