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

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneOptions;
import ml.duncte123.skybot.objects.api.GuildSettings;
import ml.duncte123.skybot.objects.api.GuildSettingsCodecImpl;
import net.dv8tion.jda.core.entities.Guild;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static ml.duncte123.skybot.utils.AirUtils.CONFIG;
import static ml.duncte123.skybot.utils.AirUtils.MONGO_CLIENT;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class GuildSettingsUtils {

    private static final Logger logger = LoggerFactory.getLogger(GuildSettingsUtils.class);

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

        MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("footerquotes").find()
                .forEach((Block<? super Document>) (quote) -> EmbedUtils.footerQuotes.put(quote.getString("quote"), quote.getString("name")));
    }

    /**
     * This will get the settings from our database and store them in the {@link AirUtils#guildSettings settings}
     */
    private static void loadGuildSettings() {
        logger.debug("Loading Guild settings.");

        MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("guildsettings", GuildSettings.class)
                .withCodecRegistry(CodecRegistries.fromCodecs(new GuildSettingsCodecImpl())).find()
                .forEach((Block<? super GuildSettings>)(guildSetting) -> AirUtils.guildSettings.put(guildSetting.getGuildId(), guildSetting));
    }

    /**
     * This wil get a guild or register it if it's not there yet
     *
     * @param guild the guild to get
     * @return the guild
     */
    public static GuildSettings getGuild(Guild guild) {

        if (!AirUtils.guildSettings.containsKey(guild.getIdLong())) {
            return registerNewGuild(guild);
        }

        return AirUtils.guildSettings.get(guild.getIdLong());

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
        MongoCollection<GuildSettings> settingsCollection = MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database"))
                .getCollection("guildsettings", GuildSettings.class).withCodecRegistry(CodecRegistries.fromCodecs(new GuildSettingsCodecImpl()));
        settingsCollection.deleteOne(new Document("guildId", guild.getIdLong()));
        settingsCollection.insertOne(settings);
    }

    /**
     * This will register a new guild with their settings on bot join
     *
     * @param g The guild that we are joining
     * @return The new guild
     */
    public static GuildSettings registerNewGuild(Guild g) {
        if (AirUtils.guildSettings.containsKey(g.getIdLong())) {
            return AirUtils.guildSettings.get(g.getIdLong());
        }
        GuildSettings newGuildSettings = new GuildSettings(g.getIdLong());

        MongoCollection<GuildSettings> settingsCollection = MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database"))
                .getCollection("guildsettings", GuildSettings.class)
                .withCodecRegistry(CodecRegistries.fromCodecs(new GuildSettingsCodecImpl()));
        settingsCollection.insertOne(newGuildSettings, new InsertOneOptions().bypassDocumentValidation(true));

        return newGuildSettings;
    }

    /**
     * This will attempt to remove a guild wen we leave it
     *
     * @param g the guild to remove from the database
     */
    public static void deleteGuild(Guild g) {
        AirUtils.guildSettings.remove(g.getIdLong());

        MongoCollection<GuildSettings> settingsCollection = MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("guildsettings", GuildSettings
                .class)
                .withCodecRegistry(CodecRegistries.fromCodecs(new GuildSettingsCodecImpl()));
        settingsCollection.deleteOne(new Document("guildId", g.getIdLong()));

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
