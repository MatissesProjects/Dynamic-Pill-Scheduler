package com.phos.core.data.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.phos.core.data.proto.PhosState
import java.io.InputStream
import java.io.OutputStream

object PhosStateSerializer : Serializer<PhosState> {
    override val defaultValue: PhosState = PhosState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PhosState {
        try {
            return PhosState.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: PhosState, output: OutputStream) {
        t.writeTo(output)
    }
}
