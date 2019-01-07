package server.life;

import constants.GameConstants;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.wz.MapleDataType;

import server.Randomizer;
import server.maps.MapleMapFactory;
import tools.FileoutputUtil;
import tools.Pair;
import tools.StringUtil;

public class MapleLifeFactory {

    private static final MapleDataProvider data = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Mob.wz"));
    private static final MapleDataProvider npcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Npc.wz"));
    private static final MapleDataProvider stringDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/String.wz"));
    private static final MapleDataProvider etcDataWZ = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Etc.wz"));
    private static final MapleData mobStringData = stringDataWZ.getData("Mob.img");
    private static final MapleData npcStringData = stringDataWZ.getData("Npc.img");
    private static final MapleData npclocData = etcDataWZ.getData("NpcLocation.img");
    private static final Map<Integer, String> npcNames = new HashMap();
    private static final Map<Integer, MapleMonsterStats> monsterStats = new HashMap();
    private static final Map<Integer, Integer> NPCLoc = new HashMap();
    private static final Map<Integer, List<Integer>> questCount = new HashMap();

    public static AbstractLoadedMapleLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        }
        if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        }
        System.err.println("Unknown Life type: " + type + "");
        return null;
    }

    public static int getNPCLocation(int npcid) {
        if (NPCLoc.containsKey(npcid)) {
            return NPCLoc.get(npcid);
        }
        int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", npclocData, -1);
        NPCLoc.put(npcid, map);
        return map;
    }

    public static void loadQuestCounts(boolean reload) {
        if (reload) {
            questCount.clear();
        }
        if (!questCount.isEmpty()) {
            return;
        }
        for (MapleDataDirectoryEntry mapz : data.getRoot().getSubdirectories()) {
            if (mapz.getName().equals("QuestCountGroup")) {
                for (MapleDataFileEntry entry : mapz.getFiles()) {
                    int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                    MapleData dat = data.getData("QuestCountGroup/" + entry.getName());
                    if ((dat != null) && (dat.getChildByPath("info") != null)) {
                        List z = new ArrayList();
                        for (MapleData da : dat.getChildByPath("info")) {
                            z.add(MapleDataTool.getInt(da, 0));
                        }
                        questCount.put(id, z);
                    } else {
                        FileoutputUtil.log("null questcountgroup");
                    }
                }
            }
        }
        for (MapleData c : npcStringData) {
            if (c.getName().contains("pack_ignore")) {
                continue;
            }
            int nid = Integer.parseInt(c.getName());
            String n = StringUtil.getLeftPaddedStr(nid + ".img", '0', 11);
            try {
                if (npcData.getData(n) != null) {
                    String name = MapleDataTool.getString("name", c, "MISSINGNO");
                    if ((!name.contains("MapleTV")) && (name.contains("婴儿月妙"))) {
                        continue;
                    }
                    npcNames.put(nid, name);
                }
            } catch (NullPointerException e) {
            } catch (RuntimeException e) {
            }
        }
    }

    public static List<Integer> getQuestCount(int id) {
        return (List) questCount.get(id);
    }

    public static MapleMonster getMonster(int mobId) {
        MapleMonsterStats stats = getMonsterStats(mobId);
        if (stats == null) {
            return null;
        }
        return new MapleMonster(mobId, stats);
    }

    public static MapleMonsterStats getMonsterStats(int mobId) {
        MapleMonsterStats stats = (MapleMonsterStats) monsterStats.get(Integer.valueOf(mobId));

        if (stats == null) {
            MapleData monsterData;
            try {
                monsterData = data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mobId) + ".img", '0', 11));
            } catch (RuntimeException e) {
                return null;
            }
            if (monsterData == null) {
                return null;
            }
            MapleData monsterInfoData = monsterData.getChildByPath("info");
            stats = new MapleMonsterStats(mobId);

            if (monsterInfoData.getChildByPath("maxHP").getData().equals("??????")) {
                stats.setHp(2147483646L);
            } else {
                stats.setHp(MapleDataTool.getIntConvert("maxHP", monsterInfoData));
            }

            stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
            stats.setExp(GameConstants.getPartyPlayEXP(mobId) > 0 ? GameConstants.getPartyPlayEXP(mobId) : mobId == 9300027 ? 0 : MapleDataTool.getIntConvert("exp", monsterInfoData, 0));
            stats.setLevel((short) MapleDataTool.getIntConvert("level", monsterInfoData, 1));
            stats.setCharismaEXP((short) MapleDataTool.getIntConvert("charismaEXP", monsterInfoData, 0));
            stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
            stats.setrareItemDropLevel((byte) MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));
            stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));
            stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
            stats.setBoss((GameConstants.getPartyPlayHP(mobId) > 0) || (MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0) || (mobId == 8810018) || (mobId == 9410066) || ((mobId >= 8810118) && (mobId <= 8810122)));
            stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
            stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
            stats.setEscort(MapleDataTool.getIntConvert("escort", monsterInfoData, 0) > 0);
            stats.setPartyBonus((GameConstants.getPartyPlayHP(mobId) > 0) || (MapleDataTool.getIntConvert("partyBonusMob", monsterInfoData, 0) > 0));
            stats.setPartyBonusRate(MapleDataTool.getIntConvert("partyBonusR", monsterInfoData, 0));
            if (mobStringData.getChildByPath(String.valueOf(mobId)) != null) {
                stats.setName(MapleDataTool.getString("name", mobStringData.getChildByPath(String.valueOf(mobId)), "MISSINGNO"));
            }
            stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
            stats.setChange(MapleDataTool.getIntConvert("changeableMob", monsterInfoData, 0) > 0);
            stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);
            stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
            stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
            stats.setCP((byte) MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
            stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
            stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
            stats.setPhysicalAttack(MapleDataTool.getIntConvert("PADamage", monsterInfoData, 0));
            stats.setMagicAttack(MapleDataTool.getIntConvert("MADamage", monsterInfoData, 0));
            stats.setPDRate((byte) MapleDataTool.getIntConvert("PDRate", monsterInfoData, 0));
            stats.setMDRate((byte) MapleDataTool.getIntConvert("MDRate", monsterInfoData, 0));
            stats.setAcc(MapleDataTool.getIntConvert("acc", monsterInfoData, 0));
            stats.setEva(MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
            stats.setSummonType((byte) MapleDataTool.getIntConvert("summonType", monsterInfoData, 0));
            stats.setCategory((byte) MapleDataTool.getIntConvert("category", monsterInfoData, 0));
            stats.setSpeed(MapleDataTool.getIntConvert("speed", monsterInfoData, 0));
            stats.setPushed(MapleDataTool.getIntConvert("pushed", monsterInfoData, 0));

            MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
            if (selfd != null) {
                stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
                stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", selfd, stats.getRemoveAfter()));
                stats.setSelfD((byte) MapleDataTool.getIntConvert("action", selfd, -1));
            } else {
                stats.setSelfD((byte) -1);
            }

            MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
            int firstAttack = 0;
            if (firstAttackData != null) {
                if (firstAttackData.getType() == MapleDataType.FLOAT) {
                    firstAttack = Math.round(MapleDataTool.getFloat(firstAttackData));
                } else {
                    firstAttack = MapleDataTool.getInt(firstAttackData);
                }
            }
            stats.setFirstAttack(firstAttack > 0);

            if ((stats.isBoss()) || (isDmgSponge(mobId))) {
                if ((monsterInfoData.getChildByPath("hpTagColor") == null) || (monsterInfoData.getChildByPath("hpTagBgcolor") == null)) {
                    stats.setTagColor(0);
                    stats.setTagBgColor(0);
                } else {
                    stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
                    stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
                }
            }

            MapleData banishData = monsterInfoData.getChildByPath("ban");
            if (banishData != null) {
                stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
            }

            MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
            if (reviveInfo != null) {
                List revives = new LinkedList();
                for (MapleData bdata : reviveInfo) {
                    revives.add(MapleDataTool.getInt(bdata));
                }
                stats.setRevives(revives);
            }

            MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
            if (monsterSkillData != null) {
                int i = 0;
                List skills = new ArrayList();
                while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                    skills.add(new Pair(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0), MapleDataTool.getInt(i + "/level", monsterSkillData, 0)));
                    i++;
                }
                stats.setSkills(skills);
            }

            decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));

            int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
            if (link != 0) {
                monsterData = data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
            }

            for (MapleData idata : monsterData) {
                if (idata.getName().equals("fly")) {
                    stats.setFly(true);
                    stats.setMobile(true);
                    break;
                }
                if (idata.getName().equals("move")) {
                    stats.setMobile(true);
                }
            }

            for (int i = 1;; i++) {
                MapleData attackData = monsterData.getChildByPath("attack" + i + "/info");
                if (attackData == null) {
                    break;
                }
                MobAttackInfo ret = new MobAttackInfo();
                ret.setDeadlyAttack(attackData.getChildByPath("deadlyAttack") != null);
                ret.setMpBurn(MapleDataTool.getInt("mpBurn", attackData, 0));
                ret.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
                ret.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
                ret.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
                ret.attackAfter = MapleDataTool.getInt("attackAfter", attackData, 0);
                ret.PADamage = MapleDataTool.getInt("PADamage", attackData, 0);
                ret.MADamage = MapleDataTool.getInt("PADamage", attackData, 0);
                ret.magic = (MapleDataTool.getInt("magic", attackData, 0) > 0);
                if (attackData.getChildByPath("range") != null) {
                    ret.range = MapleDataTool.getInt("range/r", attackData, 0);
                    if ((attackData.getChildByPath("range/lt") != null) && (attackData.getChildByPath("range/rb") != null)) {
                        ret.lt = ((Point) attackData.getChildByPath("range/lt").getData());
                        ret.rb = ((Point) attackData.getChildByPath("range/rb").getData());
                    }
                }
                stats.addMobAttack(ret);
            }

            byte hpdisplaytype = -1;
            if (stats.getTagColor() > 0) {
                hpdisplaytype = 0;
            } else if (stats.isFriendly()) {
                hpdisplaytype = 1;
            } else if ((mobId >= 9300184) && (mobId <= 9300215)) {
                hpdisplaytype = 2;
            } else if ((!stats.isBoss()) || (mobId == 9410066) || (stats.isPartyBonus())) {
                hpdisplaytype = 3;
            }
            stats.setHPDisplayType(hpdisplaytype);

            monsterStats.put(mobId, stats);
        }
        return stats;
    }

    public static void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

    private static boolean isDmgSponge(int mid) {
        switch (mid) {
            case 8810018:
            case 8810118:
            case 8810119:
            case 8810120:
            case 8810121:
            case 8810122:
            case 8820009:
            case 8820010:
            case 8820011:
            case 8820012:
            case 8820013:
            case 8820014:
            case 8820108:
            case 8820109:
            case 8820110:
            case 8820111:
            case 8820112:
            case 8820113:
            case 8820114:
            case 8820300:
            case 8820301:
            case 8820302:
            case 8820303:
            case 8820304:
                return true;
        }
        return false;
    }

    /**
     * 新建一个NPC的实例
     * @param nid
     * @return
     */
    public static MapleNPC getNPC(int nid) {
        String name = npcNames.get(Integer.valueOf(nid));
        if (name == null) {
            return null;
        }
        // 增加一个NPC的脚本属性属性值
        MapleNPC npc = new MapleNPC(nid, name);
        final MapleDataProvider wzNpc = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Npc.wz"));
        MapleData data = wzNpc.getData(StringUtil.getLeftPaddedStr(Integer.toString(nid) + ".img", '0', 11));
        MapleData link = data.getChildByPath("info/quest");
        if (link != null) {
            String scriptName = MapleDataTool.getString("info/quest", data);
            npc.setScriptName(scriptName);
            FileoutputUtil.log("NPC("+nid+")存在脚本任务："+scriptName);
        }
        return npc;
    }

    public static int getRandomNPC() {
        List vals = new ArrayList(npcNames.keySet());
        int ret = 0;
        while (ret <= 0) {
            ret = ((Integer) vals.get(Randomizer.nextInt(vals.size())));
            if ((npcNames.get(ret)).contains("MISSINGNO")) {
                ret = 0;
            }
        }
        return ret;
    }

    public static Map<Integer, String> getNPCNames() {
        return npcNames;
    }
}
