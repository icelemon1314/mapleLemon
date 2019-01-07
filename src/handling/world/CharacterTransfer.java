package handling.world;

import client.BuddylistEntry;
import client.CharacterNameAndId;
import client.MapleCharacter;
import client.MonsterFamiliar;
import client.Skill;
import client.SkillEntry;
import client.inventory.MapleImp;
import client.inventory.MapleMount;
import client.inventory.MaplePet;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.quest.MapleQuest;
import server.shop.MapleShopItem;
import tools.Pair;

public class CharacterTransfer implements Externalizable {

    public int characterid;
    public int accountid;
    public int fame;
    public int pvpExp;
    public int pvpPoints;
    public int meso;
    public int hair;
    public int face;
    public int mapid;
    public int guildid;
    public int sidekick;
    public int partyid;
    public int messengerid;
    public int ACash;
    public int MaplePoints;
    public int mount_itemid;
    public int mount_exp;
    public int points;
    public int vpoints;
    public int marriageId;
    public int maxhp;
    public int maxmp;
    public int hp;
    public int mp;
    public int familyid;
    public int seniorid;
    public int junior1;
    public int junior2;
    public int currentrep;
    public int totalrep;
    public int gachexp;
    public int guildContribution;
    public int totalWins;
    public int totalLosses;
    public byte channel;
    public byte gender;
    public byte gmLevel;
    public byte guildrank;
    public byte alliancerank;
    public byte fairyExp;
    public byte buddysize;
    public byte world;
    public byte initialSpawnPoint;
    public byte skinColor;
    public byte mount_level;
    public byte mount_Fatigue;
    public byte subcategory;
    public long lastfametime;
    public long TranferTime;
    public long exp;
    public String name;
    public String accountname;
    public String BlessOfFairy;
    public String BlessOfEmpress;
    public String chalkboard;
    public String tempIP;
    public short level;
    public short str;
    public short dex;
    public short int_;
    public short luk;
    public short remainingAp;
    public short hpApUsed;
    public short job;
    public short fatigue;
    public Object inventorys;
    public Object skillmacro;
    public Object storage;
    public Object cs;
    public Object battlers;
    public Object anticheat;
    public Object antiMacro;
    public int[] savedlocation;
    public int[] wishlist;
    public int[] rocks;
    public int remainingSp;
    public int[] regrocks;
    public int[] hyperrocks;
    public byte petStore;
    public MapleImp[] imps;
    public Map<Integer, Integer> mbook;
    public Map<Byte, Integer> reports = new LinkedHashMap();
    public Map<Integer, Pair<Byte, Integer>> keymap;
    public List<Pair<Integer, Integer>> quickslot;
    public Map<Integer, MonsterFamiliar> familiars;
    public List<Integer> finishedAchievements = null;
    public List<Integer> famedcharacters = null;
    public List<Integer> battledaccs = null;
    public List<MapleShopItem> rebuy = null;
    public final List boxed;
    public final Map<CharacterNameAndId, Boolean> buddies = new LinkedHashMap();
    public final Map<Integer, Object> Quest = new LinkedHashMap();
    public Map<Integer, String> InfoQuest;
    public Map<String, String> KeyValue;
    public final Map<Integer, SkillEntry> Skills = new LinkedHashMap();
    public int decorate;
    public int beans;
    public int warning;
    public int dollars;
    public int shareLots;
    public int apstorage;
    public int honor;
    public int cardStack;
    public int morphCount;
    public int powerCount;
    public int love;
    public long lastLoveTime;
    public Map<Integer, Long> loveCharacters = null;
    public int playerPoints;
    public int playerEnergy;
    public Object pvpStats;
    public int pvpDeaths;
    public int pvpKills;
    public int pvpVictory;
    public int batterytime;
    public long runeresettime;
    public long userunenowtime;
    public int exittime;
    public int runningDark;
    public int runningDarkSlot;
    public int runningLight;
    public int runningLightSlot;
    public Object potionPot;
    public Object coreAura;

    public CharacterTransfer() {
        this.boxed = new ArrayList();
        this.finishedAchievements = new ArrayList();
        this.famedcharacters = new ArrayList();
        this.battledaccs = new ArrayList();
        this.loveCharacters = new LinkedHashMap();
        this.rebuy = new ArrayList();
        this.KeyValue = new LinkedHashMap();
        this.InfoQuest = new LinkedHashMap();
        this.keymap = new LinkedHashMap();
        this.quickslot = new ArrayList();
        this.familiars = new LinkedHashMap();
        this.mbook = new LinkedHashMap();
    }

    /**
     * 角色快照数据
     * @param chr
     */
    public CharacterTransfer(MapleCharacter chr) {
        this.characterid = chr.getId();
        this.accountid = chr.getAccountID();
        this.accountname = chr.getClient().getAccountName();
        this.channel = (byte) chr.getClient().getChannel();
        this.ACash = chr.getCSPoints(1);
        this.MaplePoints = chr.getCSPoints(2);
        this.vpoints = chr.getVPoints();
        this.name = chr.getName();
        this.fame = chr.getFame();
        this.love = chr.getLove();
        this.gender = chr.getClient().getGender();
        this.level = chr.getLevel();
        this.str = chr.getStat().getStr();
        this.dex = chr.getStat().getDex();
        this.int_ = chr.getStat().getInt();
        this.luk = chr.getStat().getLuk();
        this.hp = chr.getStat().getHp();
        this.mp = chr.getStat().getMp();
        this.maxhp = chr.getStat().getMaxHp();
        this.maxmp = chr.getStat().getMaxMp();
        this.exp = chr.getExp();
        this.hpApUsed = chr.getHpApUsed();
        this.remainingAp = chr.getRemainingAp();
        this.remainingSp = chr.getRemainingSp();
        this.meso = chr.getMeso();
        this.pvpExp = chr.getTotalBattleExp();
        this.pvpPoints = chr.getBattlePoints();
        this.skinColor = chr.getSkinColor();
        this.job = chr.getJob();
        this.hair = chr.getHair();
        this.face = chr.getFace();
        this.mapid = chr.getMapId();
        this.initialSpawnPoint = chr.getInitialSpawnpoint();
        this.marriageId = chr.getMarriageId();
        this.world = chr.getWorld();
        this.guildid = chr.getGuildId();
        this.guildrank = chr.getGuildRank();
        this.guildContribution = chr.getGuildContribution();
        this.alliancerank = chr.getAllianceRank();
        this.gmLevel = (byte) chr.getGMLevel();
        this.points = chr.getPoints();
        this.fairyExp = chr.getFairyExp();
        this.petStore = chr.getPetStores();
        this.subcategory = chr.getSubcategory();
        this.imps = chr.getImps();
        this.fatigue = chr.getFatigue();
        this.currentrep = chr.getCurrentRep();
        this.totalrep = chr.getTotalRep();
        this.totalWins = chr.getTotalWins();
        this.totalLosses = chr.getTotalLosses();
        this.gachexp = chr.getGachExp();
        this.boxed = chr.getBoxed();
        this.familiars = chr.getFamiliars();
        this.tempIP = chr.getClient().getTempIP();
        this.decorate = chr.getDecorate();
        this.dollars = chr.getDollars();
        this.shareLots = chr.getShareLots();
        this.apstorage = chr.getAPS();
        this.cardStack = chr.getCardStack();
        this.morphCount = chr.getMorphCount();
        this.powerCount = chr.getPowerCount();
        this.playerPoints = chr.getPlayerPoints();
        this.playerEnergy = chr.getPlayerEnergy();
        this.runningDark = chr.getDarkType();
        this.runningDarkSlot = chr.getDarkTotal();
        this.runningLight = chr.getLightType();
        this.runningLightSlot = chr.getLightTotal();

        boolean uneq = false;
        MaplePet pet = chr.getSpawnPet();
        if (this.petStore == 0) {
            this.petStore = -1;
        }
        if (pet != null) {
            uneq = true;
            this.petStore = (byte) Math.max(this.petStore, pet.getInventoryPosition());
        }
        if (uneq) {
            chr.unequipAllSpawnPets();
        }
        if (chr.getSidekick() != null) {
            this.sidekick = chr.getSidekick().getId();
        } else {
            this.sidekick = 0;
        }
        for (BuddylistEntry qs : chr.getBuddylist().getBuddies()) {
            this.buddies.put(new CharacterNameAndId(qs.getCharacterId(), qs.getName(), qs.getGroup()), qs.isVisible());
        }
        this.buddysize = chr.getBuddyCapacity();

        this.partyid = (chr.getParty() == null ? -1 : chr.getParty().getId());

        if (chr.getMessenger() != null) {
            this.messengerid = chr.getMessenger().getId();
        } else {
            this.messengerid = 0;
        }
        this.finishedAchievements = chr.getFinishedAchievements();
        this.KeyValue = chr.getKeyValue_Map();
        this.InfoQuest = chr.getInfoQuest_Map();
        for (Map.Entry qs : chr.getQuest_Map().entrySet()) {
            this.Quest.put(((MapleQuest) qs.getKey()).getId(), qs.getValue());
        }
        this.inventorys = chr.getInventorys();
        for (Entry<Skill, SkillEntry> qs : chr.getSkills().entrySet()) {
            this.Skills.put(((Skill) qs.getKey()).getId(), qs.getValue());
        }
        this.BlessOfFairy = chr.getBlessOfFairyOrigin();
        this.BlessOfEmpress = chr.getBlessOfEmpressOrigin();
        this.chalkboard = chr.getChalkboard();
        this.keymap = chr.getKeyLayout().Layout();
        this.quickslot = chr.getQuickSlot().Layout();
        this.savedlocation = chr.getSavedLocations();
        this.wishlist = chr.getWishlist();
        this.regrocks = chr.getRegRocks();
        this.famedcharacters = chr.getFamedCharacters();
        this.lastfametime = chr.getLastFameTime();
        this.storage = chr.getStorage();
        this.cs = chr.getCashInventory();
        MapleMount mount = chr.getMount();
        this.mount_itemid = mount.getItemId();
        this.mount_Fatigue = mount.getFatigue();
        this.mount_level = mount.getLevel();
        this.mount_exp = mount.getExp();
        this.battlers = chr.getBattlers();
        this.lastLoveTime = chr.getLastLoveTime();
        this.loveCharacters = chr.getLoveCharacters();
        this.TranferTime = System.currentTimeMillis();
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.characterid = in.readInt();
        this.accountid = in.readInt();
        this.accountname = in.readUTF();
        this.channel = in.readByte();
        this.ACash = in.readInt();
        this.MaplePoints = in.readInt();
        this.name = in.readUTF();
        this.fame = in.readInt();
        this.love = in.readInt();
        this.gender = in.readByte();
        this.level = in.readShort();
        this.str = in.readShort();
        this.dex = in.readShort();
        this.int_ = in.readShort();
        this.luk = in.readShort();
        this.hp = in.readInt();
        this.mp = in.readInt();
        this.maxhp = in.readInt();
        this.maxmp = in.readInt();
        this.exp = in.readLong();
        this.hpApUsed = in.readShort();
        this.remainingAp = in.readShort();
        this.remainingSp = in.readShort();
        this.meso = in.readInt();
        this.skinColor = in.readByte();
        this.job = in.readShort();
        this.hair = in.readInt();
        this.face = in.readInt();
        this.mapid = in.readInt();
        this.initialSpawnPoint = in.readByte();
        this.world = in.readByte();
        this.guildid = in.readInt();
        this.guildrank = in.readByte();
        this.guildContribution = in.readInt();
        this.alliancerank = in.readByte();
        this.sidekick = in.readInt();
        this.gmLevel = in.readByte();
        this.points = in.readInt();
        this.vpoints = in.readInt();

        if (in.readByte() == 1) {
            this.BlessOfFairy = in.readUTF();
        } else {
            this.BlessOfFairy = null;
        }
        if (in.readByte() == 1) {
            this.BlessOfEmpress = in.readUTF();
        } else {
            this.BlessOfEmpress = null;
        }
        if (in.readByte() == 1) {
            this.chalkboard = in.readUTF();
        } else {
            this.chalkboard = null;
        }
        this.skillmacro = in.readObject();
        this.lastfametime = in.readLong();
        this.storage = in.readObject();
        this.pvpStats = in.readObject();
        this.potionPot = in.readObject();
        this.coreAura = in.readObject();
        this.cs = in.readObject();
        this.battlers = in.readObject();
        this.mount_itemid = in.readInt();
        this.mount_Fatigue = in.readByte();
        this.mount_level = in.readByte();
        this.mount_exp = in.readInt();
        this.partyid = in.readInt();
        this.messengerid = in.readInt();
        this.inventorys = in.readObject();
        this.fairyExp = in.readByte();
        this.subcategory = in.readByte();
        this.fatigue = in.readShort();
        this.marriageId = in.readInt();
        this.familyid = in.readInt();
        this.seniorid = in.readInt();
        this.junior1 = in.readInt();
        this.junior2 = in.readInt();
        this.currentrep = in.readInt();
        this.totalrep = in.readInt();
        this.gachexp = in.readInt();
        this.totalWins = in.readInt();
        this.totalLosses = in.readInt();
        this.anticheat = in.readObject();
        this.tempIP = in.readUTF();
        this.pvpExp = in.readInt();
        this.pvpPoints = in.readInt();
        this.antiMacro = in.readObject();
        this.decorate = in.readInt();
        this.beans = in.readInt();
        this.warning = in.readInt();
        this.dollars = in.readInt();
        this.shareLots = in.readInt();
        this.apstorage = in.readInt();
        this.honor = in.readInt();
        this.cardStack = in.readInt();
        this.morphCount = in.readInt();
        this.powerCount = in.readInt();
        this.playerPoints = in.readInt();
        this.playerEnergy = in.readInt();
        this.pvpDeaths = in.readInt();
        this.pvpKills = in.readInt();
        this.pvpVictory = in.readInt();
        this.runningDark = in.readInt();
        this.runningDarkSlot = in.readInt();
        this.runningLight = in.readInt();
        this.runningLightSlot = in.readInt();
        int mbooksize = in.readShort();
        for (int i = 0; i < mbooksize; i++) {
            this.mbook.put(in.readInt(), in.readInt());
        }
        int skillsize = in.readShort();
        for (int i = 0; i < skillsize; i++) {
            this.Skills.put(in.readInt(), new SkillEntry(in.readInt(), in.readByte(), in.readLong(), in.readInt(), in.readByte()));
        }
        this.buddysize = in.readByte();
        short addedbuddysize = in.readShort();
        for (int i = 0; i < addedbuddysize; i++) {
            this.buddies.put(new CharacterNameAndId(in.readInt(), in.readUTF(), in.readUTF()), in.readBoolean());
        }
        int questsize = in.readShort();
        for (int i = 0; i < questsize; i++) {
            this.Quest.put(in.readInt(), in.readObject());
        }
        int rzsize = in.readByte();
        for (int i = 0; i < rzsize; i++) {
            this.reports.put(in.readByte(), in.readInt());
        }
        int achievesize = in.readByte();
        for (int i = 0; i < achievesize; i++) {
            this.finishedAchievements.add(in.readInt());
        }
        int famesize = in.readByte();
        for (int i = 0; i < famesize; i++) {
            this.famedcharacters.add(in.readInt());
        }
        int battlesize = in.readInt();
        for (int i = 0; i < battlesize; i++) {
            this.battledaccs.add(in.readInt());
        }
        int savesize = in.readByte();
        this.savedlocation = new int[savesize];
        for (int i = 0; i < savesize; i++) {
            this.savedlocation[i] = in.readInt();
        }
        int wsize = in.readByte();
        this.wishlist = new int[wsize];
        for (int i = 0; i < wsize; i++) {
            this.wishlist[i] = in.readInt();
        }
        int rsize = in.readByte();
        this.rocks = new int[rsize];
        for (int i = 0; i < rsize; i++) {
            this.rocks[i] = in.readInt();
        }
        int resize = in.readByte();
        this.regrocks = new int[resize];
        for (int i = 0; i < resize; i++) {
            this.regrocks[i] = in.readInt();
        }
        int hesize = in.readByte();
        this.hyperrocks = new int[resize];
        for (int i = 0; i < hesize; i++) {
            this.hyperrocks[i] = in.readInt();
        }
        int KeyValueSize = in.readShort();
        for (int i = 0; i < KeyValueSize; i++) {
            this.KeyValue.put(in.readUTF(), in.readUTF());
        }
        int infosize = in.readShort();
        for (int i = 0; i < infosize; i++) {
            this.InfoQuest.put(in.readInt(), in.readUTF());
        }
        int keysize = in.readInt();
        for (int i = 0; i < keysize; i++) {
            this.keymap.put(in.readInt(), new Pair(in.readByte(), in.readInt()));
        }
        int qssize = in.readInt();
        for (int i = 0; i < qssize; i++) {
            this.quickslot.add(new Pair(in.readInt(), in.readInt()));
        }
        int fsize = in.readShort();
        for (int i = 0; i < fsize; i++) {
            this.familiars.put(in.readInt(), new MonsterFamiliar(this.characterid, in.readInt(), in.readInt(), in.readLong(), in.readUTF(), in.readInt(), in.readByte()));
        }
        this.petStore = in.readByte();
        int boxedsize = in.readShort();
        for (int i = 0; i < boxedsize; i++) {
            this.boxed.add(in.readObject());
        }
        int rebsize = in.readShort();
        for (int i = 0; i < rebsize; i++) {
            this.rebuy.add((MapleShopItem) in.readObject());
        }
        this.imps = new MapleImp[in.readByte()];
        for (int x = 0; x < this.imps.length; x++) {
            if (in.readByte() > 0) {
                MapleImp i = new MapleImp(in.readInt());
                i.setFullness(in.readShort());
                i.setCloseness(in.readShort());
                i.setState(in.readByte());
                i.setLevel(in.readByte());
                this.imps[x] = i;
            }
        }
        this.lastLoveTime = in.readLong();
        int lovesize = in.readByte();
        for (int i = 0; i < lovesize; i++) {
            this.loveCharacters.put(in.readInt(), in.readLong());
        }
        this.TranferTime = System.currentTimeMillis();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.characterid);
        out.writeInt(this.accountid);
        out.writeUTF(this.accountname);
        out.writeByte(this.channel);
        out.writeInt(this.ACash);
        out.writeInt(this.MaplePoints);
        out.writeUTF(this.name);
        out.writeInt(this.fame);
        out.writeInt(this.love);
        out.writeByte(this.gender);
        out.writeShort(this.level);
        out.writeShort(this.str);
        out.writeShort(this.dex);
        out.writeShort(this.int_);
        out.writeShort(this.luk);
        out.writeInt(this.hp);
        out.writeInt(this.mp);
        out.writeInt(this.maxhp);
        out.writeInt(this.maxmp);
        out.writeLong(this.exp);
        out.writeShort(this.hpApUsed);
        out.writeShort(this.remainingAp);
        out.writeShort(this.remainingSp);
        out.writeLong(this.meso);
        out.writeByte(this.skinColor);
        out.writeShort(this.job);
        out.writeInt(this.hair);
        out.writeInt(this.face);
        out.writeInt(this.mapid);
        out.writeByte(this.initialSpawnPoint);
        out.writeByte(this.world);
        out.writeInt(this.guildid);
        out.writeByte(this.guildrank);
        out.writeInt(this.guildContribution);
        out.writeByte(this.alliancerank);
        out.writeInt(this.sidekick);
        out.writeByte(this.gmLevel);
        out.writeInt(this.points);
        out.writeInt(this.vpoints);
        out.writeByte(this.BlessOfFairy == null ? 0 : 1);
        if (this.BlessOfFairy != null) {
            out.writeUTF(this.BlessOfFairy);
        }
        out.writeByte(this.BlessOfEmpress == null ? 0 : 1);
        if (this.BlessOfEmpress != null) {
            out.writeUTF(this.BlessOfEmpress);
        }
        out.writeByte(this.chalkboard == null ? 0 : 1);
        if (this.chalkboard != null) {
            out.writeUTF(this.chalkboard);
        }
        out.writeObject(this.skillmacro);
        out.writeLong(this.lastfametime);
        out.writeObject(this.storage);
        out.writeObject(this.pvpStats);
        out.writeObject(this.potionPot);
        out.writeObject(this.coreAura);
        out.writeObject(this.cs);
        out.writeObject(this.battlers);
        out.writeInt(this.mount_itemid);
        out.writeByte(this.mount_Fatigue);
        out.writeByte(this.mount_level);
        out.writeInt(this.mount_exp);
        out.writeInt(this.partyid);
        out.writeInt(this.messengerid);
        out.writeObject(this.inventorys);
        out.writeByte(this.fairyExp);
        out.writeByte(this.subcategory);
        out.writeShort(this.fatigue);
        out.writeInt(this.marriageId);
        out.writeInt(this.familyid);
        out.writeInt(this.seniorid);
        out.writeInt(this.junior1);
        out.writeInt(this.junior2);
        out.writeInt(this.currentrep);
        out.writeInt(this.totalrep);
        out.writeInt(this.gachexp);
        out.writeInt(this.totalWins);
        out.writeInt(this.totalLosses);
        out.writeObject(this.anticheat);
        out.writeUTF(this.tempIP);
        out.writeInt(this.pvpExp);
        out.writeInt(this.pvpPoints);
        out.writeObject(this.antiMacro);
        out.writeInt(this.decorate);
        out.writeInt(this.beans);
        out.writeInt(this.warning);
        out.writeInt(this.dollars);
        out.writeInt(this.shareLots);
        out.writeInt(this.apstorage);
        out.writeInt(this.honor);
        out.writeInt(this.cardStack);
        out.writeInt(this.morphCount);
        out.writeInt(this.powerCount);
        out.writeInt(this.playerPoints);
        out.writeInt(this.playerEnergy);
        out.writeInt(this.pvpDeaths);
        out.writeInt(this.pvpKills);
        out.writeInt(this.pvpVictory);
        out.writeInt(this.runningDark);
        out.writeInt(this.runningDarkSlot);
        out.writeInt(this.runningLight);
        out.writeInt(this.runningLightSlot);

        out.writeShort(this.mbook.size());
        for (Map.Entry ms : this.mbook.entrySet()) {
            out.writeInt(((Integer) ms.getKey()));
            out.writeInt(((Integer) ms.getValue()));
        }

        out.writeShort(this.Skills.size());
        for (Map.Entry qs : this.Skills.entrySet()) {
            out.writeInt(((Integer) qs.getKey()));
            out.writeInt(((SkillEntry) qs.getValue()).skillLevel);
            out.writeByte(((SkillEntry) qs.getValue()).masterlevel);
            out.writeLong(((SkillEntry) qs.getValue()).expiration);
            out.writeInt(((SkillEntry) qs.getValue()).teachId);
            out.writeByte(((SkillEntry) qs.getValue()).position);
        }

        out.writeByte(this.buddysize);
        out.writeShort(this.buddies.size());
        for (Map.Entry qs : this.buddies.entrySet()) {
            out.writeInt(((CharacterNameAndId) qs.getKey()).getId());
            out.writeUTF(((CharacterNameAndId) qs.getKey()).getName());
            out.writeUTF(((CharacterNameAndId) qs.getKey()).getGroup());
            out.writeBoolean(((Boolean) qs.getValue()));
        }

        out.writeShort(this.Quest.size());
        for (Map.Entry qs : this.Quest.entrySet()) {
            out.writeInt(((Integer) qs.getKey()));
            out.writeObject(qs.getValue());
        }

        out.writeByte(this.reports.size());
        for (Map.Entry ss : this.reports.entrySet()) {
            out.writeByte(((Byte) ss.getKey()));
            out.writeInt(((Integer) ss.getValue()));
        }

        out.writeByte(this.finishedAchievements.size());
        for (Integer zz : this.finishedAchievements) {
            out.writeInt(zz);
        }

        out.writeByte(this.famedcharacters.size());
        for (Integer zz : this.famedcharacters) {
            out.writeInt(zz);
        }

        out.writeInt(this.battledaccs.size());
        for (Integer zz : this.battledaccs) {
            out.writeInt(zz);
        }

        out.writeByte(this.savedlocation.length);
        for (int zz : this.savedlocation) {
            out.writeInt(zz);
        }

        out.writeByte(this.wishlist.length);
        for (int zz : this.wishlist) {
            out.writeInt(zz);
        }

        out.writeByte(this.rocks.length);
        for (int zz : this.rocks) {
            out.writeInt(zz);
        }

        out.writeByte(this.regrocks.length);
        for (int zz : this.regrocks) {
            out.writeInt(zz);
        }

        out.writeByte(this.hyperrocks.length);
        for (int zz : this.hyperrocks) {
            out.writeInt(zz);
        }

        out.writeShort(this.KeyValue.size());
        for (Map.Entry key : this.KeyValue.entrySet()) {
            out.writeUTF((String) key.getKey());
            out.writeUTF((String) key.getValue());
        }

        out.writeShort(this.InfoQuest.size());
        for (Map.Entry qs : this.InfoQuest.entrySet()) {
            out.writeInt(((Integer) qs.getKey()));
            out.writeUTF((String) qs.getValue());
        }

        out.writeInt(this.keymap.size());
        for (Map.Entry qs : this.keymap.entrySet()) {
            out.writeInt(((Integer) qs.getKey()));
            out.writeByte(((Byte) ((Pair) qs.getValue()).left));
            out.writeInt(((Integer) ((Pair) qs.getValue()).right));
        }

        out.writeInt(this.quickslot.size());
        for (Pair qs : this.quickslot) {
            out.writeInt(((Integer) qs.getLeft()));
            out.writeInt(((Integer) qs.getRight()));
        }

        out.writeShort(this.familiars.size());
        for (Map.Entry qs : this.familiars.entrySet()) {
            out.writeInt(((Integer) qs.getKey()));
            MonsterFamiliar f = (MonsterFamiliar) qs.getValue();
            out.writeInt(f.getId());
            out.writeInt(f.getFamiliar());
            out.writeLong(f.getExpiry());
            out.writeUTF(f.getName());
            out.writeInt(f.getFatigue());
            out.writeByte(f.getVitality());
        }

        out.writeByte(this.petStore);

        out.writeShort(this.boxed.size());
        for (Object boxed1 : this.boxed) {
            out.writeObject(boxed1);
        }

        out.writeShort(this.rebuy.size());
        for (MapleShopItem rebuy1 : this.rebuy) {
            out.writeObject(rebuy1);
        }

        out.writeByte(this.imps.length);
        for (MapleImp imp : this.imps) {
            if (imp != null) {
                out.writeByte(1);
                out.writeInt(imp.getItemId());
                out.writeShort(imp.getFullness());
                out.writeShort(imp.getCloseness());
                out.writeByte(imp.getState());
                out.writeByte(imp.getLevel());
            } else {
                out.writeByte(0);
            }
        }

        out.writeLong(this.lastLoveTime);
        out.writeByte(this.loveCharacters.size());
        for (Map.Entry loves : this.loveCharacters.entrySet()) {
            out.writeInt(((Integer) loves.getKey()));
            out.writeLong(((Long) loves.getValue()));
        }
    }
}
