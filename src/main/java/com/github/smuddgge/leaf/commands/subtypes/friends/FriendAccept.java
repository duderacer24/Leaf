package com.github.smuddgge.leaf.commands.subtypes.friends;

import com.github.smuddgge.leaf.MessageManager;
import com.github.smuddgge.leaf.commands.CommandStatus;
import com.github.smuddgge.leaf.commands.CommandSuggestions;
import com.github.smuddgge.leaf.commands.CommandType;
import com.github.smuddgge.leaf.configuration.squishyyaml.ConfigurationSection;
import com.github.smuddgge.leaf.datatype.User;
import com.github.smuddgge.leaf.inventorys.inventorys.FriendRequestInventory;

/**
 * <h1>Friend Accept Subcommand Type</h1>
 * Opens a {@link com.github.smuddgge.leaf.inventorys.InventoryInterface}
 * containing the players friend requests.
 */
public class FriendAccept implements CommandType {

    @Override
    public String getName() {
        return "accept";
    }

    @Override
    public String getSyntax() {
        return "/[parent] [name]";
    }

    @Override
    public CommandSuggestions getSuggestions(ConfigurationSection section, User user) {
        return null;
    }

    @Override
    public CommandStatus onConsoleRun(ConfigurationSection section, String[] arguments) {
        return new CommandStatus().playerCommand();
    }

    @Override
    public CommandStatus onPlayerRun(ConfigurationSection section, String[] arguments, User user) {

        // Open the players friend requests inventory.
        try {
            FriendRequestInventory friendRequestInventory = new FriendRequestInventory(section.getSection(this.getName()), user);
            friendRequestInventory.open();

        } catch (Exception exception) {
            user.sendMessage(section.getSection(this.getName()).getString("error", "{error_colour}Error occurred when opening inventory."));
            MessageManager.warn("Exception occurred when opening a friend request inventory!");
            exception.printStackTrace();
        }

        return new CommandStatus();
    }
}
