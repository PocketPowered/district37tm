package com.district37.toastmasters.infra.serializers

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for kotlinx.datetime.Instant
 * Handles various ISO-8601 timestamp formats from the server
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val dateString = decoder.decodeString()
        return if (dateString.contains('T') && !dateString.contains('Z') && !dateString.contains('+')) {
            // Handle format like "2024-12-01T23:00:00" by appending UTC timezone
            Instant.parse("${dateString}Z")
        } else {
            Instant.parse(dateString)
        }
    }
}
