package ml.duncte123.skybot.objects.guild;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;
import java.util.List;

public class GuildSettingsCodecImpl implements Codec<GuildSettings> {
    @Override
    public GuildSettings decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        reader.readObjectId("_id");

        GuildSettings setting = new GuildSettings(reader.readInt64("guildId"))
                .setCustomPrefix(reader.readString("prefix"))
                .setAutoroleRole(reader.readInt64("autoRole"))
                .setMuteRoleId(reader.readInt64("muteRoleId"));

        List<Long> rates = new ArrayList<>(6);

        reader.readStartArray();

        for (int i = 0; i < 6; i++) {
            rates.add(reader.readInt64());
        }

        reader.readEndArray();

        setting.setRatelimits(rates.stream().mapToLong((l) -> l).toArray())
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
                .setCustomJoinMessage(reader.readString("customWelcomeMessage"));

        reader.readEndDocument();

        return setting;
    }

    @Override
    public void encode(BsonWriter writer, GuildSettings value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeInt64("guildId", value.getGuildIdLong());
        writer.writeString("prefix", value.getCustomPrefix());
        writer.writeInt64("autoRole", value.getAutoroleRoleLong());
        writer.writeInt64("muteRoleId", value.getMuteRoleIdLong());
        writer.writeStartArray("ratelimits");
        for (final long limit : value.getRatelimits()) {
            writer.writeInt64(limit);
        }
        writer.writeEndArray();
        writer.writeBoolean("autoDehoist", value.isAutoDeHoist());
        writer.writeBoolean("joinMessage", value.isEnableJoinMessage());
        writer.writeBoolean("swearFilter", value.isEnableSwearFilter());
        writer.writeInt64("logChannelId", value.getLogChannelLong());
        writer.writeBoolean("filterInvites", value.isFilterInvites());
        writer.writeBoolean("spamFilterState", value.getSpamFilterState());
        writer.writeBoolean("kickInsteadState", value.getKickState());
        writer.writeBoolean("announceNextTrack", value.isAnnounceTracks());
        writer.writeString("serverDescription", value.getServerDesc());
        writer.writeString("customLeaveMessage", value.getCustomLeaveMessage());
        writer.writeInt64("welcomeLeaveChannel", value.getWelcomeLeaveChannelLong());
        writer.writeString("customWelcomeMessage", value.getCustomJoinMessage());
        writer.writeEndDocument();
    }

    @Override
    public Class<GuildSettings> getEncoderClass() {
        return GuildSettings.class;
    }
}
