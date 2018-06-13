package ml.duncte123.skybot.objects.command.custom;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class CustomCommandCodecImpl implements Codec<CustomCommand> {
    @Override
    public CustomCommand decode(BsonReader reader, DecoderContext decoderContext) {

        reader.readStartDocument();
        reader.readObjectId();

        CustomCommand cmd = new CustomCommandImpl(reader.readString("invoke"), reader.readString("message"), reader.readInt64("guildId"));

        reader.readEndDocument();

        return cmd;
    }

    @Override
    public void encode(BsonWriter writer, CustomCommand value, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("invoke", value.getName());
        writer.writeString("message", value.getMessage());
        writer.writeInt64("guildId", value.getGuildIdLong());
        writer.writeEndDocument();
    }

    @Override
    public Class<CustomCommand> getEncoderClass() {
        return CustomCommand.class;
    }
}
