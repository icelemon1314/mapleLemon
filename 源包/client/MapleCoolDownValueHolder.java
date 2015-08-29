package client;

public class MapleCoolDownValueHolder {

    public int skillId;
    public long startTime;
    public long length;

    public MapleCoolDownValueHolder(int skillId, long startTime, long length) {
        this.skillId = skillId;
        this.startTime = startTime;
        this.length = length;
    }
}
