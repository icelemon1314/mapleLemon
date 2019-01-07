package client.messages.commands;

import client.MapleClient;
import client.messages.CommandType;

public abstract class CommandExecute {

    public abstract int execute(MapleClient paramMapleClient, String[] paramArrayOfString);

    public CommandType getType() {
        return CommandType.NORMAL;
    }

    public static abstract class TradeExecute extends CommandExecute {

        @Override
        public CommandType getType() {
            return CommandType.TRADE;
        }
    }

    static enum ReturnValue {

        DONT_LOG,
        LOG;
    }
}


