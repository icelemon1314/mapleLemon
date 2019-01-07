package handling.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class CharacterIdChannelPair implements Externalizable, Comparable<CharacterIdChannelPair> {

    private int charid = 0;
    private int channel = 1;

    public CharacterIdChannelPair() {
    }

    public CharacterIdChannelPair(int charid, int channel) {
        this.charid = charid;
        this.channel = channel;
    }

    public int getCharacterId() {
        return this.charid;
    }

    public int getChannel() {
        return this.channel;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.charid = in.readInt();
        this.channel = in.readByte();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.charid);
        out.writeByte(this.channel);
    }

    @Override
    public int compareTo(CharacterIdChannelPair o) {
        return this.channel - o.channel;
    }
}
