package handling.channel.handler;

import client.MapleCharacter;
import client.Skill;
import client.SkillFactory;
import constants.GameConstants;
import java.awt.Point;
import java.util.List;
import server.MapleStatEffect;
import server.movement.LifeMovementFragment;
import tools.AttackPair;

public class AttackInfo {

    public int skillId;
    public int charge;
    public int lastAttackTickCount;
    public List<AttackPair> allDamage;
    public Point position;
    public Point skillposition = null;
    public int display;
    public int direction;
    public int stance;
    public short starSlot;
    public short cashSlot;
    public byte numDamage;
    public byte numAttacked;
    public byte numAttackedAndDamage;
    public byte speed;
    public byte AOE;
    public byte unk;
    public byte zeroUnk;
    public List<LifeMovementFragment> movei;
    public boolean real = true;
    public boolean move = false;
    public boolean isCloseRangeAttack = false;
    public boolean isRangedAttack = false;
    public boolean isMagicAttack = false;

    public MapleStatEffect getAttackEffect(MapleCharacter chr, int skillLevel, Skill theSkill) {
        if ((GameConstants.isMulungSkill(this.skillId)) || (GameConstants.isPyramidSkill(this.skillId)) || (GameConstants.isInflationSkill(this.skillId))) {
            skillLevel = 1;
        } else if (skillLevel <= 0) {
            return null;
        }
        return theSkill.getEffect(skillLevel);
    }
}
