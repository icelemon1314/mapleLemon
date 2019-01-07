package client.messages.commands;

import client.MapleClient;
import client.messages.CommandType;

public class CommandObject {

    private final int gmLevelReq;
    private final CommandExecute exe;

    public CommandObject(CommandExecute c, int gmLevel) {
        this.exe = c;
        this.gmLevelReq = gmLevel;
    }

    public int execute(MapleClient c, String[] splitted) {
        return this.exe.execute(c, splitted);
    }

    public CommandType getType() {
        return this.exe.getType();
    }

    public int getReqGMLevel() {
        return this.gmLevelReq;
    }
}

