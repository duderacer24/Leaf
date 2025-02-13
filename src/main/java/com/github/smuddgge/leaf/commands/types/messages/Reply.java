package com.github.smuddgge.leaf.commands.types.messages;

import com.github.smuddgge.leaf.MessageManager;
import com.github.smuddgge.leaf.commands.BaseCommandType;
import com.github.smuddgge.leaf.commands.CommandStatus;
import com.github.smuddgge.leaf.commands.CommandSuggestions;
import com.github.smuddgge.leaf.configuration.ConfigCommands;
import com.github.smuddgge.leaf.configuration.squishyyaml.ConfigurationSection;
import com.github.smuddgge.leaf.datatype.User;
import com.github.smuddgge.leaf.placeholders.PlaceholderManager;

/**
 * <h1>Reply Command Type</h1>
 * Used reply to the most recent conversation.
 */
public class Reply extends BaseCommandType {

    @Override
    public String getName() {
        return "reply";
    }

    @Override
    public String getSyntax() {
        return "/[name] [message]";
    }

    @Override
    public CommandSuggestions getSuggestions(ConfigurationSection section, User user) {
        return null;
    }

    @Override
    public CommandStatus onConsoleRun(ConfigurationSection section, String[] arguments) {
        MessageManager.log("{error_colour}The console cannot reply to messages.");
        return new CommandStatus();
    }

    @Override
    public CommandStatus onPlayerRun(ConfigurationSection section, String[] arguments, User user) {
        if (arguments.length < 1) return new CommandStatus().incorrectArguments();

        // Get the last pearson the user has messaged.
        User recipient = user.getLastMessaged();

        // Check if the recipient doesn't exist.
        if (recipient == null) {
            user.sendMessage(section.getString("not_found", "{error_colour}You have no conversation to reply to."));
            return new CommandStatus();
        }

        // Get if vanishable players can message vanishable players.
        boolean allowVanishablePlayers = ConfigCommands.canVanishableSeeVanishable();
        boolean userIsVanishable = !user.isNotVanishable();
        boolean recipientNotVanished = !recipient.isVanished();

        // Check if user is vanishable and vanishable players can message vanishable players.
        // Or check if recipient is not vanished.
        if ((allowVanishablePlayers && userIsVanishable)
                || recipientNotVanished) {

            // Check if they are ignoring.
            if (user.isIgnoring(recipient.getUniqueId())) {
                user.sendMessage(section.getString("ignoring", "{error_colour}You have ignored this player."));
                return new CommandStatus();
            }

            if (recipient.isIgnoring(user.getUniqueId())) {
                user.sendMessage(section.getString("recipient_ignoring", "{error_colour}This player has ignored you."));
                return new CommandStatus();
            }

            // Check for toggles.
            if (!user.canMessage()) {
                user.sendMessage(section.getString("toggled", "{error_colour}You have your messages toggled."));
                return new CommandStatus();
            }

            if (!recipient.canMessage()) {
                user.sendMessage(section.getString("recipient_toggled", "{error_colour}This player has there messages toggled."));
                return new CommandStatus();
            }

            // Get the message.
            String message = String.join(" ", arguments).trim();

            // Send messages.
            recipient.sendMessage(PlaceholderManager.parse(section.getString("from")
                    .replace("%message%", message), null, user));

            user.sendMessage(PlaceholderManager.parse(section.getString("to")
                    .replace("%message%", message), null, recipient));

            MessageManager.sendSpy(section.getString("spy_format", "&8&o%from% -> %to% : %message%")
                    .replace("%from%", user.getName())
                    .replace("%to%", recipient.getName())
                    .replace("%message%", message));

            // Log message interaction.
            MessageManager.setLastMessaged(user.getUniqueId(), recipient.getUniqueId());
            return new CommandStatus();
        }

        user.sendMessage(section.getString("not_found", "{error_colour}You have no conversation to reply to."));
        return new CommandStatus();
    }
}
