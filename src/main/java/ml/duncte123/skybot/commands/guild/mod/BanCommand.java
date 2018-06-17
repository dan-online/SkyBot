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

package ml.duncte123.skybot.commands.guild.mod;

import ml.duncte123.skybot.objects.command.Command;
import ml.duncte123.skybot.objects.command.CommandCategory;
import ml.duncte123.skybot.utils.AirUtils;
import ml.duncte123.skybot.utils.MessageUtils;
import ml.duncte123.skybot.utils.ModerationUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BanCommand extends Command {

    public BanCommand() {
        this.category = CommandCategory.MOD_ADMIN;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void executeCommand(String invoke, String[] args, GuildMessageReceivedEvent event) {

        if (!event.getMember().hasPermission(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)) {
            MessageUtils.sendMsg(event, "You need the kick members and the ban members permission for this command, please contact your server administrator about this");
            return;
        }

        if (event.getMessage().getMentionedUsers().size() < 1 || args.length < 2) {
            MessageUtils.sendMsg(event, "Usage is " + PREFIX + getName() + " <@user> [<time><m/h/d/w/M/Y>] <Reason>. You can use " +
                    "https://regex101.com/r/NJhfoQ/1 for testing. Just insert your time in the \"Test String\" selection.");
            return;
        }

        try {
            final User toBan = event.getMessage().getMentionedUsers().get(0);
            if (toBan.equals(event.getAuthor()) &&
                    !Objects.requireNonNull(event.getGuild().getMember(event.getAuthor())).canInteract(Objects.requireNonNull(event.getGuild().getMember(toBan)))) {
                MessageUtils.sendMsg(event, "You are not permitted to perform this action.");
                return;
            }
            //noinspection ConstantConditions
            if (args.length >= 2) {
                String reason = StringUtils.join(Arrays.copyOfRange(args, 2, args.length), " ");
                String[] timeParts = args[1].split("(?<=\\D)+(?=\\d)+|(?<=\\d)+(?=\\D)+"); //Split the string into ints and letters

                if (!AirUtils.isInt(timeParts[0])) {
                    String newReason = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
                    event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                            (m) -> {
                                ModerationUtils.modLog(event.getAuthor(), toBan, "banned", newReason, event.getGuild());
                                MessageUtils.sendSuccess(event.getMessage());
                            }
                    );
                    return;
                }

                ConversionUtil conversion = new ConversionUtil(StringUtils.join(timeParts, ""));
                event.getGuild().getController().ban(toBan.getId(), 1, reason).queue(
                        (voidMethod) -> {
                            ModerationUtils.addBannedUserToDb(event.getAuthor().getIdLong(), toBan.getName(), toBan.getDiscriminator(),
                                    toBan.getIdLong(), conversion.unbanDate, event.getGuild().getIdLong());
                            ModerationUtils.modLog(event.getAuthor(), toBan, "banned", reason, args[1], event.getGuild());
                        }
                );
                MessageUtils.sendSuccess(event.getMessage());
            } else {
                event.getGuild().getController().ban(toBan.getId(), 1, "No reason was provided").queue(
                        (v) -> ModerationUtils.modLog(event.getAuthor(), toBan, "banned", "*No reason was provided.*", event.getGuild())
                );
            }
        } catch (HierarchyException e) {
            //e.printStackTrace();
            MessageUtils.sendMsg(event, "I can't ban that member because his roles are above or equals to mine.");
        }
    }

    @Override
    public String help() {
        return "Bans a user from the guild **(THIS WILL DELETE MESSAGES)**\n" +
                "Usage: `" + PREFIX + getName() + " <@user> [<time><m/h/d/w/M/Y>] <Reason>`";
    }

    @Override
    public String getName() {
        return "ban";
    }

    private class ConversionUtil {
        private OffsetDateTime unbanDate;
        private final Pattern timeRegex =
                Pattern.compile("(\\d+)((?:y(?:ear(?:s)?)?)|(?:mon(?:th(?:s)?)?)|(?:w(?:eek(?:s)?)?)|(?:d(?:ay(?:s)?)?)|(?:h(?:our(?:s)?)?)|(?:m" +
                        "(?:in(?:ute(?:s)?)?)?)|(?:s(?:ec(?:ond(?:s)?)?)?))");

        ConversionUtil(String input) {
            Matcher matcher = timeRegex.matcher(input);
            OffsetDateTime temp = OffsetDateTime.now();

            while (matcher.find()) {
                String[] parts = matcher.group(0).split("", 2);
                long number = Long.parseLong(parts[0]);
                String unit = parts[1];
                switch (unit) {
                    case "y": case "year": case "years":
                        temp = temp.plusYears(number);
                        break;
                    case "mon": case "month": case "months":
                        temp = temp.plusMonths(number);
                        break;
                    case "w": case "week": case "weeks":
                        temp = temp.plusWeeks(number);
                        break;
                    case "d": case "day": case "days":
                        temp = temp.plusDays(number);
                        break;
                    case "h": case "hour": case "hours":
                        temp = temp.plusHours(number);
                        break;
                    case "m": case "min": case "minute": case "minutes":
                        temp = temp.plusMinutes(number);
                        break;
                    case "s": case "sec": case "second": case "seconds":
                        temp = temp.plusSeconds(number);
                        break;
                }
            }

            this.unbanDate = temp;
        }
    }
}
