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

package ml.duncte123.skybot;

import com.mongodb.Block;
import com.mongodb.client.model.Filters;
import ml.duncte123.skybot.exceptions.VRCubeException;
import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.objects.command.custom.CustomCommand;
import ml.duncte123.skybot.objects.command.custom.CustomCommandCodecImpl;
import ml.duncte123.skybot.unstable.utils.ComparatingUtils;
import ml.duncte123.skybot.utils.GuildSettingsUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ml.duncte123.skybot.utils.AirUtils.CONFIG;
import static ml.duncte123.skybot.utils.AirUtils.MONGO_CLIENT;

@SuppressWarnings("WeakerAccess")
public class CommandManager {

    /**
     * This stores all our commands
     */
    private final Set<Command> commands = ConcurrentHashMap.newKeySet();
    private final List<Command> commandsSorted = new ArrayList<>();
    private final Set<CustomCommand> customCommands = ConcurrentHashMap.newKeySet();
    private final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    /**
     * This makes sure that all the commands are added
     */
    public CommandManager() {
        //Get reflections for this project
        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.commands"));
        registerCommandsFromReflection(new Reflections("ml.duncte123.skybot.unstable.commands"));

        loadCustomCommands();
    }

    /**
     * This is method to get the commands on request
     *
     * @return A list of all the commands
     */
    public Set<Command> getCommands() {
        return commands;
    }

    public List<Command> getSortedCommands() {
        if(commandsSorted.isEmpty()) {
            List<Command> commandSet = new ArrayList<>();
            List<String> names = getCommands().stream().filter(cmd -> cmd.getCategory() != CommandCategory.UNLISTED)
                    .map(Command::getName).sorted().collect(Collectors.toList());
            names.forEach(n -> commandSet.add(getCommand(n)));
            commandsSorted.addAll(commandSet);
        }
        return commandsSorted;
    }

    public Set<CustomCommand> getCustomCommands() {
        return customCommands;
    }

    /**
     * This tries to get a command with the provided name/alias
     *
     * @param name the name of the command
     * @return a possible null command for the name
     */
    public Command getCommand(String name) {
        Optional<Command> cmd = commands.stream().filter(c -> c.getName().equals(name)).findFirst();

        if (!cmd.isPresent()) {
            cmd = commands.stream().filter(c -> Arrays.asList(c.getAliases()).contains(name)).findFirst();
        }

        return cmd.orElse(null);
    }

    public List<Command> getCommands(CommandCategory category) {
        return commands.stream().filter(c -> c.getCategory().equals(category)).collect(Collectors.toList());
    }


    public CustomCommand getCustomCommand(String invoke, String guildId) {
        return customCommands.stream().filter(c -> c.getGuildId().equals(guildId))
                .filter(c -> c.getName().equalsIgnoreCase(invoke)).findFirst().orElse(null);
    }

    public void editCustomCommand(GuildMessageReceivedEvent event, CustomCommand c) {
        addCustomCommand(event, c, true, true);
    }

    public void addCustomCommand(GuildMessageReceivedEvent event, CustomCommand c) {
        addCustomCommand(event, c, true, false);
    }

    public void addCustomCommand(GuildMessageReceivedEvent event, CustomCommand command, boolean insertInDb, boolean isEdit) {
        Message message = (event == null) ? null : event.getMessage();
        if (command.getName().contains(" ")) {
            throw new VRCubeException("Name can't have spaces!");
        }

        boolean commandFound = this.customCommands.stream()
                .anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()) && cmd.getGuildId().equals(command.getGuildId())) && !isEdit;
        boolean limitReached = this.customCommands.stream().filter((cmd) -> cmd.getGuildId().equals(command.getGuildId())).count() >= 50 && !isEdit;

        if (commandFound || limitReached) {
            if (message != null)
                MessageUtils.sendErrorWithMessage(message, "Either the command was already added, limit reached or an database " +
                    "error appeared.\n" +
                    "Try to contact the developers if you spot an database error.");
        }


        if (insertInDb) {
            if (isEdit) {
                MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("customcommands",
            CustomCommand.class)
            .withCodecRegistry(CodecRegistries.fromCodecs(new CustomCommandCodecImpl())).updateOne(
                    Filters.and(
                            Filters.eq("invoke", command.getName()),
                            Filters.eq("guildId", Long.parseLong(command.getGuildId()))),
                    new Document("message", command.getMessage()));
                this.customCommands.remove(getCustomCommand(command.getName(), command.getGuildId()));
                this.customCommands.add(command);
                if (message != null)
                    MessageUtils.sendSuccess(message);
            } else {
                MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("customcommands",
            CustomCommand.class)
            .withCodecRegistry(CodecRegistries.fromCodecs(new CustomCommandCodecImpl())).insertOne(command);
                this.customCommands.add(command);
                if (message != null)
                    MessageUtils.sendSuccess(message);
            }
        } else {
            this.customCommands.add(command);
        }
    }

    /**
     * This removes a command from the commands
     *
     * @param command the command to remove
     * @return {@code true} on success
     */
    public boolean removeCommand(String command) {
        return commands.remove(getCommand(command));
    }

    public void removeCustomCommand(GuildMessageReceivedEvent event, String name, String guildId) {
        CustomCommand cmd = getCustomCommand(name, guildId);
        final Message message = event.getMessage();
        if (cmd == null) {
            MessageUtils.sendErrorWithMessage(message, String.format("The command with name %s does not exist here!", name));
        }

        MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("customcommands",
            CustomCommand.class)
            .withCodecRegistry(CodecRegistries.fromCodecs(new CustomCommandCodecImpl())).deleteOne(Filters.and(Filters.eq("invoke", name),
                Filters.eq("guildId", Long.parseLong(guildId))));
        this.customCommands.remove(cmd);
        MessageUtils.sendSuccess(message);
    }

    /**
     * This handles adding the command
     *
     * @param command The command to add
     * @return true if the command is added
     */
    @SuppressWarnings({"UnusedReturnValue", "ConstantConditions"})
    public boolean addCommand(Command command) {
        if (command.getName().contains(" ")) {
            throw new VRCubeException("Name can't have spaces!");
        }

        if (this.commands.stream().anyMatch((cmd) -> cmd.getName().equalsIgnoreCase(command.getName()))) {
            @SinceSkybot(version = "3.52.1")
            List<String> aliases = Arrays.asList(this.commands.stream().filter((cmd) -> cmd.getName()
                    .equalsIgnoreCase(command.getName())).findFirst().get().getAliases());
            for (String alias : command.getAliases()) {
                if (aliases.contains(alias)) {
                    return false;
                }
            }
            return false;
        }
        this.commands.add(command);

        return true;
    }

    /**
     * This will run the command when we need them
     *
     * @param event the event for the message
     */
    public void runCommand(GuildMessageReceivedEvent event) {
        final String[] split = event.getMessage().getContentRaw().replaceFirst(
                "(?i)" + Pattern.quote(Settings.PREFIX) + "|" + Pattern.quote(Settings.OTHER_PREFIX) + "|" +
                        Pattern.quote(GuildSettingsUtils.getGuild(event.getGuild()).getCustomPrefix()),
                "").split("\\s+");
        final String invoke = split[0].toLowerCase();

        dispatchCommand(invoke, Arrays.copyOfRange(split, 1, split.length), event);
    }

    public void dispatchCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {
        Command cmd = getCommand(invoke);
        if (cmd == null) {
            cmd = (Command) getCustomCommand(invoke, event.getGuild().getId());
        }
        dispatchCommand(cmd, invoke, args, event);
    }

    public void dispatchCommand(Command cmd, String invoke, String[] args, GuildMessageReceivedEvent event) {
        if (cmd != null) {
            try {
                cmd.executeCommand(invoke, args, event);
            } catch (Throwable ex) {
                ComparatingUtils.execCheck(ex);
            }
        }
    }

    private void registerCommandsFromReflection(Reflections reflections) {
        //Loop over them commands
        for (Class<? extends Command> cmd : reflections.getSubTypesOf(Command.class)) {
            try {
                //Add the command
                this.addCommand(cmd.getDeclaredConstructor().newInstance());
            } catch (Exception ignored) {
            }
        }
    }

    private void loadCustomCommands() {
        MONGO_CLIENT.getDatabase(CONFIG.getString("mongo.database")).getCollection("customcommands",
            CustomCommand.class)
            .withCodecRegistry(CodecRegistries.fromCodecs(new CustomCommandCodecImpl())).find()
                .forEach((Block<? super CustomCommand>)(command) -> addCustomCommand(null, command, false, false));
    }
}
