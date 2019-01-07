package handling.world;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleCoolDownValueHolder;
import client.MapleDiseaseValueHolder;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetDataFactory;
import client.status.MonsterStatusEffect;
import handling.channel.ChannelServer;
import java.util.ArrayList;
import java.util.List;
import server.Timer.WorldTimer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapItem;
import tools.MaplePacketCreator;
import tools.packet.PetPacket;

public class WorldRespawnService {

    private static final int CHANNELS_PER_THREAD = 3;

    public static WorldRespawnService getInstance() {
        return SingletonHolder.instance;
    }

    private WorldRespawnService() {
        Integer[] chs = (Integer[]) ChannelServer.getAllInstance().toArray(new Integer[0]);
        for (int i = 0; i < chs.length; i += 3) {
            WorldTimer.getInstance().register(new Respawn(chs, i), 4000);
        }
    }

    public static void handleMap(MapleMap map, int numTimes, int size, long now) {
        if (map.getItemsSize() > 0) {
            for (MapleMapItem item : map.getAllItemsThreadsafe()) {
                if (item.shouldExpire(now)) {
                    item.expire(map);
                } else if (item.shouldFFA(now)) {
                    item.setDropType((byte) 2);
                }
            }
        }
        if (map.getCharactersSize() > 0 || map.getId() == 931000500) {
            if (map.canSpawn(now)) {
                map.respawn(false, now);
            }
            boolean hurt = map.canHurt(now);
            boolean canrune = true;
            for (MapleCharacter chr : map.getCharactersThreadsafe()) {
                handleCooldowns(chr, numTimes, hurt, now);
            }
            if (map.getMobsSize() > 0) {
                for (MapleMonster mons : map.getAllMonstersThreadsafe()) {
                    if (mons.getStats().isBoss()) {
                        canrune = false;
                    }
                    if ((mons.isAlive()) && (mons.shouldKill(now))) {
                        map.killMonster(mons);
                    } else if ((mons.isAlive()) && (mons.shouldDrop(now))) {
                        mons.doDropItem(now);
                    } else if ((mons.isAlive()) && (mons.getStatiSize() > 0)) {
                        for (MonsterStatusEffect mse : mons.getAllBuffs()) {
                            if (mse.shouldCancel(now)) {
                                mons.cancelSingleStatus(mse);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 可以创建定时技能
     *
     * @param chr
     * @param numTimes
     * @param hurt
     * @param now
     */
    public static void handleCooldowns(MapleCharacter chr, int numTimes, boolean hurt, long now) {
        if (chr == null) {
            return;
        }
        if (chr.getCooldownSize() > 0) {
            for (MapleCoolDownValueHolder m : chr.getCooldowns()) {
                if (m.startTime + m.length < now) {
                    int skillId = m.skillId;
                    chr.removeCooldown(skillId);
                    chr.getClient().getSession().write(MaplePacketCreator.skillCooldown(skillId, 0));
                }
            }
        }

        if (chr.isAlive()) {
            if (((chr.getJob() == 131) || (chr.getJob() == 132)) && (chr.canBlood(now))) {
                chr.doDragonBlood();
            }

            if (chr.canRecover(now)) {
                chr.doRecovery();
            }
            if (chr.canHPRecover(now)) {
                chr.addHP((int) chr.getStat().getHealHP());
            }
            if (chr.canMPRecover(now)) {
                chr.addMP((int) chr.getStat().getHealMP());
                if ((chr.getJob() == 3111) || (chr.getJob() == 3112)) {
                    chr.addDemonMp((int) chr.getStat().getHealMP());
                }
            }

            if (chr.canFairy(now)) {
                chr.doFairy();
            }
            if (chr.canDOT(now)) {
                chr.doDOT();
            }
            if (chr.canExpiration(now)) {
                chr.expirationTask();
            }

        }

        if (chr.getDiseaseSize() > 0) {
            for (MapleDiseaseValueHolder m : chr.getAllDiseases()) {
                if ((m != null) && (m.startTime + m.length < now)) {
                    chr.dispelDebuff(m.disease);
                }

            }
        }

        if ((numTimes % 7 == 0) && (chr.getMount() != null) && (chr.getMount().canTire(now))) {
            chr.getMount().increaseFatigue();
        }
        if (numTimes % 13 == 0) {
            chr.doFamiliarSchedule(now);
            for (MaplePet pet : chr.getSummonedPets()) {
                if ((pet.getPetItemId() == 5000054) && (pet.getSecondsLeft() > 0)) {
                    pet.setSecondsLeft(pet.getSecondsLeft() - 1);
                    if (pet.getSecondsLeft() <= 0) {
                        chr.unequipSpawnPet(pet, true, true);
                        return;
                    }
                }
                int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getPetItemId());
                if (newFullness <= 5) {
                    pet.setFullness(15);
                    chr.unequipSpawnPet(pet, true, true);
                } else {
                    pet.setFullness(newFullness);
                    chr.getClient().getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false));
                }
            }
        }
        if ((hurt) && (chr.isAlive())) {
            if (chr.getInventory(MapleInventoryType.EQUIPPED).findById(chr.getMap().getHPDecProtect()) == null) {
                // 雪域自动扣血
                if (chr.getMapId() >= 211000000 && chr.getMapId()<=211999999) {
                    if (chr.getBuffedValue(MapleBuffStat.HP减少无效) == null) {
                        chr.addHP(-(chr.getMap().getHPDec()));
                    }
                } else {
                    chr.addHP(-(chr.getMap().getHPDec()));
                }

            }
        }
    }

    private static class SingletonHolder {

        protected static final WorldRespawnService instance = new WorldRespawnService();
    }

    private static class Respawn implements Runnable {

        private int numTimes = 0;
        private final List<ChannelServer> cservs = new ArrayList(3);

        public Respawn(Integer[] chs, int c) {
//            StringBuilder s = new StringBuilder("[Respawn Worker] Registered for channels ");
            for (int i = 1; (i <= CHANNELS_PER_THREAD) && (chs.length >= c + i); i++) {
                this.cservs.add(ChannelServer.getInstance(c + i));
//                s.append(c + i).append(" ");
            }
//            FileoutputUtil.log(s.toString());
        }

        @Override
        public void run() {
            numTimes++;
            long now = System.currentTimeMillis();
            for (ChannelServer cserv : this.cservs) {
                if (!cserv.hasFinishedShutdown()) {
                    for (MapleMap map : cserv.getMapFactory().getAllLoadedMaps()) {
                        WorldRespawnService.handleMap(map, numTimes, map.getCharactersSize(), now);
                    }
                }
            }
        }
    }
}
