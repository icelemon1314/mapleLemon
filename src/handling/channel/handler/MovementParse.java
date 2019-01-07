package handling.channel.handler;

import client.MapleCharacter;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import server.maps.AnimatedMapleMapObject;
import server.movement.AbsoluteLifeMovement;
import server.movement.BounceMovement;
import server.movement.ChangeEquipSpecialAwesome;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import server.movement.RelativeLifeMovement;
import server.movement.TeleportMovement;
import tools.FileoutputUtil;
import tools.data.input.SeekableLittleEndianAccessor;

public class MovementParse {

    private static final Logger log = Logger.getLogger(MovementParse.class);

    //1 = player, 2 = mob, 3 = pet, 4 = summon, 5 = dragon
    public static List<LifeMovementFragment> parseMovement(final SeekableLittleEndianAccessor lea, final int kind) {
        return parseMovement(lea, kind, null);
    }

    //来源于IDB的类型
    public static List<LifeMovementFragment> parseMovement(SeekableLittleEndianAccessor lea, int kind, MapleCharacter chr) {
        final List<LifeMovementFragment> res = new ArrayList<>();
        final byte numCommands = lea.readByte();
        for (byte i = 0; i < numCommands; i++) {
            final int command = lea.readByte();
            switch (command) {
                case -1:
                    short xpos = lea.readShort();
                    short ypos = lea.readShort();
                    short unk = lea.readShort();
                    short fh = lea.readShort();
                    byte newstate = lea.readByte();
                    short duration = lea.readShort();
                    final BounceMovement bm = new BounceMovement(command, new Point(xpos, ypos), duration, newstate);
                    bm.setFH(fh);
                    bm.setUnk(unk);
                    res.add(bm);
                    break;
                case 0: // ok
                case 5:
                    xpos = lea.readShort();
                    ypos = lea.readShort();
                    short xwobble = lea.readShort();
                    short ywobble = lea.readShort();
                    duration = lea.readShort();
                    newstate = lea.readByte();
                    short newfh=lea.readShort();
                    final AbsoluteLifeMovement alm = new AbsoluteLifeMovement(command, new Point(xpos, ypos), duration, newstate);
                    alm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    alm.setNewFH(newfh);
                    res.add(alm);
                    break;
                case 1:
                case 2:
                case 6:
                    xwobble = lea.readShort();
                    ywobble = lea.readShort();
                    newstate = lea.readByte();
                    duration = lea.readShort();
                    final RelativeLifeMovement rlm = new RelativeLifeMovement(command, new Point(xwobble, ywobble), duration, newstate);
                    res.add(rlm);
                    break;
                case 3:
                case 4:
                case 7:
                    xpos = lea.readShort();
                    ypos = lea.readShort();
                    xwobble = lea.readShort();
                    ywobble = lea.readShort();
                    newstate = lea.readByte();
                    final TeleportMovement tm = new TeleportMovement(command, new Point(xpos, ypos),0, newstate);
                    tm.setPixelsPerSecond(new Point(xwobble, ywobble));
                    res.add(tm);
                    break;
                case 8:
                    res.add(new ChangeEquipSpecialAwesome(lea.readByte(),0));
                    break;
                default:
                    FileoutputUtil.log(FileoutputUtil.Movement_Err, "未知移动封包：[" + command + "]" + "(" + chr.getName() + ") 职业：" + chr.getJobName() + "(" + chr.getJob() + "移动封包 剩余次数: " + (numCommands - res.size()) + "  封包: " + lea.toString(true));
                    break;
            }
        }
        if (numCommands != res.size()) {
            log.warn("循环次数[" + numCommands + "]和实际上获取的循环次数[" + res.size() + "]不符");
            FileoutputUtil.log(FileoutputUtil.Movement_Err, "循环次数[" + numCommands + "]和实际上获取的循环次数[" + res.size() + "]不符 " + "(" + chr.getName() + ") 职业：" + chr.getJobName() + "(" + chr.getJob() + "移动封包 剩余次数: " + (numCommands - res.size()) + "  封包: " + lea.toString(true));
            return null;
        }
        return res;
    }

    public static void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        if (movement == null) {
            return;
        }
        for (LifeMovementFragment move : movement) {
            if ((move instanceof LifeMovement)) {
                if ((move instanceof AbsoluteLifeMovement)) {
                    Point position = ((LifeMovement) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                }
                target.setStance(((LifeMovement) move).getNewstate());
            }
        }
    }
}
