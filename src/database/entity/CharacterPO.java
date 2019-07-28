package database.entity;

import javax.persistence.*;

@Entity
@Table(name="characters")
public class CharacterPO {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @ManyToOne(cascade = CascadeType.ALL, optional = false)
  @JoinColumn(name = "accountid")
  private AccountsPO account;
  private byte world;
  private String name;
  private Short level;
  private long exp;
  private short str;
  private short dex;
  private short luk;
  @Column(name="[int]")
  private short _int;
  private int hp;
  private int mp;
  private int maxhp;
  private int maxmp;
  private int meso;
  private short hpApUsed;
  private short job;
  private byte skincolor;
  private byte gender;
  private Integer fame;
  private int hair;
  private int face;
  private short ap;
  private int map;
  private byte spawnpoint;
  private byte gm;
  private int party;
  private byte buddyCapacity;
  private java.sql.Timestamp createdate;
  private String pets;
  private int sp;
  private byte subcategory;
  private int rank;
  private int rankMove;
  private int jobRank;
  private int jobRankMove;
  private int marriageId;
  private long familyid;
  private long seniorid;
  private long junior1;
  private long junior2;
  private int currentrep;
  private int totalrep;
  private int gachexp;
  private short fatigue;
  private long charm;
  private long craft;
  private long charisma;
  private long will;
  private long sense;
  private long insight;
  private int totalWins;
  private int totalLosses;
  private long pvpExp;
  private long pvpPoints;
  private long decorate;
  private long elfEar;
  private long beans;
  private long warning;
  private long dollars;
  private long shareLots;
  private int apstorage;
  private long honor;
  private long love;
  private long playerPoints;
  private long playerEnergy;
  private long pvpDeaths;
  private long pvpKills;
  private long pvpVictory;
  private long batterytime;
  private long exittime;
  private long runeresettime;
  private long userunenowtime;


  public int getId() {
    return id;
  }

  public AccountsPO getAccount() {
    return account;
  }

  public void setAccount(AccountsPO accountInfo) {
    this.account = accountInfo;
  }


  public byte getWorld() {
    return world;
  }

  public void setWorld(byte world) {
    this.world = world;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public Short getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = (short)level;
  }


  public long getExp() {
    return exp;
  }

  public void setExp(long exp) {
    this.exp = exp;
  }


  public short getStr() {
    return str;
  }

  public void setStr(short str) {
    this.str = str;
  }


  public short getDex() {
    return dex;
  }

  public void setDex(short dex) {
    this.dex = dex;
  }


  public short getLuk() {
    return luk;
  }

  public void setLuk(short luk) {
    this.luk = luk;
  }


  public short getInt() {
    return _int;
  }

  public void setInt(short _int) {
    this._int = _int;
  }


  public int getHp() {
    return hp;
  }

  public void setHp(int hp) {
    this.hp = hp;
  }


  public int getMp() {
    return mp;
  }

  public void setMp(int mp) {
    this.mp = mp;
  }


  public int getMaxhp() {
    return maxhp;
  }

  public void setMaxhp(int maxhp) {
    this.maxhp = maxhp;
  }


  public int getMaxmp() {
    return maxmp;
  }

  public void setMaxmp(int maxmp) {
    this.maxmp = maxmp;
  }


  public int getMeso() {
    if (meso < 0) {
      return 0;
    }
    return meso;
  }

  public void setMeso(int meso) {
    this.meso = meso;
  }


  public short getHpApUsed() {
    return hpApUsed;
  }

  public void setHpApUsed(short hpApUsed) {
    this.hpApUsed = hpApUsed;
  }


  public short getJob() {
    return job;
  }

  public void setJob(int job) {
    this.job = (short)job;
  }


  public byte getSkincolor() {
    return skincolor;
  }

  public void setSkincolor(byte skincolor) {
    this.skincolor = skincolor;
  }


  public byte getGender() {
    return gender;
  }

  public void setGender(byte gender) {
    this.gender = gender;
  }


  public Integer getFame() {
    return fame;
  }

  public void setFame(Integer fame) {
    this.fame = fame;
  }


  public int getHair() {
    return hair;
  }

  public void setHair(int hair) {
    this.hair = hair;
  }


  public int getFace() {
    return face;
  }

  public void setFace(int face) {
    this.face = face;
  }


  public short getAp() {
    return ap;
  }

  public void setAp(short ap) {
    this.ap = ap;
  }


  public int getMap() {
    return map;
  }

  public void setMap(int map) {
    this.map = map;
  }


  public byte getSpawnpoint() {
    return spawnpoint;
  }

  public void setSpawnpoint(byte spawnpoint) {
    this.spawnpoint = spawnpoint;
  }


  public byte getGm() {
    return gm;
  }

  public void setGm(byte gm) {
    this.gm = gm;
  }


  public int getParty() {
    return party;
  }

  public void setParty(int party) {
    this.party = party;
  }


  public byte getBuddyCapacity() {
    return buddyCapacity;
  }

  public void setBuddyCapacity(byte buddyCapacity) {
    this.buddyCapacity = buddyCapacity;
  }


  public java.sql.Timestamp getCreatedate() {
    return createdate;
  }

  public void setCreatedate(java.sql.Timestamp createdate) {
    this.createdate = createdate;
  }

  public String getPets() {
    return pets;
  }

  public void setPets(String pets) {
    this.pets = pets;
  }


  public int getSp() {
    return sp;
  }

  public void setSp(int sp) {
    this.sp = sp;
  }


  public byte getSubcategory() {
    return subcategory;
  }

  public void setSubcategory(byte subcategory) {
    this.subcategory = subcategory;
  }


  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }


  public int getRankMove() {
    return rankMove;
  }

  public void setRankMove(int rankMove) {
    this.rankMove = rankMove;
  }


  public int getJobRank() {
    return jobRank;
  }

  public void setJobRank(int jobRank) {
    this.jobRank = jobRank;
  }


  public int getJobRankMove() {
    return jobRankMove;
  }

  public void setJobRankMove(int jobRankMove) {
    this.jobRankMove = jobRankMove;
  }


  public int getMarriageId() {
    return marriageId;
  }

  public void setMarriageId(int marriageId) {
    this.marriageId = marriageId;
  }


  public long getFamilyid() {
    return familyid;
  }

  public void setFamilyid(long familyid) {
    this.familyid = familyid;
  }


  public long getSeniorid() {
    return seniorid;
  }

  public void setSeniorid(long seniorid) {
    this.seniorid = seniorid;
  }


  public long getJunior1() {
    return junior1;
  }

  public void setJunior1(long junior1) {
    this.junior1 = junior1;
  }


  public long getJunior2() {
    return junior2;
  }

  public void setJunior2(long junior2) {
    this.junior2 = junior2;
  }


  public int getCurrentrep() {
    return currentrep;
  }

  public void setCurrentrep(int currentrep) {
    this.currentrep = currentrep;
  }


  public int getTotalrep() {
    return totalrep;
  }

  public void setTotalrep(int totalrep) {
    this.totalrep = totalrep;
  }


  public int getGachexp() {
    return gachexp;
  }

  public void setGachexp(int gachexp) {
    this.gachexp = gachexp;
  }


  public short getFatigue() {
    return fatigue;
  }

  public void setFatigue(short fatigue) {
    this.fatigue = fatigue;
  }


  public long getCharm() {
    return charm;
  }

  public void setCharm(long charm) {
    this.charm = charm;
  }


  public long getCraft() {
    return craft;
  }

  public void setCraft(long craft) {
    this.craft = craft;
  }


  public long getCharisma() {
    return charisma;
  }

  public void setCharisma(long charisma) {
    this.charisma = charisma;
  }


  public long getWill() {
    return will;
  }

  public void setWill(long will) {
    this.will = will;
  }


  public long getSense() {
    return sense;
  }

  public void setSense(long sense) {
    this.sense = sense;
  }


  public long getInsight() {
    return insight;
  }

  public void setInsight(long insight) {
    this.insight = insight;
  }


  public int getTotalWins() {
    return totalWins;
  }

  public void setTotalWins(int totalWins) {
    this.totalWins = totalWins;
  }


  public int getTotalLosses() {
    return totalLosses;
  }

  public void setTotalLosses(int totalLosses) {
    this.totalLosses = totalLosses;
  }


  public long getPvpExp() {
    return pvpExp;
  }

  public void setPvpExp(long pvpExp) {
    this.pvpExp = pvpExp;
  }


  public long getPvpPoints() {
    return pvpPoints;
  }

  public void setPvpPoints(long pvpPoints) {
    this.pvpPoints = pvpPoints;
  }


  public long getDecorate() {
    return decorate;
  }

  public void setDecorate(long decorate) {
    this.decorate = decorate;
  }


  public long getElfEar() {
    return elfEar;
  }

  public void setElfEar(long elfEar) {
    this.elfEar = elfEar;
  }


  public long getBeans() {
    return beans;
  }

  public void setBeans(long beans) {
    this.beans = beans;
  }


  public long getWarning() {
    return warning;
  }

  public void setWarning(long warning) {
    this.warning = warning;
  }


  public long getDollars() {
    return dollars;
  }

  public void setDollars(long dollars) {
    this.dollars = dollars;
  }


  public long getShareLots() {
    return shareLots;
  }

  public void setShareLots(long shareLots) {
    this.shareLots = shareLots;
  }


  public int getApstorage() {
    return apstorage;
  }

  public void setApstorage(int apstorage) {
    this.apstorage = apstorage;
  }


  public long getHonor() {
    return honor;
  }

  public void setHonor(long honor) {
    this.honor = honor;
  }


  public long getLove() {
    return love;
  }

  public void setLove(long love) {
    this.love = love;
  }


  public long getPlayerPoints() {
    return playerPoints;
  }

  public void setPlayerPoints(long playerPoints) {
    this.playerPoints = playerPoints;
  }


  public long getPlayerEnergy() {
    return playerEnergy;
  }

  public void setPlayerEnergy(long playerEnergy) {
    this.playerEnergy = playerEnergy;
  }


  public long getPvpDeaths() {
    return pvpDeaths;
  }

  public void setPvpDeaths(long pvpDeaths) {
    this.pvpDeaths = pvpDeaths;
  }


  public long getPvpKills() {
    return pvpKills;
  }

  public void setPvpKills(long pvpKills) {
    this.pvpKills = pvpKills;
  }


  public long getPvpVictory() {
    return pvpVictory;
  }

  public void setPvpVictory(long pvpVictory) {
    this.pvpVictory = pvpVictory;
  }


  public long getBatterytime() {
    return batterytime;
  }

  public void setBatterytime(long batterytime) {
    this.batterytime = batterytime;
  }


  public long getExittime() {
    return exittime;
  }

  public void setExittime(long exittime) {
    this.exittime = exittime;
  }


  public long getRuneresettime() {
    return runeresettime;
  }

  public void setRuneresettime(long runeresettime) {
    this.runeresettime = runeresettime;
  }


  public long getUserunenowtime() {
    return userunenowtime;
  }

  public void setUserunenowtime(long userunenowtime) {
    this.userunenowtime = userunenowtime;
  }

}
