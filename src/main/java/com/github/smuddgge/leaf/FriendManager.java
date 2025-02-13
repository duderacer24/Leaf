package com.github.smuddgge.leaf;

import com.github.smuddgge.leaf.configuration.ConfigCommands;
import com.github.smuddgge.leaf.configuration.squishyyaml.ConfigurationSection;
import com.github.smuddgge.leaf.database.records.FriendRecord;
import com.github.smuddgge.leaf.database.records.FriendRequestRecord;
import com.github.smuddgge.leaf.database.records.FriendSettingsRecord;
import com.github.smuddgge.leaf.database.records.PlayerRecord;
import com.github.smuddgge.leaf.database.tables.FriendRequestTable;
import com.github.smuddgge.leaf.database.tables.FriendSettingsTable;
import com.github.smuddgge.leaf.database.tables.FriendTable;
import com.github.smuddgge.leaf.database.tables.PlayerTable;
import com.github.smuddgge.leaf.datatype.User;
import com.github.smuddgge.leaf.placeholders.PlaceholderManager;
import com.github.smuddgge.leaf.utility.DateAndTime;
import com.github.smuddgge.squishydatabase.Query;
import com.velocitypowered.api.proxy.Player;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the friend request manager.
 */
public class FriendManager {

    /**
     * Used to send a friend request though the database.
     * This method does not send any extra messages to the player.
     *
     * @param userFrom       The user the request will be from.
     * @param playerRecordTo The player to send the request to.
     */
    public static boolean sendRequest(User userFrom, PlayerRecord playerRecordTo) {
        if (Leaf.isDatabaseDisabled()) return false;

        FriendRequestTable friendRequestTable = Leaf.getDatabase().getTable(FriendRequestTable.class);

        FriendRequestRecord requestRecord = new FriendRequestRecord();
        requestRecord.uuid = String.valueOf(UUID.randomUUID());
        requestRecord.dateCreated = DateAndTime.getNow();
        requestRecord.notes = "A friend request!";
        requestRecord.playerFromUuid = userFrom.getUniqueId().toString();
        requestRecord.playerToUuid = playerRecordTo.uuid;

        friendRequestTable.insertRecord(requestRecord);
        return true;
    }

    /**
     * Used to accept a friend request.
     *
     * @param requestRecord The instance of the request record.
     */
    public static void acceptRequest(FriendRequestRecord requestRecord) {
        if (Leaf.isDatabaseDisabled()) return;

        FriendRequestTable friendRequestTable = Leaf.getDatabase().getTable(FriendRequestTable.class);
        FriendTable friendTable = Leaf.getDatabase().getTable(FriendTable.class);
        PlayerTable playerTable = Leaf.getDatabase().getTable(PlayerTable.class);

        PlayerRecord playerFrom = playerTable.getFirstRecord(new Query().match("uuid", requestRecord.playerFromUuid));
        PlayerRecord playerTo = playerTable.getFirstRecord(new Query().match("uuid", requestRecord.playerToUuid));
        assert playerTo != null;
        assert playerFrom != null;

        friendRequestTable.removeRecord(requestRecord);

        FriendRecord friendRecord = new FriendRecord();
        friendRecord.uuid = UUID.randomUUID().toString();
        friendRecord.timeStampCreated = String.valueOf(System.currentTimeMillis());
        friendRecord.dateCreated = DateAndTime.getNow();
        friendRecord.staredBoolean = "false";
        friendRecord.toggleProxyJoin = "false";
        friendRecord.toggleProxyLeave = "false";
        friendRecord.toggleServerChange = "false";
        friendRecord.friendNameFormatted = playerTo.name;
        friendRecord.playerUuid = requestRecord.playerFromUuid;
        friendRecord.friendPlayerUuid = requestRecord.playerToUuid;

        friendTable.insertRecord(friendRecord);

        FriendRecord friendRecord2 = new FriendRecord();
        friendRecord2.uuid = UUID.randomUUID().toString();
        friendRecord2.timeStampCreated = friendRecord.timeStampCreated;
        friendRecord2.dateCreated = friendRecord.dateCreated;
        friendRecord2.staredBoolean = "false";
        friendRecord2.toggleProxyJoin = "false";
        friendRecord2.toggleProxyLeave = "false";
        friendRecord2.toggleServerChange = "false";
        friendRecord2.friendNameFormatted = playerFrom.name;
        friendRecord2.playerUuid = requestRecord.playerToUuid;
        friendRecord2.friendPlayerUuid = requestRecord.playerFromUuid;

        friendTable.insertRecord(friendRecord2);
    }

    /**
     * Used to unfriend a player.
     *
     * @param playerUuid The players uuid.
     * @param friendUuid THe friends uuid.
     */
    public static void unFriend(String playerUuid, String friendUuid) {
        if (Leaf.isDatabaseDisabled()) return;

        FriendTable friendTable = Leaf.getDatabase().getTable(FriendTable.class);
        FriendRecord friendRecord1 = friendTable.getFriend(playerUuid, friendUuid);
        FriendRecord friendRecord2 = friendTable.getFriend(friendUuid, playerUuid);
        assert friendRecord1 != null;
        assert friendRecord2 != null;

        friendTable.removeRecord(friendRecord1);
        friendTable.removeRecord(friendRecord2);
    }

    /**
     * Used to check if a player has already requested a player.
     *
     * @param playerFrom Player uuid the request was sent from.
     * @param playerTo   Player uuid the request was sent to.
     * @return True if they have already requested.
     */
    public static boolean hasRequested(UUID playerFrom, String playerTo) {
        FriendRequestTable friendRequestTable = Leaf.getDatabase().getTable(FriendRequestTable.class);

        int amount = friendRequestTable.getAmountOfRecords(new Query()
                .match("playerFromUuid", playerFrom.toString())
                .match("playerToUuid", playerTo)
        );

        return amount > 0;
    }

    /**
     * Called when a user joins the proxy and is able to be seen.
     *
     * @param user The instance of the user.
     */
    public static void onProxyJoin(User user) {
        ConfigurationSection section = ConfigCommands.getCommandType("friends");

        if (section == null) return;

        FriendTable friendTable = Leaf.getDatabase().getTable(FriendTable.class);
        FriendSettingsTable friendSettingsTable = Leaf.getDatabase().getTable(FriendSettingsTable.class);

        String message = section.getString("proxy_join", "&8[&a+&8] &7Your friend &a<player> &7joined {server_formatted}");

        for (FriendRecord friendRecord : friendTable.getFriendList(user.getUniqueId().toString())) {
            Optional<Player> optional = Leaf.getServer().getPlayer(friendRecord.friendNameFormatted);
            if (optional.isEmpty()) continue;
            Player player = optional.get();

            FriendSettingsRecord settings = friendSettingsTable.getSettings(friendRecord.friendPlayerUuid);

            if (Objects.equals(settings.toggleProxyJoin, "false")) continue;

            new User(player).sendMessage(PlaceholderManager.parse(message, null, user));
        }
    }

    /**
     * Called when a user leaves the proxy and is able to be seen.
     *
     * @param user The instance of the user.
     */
    public static void onProxyLeave(User user) {
        ConfigurationSection section = ConfigCommands.getCommandType("friends");

        if (section == null) return;

        FriendTable friendTable = Leaf.getDatabase().getTable(FriendTable.class);
        FriendSettingsTable friendSettingsTable = Leaf.getDatabase().getTable(FriendSettingsTable.class);

        String message = section.getString("proxy_leave", "&8[&c-&8] &7Your friend &c<player> &7left the network");

        for (FriendRecord friendRecord : friendTable.getFriendList(user.getUniqueId().toString())) {
            Optional<Player> optional = Leaf.getServer().getPlayer(friendRecord.friendNameFormatted);
            if (optional.isEmpty()) continue;
            Player player = optional.get();

            FriendSettingsRecord settings = friendSettingsTable.getSettings(friendRecord.friendPlayerUuid);

            if (Objects.equals(settings.toggleProxyLeave, "false")) continue;

            new User(player).sendMessage(PlaceholderManager.parse(message, null, user));
        }
    }

    /**
     * Called when a user changes server and is able to be seen.
     *
     * @param user The instance of the user.
     */
    public static void onChangeServer(User user) {
        ConfigurationSection section = ConfigCommands.getCommandType("friends");

        if (section == null) return;

        FriendTable friendTable = Leaf.getDatabase().getTable(FriendTable.class);
        FriendSettingsTable friendSettingsTable = Leaf.getDatabase().getTable(FriendSettingsTable.class);

        String message = section.getString("server_change", "&8[&e=&8] &7Your friend &e<player> &7switched to {server_formatted}");

        for (FriendRecord friendRecord : friendTable.getFriendList(user.getUniqueId().toString())) {
            Optional<Player> optional = Leaf.getServer().getPlayer(friendRecord.friendNameFormatted);
            if (optional.isEmpty()) continue;
            Player player = optional.get();

            FriendSettingsRecord settings = friendSettingsTable.getSettings(friendRecord.friendPlayerUuid);

            if (Objects.equals(settings.toggleServerChange, "false")) continue;

            new User(player).sendMessage(PlaceholderManager.parse(message, null, user));
        }
    }
}
