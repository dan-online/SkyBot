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

package ml.duncte123.skybot.utils;

import com.mongodb.client.model.Filters;
import ml.duncte123.skybot.objects.ConsoleUser;
import ml.duncte123.skybot.objects.FakeUser;
import ml.duncte123.skybot.objects.api.BanObject;
import ml.duncte123.skybot.objects.api.GuildSettings;
import ml.duncte123.skybot.objects.api.Warning;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

public class ModerationUtils {

    private static Logger logger = LoggerFactory.getLogger(ModerationUtils.class);

    /**
     * This will send a message to a channel called modlog
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param time         How long it takes for the punishment to get removed
     * @param g            A instance of the {@link Guild}
     */
    @SuppressWarnings("ConstantConditions")
    public static void modLog(User mod, User punishedUser, String punishment, String reason, String time, Guild g) {
        String chan = GuildSettingsUtils.getGuild(g).getLogChannel();
        if (chan != null && !chan.isEmpty()) {
            TextChannel logChannel = AirUtils.getLogChannel(chan, g);
            String length = "";
            if (time != null && !time.isEmpty()) {
                length = " lasting " + time + "";
            }

            MessageUtils.sendMsg(logChannel, String.format("User **%#s** got **%s** by **%#s**%s%s",
                    punishedUser,
                    punishment,
                    mod,
                    length,
                    reason.isEmpty() ? "" : " with reason _\"" + reason + "\"_"
            ));
        }
    }

    /**
     * A version of {@link #modLog(User, User, String, String, String, Guild)} but without the time
     *
     * @param mod          The mod that performed the punishment
     * @param punishedUser The user that got punished
     * @param punishment   The type of punishment
     * @param reason       The reason of the punishment
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User punishedUser, String punishment, String reason, Guild g) {
        modLog(mod, punishedUser, punishment, reason, "", g);
    }

    /**
     * To log a unban or a unmute
     *
     * @param mod          The mod that permed the executeCommand
     * @param unbannedUser The user that the executeCommand is for
     * @param punishment   The type of punishment that got removed
     * @param g            A instance of the {@link Guild}
     */
    public static void modLog(User mod, User unbannedUser, String punishment, Guild g) {
        modLog(mod, unbannedUser, punishment, "", g);
    }

    /**
     * Add the banned user to the database
     *
     * @param modID             The user id from the mod
     * @param userName          The username from the banned user
     * @param userDiscriminator the discriminator from the user
     * @param userId            the id from the banned users
     * @param unbanDate         When we need to unban the user
     * @param guildId           What guild the user got banned in
     */
    public static void addBannedUserToDb(long modID, String userName, String userDiscriminator, long userId, OffsetDateTime unbanDate, long guildId) {

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            AirUtils.MONGO_ASYNC_BANS.insertOne(session, new BanObject(OffsetDateTime.now(), unbanDate, userName, modID, userId, guildId,
                    userDiscriminator), GuildSettingsUtils.DEFAULT_VOID_CALLBACK);

            session.close();
        });
    }

    /**
     * Returns the current amount of warnings that a user has
     *
     * @param u the {@link User User} to check the warnings for
     * @return The current amount of warnings that a user has
     */
    public static int getWarningCountForUser(User u, Guild g) {
        if (u == null)
            throw new IllegalArgumentException("User to check can not be null");

        return ApiUtils.getWarnsForUser(u.getId(), g.getId()).getWarnings().size();
    }

    /**
     * This attempts to register a warning in the database
     *
     * @param moderator The mod that executed the warning
     * @param target    The user to warn
     * @param reason    the reason for the warn
     * @param jda       a jda instance because we need the token for auth
     */
    public static void addWarningToDb(User moderator, User target, String reason, Guild guild, JDA jda) {

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionExxception) -> {
            if (sessionExxception != null) {
                sessionExxception.printStackTrace();
                return;
            }

            OffsetDateTime now = OffsetDateTime.now();

            AirUtils.MONGO_ASYNC_WARNINGS.insertOne(session, new Warning(reason, now, moderator.getIdLong(), now.plusDays(3), target.getIdLong(),
                    guild.getIdLong()), GuildSettingsUtils.DEFAULT_VOID_CALLBACK);

            session.close();
        });
    }

    /**
     * This will check if there are users that can be unbanned
     *
     * @param shardManager the current shard manager for this bot
     */
    public static void checkUnbans(ShardManager shardManager) {

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                logger.error("Aborting! Sessions are denied by the database.", sessionException);
                return;
            }

            AirUtils.MONGO_ASYNC_BANS.find(session, Filters.lt("unban_date", OffsetDateTime.now().toEpochSecond())).forEach((ban) -> {
                logger.debug("Unbanning {}", ban.getUsername());
                Guild guild = shardManager.getGuildById(ban.getGuildId());
                if (guild != null) {
                    String userId = Long.toUnsignedString(ban.getUserId());
                    guild.getController().unban(userId).queue();
                    modLog(new ConsoleUser(), new FakeUser(ban.getUsername(), userId, ban.getDiscriminator()), "unbanned", guild);
                }
            }, GuildSettingsUtils.DEFAULT_VOID_CALLBACK);
            AirUtils.MONGO_ASYNC_BANS.deleteMany(session, Filters.lt("unban_date", OffsetDateTime.now().toEpochSecond()), ((result, t) -> {
                if (t != null) {
                    t.printStackTrace();
                }
                if (result != null)
                    logger.debug("Checking done, unbanned {} users.", result.getDeletedCount());
            }));

            session.close();
        });
    }

    public static void muteUser(Guild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute) {
        muteUser(guild, member, channel, cause, minutesUntilUnMute, false);
    }

    public static void muteUser(Guild guild, Member member, TextChannel channel, String cause, long minutesUntilUnMute, boolean sendMessages) {
        Member self = guild.getSelfMember();
        GuildSettings guildSettings = GuildSettingsUtils.getGuild(guild);
        long muteRoleId = guildSettings.getMuteRoleIdLong();

        if (muteRoleId == -1) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "The role for the punished people is not configured. Please set it up." +
                    "We disabled your spam filter until you have set up a role.");

            guildSettings.setSpamFilterState(false);
            return;
        }

        Role muteRole = guild.getRoleById(muteRoleId);

        if (muteRole == null) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "The role for the punished people is inexistent.");
            return;
        }

        if (!self.hasPermission(Permission.MANAGE_ROLES)) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "I don't have permissions for muting a person. Please give me role managing permissions.");
            return;
        }

        if (!self.canInteract(member) || !self.canInteract(muteRole)) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "I can not access either the member or the role.");
            return;
        }
        String reason = String.format("The member %#s was muted for %s until %d", member.getUser(), cause, minutesUntilUnMute);
        guild.getController().addSingleRoleToMember(member, muteRole).reason(reason).queue(
                (success) -> {
                    guild.getController().removeSingleRoleFromMember(member, muteRole).reason("Scheduled un-mute")
                            .queueAfter(minutesUntilUnMute, TimeUnit.MINUTES);
                },
                (failure) -> {
                    long chan = GuildSettingsUtils.getGuild(guild).getLogChannelLong();
                    if (chan != -1) {
                        TextChannel logChannel = AirUtils.getLogChannel(chan, guild);

                        String message = String.format("%#s bypassed the mute.", member.getUser());

                        if(sendMessages)
                            MessageUtils.sendEmbed(logChannel, EmbedUtils.embedMessage(message));
                    }
                });
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause) {
        kickUser(guild, member, channel, cause, false);
    }

    public static void kickUser(Guild guild, Member member, TextChannel channel, String cause, boolean sendMessages) {
        Member self = guild.getSelfMember();

        if (!self.hasPermission(Permission.KICK_MEMBERS)) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "I don't have permissions for kicking a person. Please give me kick members permissions.");
            return;
        }

        if (!self.canInteract(member)) {
            if(sendMessages)
                MessageUtils.sendMsg(channel, "I can not access the member.");
            return;
        }
        String reason = String.format("The member %#s was kicked for %s.", member.getUser(), cause);
        guild.getController().kick(member).reason(reason).queue();
    }
}
