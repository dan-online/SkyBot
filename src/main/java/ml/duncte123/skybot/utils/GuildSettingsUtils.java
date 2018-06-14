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

import com.mongodb.MongoCommandException;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import ml.duncte123.skybot.objects.guild.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);
    public static final SingleResultCallback<Void> DEFAULT_VOID_CALLBACK = (aVoid, exception) -> {
        if (exception!= null) {
            if (exception instanceof MongoCommandException) {
                MongoCommandException mongoCommandException = (MongoCommandException) exception;
                if (mongoCommandException.getErrorCode() != 8000) {
                    mongoCommandException.printStackTrace();
                }
            } else {
                exception.printStackTrace();
            }
        }
        else
            logger.info("DB statement performed.");
    };
    public static final SingleResultCallback<?> DEFAULT_OBJECT_CALLBACK = (object, exception) -> {
        if (exception!= null)
            exception.printStackTrace();
        else if (object != null)
            logger.info(String.format("DB statement performed with result %s.", object.toString()));
        else
            logger.info("DB statement performed without any result.");
    };

    /**
     * This runs both {@link #loadGuildSettings()} and {@link #loadFooterQuotes()}
     */
    public static void loadAllSettings() {
        loadGuildSettings();
        loadFooterQuotes();
    }

    /**
     * This will load all the footer quotes from the database and store them in the {@link  EmbedUtils#footerQuotes}
     */
    private static void loadFooterQuotes() {
        if (!AirUtils.NONE_SQLITE) return;
        logger.debug("Loading footer quotes");

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                logger.error("Aborting! Sessions are denied by the database.", sessionException);
                System.exit(-2);
            }

            AirUtils.MONGO_QUOTES.find(session)
                    .forEach((quote) -> EmbedUtils.footerQuotes.put(quote.getString("quote"), quote.getString("name")), DEFAULT_VOID_CALLBACK);

            session.close();
        });
    }

    /**
     * This will get the settings from our database and store them in the {@link AirUtils#guildSettings settings}
     */
    private static void loadGuildSettings() {
        logger.debug("Loading Guild settings.");

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                logger.error("Aborting! Sessions are denied by the database.", sessionException);
                System.exit(-2);
            }

            AirUtils.MONGO_GUILDSETTINGS.find(session)
                    .forEach((guildSetting) -> AirUtils.guildSettings.put(guildSetting.getGuildId(), guildSetting), DEFAULT_VOID_CALLBACK);

            session.close();
        });
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild the guild to get
     * @return the guild
     */
    public static GuildSettings getGuild(Guild guild) {

        if (!AirUtils.guildSettings.containsKey(guild.getId())) {
            return registerNewGuild(guild);
        }

        return AirUtils.guildSettings.get(guild.getId());

    }

    /**
     * This will save the settings into the database when the guild owner/admin updates it
     *
     * @param guild    The guild to update it for
     * @param settings the new settings
     */
    public static void updateGuildSettings(Guild guild, GuildSettings settings) {
        if (!AirUtils.guildSettings.containsKey(settings.getGuildId())) {
            registerNewGuild(guild);
            return;
        }
        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                sessionException.printStackTrace();
            }

            MongoCollection<GuildSettings> settingsCollection = AirUtils.MONGO_GUILDSETTINGS;
            settingsCollection.deleteOne(session, new Document("guildId", guild.getIdLong()), (SingleResultCallback<DeleteResult>) DEFAULT_OBJECT_CALLBACK);
            settingsCollection.insertOne(session, settings, DEFAULT_VOID_CALLBACK);

            session.close();
        });
    }

    /**
     * This will register a new guild with their settings on bot join
     *
     * @param g The guild that we are joining
     * @return The new guild
     */
    public static GuildSettings registerNewGuild(Guild g) {
        if (AirUtils.guildSettings.containsKey(g.getId())) {
            return AirUtils.guildSettings.get(g.getId());
        }
        GuildSettings newGuildSettings = new GuildSettings(g.getId());

        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                sessionException.printStackTrace();
            }

            MongoCollection<GuildSettings> settingsCollection = AirUtils.MONGO_GUILDSETTINGS;
            settingsCollection.insertOne(session, newGuildSettings, DEFAULT_VOID_CALLBACK);

            session.close();
        });

        return newGuildSettings;
    }

    /**
     * This will attempt to remove a guild wen we leave it
     *
     * @param g the guild to remove from the database
     */
    public static void deleteGuild(Guild g) {
        AirUtils.guildSettings.remove(g.getId());
        AirUtils.MONGO_ASYNC_CLIENT.startSession((session, sessionException) -> {
            if (sessionException != null) {
                sessionException.printStackTrace();
            }

            MongoCollection<GuildSettings> settingsCollection = AirUtils.MONGO_GUILDSETTINGS;
            settingsCollection.deleteOne(session, new Document("guildId", g.getIdLong()), (SingleResultCallback<DeleteResult>) DEFAULT_OBJECT_CALLBACK);

            session.close();
        });
    }

    private static String replaceNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\\\n", "\n");
    }

    private static String fixNewLines(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\n", "\\\\n");
    }

    private static String replaceUnicode(String entery) {
        if (entery == null || entery.isEmpty())
            return null;
        return entery.replaceAll("\\P{Print}", "");
    }

    /*private static String replaceUnicodeAndLines(String s) {
        return replaceUnicode(replaceNewLines(s));
    }*/

    private static String fixUnicodeAndLines(String s) {
        return replaceUnicode(fixNewLines(replaceNewLines(s)));
    }

    private static long[] convertS2J(String in) {
        if (in.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};
        return Arrays.stream(in.split("\\|")).mapToLong(Long::valueOf).toArray();
    }

    public static long[] ratelimmitChecks(String fromDb) {
        if (fromDb == null || fromDb.isEmpty())
            return new long[]{20, 45, 60, 120, 240, 2400};

        return convertS2J(fromDb.replaceAll("\\P{Print}", ""));
    }
}
