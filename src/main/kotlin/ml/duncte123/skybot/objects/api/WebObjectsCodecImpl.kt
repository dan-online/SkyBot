/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ml.duncte123.skybot.objects.api

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.time.OffsetDateTime
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class BanObjectCodecImpl : Codec<BanObject> {
    private var index = 1
    override fun getEncoderClass(): Class<BanObject> = BanObject::class.java
    override fun encode(writer: BsonWriter, value: BanObject, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        value::class.memberProperties.forEach {
            val itsValue = it.getter.call(this)
            when (itsValue) {
                is String -> writer.writeString(it.name, itsValue)
                is Long -> writer.writeInt64(it.name, itsValue)
                is Int -> writer.writeInt32(it.name, itsValue)
                is Boolean -> writer.writeBoolean(it.name, itsValue)
                is Double -> writer.writeDouble(it.name, itsValue)
                is OffsetDateTime -> writer.writeDateTime(it.name, itsValue.toEpochSecond())
            }
        }
        writer.writeEndDocument()
    }
    @Suppress("UNCHECKED_CAST")
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): BanObject {
        val apiObject = BanObject()
        try {
            reader.readStartDocument()
            reader.readObjectId()
            var type = reader.readBsonType()
            while (type != BsonType.END_OF_DOCUMENT) {
                val property: KProperty1<out DBObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                val field = property?.javaField
                if (field != null && !field.isAccessible) {
                    field.isAccessible = true
                }
                when (type) {
                    BsonType.STRING -> {
                        field?.set(apiObject, reader.readString())
                    }
                    BsonType.BOOLEAN -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.INT64 -> {
                        field?.set(apiObject, reader.readInt64())
                    }
                    BsonType.INT32 -> {
                        field?.set(apiObject, reader.readInt32())
                    }
                    BsonType.DOUBLE -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.DATE_TIME -> {
                        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                        gmt.timeInMillis = reader.readDateTime()
                        field?.set(apiObject, OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId()))
                    }
                    BsonType.NULL -> {
                        field?.set(apiObject, reader.readNull())
                    }
                    else -> {
                    }
                }
                type = reader.readBsonType()
            }
            reader.readEndDocument()
        } catch (ex: Exception) {
            ex.printStackTrace()
            System.err.println("\n=========\nAt index $index!\n==========\n")
        }
        index += 1
        return apiObject
    }
}
class LlamaObjectCodecImpl : Codec<LlamaObject> {
    private var index = 1
    override fun getEncoderClass(): Class<LlamaObject> = LlamaObject::class.java
    override fun encode(writer: BsonWriter, value: LlamaObject, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        value::class.memberProperties.forEach {
            val itsValue = it.getter.call(this)
            when (itsValue) {
                is String -> writer.writeString(it.name, itsValue)
                is Long -> writer.writeInt64(it.name, itsValue)
                is Int -> writer.writeInt32(it.name, itsValue)
                is Boolean -> writer.writeBoolean(it.name, itsValue)
                is Double -> writer.writeDouble(it.name, itsValue)
                is OffsetDateTime -> writer.writeDateTime(it.name, itsValue.toEpochSecond())
            }
        }
        writer.writeEndDocument()
    }
    @Suppress("UNCHECKED_CAST")
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): LlamaObject {
        val apiObject = LlamaObject()
        try {
            reader.readStartDocument()
            reader.readObjectId()
            var type = reader.readBsonType()
            while (type != BsonType.END_OF_DOCUMENT) {
                val property: KProperty1<out DBObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                val field = property?.javaField
                if (field != null && !field.isAccessible) {
                    field.isAccessible = true
                }
                when (type) {
                    BsonType.STRING -> {
                        field?.set(apiObject, reader.readString())
                    }
                    BsonType.BOOLEAN -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.INT64 -> {
                        field?.set(apiObject, reader.readInt64())
                    }
                    BsonType.INT32 -> {
                        field?.set(apiObject, reader.readInt32())
                    }
                    BsonType.DOUBLE -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.DATE_TIME -> {
                        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                        gmt.timeInMillis = reader.readDateTime()
                        field?.set(apiObject, OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId()))
                    }
                    BsonType.NULL -> {
                        field?.set(apiObject, reader.readNull())
                    }
                    else -> {
                    }
                }
                type = reader.readBsonType()
            }

            reader.readEndDocument()
        } catch (ex: Exception) {
            ex.printStackTrace()
            System.err.println("\n=========\nAt index $index!\n==========\n")
        }
        index += 1
        return apiObject
    }
}
class WarningCodecImpl : Codec<Warning> {
    private var index = 1
    override fun getEncoderClass(): Class<Warning> = Warning::class.java
    override fun encode(writer: BsonWriter, value: Warning, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        value::class.memberProperties.forEach {
            val itsValue = it.getter.call(this)
            when (itsValue) {
                is String -> writer.writeString(it.name, itsValue)
                is Long -> writer.writeInt64(it.name, itsValue)
                is Int -> writer.writeInt32(it.name, itsValue)
                is Boolean -> writer.writeBoolean(it.name, itsValue)
                is Double -> writer.writeDouble(it.name, itsValue)
                is OffsetDateTime -> writer.writeDateTime(it.name, itsValue.toEpochSecond())
            }
        }
        writer.writeEndDocument()
    }
    @Suppress("UNCHECKED_CAST")
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Warning {
        val apiObject = Warning()
        try {
            reader.readStartDocument()
            reader.readObjectId()
            var type = reader.readBsonType()
            while (type != BsonType.END_OF_DOCUMENT) {
                val property: KProperty1<out DBObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                val field = property?.javaField
                if (field != null && !field.isAccessible) {
                    field.isAccessible = true
                }
                when (type) {
                    BsonType.STRING -> {
                        field?.set(apiObject, reader.readString())
                    }
                    BsonType.BOOLEAN -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.INT64 -> {
                        field?.set(apiObject, reader.readInt64())
                    }
                    BsonType.INT32 -> {
                        field?.set(apiObject, reader.readInt32())
                    }
                    BsonType.DOUBLE -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.DATE_TIME -> {
                        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                        gmt.timeInMillis = reader.readDateTime()
                        field?.set(apiObject, OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId()))
                    }
                    BsonType.NULL -> {
                        field?.set(apiObject, reader.readNull())
                    }
                    else -> {
                    }
                }
                type = reader.readBsonType()
            }
            reader.readEndDocument()
        } catch (ex: Exception) {
            ex.printStackTrace()
            System.err.println("\n=========\nAt index $index!\n==========\n")
        }
        index += 1
        return apiObject
    }
}
class KpopCodecImpl : Codec<KpopObject> {
    private var index = 1
    override fun getEncoderClass(): Class<KpopObject> = KpopObject::class.java
    override fun encode(writer: BsonWriter, value: KpopObject, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        value::class.memberProperties.forEach {
            val itsValue = it.getter.call(this)
            when (itsValue) {
                is String -> writer.writeString(it.name, itsValue)
                is Long -> writer.writeInt64(it.name, itsValue)
                is Int -> writer.writeInt32(it.name, itsValue)
                is Boolean -> writer.writeBoolean(it.name, itsValue)
                is Double -> writer.writeDouble(it.name, itsValue)
                is OffsetDateTime -> writer.writeDateTime(it.name, itsValue.toEpochSecond())
            }
        }
        writer.writeEndDocument()
    }
    @Suppress("UNCHECKED_CAST")
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): KpopObject {
        val apiObject = KpopObject()
        try {
            reader.readStartDocument()
            reader.readObjectId()
            var type = reader.readBsonType()
            while (type != BsonType.END_OF_DOCUMENT) {
                val property: KProperty1<out DBObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                val field = property?.javaField
                if (field != null && !field.isAccessible) {
                    field.isAccessible = true
                }
                when (type) {
                    BsonType.STRING -> {
                        field?.set(apiObject, reader.readString())
                    }
                    BsonType.BOOLEAN -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.INT64 -> {
                        field?.set(apiObject, reader.readInt64())
                    }
                    BsonType.INT32 -> {
                        field?.set(apiObject, reader.readInt32())
                    }
                    BsonType.DOUBLE -> {
                        field?.set(apiObject, reader.readBoolean())
                    }
                    BsonType.DATE_TIME -> {
                        val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                        gmt.timeInMillis = reader.readDateTime()
                        field?.set(apiObject, OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId()))
                    }
                    BsonType.NULL -> {
                        field?.set(apiObject, reader.readNull())
                    }
                    else -> {
                    }
                }
                type = reader.readBsonType()
            }

            reader.readEndDocument()
        } catch (ex: Exception) {
            ex.printStackTrace()

            System.err.println("\n=========\nAt index $index!\n==========\n")
        }

        index += 1

        return apiObject
    }
}