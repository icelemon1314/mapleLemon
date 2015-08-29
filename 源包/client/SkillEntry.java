package client;

import java.io.Serializable;

public class SkillEntry implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public int skillLevel;
    public int teachId;
    public byte masterlevel;
    public byte position;
    public byte rank;
    public long expiration;

    public SkillEntry(int skillevel, byte masterlevel, long expiration) {
        this.skillLevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = 0;
        this.position = -1;
    }

    public SkillEntry(int skillevel, byte masterlevel, long expiration, int teachId) {
        this.skillLevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.position = -1;
    }

    public SkillEntry(int skillevel, byte masterlevel, long expiration, int teachId, byte position) {
        this.skillLevel = skillevel;
        this.masterlevel = masterlevel;
        this.expiration = expiration;
        this.teachId = teachId;
        this.position = position;
    }

    @Override
    public String toString() {
        return this.skillLevel + ":" + this.masterlevel;
    }
}
