package server;

import client.MapleDisease;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.life.MobSkill;
import server.life.MobSkillFactory;

public class MapleCarnivalFactory {

    private static final MapleCarnivalFactory instance = new MapleCarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap();
    private final Map<Integer, MCSkill> guardians = new HashMap();

    public MapleCarnivalFactory() {
        initialize();
    }

    public static MapleCarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (!this.skills.isEmpty()) {
            return;
        }
    }

    public MCSkill getSkill(int id) {
        return (MCSkill) this.skills.get(id);
    }

    public MCSkill getGuardian(int id) {
        return (MCSkill) this.guardians.get(id);
    }

    public static class MCSkill {

        public int cpLoss;
        public int skillid;
        public int level;
        public boolean targetsAll;

        public MCSkill(int _cpLoss, int _skillid, int _level, boolean _targetsAll) {
            this.cpLoss = _cpLoss;
            this.skillid = _skillid;
            this.level = _level;
            this.targetsAll = _targetsAll;
        }

        public MobSkill getSkill() {
            return MobSkillFactory.getInstance().getMobSkill(this.skillid, 1);
        }

        public MapleDisease getDisease() {
            if (this.skillid <= 0) {
                return MapleDisease.getRandom();
            }
            return MapleDisease.getBySkill(this.skillid);
        }
    }
}
