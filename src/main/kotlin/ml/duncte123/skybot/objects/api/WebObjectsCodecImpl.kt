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
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

class WarningCodecImpl() : ApiObjectCodecImpl<Warning>(Warning::class.java, Warning())
class KpopCodecImpl() : ApiObjectCodecImpl<KpopObject>(KpopObject::class.java, KpopObject())

open class ApiObjectCodecImpl<T>(private val clazz: Class<T>, private val obj: ApiObject) : Codec<T> {
    override fun getEncoderClass(): Class<T> = clazz

    override fun encode(writer: BsonWriter, value: T, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        this::class.memberProperties.forEach {
            if (it.visibility == KVisibility.PUBLIC) {
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
        }
        writer.writeEndDocument()
    }

    @Suppress("UNCHECKED_CAST")
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): T {
        val apiObject = obj
        reader.readStartDocument()
        reader.readObjectId()
        var type = reader.readBsonType()
        while (type != BsonType.END_OF_DOCUMENT) {
            when (type) {
                BsonType.STRING -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readString())
                }
                BsonType.BOOLEAN -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readBoolean())
                }
                BsonType.INT64 -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readInt64())
                }
                BsonType.INT32 -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readInt32())
                }
                BsonType.DOUBLE -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readBoolean())
                }
                BsonType.DATE_TIME -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    val gmt = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                    gmt.timeInMillis = reader.readDateTime()
                    field?.set(apiObject, OffsetDateTime.ofInstant(gmt.toInstant(), gmt.timeZone.toZoneId()))
                }
                BsonType.NULL -> {
                    val property: KProperty1<out ApiObject, Any?>? = apiObject::class.memberProperties.find { it.name == reader.readName() }
                    val field = property?.javaField
                    field?.isAccessible = true
                    field?.set(apiObject, reader.readNull())
                }
                else -> {
                }
            }
            type = reader.readBsonType()
        }

        reader.readEndDocument()

        return apiObject as T
    }
}