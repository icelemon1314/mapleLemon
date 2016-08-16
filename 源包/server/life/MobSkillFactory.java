package server.life;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import tools.Pair;

public class MobSkillFactory {

    protected Map<Pair<Integer, Integer>, MobSkill> mobSkill;

    public static MobSkillFactory getInstance() {
        return SingletonHolder.instance;
    }

    private MobSkillFactory() {
        this.mobSkill = new HashMap();
        initialize();
    }

    private void initialize() {
        this.mobSkill.clear();
        MapleDataProvider dataSource = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/Skill.wz"));
        MapleData skillRoot = dataSource.getData("MobSkill.img");
        for (MapleData skillData : skillRoot.getChildren()) {
            for (MapleData levelData : skillData.getChildByPath("level").getChildren()) {
                int skillId = Integer.parseInt(skillData.getName());
                int level = Integer.parseInt(levelData.getName());
                List toSummon = new ArrayList();
                for (int i = 0; (i <= 200)
                        && (levelData.getChildByPath(String.valueOf(i)) != null); i++) {
                    toSummon.add(MapleDataTool.getInt(levelData.getChildByPath(String.valueOf(i)), 0));
                }
                MapleData ltdata = levelData.getChildByPath("lt");
                Point lt = null;
                if (ltdata != null) {
                    lt = (Point) ltdata.getData();
                }
                MapleData rbdata = levelData.getChildByPath("rb");
                Point rb = null;
                if (rbdata != null) {
                    rb = (Point) rbdata.getData();
                }
                MobSkill ret = new MobSkill(skillId, level);
                ret.addSummons(toSummon);
                ret.setCoolTime(MapleDataTool.getInt("interval", levelData, 0) * 1000);
                ret.setDuration(MapleDataTool.getInt("time", levelData, 0) * 1000);
                ret.setHp(MapleDataTool.getInt("hp", levelData, 100));
                ret.setMpCon(MapleDataTool.getInt("mpCon", levelData, 0));
                ret.setSpawnEffect(MapleDataTool.getInt("summonEffect", levelData, 0));
                ret.setX(MapleDataTool.getInt("x", levelData, 1));
                ret.setY(MapleDataTool.getInt("y", levelData, 1));
                ret.setProp(MapleDataTool.getInt("prop", levelData, 100) / 100.0F);
                ret.setLimit((short) MapleDataTool.getInt("limit", levelData, 0));
                ret.setOnce(MapleDataTool.getInt("summonOnce", levelData, 0) > 0);
                ret.setLtRb(lt, rb);
                this.mobSkill.put(new Pair(skillId, level), ret);
            }
        }
        MapleData skillData;
//      FileoutputUtil.log("共加载 " + this.mobSkill.size() + " 个怪物技能信息...");
    }

    public MobSkill getMobSkill(int skillId, int level) {
        return (MobSkill) this.mobSkill.get(new Pair(skillId, level));
    }

    private static class SingletonHolder {

        protected static final MobSkillFactory instance = new MobSkillFactory();
    }
}
