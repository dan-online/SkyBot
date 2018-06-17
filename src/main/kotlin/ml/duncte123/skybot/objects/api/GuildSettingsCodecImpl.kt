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
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

class GuildSettingsCodecImpl : Codec<GuildSettings> {
    override fun getEncoderClass(): Class<GuildSettings> = GuildSettings::class.java

    override fun encode(writer: BsonWriter, value: GuildSettings, encoderContext: EncoderContext) {
        writer.writeStartDocument()
        writer.writeInt64("guildId", value.guildId)
        writer.writeString("prefix", value.customPrefix)
        writer.writeInt64("autoRole", value.autoroleRoleLong)
        writer.writeInt64("muteRoleId", value.muteRoleIdLong)
        writer.writeStartArray("ratelimits")

        for (limit in value.ratelimits)
            writer.writeInt64(limit)

        writer.writeEndArray()
        writer.writeBoolean("autoDehoist", value.autoDeHoist)
        writer.writeBoolean("joinMessage", value.enableJoinMessage)
        writer.writeBoolean("swearFilter", value.enableSwearFilter)
        writer.writeInt64("logChannelId", value.logChannelLong)
        writer.writeBoolean("filterInvites", value.filterInvites)
        writer.writeBoolean("spamFilterState", value.spamFilterState)
        writer.writeBoolean("kickInsteadState", value.kickInstead)
        writer.writeBoolean("announceNextTrack", value.announceTracks)
        writer.writeString("serverDescription", value.serverDesc)
        writer.writeString("customLeaveMessage", value.customLeaveMessage)
        writer.writeInt64("welcomeLeaveChannel", value.welcomeLeaveChannelLong)
        writer.writeString("customWelcomeMessage", value.customJoinMessage)
        writer.writeEndDocument()
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): GuildSettings {
        reader.readStartDocument()
        reader.readObjectId("_id")

        val setting = GuildSettings(reader.readInt64("guildId"))
                .setCustomPrefix(reader.readString("prefix"))
                .setAutoroleRole(reader.readInt64("autoRole"))
                .setMuteRoleId(reader.readInt64("muteRoleId"))

        val rates = arrayListOf<Long>()

        reader.readStartArray()

        for (i in 0 until 6)
            rates.add(reader.readInt64())

        reader.readEndArray()

        setting.setRatelimits(rates.toLongArray())
        .setAutoDeHoist(reader.readBoolean("autoDehoist"))
                .setEnableJoinMessage(reader.readBoolean("joinMessage"))
                .setEnableSwearFilter(reader.readBoolean("swearFilter"))
                .setLogChannel(reader.readInt64("logChannelId"))
                .setFilterInvites(reader.readBoolean("filterInvites"))
                .setSpamFilterState(reader.readBoolean("spamFilterState"))
                .setKickState(reader.readBoolean("kickInsteadState"))
                .setAnnounceTracks(reader.readBoolean("announceNextTrack"))
                .setServerDesc(reader.readString("serverDescription"))
                .setCustomLeaveMessage(reader.readString("customLeaveMessage"))
                .setWelcomeLeaveChannel(reader.readInt64("welcomeLeaveChannel"))
                .setCustomJoinMessage(reader.readString("customWelcomeMessage"))

        reader.readEndDocument()

        return setting
    }
}
