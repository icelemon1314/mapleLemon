package client;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterStats;
import server.maps.AnimatedMapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.AbsoluteLifeMovement;
import server.movement.LifeMovement;
import server.movement.LifeMovementFragment;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packet.PacketHelper;

public final class MonsterFamiliar extends AnimatedMapleMapObject implements Serializable {

    private static final long serialVersionUID = 795419937713738569L;
    private int id;
    private int familiar;
    private int fatigue;
    private int characterid;
    private String name;
    private long expiry;
    private short fh = 0;
    private byte vitality;

    public MonsterFamiliar(int characterid, int id, int familiar, long expiry, String name, int fatigue, byte vitality) {
        this.familiar = familiar;
        this.characterid = characterid;
        this.expiry = expiry;
        this.vitality = vitality;
        this.id = id;
        this.name = name;
        this.fatigue = fatigue;
        setStance(0);
        setPosition(new Point(0, 0));
    }

    public MonsterFamiliar(int characterid, int familiar, long expiry) {
        this.familiar = familiar;
        this.characterid = characterid;
        this.expiry = expiry;
        this.fatigue = 0;
        this.vitality = 1;
        this.name = getOriginalName();
        this.id = Randomizer.nextInt();
    }

    public String getOriginalName() {
        return getOriginalStats().getName();
    }

    public MapleMonsterStats getOriginalStats() {
        return MapleLifeFactory.getMonsterStats(MapleItemInformationProvider.getInstance().getFamiliar(this.familiar).mob);
    }

    public void addFatigue(MapleCharacter owner) {
        addFatigue(owner, 1);
    }

    public void addFatigue(MapleCharacter owner, int f) {
        this.fatigue = Math.min(this.vitality * 300, Math.max(0, this.fatigue + f));
        owner.getClient().getSession().write(MaplePacketCreator.updateFamiliar(this));
        if (this.fatigue >= this.vitality * 300) {
            owner.removeFamiliar();
        }
    }

    public int getFamiliar() {
        return this.familiar;
    }

    public int getId() {
        return this.id;
    }

    public int getFatigue() {
        return this.fatigue;
    }

    public int getCharacterId() {
        return this.characterid;
    }

    public String getName() {
        return this.name;
    }

    public long getExpiry() {
        return this.expiry;
    }

    public byte getVitality() {
        return this.vitality;
    }

    public void setFatigue(int f) {
        this.fatigue = f;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setExpiry(long e) {
        this.expiry = e;
    }

    public void setVitality(int v) {
        this.vitality = (byte) v;
    }

    public void setFh(int f) {
        this.fh = (short) f;
    }

    public short getFh() {
        return this.fh;
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        FileoutputUtil.log("召唤spawnFamiliar");
        client.getSession().write(MaplePacketCreator.spawnFamiliar(this, true));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.getSession().write(MaplePacketCreator.spawnFamiliar(this, false));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.FAMILIAR;
    }

    public void updatePosition(List<LifeMovementFragment> movement) {
        for (LifeMovementFragment move : movement) {
            if (((move instanceof LifeMovement)) && ((move instanceof AbsoluteLifeMovement))) {
                setFh(((AbsoluteLifeMovement) move).getNewFH());
            }
        }
    }

    public void writeRegisterPacket(MaplePacketLittleEndianWriter mplew, boolean chr) {
        mplew.writeInt(getCharacterId());
        mplew.writeInt(getFamiliar());
        mplew.writeZero(13);
        mplew.write(chr ? 1 : 0);
        mplew.writeShort((short) getVitality());
        mplew.writeInt(getFatigue());
        mplew.writeLong(PacketHelper.getTime(getVitality() >= 3 ? System.currentTimeMillis() : -2L));
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        mplew.writeLong(PacketHelper.getTime(getExpiry()));
        mplew.writeShort((short) getVitality());
    }
}
