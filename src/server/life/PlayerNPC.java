package server.life;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.world.WorldFindService;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import server.maps.MapleMap;
import tools.packet.NPCPacket;

public final class PlayerNPC extends MapleNPC {

    private Map<Byte, Integer> equips = new HashMap();
    private int mapid;
    private int face;
    private int hair;
    private int charId;
    private byte skin;
    private byte gender;
    private int[] pets = new int[3];

    public PlayerNPC(ResultSet rs) throws Exception {
        super(rs.getInt("ScriptId"), rs.getString("name"));
        this.hair = rs.getInt("hair");
        this.face = rs.getInt("face");
        this.mapid = rs.getInt("map");
        this.skin = rs.getByte("skin");
        this.charId = rs.getInt("charid");
        this.gender = rs.getByte("gender");
        setCoords(rs.getInt("x"), rs.getInt("y"), rs.getInt("dir"), rs.getInt("Foothold"));
        String[] pet = rs.getString("pets").split(",");
        for (int i = 0; i < 3; i++) {
            if (pet[i] != null) {
                this.pets[i] = Integer.parseInt(pet[i]);
            } else {
                this.pets[i] = 0;
            }
        }

        Connection con = DatabaseConnection.getConnection();
        try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs_equip WHERE NpcId = ?")) {
            ps.setInt(1, getId());
            ResultSet rs2 = ps.executeQuery();
            while (rs2.next()) {
                this.equips.put(rs2.getByte("equippos"), rs2.getInt("equipid"));
            }
            rs2.close();
            ps.close();
        }
    }

    public PlayerNPC(MapleCharacter cid, int npc, MapleMap map, MapleCharacter base) {
        super(npc, cid.getName());
        this.charId = cid.getId();
        this.mapid = map.getId();
        setCoords(base.getTruePosition().x, base.getTruePosition().y, 0, base.getFH());
        update(cid);
    }

    public void setCoords(int x, int y, int f, int fh) {
        setPosition(new Point(x, y));
        setCy(y);
        setRx0(x - 50);
        setRx1(x + 50);
        setF(f);
        setFh(fh);
    }

    public static void loadAll() {
        List<PlayerNPC> toAdd = new ArrayList();
        Connection con = DatabaseConnection.getConnection();
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM playernpcs")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    toAdd.add(new PlayerNPC(rs));
                }
                rs.close();
                ps.close();
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        for (PlayerNPC npc : toAdd) {
            npc.addToServer();
        }
    }

    public static void updateByCharId(MapleCharacter chr) {
        if (WorldFindService.getInstance().findChannel(chr.getId()) > 0) {
            for (PlayerNPC npc : ChannelServer.getInstance(WorldFindService.getInstance().findChannel(chr.getId())).getAllPlayerNPC()) {
                npc.update(chr);
            }
        }
    }

    public void addToServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.addPlayerNPC(this);
        }
    }

    public void removeFromServer() {
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            cserv.removePlayerNPC(this);
        }
    }

    public void update(MapleCharacter chr) {
        if ((chr == null) || (this.charId != chr.getId())) {
            return;
        }
        setName(chr.getName());
        setHair(chr.getHair());
        setFace(chr.getFace());
        setSkin(chr.getSkinColor());
        setGender(chr.getGender());

        this.equips = new HashMap();
        for (Item item : chr.getInventory(MapleInventoryType.EQUIPPED).newList()) {
            if (item.getPosition() < -127) {
                continue;
            }
            this.equips.put((byte) item.getPosition(), item.getItemId());
        }
        saveToDB();
    }

    public void destroy() {
        destroy(false);
    }

    public void destroy(boolean remove) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM playernpcs WHERE scriptid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("DELETE FROM playernpcs_equip WHERE npcid = ?");
            ps.setInt(1, getId());
            ps.executeUpdate();
            ps.close();
            if (remove) {
                removeFromServer();
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public void saveToDB() {
        Connection con = DatabaseConnection.getConnection();
        try {
            if (getNPCFromWZ() == null) {
                destroy(true);
                return;
            }
            destroy();
            PreparedStatement ps = con.prepareStatement("INSERT INTO playernpcs(name, hair, face, skin, x, y, map, charid, scriptid, foothold, dir, gender, pets) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, getName());
            ps.setInt(2, getHair());
            ps.setInt(3, getFace());
            ps.setInt(4, getSkin());
            ps.setInt(5, getTruePosition().x);
            ps.setInt(6, getTruePosition().y);
            ps.setInt(7, getMapId());
            ps.setInt(8, getCharId());
            ps.setInt(9, getId());
            ps.setInt(10, getFh());
            ps.setInt(11, getF());
            ps.setInt(12, getGender());
            String[] pet = {"0", "0", "0"};
            for (int i = 0; i < 3; i++) {
                if (this.pets[i] > 0) {
                    pet[i] = String.valueOf(this.pets[i]);
                }
            }
            ps.setString(13, pet[0] + "," + pet[1] + "," + pet[2]);
            ps.executeUpdate();
            ps.close();

            ps = con.prepareStatement("INSERT INTO playernpcs_equip(npcid, charid, equipid, equippos) VALUES (?, ?, ?, ?)");
            ps.setInt(1, getId());
            ps.setInt(2, getCharId());
            for (Map.Entry equip : this.equips.entrySet()) {
                ps.setInt(3, ((Integer) equip.getValue()));
                ps.setInt(4, ((Byte) equip.getKey()));
                ps.executeUpdate();
            }
            ps.close();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public Map<Byte, Integer> getEquips() {
        return this.equips;
    }

    public byte getSkin() {
        return this.skin;
    }

    public int getGender() {
        return this.gender;
    }

    public int getFace() {
        return this.face;
    }

    public int getHair() {
        return this.hair;
    }

    public int getCharId() {
        return this.charId;
    }

    public int getMapId() {
        return this.mapid;
    }

    public void setSkin(byte s) {
        this.skin = s;
    }

    public void setFace(int f) {
        this.face = f;
    }

    public void setHair(int h) {
        this.hair = h;
    }

    public void setGender(int g) {
        this.gender = (byte) g;
    }

    public int getPet(int i) {
        return this.pets[i] > 0 ? this.pets[i] : 0;
    }

    public void setPets(List<MaplePet> p) {
        for (int i = 0; i < 3; i++) {
            if ((p != null) && (p.size() > i) && (p.get(i) != null)) {
                this.pets[i] = ((MaplePet) p.get(i)).getPetItemId();
            } else {
                this.pets[i] = 0;
            }
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.getSession().write(NPCPacket.spawnNPC(this, true));
        client.getSession().write(NPCPacket.spawnPlayerNPC(this));
        client.getSession().write(NPCPacket.spawnNPCRequestController(this, true));
    }

    public MapleNPC getNPCFromWZ() {
        MapleNPC npc = MapleLifeFactory.getNPC(getId());
        if (npc != null) {
            npc.setName(getName());
        }
        return npc;
    }
}
