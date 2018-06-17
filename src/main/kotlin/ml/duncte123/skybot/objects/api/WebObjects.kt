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

import ml.duncte123.skybot.Settings
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties


data class LlamaObject(val file: String = "") : DBObject()

data class KpopObject(val name: String = "", val band: String = "", val image: String = "") : DBObject()

data class WarnObject(val userId: String, val warnings: List<Warning>) : DBObject()
data class Warning(val reason: String = "",
                   val date: OffsetDateTime = OffsetDateTime.now(),
                   val modId: Long = -1L,
                   val expiryDate: OffsetDateTime = OffsetDateTime.now(),
                   val userId: Long = -1L,
                   val guildId: Long = -1L) : DBObject()

data class BanObject(
        val ban_date: OffsetDateTime? = null,
        val unban_date: OffsetDateTime? = null,
        val Username: String = "",
        val modUserId: Long = -1,
        val userId: Long = -1,
        val guildId: Long = -1,
        val discriminator: String = "") : DBObject()

public data class GuildSettings(val guildId: Long) {

    /**
     * This will return the guild id that these options are for
     *
     * @return The id of that guild as a Long
     */
    var enableJoinMessage = false
        private set
    var enableSwearFilter = false
        private set
    var customJoinMessage = "Welcome {{USER_MENTION}}, to the official **{{GUILD_NAME}}** guild."
        private set
    var customLeaveMessage = "**{{USER_NAME}}** has left **{{GUILD_NAME}}** :worried:"
        private set
    var customPrefix = Settings.PREFIX
        private set
    /**
     * Returns the channel to log in
     *
     * @return the channel to log in
     */
    var logChannelLong: Long = -1
        private set
    /**
     * Returns the channel in where the welcome or leave messages should display
     *
     * @return the channel in where the welcome or leave messages should display
     */
    var welcomeLeaveChannelLong: Long = -1
        private set
    /**
     * Returns the role id for the autorole feature
     *
     * @return the role id for the autorole feature
     */
    var autoroleRoleLong: Long = -1
        private set
    var serverDesc = ""
        private set
    var announceTracks = false
        private set
    var autoDeHoist = false
        private set
    var filterInvites = false
        private set
    var spamFilterState = false
        private set
    var muteRoleIdLong: Long = -1
        private set
    var ratelimits = longArrayOf(20, 45, 60, 120, 240, 2400)
        private set
    var kickInstead = false
        private set

    /**
     * Returns the channel to log in
     *
     * @return the channel to log in
     */
    val logChannel: String
        get() = logChannelLong.toString()

    /**
     * Returns the role id for the autorole feature
     *
     * @return the role id for the autorole feature
     */
    val autoroleRole: String
        get() = java.lang.Long.toUnsignedString(autoroleRoleLong)

    val isAutoroleEnabled: Boolean
        get() = this.autoroleRoleLong != -1L

    /**
     * Returns the channel in where the welcome or leave messages should display
     *
     * @return the channel in where the welcome or leave messages should display
     */
    val welcomeLeaveChannel: String
        get() = java.lang.Long.toUnsignedString(welcomeLeaveChannelLong)

    val muteRoleId: String
        get() = java.lang.Long.toUnsignedString(muteRoleIdLong)

    val rateLimitsForTwig: LongArray
        get() {
            val temp = longArrayOf()
            for (i in ratelimits.indices)
                temp[i] = ratelimits[i]
            return temp
        }

    /**
     * This will init everything
     *
     * @param guildId the id of the guild that the settings are for
     */

    /**
     * this will check if the join message is enabled
     *
     * @return true if the join message is enabled
     */
    fun isEnableJoinMessage(): Boolean {
        return enableJoinMessage
    }

    /**
     * We use this to update if the join message should display
     *
     * @param enableJoinMessage whether we should display the join message
     * @return The current [GuildSettings]
     */
    fun setEnableJoinMessage(enableJoinMessage: Boolean): GuildSettings {
        this.enableJoinMessage = enableJoinMessage
        return this
    }

    /**
     * This will check if the swear filter is enabled
     *
     * @return true if the filter is on for this guild
     */
    fun isEnableSwearFilter(): Boolean {
        return enableSwearFilter
    }

    /**
     * We use this to update if we should block swearwords
     *
     * @param enableSwearFilter whether we should block swearing
     * @return The current [GuildSettings]
     */
    fun setEnableSwearFilter(enableSwearFilter: Boolean): GuildSettings {
        this.enableSwearFilter = enableSwearFilter
        return this
    }

    /**
     * This will set the custom join for the corresponding guild
     *
     * @param customJoinMessage The new join message
     * @return The current [GuildSettings]
     */
    fun setCustomJoinMessage(customJoinMessage: String): GuildSettings {
        this.customJoinMessage = customJoinMessage
        return this
    }

    /**
     * This will set the custom leave message for the corresponding guild
     *
     * @param customLeaveMessage The new leave message
     * @return The current [GuildSettings]
     */
    fun setCustomLeaveMessage(customLeaveMessage: String): GuildSettings {
        this.customLeaveMessage = customLeaveMessage
        return this
    }


    /**
     * This will set the custom prefix for the corresponding guild
     *
     * @param customPrefix The new prefix
     * @return The current [GuildSettings]
     */
    fun setCustomPrefix(customPrefix: String): GuildSettings {
        this.customPrefix = customPrefix
        return this
    }

    /**
     * This will set the channel that we log all the mod stuff in
     *
     * @param tc the channel to log
     * @return the current [GuildSettings]
     */
    fun setLogChannel(tc: String): GuildSettings {
        this.logChannelLong = java.lang.Long.parseLong(tc)
        return this
    }

    fun setLogChannel(tc: Long): GuildSettings {
        this.logChannelLong = tc
        return this
    }

    /**
     * This sets the role id for the autorole
     *
     * @param autoroleRole the role to set the autorole to
     * @return the current [GuildSettings]
     */
    fun setAutoroleRole(autoroleRole: String): GuildSettings {
        this.autoroleRoleLong = java.lang.Long.parseLong(autoroleRole)
        return this
    }

    fun setAutoroleRole(autoroleRole: Long): GuildSettings {
        this.autoroleRoleLong = autoroleRole
        return this
    }

    /**
     * This sets the channel in where the welcome or leave messages should display
     *
     * @param welcomeLeaveChannel the channel in where the welcome or leave messages should display
     * @return the current [GuildSettings]
     */
    fun setWelcomeLeaveChannel(welcomeLeaveChannel: String): GuildSettings {
        this.welcomeLeaveChannelLong = java.lang.Long.parseLong(welcomeLeaveChannel)
        return this
    }

    /**
     * This sets the channel in where the welcome or leave messages should display
     *
     * @param welcomeLeaveChannel the channel in where the welcome or leave messages should display
     * @return the current [GuildSettings]
     */
    fun setWelcomeLeaveChannel(welcomeLeaveChannel: Long): GuildSettings {
        this.welcomeLeaveChannelLong = welcomeLeaveChannel
        return this
    }

    /**
     * Sets the current sever description to show up in DB!guildinfo
     *
     * @param serverDesc the custom server description
     * @return the current [GuildSettings]
     */
    fun setServerDesc(serverDesc: String): GuildSettings {
        this.serverDesc = serverDesc
        return this
    }

    /**
     * Returns if we should announce the next track
     *
     * @return if we should announce the next track
     */
    fun isAnnounceTracks(): Boolean {
        return announceTracks
    }

    /**
     * Sets if the audio player should announce the tracks
     *
     * @param announceTracks true to announce tracks
     * @return the current [GuildSettings]
     */
    fun setAnnounceTracks(announceTracks: Boolean): GuildSettings {
        this.announceTracks = announceTracks
        return this
    }

    /**
     * Returns if we should auto de-hoist people (soon™)
     *
     * @return if we should auto de-hoist people (soon™)
     */
    fun isAutoDeHoist(): Boolean {
        return autoDeHoist
    }

    /**
     * This sets if we should auto de-hoist people
     *
     * @param autoDeHoist if we should auto de-hoist people
     * @return the current [GuildSettings]
     */
    fun setAutoDeHoist(autoDeHoist: Boolean): GuildSettings {
        this.autoDeHoist = autoDeHoist
        return this
    }

    /**
     * Returns if we should filter discord invites
     *
     * @return if we should filter discord invites
     */
    fun isFilterInvites(): Boolean {
        return filterInvites
    }

    /**
     * @param filterInvites Sets if we should filter out invites in messages
     * @return boolean weather invites are filtered
     */
    fun setFilterInvites(filterInvites: Boolean): GuildSettings {
        this.filterInvites = filterInvites
        return this
    }

    fun setSpamFilterState(newState: Boolean): GuildSettings {
        spamFilterState = newState
        return this
    }

    fun setMuteRoleId(muteRoleId: String): GuildSettings {
        this.muteRoleIdLong = java.lang.Long.parseLong(muteRoleId)
        return this
    }

    fun setMuteRoleId(muteRoleId: Long): GuildSettings {
        this.muteRoleIdLong = muteRoleId
        return this
    }

    fun setRatelimits(ratelimits: LongArray): GuildSettings {
        this.ratelimits = ratelimits
        return this
    }

    fun getKickState(): Boolean {
        return kickInstead
    }

    fun setKickState(newState: Boolean): GuildSettings {
        kickInstead = newState
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun toString(): String {
        return String.format("GuildSettings[%s](prefix=%s, Swearword filter=%s, autorole id=%s, spam filter=%s)", guildId, customPrefix,
                if (enableSwearFilter) "Enabled" else "Disabled", autoroleRoleLong, if (spamFilterState) "Enabled" else "Disabled")
    }
}


open class DBObject {
    fun toJson(): JSONObject {
        val json = JSONObject()

        this::class.memberProperties.forEach {
            if (it.visibility == KVisibility.PUBLIC) {
                json.put(it.name, it.getter.call(this))
            }
        }

        return json
    }
}