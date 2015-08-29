package client.messages.commands;

import client.messages.PlayerGMRank;

public class DonatorCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.DONATOR;
    }
}
