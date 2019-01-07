package handling.world.guild;

import tools.packet.GuildPacket;

public enum MapleGuildResponse {

    ALREADY_IN_GUILD(46),
    NOT_IN_CHANNEL(48),
    NOT_IN_GUILD(51);

    private final int value;

    private MapleGuildResponse(int val) {
        this.value = val;
    }

    public int getValue() {
        return this.value;
    }

    public byte[] getPacket() {
        return GuildPacket.genericGuildMessage((byte) this.value);
    }
}


