package handling.world.guild;

import java.io.Serializable;

public class MapleBBSReply
        implements Serializable {

    public int replyid;
    public int ownerID;
    public long timestamp;
    public String content;

    public MapleBBSReply(int replyid, int ownerID, String content, long timestamp) {
        this.ownerID = ownerID;
        this.replyid = replyid;
        this.content = content;
        this.timestamp = timestamp;
    }
}
