package handling.channel.handler;

import client.MapleCharacter;
import client.MapleCharacterUtil;
import client.MapleClient;
import client.MapleStat;
import client.MonsterFamiliar;
import client.PlayerStats;
import client.Skill;
import client.SkillFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetFlag;
import constants.GameConstants;
import constants.ItemConstants;
import handling.channel.ChannelServer;
import handling.world.WorldBroadcastService;
import java.awt.Rectangle;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import server.AutobanManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.RandomRewards;
import server.Randomizer;
import server.StructFamiliar;
import server.StructItemOption;
import server.cashshop.CashItemFactory;
import server.events.MapleEvent;
import server.events.MapleEventType;
import server.life.MapleLifeFactory;
import server.maps.FieldLimitType;
import server.maps.MapleLove;
import server.maps.MapleMap;
import server.maps.MapleDefender;
import server.maps.MapleTVEffect;
import server.quest.MapleQuest;
import server.shop.MapleShopFactory;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.InventoryPacket;
import tools.packet.MTSCSPacket;
import tools.packet.PetPacket;

public class UseCashItemHandler {

    public static void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 2B 0F 00
        // D0 C4 1F 00 // 道具ID
        // 10 00 B2 E2 CA D4 C0 AE B0 C8 A3 A1 A3 A1 A3 A1 A3 A1
        // 01
        if ((chr == null) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        chr.setScrolledPosition((short) 0);
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        byte slot = (byte) slea.readShort();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem((short) slot);
        if ((toUse == null) || (toUse.getItemId() != itemId) || (toUse.getQuantity() < 1) || (chr.hasBlockedInventory())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (chr.isShowPacket()) {
            chr.dropMessage(5, new StringBuilder().append("使用商城道具 物品ID: ").append(itemId).append(" 物品类型: ").append(itemType).toString());
        }
        boolean used = false;
        boolean cc = false;
        switch (itemType) {
            case 217: // 缩地石
                    used = InventoryHandler.UseTeleRock(slea, c, itemId);
                break;
            case 218: // 洗点卷
                // 2B 10 00 A0 43 21 00 80 00 00 00 40 00 00 00 敏捷加力量
                // 2B 10 00 A0 43 21 00 00 01 00 00 00 02 00 00 减运气加智力
                // 2B 1B 00 A0 43 21 00 80 00 00 00 40 00 00 00 减力量加敏捷
                if (itemId == 2180000) { // 洗能力点
                    Map statupdate = new EnumMap(MapleStat.class);
                    int apto = slea.readInt();
                    int apfrom = slea.readInt();
                    int statLimit = c.getChannelServer().getStatLimit();
                    if (chr.isShowPacket()) {
                        chr.dropMessage(5, new StringBuilder().append("洗能力点 apto: ").append(apto).append(" apfrom: ").append(apfrom).toString());
                    }
                    if (apto == apfrom) {
                        break;
                    }
                    int job = chr.getJob();
                    PlayerStats playerst = chr.getStat();
                    used = true;
                    switch (apto) {
                        case 0x40:
                            if (playerst.getStr() < statLimit) {
                                    break;
                                }
                            used = false;
                            break;
                        case 0x80:
                            if (playerst.getDex() < statLimit) {
                                    break;
                                }
                            used = false;
                            break;
                        case 0x100:
                            if (playerst.getInt() < statLimit) {
                                    break;
                                }
                            used = false;
                            break;
                        case 0x200:
                            if (playerst.getLuk() < statLimit) {
                                    break;
                                }
                            used = false;
                            break;
                        case 0x400:
                            if (playerst.getMaxHp() < chr.getMaxHpForSever()) {
                                    break;
                                }
                            used = false;
                            break;
                        case 0x800:
                            if (playerst.getMaxMp() < chr.getMaxMpForSever()) {
                                    break;
                                }
                            used = false;
                            break;
                        default:
                            System.out.print("洗点未知的操作码："+apto);
                            break;
                    }

                    switch (apfrom) {
                    case 0x40:
                        if (playerst.getStr() > 4) {
                                break;
                            }
                        used = false;
                        break;
                    case 0x80:
                        if (playerst.getDex() > 4) {
                                break;
                            }
                        used = false;
                        break;
                    case 0x100:
                        if (playerst.getInt() > 4) {
                                break;
                            }
                        used = false;
                        break;
                    case 0x200:
                        if (playerst.getLuk() > 4) {
                                break;
                            }
                        used = false;
                        break;
                    case 0x400:
                        if ((chr.getHpApUsed() > 0) && (chr.getHpApUsed() < 10000)) {
                                break;
                            }
                        used = false;
                        break;
                    case 0x800:
                        if ((chr.getHpApUsed() > 0) && (chr.getHpApUsed() < 10000)) {
                                break;
                            }
                        used = false;
                    }

                    if (used) {
                    switch (apto) {
                        case 0x40:
                                long toSet = playerst.getStr() + 1;
                                playerst.setStr((short) (int) toSet, chr);
                                statupdate.put(MapleStat.力量, toSet);
                                break;
                        case 0x80:
                                toSet = playerst.getDex() + 1;
                                playerst.setDex((short) (int) toSet, chr);
                                statupdate.put(MapleStat.敏捷, toSet);
                                break;
                        case 0x100:
                                toSet = playerst.getInt() + 1;
                                playerst.setInt((short) (int) toSet, chr);
                                statupdate.put(MapleStat.智力, toSet);
                                break;
                        case 0x200:
                                toSet = playerst.getLuk() + 1;
                                playerst.setLuk((short) (int) toSet, chr);
                                statupdate.put(MapleStat.运气, toSet);
                                break;
                        case 0x400:
                                int maxhp = playerst.getMaxHp();
                                if (GameConstants.is新手职业(job)) {
                                        maxhp += Randomizer.rand(4, 8);
                                    }else if (((job >= 100) && (job <= 132)) ) {
                                        maxhp += Randomizer.rand(36, 42);
                                    } else if (((job >= 200) && (job <= 232)) ) {
                                        maxhp += Randomizer.rand(10, 12);
                                    } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)) ) {
                                        maxhp += Randomizer.rand(14, 18);
                                    } else if (((job >= 510) && (job <= 512)) || ((job >= 580) && (job <= 582))) {
                                        maxhp += Randomizer.rand(24, 28);
                                    } else if (((job >= 500) && (job <= 532)) || ((job >= 590) && (job <= 592))) {
                                        maxhp += Randomizer.rand(16, 20);
                                    }else {
                                        maxhp += Randomizer.rand(16, 20);
                                    }
                                maxhp = Math.min(chr.getMaxHpForSever(), Math.abs(maxhp));
                                chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                                playerst.setMaxHp(maxhp, chr);
                                statupdate.put(MapleStat.MAXHP, (long) maxhp);
                                break;
                        case 0x800:
                                int maxmp = playerst.getMaxMp();
                                if (GameConstants.is新手职业(job)) {
                                        maxmp += Randomizer.rand(6, 8);
                                    } else {
                                        if (((job >= 100) && (job <= 132)) ) {
                                                maxmp += Randomizer.rand(4, 9);
                                            } else if (((job >= 200) && (job <= 232)) ) {
                                                maxmp += Randomizer.rand(32, 36);
                                            } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)) || ((job >= 500) && (job <= 592))) {
                                                maxmp += Randomizer.rand(8, 10);
                                            } else {
                                                maxmp += Randomizer.rand(6, 8);
                                            }
                                    }
                                maxmp = Math.min(chr.getMaxMpForSever(), Math.abs(maxmp));
                                chr.setHpApUsed((short) (chr.getHpApUsed() + 1));
                                playerst.setMaxMp(maxmp, chr);
                                statupdate.put(MapleStat.MAXMP, (long) maxmp);
                    }

                    switch (apfrom) {
                        case 0x40:
                                long toSet = playerst.getStr() - 1;
                                playerst.setStr((short) (int) toSet, chr);
                                statupdate.put(MapleStat.力量, toSet);
                                break;
                        case 0x80:
                                toSet = playerst.getDex() - 1;
                                playerst.setDex((short) (int) toSet, chr);
                                statupdate.put(MapleStat.敏捷, toSet);
                                break;
                        case 0x100:
                                toSet = playerst.getInt() - 1;
                                playerst.setInt((short) (int) toSet, chr);
                                statupdate.put(MapleStat.智力, toSet);
                                break;
                        case 0x200:
                                toSet = playerst.getLuk() - 1;
                                playerst.setLuk((short) (int) toSet, chr);
                                statupdate.put(MapleStat.运气, toSet);
                                break;
                        case 0x400:
                                int maxhp = playerst.getMaxHp();
                                if (GameConstants.is新手职业(job)) {
                                        maxhp -= 12;
                                    }else if (((job >= 200) && (job <= 232))) {
                                        maxhp -= 10;
                                    } else if (((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434))) {
                                        maxhp -= 15;
                                    } else if (((job >= 500) && (job <= 592))) {
                                        maxhp -= 22;
                                    } else if (((job >= 100) && (job <= 132))) {
                                        maxhp -= 32;
                                    }else {
                                        maxhp -= 20;
                                    }
                                chr.setHpApUsed((short) (chr.getHpApUsed() - 1));
                                playerst.setMaxHp(maxhp, chr);
                                statupdate.put(MapleStat.MAXHP, (long) maxhp);
                                break;
                        case 0x800:
                                int maxmp = playerst.getMaxMp();
                                if (GameConstants.is新手职业(job)) {
                                    maxmp -= 8;
                                } else {
                                    if (((job >= 100) && (job <= 132)) || ((job >= 1100) && (job <= 1112)) || ((job >= 5100) && (job <= 5112))) {
                                        maxmp -= 4;
                                    } else if (((job >= 200) && (job <= 232)) || ((job >= 1200) && (job <= 1212)) || ((job >= 2700) && (job <= 2712))) {
                                        maxmp -= 30;
                                    } else if (((job >= 500) && (job <= 592)) || ((job >= 300) && (job <= 322)) || ((job >= 400) && (job <= 434)) || ((job >= 1300) && (job <= 1312)) || ((job >= 1400) && (job <= 1412)) || ((job >= 1500) && (job <= 1512)) || ((job >= 3300) && (job <= 3312)) || ((job >= 3500) && (job <= 3512)) || ((job >= 2300) && (job <= 2312)) || ((job >= 2400) && (job <= 2412))) {
                                        maxmp -= 10;
                                    } else if ((job >= 2000) && (job <= 2112)) {
                                        maxmp -= 5;
                                    } else {
                                        maxmp -= 20;
                                    }
                                }
                                chr.setHpApUsed((short) (chr.getHpApUsed() - 1));
                                playerst.setMaxMp(maxmp, chr);
                                statupdate.put(MapleStat.MAXMP, (long) maxmp);
                        }

                        c.getSession().write(MaplePacketCreator.updatePlayerStats(statupdate, true, chr));
                    }
                }else {
                    // 洗技能点
                    // 2B 0B 00 A1 43 21 00 40 42 0F 00 41 42 0F 00
                    used = false;
                    int skillTo = slea.readInt();
                    int skillFrom = slea.readInt();
                    Skill skillSPTo = SkillFactory.getSkill(skillTo);
                    Skill skillSPFrom = SkillFactory.getSkill(skillFrom);
                    if ((itemId == 2180001 && (skillTo/10000)%100 == 0 && (skillFrom/10000)%100 == 0)
                         || (itemId == 2180002 && ((skillTo/10000)%100 == 10 || (skillTo/10000)%100 == 20 || (skillTo/10000)%100 == 30) && ((skillFrom/10000)%100 != 10 || (skillFrom/10000)%100 != 20 || (skillFrom/10000)%100 != 30))
                         || (itemId == 2180003 && ((skillTo/10000)%100 == 11 || (skillTo/10000)%100 == 21 || (skillTo/10000)%100 == 31) && ((skillFrom/10000)%100 != 11 || (skillFrom/10000)%100 != 21 || (skillFrom/10000)%100 != 31))
                            ) {
                        if (GameConstants.getSkillBookBySkill(skillTo) != GameConstants.getSkillBookBySkill(skillFrom)) {
                            chr.dropMessage(1, "只能选择同一职业下的技能。");
                        } else if ((chr.getSkillLevel(skillSPTo) + 1 <= skillSPTo.getMaxLevel()) && (chr.getSkillLevel(skillSPFrom) > 0) && (skillSPTo.canBeLearnedBy(chr.getJob()))) {
                            chr.changeSingleSkillLevel(skillSPFrom, (byte) (chr.getSkillLevel(skillSPFrom) - 1), chr.getMasterLevel(skillSPFrom));
                            chr.changeSingleSkillLevel(skillSPTo, (byte) (chr.getSkillLevel(skillSPTo) + 1), chr.getMasterLevel(skillSPTo));
                            used = true;
                        }
                    } else {
                        chr.dropMessage(1, "请使用相应的洗点技能卡，不要作弊！");
                    }
                }
                break;
            case 208: // 喇叭
                // 2B 08 00 E8 C0 1F 00 0A 00 BA EC C0 AE B0 C8 B2 E2 CA D4
                if (chr.isShowPacket()) {
                    chr.dropMessage(0, new StringBuilder().append("使用商场喇叭 道具类型: ").append(itemId / 1000 % 10).toString());
                }
                if (!c.getChannelServer().getMegaphoneMuteState()) {
                    int msgType = itemId / 1000 % 10;
                    used = true;
                    switch (msgType) {
                        case 0:
                            chr.getMap().broadcastMessage(MaplePacketCreator.serverMessageMega(new StringBuilder().append(chr.getMedalText()).append(chr.getName()).append(" : ").append(slea.readMapleAsciiString()).toString()));
                            break;
                        case 1:
                            c.getChannelServer().broadcastSmegaPacket(MaplePacketCreator.serverMessageMega(new StringBuilder().append(chr.getMedalText()).append(chr.getName()).append(" : ").append(slea.readMapleAsciiString()).toString()));
                            break;
                        case 2:
                            WorldBroadcastService.getInstance().broadcastSmega(MaplePacketCreator.serverNotice(3, c.getChannel(), new StringBuilder().append(chr.getMedalText()).append(chr.getName()).append(" : ").append(slea.readMapleAsciiString()).toString(), slea.readByte() != 0));
                            break;
                    }
                } else {
                    chr.dropMessage(5, "当前频道禁止使用道具喇叭。");
                }
                break;
            case 213: // 真情告白
                // 2B 09 00 51 80 20 00
                // 31 00 31 32 33 32 31 34 33 32 34 33 71 65 77 71 65 77 71 65 77 71 65 0A 65 77 71 65 77 71 65 77 0A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A 7A
                MapleLove love = new MapleLove(chr, chr.getPosition(), chr.getMap().getFootholds().findBelow(chr.getPosition()).getId(), slea.readMapleAsciiString(), itemId);
                chr.getMap().spawnLove(love);
                used = true;
                break;
            case 216: // 消息
                String sendTo = slea.readMapleAsciiString();
                String msg = slea.readMapleAsciiString();
                chr.sendNote(sendTo, msg);
                used = true;
                break;
            case 215: // 音乐盒
                chr.getMap().startJukebox(chr.getName()+"演奏了祝贺曲！", itemId);
                used = true;
                break;
            case 209: // 地图祝福
                msg = ii.getMsg(itemId);
                String ourMsg = slea.readMapleAsciiString();
                if (!msg.contains("%s")) {
                    msg = ourMsg;
                } else {
                    msg = msg.replaceFirst("%s", chr.getName());
                    if (!msg.contains("%s")) {
                        msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                    } else {
                        try {
                            msg = msg.replaceFirst("%s", ourMsg);
                        } catch (Exception e) {
                            msg = ii.getMsg(itemId).replaceFirst("%s", ourMsg);
                        }
                    }
                }
                chr.getMap().startMapEffect(msg, itemId);
                int buff = ii.getStateChangeItem(itemId);
                if (buff != 0) {
                    for (MapleCharacter mChar : chr.getMap().getCharactersThreadsafe()) {
                        ii.getItemEffect(buff).applyTo(mChar);
                    }
                }
                used = true;
                break;
            case 404: // 表情
                break;
            case 405: // 美发卡
                if ((itemId >= 5152100) && (itemId <= 5152107)) {
                    int color = (itemId - 5152100) * 100;

                    if (color >= 0) {
                        if (changeFace(chr, color)) {
                            used = true;
                        } else {
                            chr.dropMessage(1, "当前脸型不支持这个颜色的隐形眼镜。");
                        }
                    } else {
                        chr.dropMessage(1, "使用一次性隐形眼镜出现错误。");
                    }
                } else if (itemId == 5156000) {
                    if ((chr.getMarriageId() > 0) || (chr.getMarriageRing() != null)) {
                        chr.dropSpouseMessage(11, "已婚人士无法使用。");
                    } else {
                        chr.setGender(chr.getGender() == 0 ? 1 : (byte) 0);
                        Pair ret = GameConstants.getDefaultFaceAndHair(chr.getJob(), chr.getGender());
                        Map statup = new EnumMap(MapleStat.class);
                        chr.setFace(((Integer) ret.getLeft()));
                        chr.setHair(((Integer) ret.getRight()));
                        statup.put(MapleStat.脸型, (long) chr.getFace());
                        statup.put(MapleStat.发型, (long) chr.getHair());
                        c.getSession().write(MaplePacketCreator.updatePlayerStats(statup, chr));
                        c.getSession().write(MaplePacketCreator.showOwnCraftingEffect("Effect/BasicEff.img/TransGender", 0, 0));
                        chr.getMap().broadcastMessage(chr, MaplePacketCreator.showCraftingEffect(chr.getId(), "Effect/BasicEff.img/TransGender", 0, 0), false);
                        chr.equipChanged();
                        used = true;
                    }
                } else {
                    chr.dropMessage(1, "暂不支持这个道具的使用。");
                }
                break;
            case 211: // 宠物取名卡
                int uniqueid = (int) slea.readLong();
                MaplePet pet = null;
                for (MaplePet petx : chr.getPets()) {
                    if ((petx != null) && (petx.getUniqueId() == uniqueid)) {
                        pet = petx;
                        break;
                    }
                }
                if (pet == null) {
                    chr.dropMessage(1, "宠物改名错误，找不到宠物的信息。");
                } else {
                    String nName = slea.readMapleAsciiString();
                    for (String z : GameConstants.RESERVED) {
                        if ((pet.getName().contains(z)) || (nName.contains(z))) {
                            break;
                        }
                    }
                    if (!MapleCharacterUtil.canChangePetName(nName)) {
                        break;
                    }
                    pet.setName(nName);
                    pet.saveToDb();
                    c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
                    c.getSession().write(MaplePacketCreator.enableActions());
                    chr.getMap().broadcastMessage(MTSCSPacket.changePetName(chr, nName, pet.getInventoryPosition()));
                    used = true;
                }
                break;
            case 519:
                if ((itemId >= 5190000) && (itemId <= 5190011)) {
                    uniqueid = (int) slea.readLong();
                    pet = null;
                    for (MaplePet petx : chr.getPets()) {
                        if ((petx != null) && (petx.getUniqueId() == uniqueid)) {
                            pet = petx;
                            break;
                        }
                    }
                    if (pet == null) {
                        chr.dropMessage(1, "宠物改名错误，找不到宠物的信息。");
                    } else {
                        PetFlag petFlag = PetFlag.getByAddId(itemId);
                        if ((petFlag == null) || (petFlag.check(pet.getFlags()))) {
                            break;
                        }
                        pet.setFlags(pet.getFlags() | petFlag.getValue());
                        pet.saveToDb();
                        c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
                        c.getSession().write(MaplePacketCreator.enableActions());
                        c.getSession().write(MTSCSPacket.changePetFlag(uniqueid, true, petFlag.getValue()));
                        used = true;
                    }
                } else {
                    if ((itemId < 5191000) || (itemId > 5191004)) {
                        break;
                    }
                    uniqueid = (int) slea.readLong();
                    pet = null;
                    for (MaplePet petx : chr.getPets()) {
                        if ((petx != null) && (petx.getUniqueId() == uniqueid)) {
                            pet = petx;
                            break;
                        }
                    }
                    if (pet == null) {
                        chr.dropMessage(1, "宠物改名错误，找不到宠物的信息。");
                    } else {
                        PetFlag petFlag = PetFlag.getByDelId(itemId);
                        if ((petFlag != null) && (petFlag.check(pet.getFlags()))) {
                            pet.setFlags(pet.getFlags() - petFlag.getValue());
                            pet.saveToDb();
                            c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
                            c.getSession().write(MaplePacketCreator.enableActions());
                            c.getSession().write(MTSCSPacket.changePetFlag(uniqueid, false, petFlag.getValue()));
                            used = true;
                        }
                    }
                }
                break;
            case 214: // 金币包
                int mesars = ii.getMeso(itemId);
                if ((mesars > 0) && (chr.getMeso() < 2147483647 - mesars)) {
                    used = true;
                    if (Math.random() > 0.1D) {
                        int gainmes = Randomizer.nextInt(mesars);
                        chr.gainMeso(gainmes, false);
                        c.getSession().write(MTSCSPacket.sendMesobagSuccess(gainmes));
                    } else {
                        c.getSession().write(MTSCSPacket.sendMesobagFailed());
                    }
                } else {
                    chr.dropMessage(1, "金币已达到上限无法使用这个道具。");
                }
                break;
            case 522:
                if (itemId == 5220083) {
                    used = true;
                    for (Entry<Integer, StructFamiliar> f : ii.getFamiliars().entrySet()) {
                        if ((((StructFamiliar) f.getValue()).itemid == 2870055) || (((StructFamiliar) f.getValue()).itemid == 2871002) || (((StructFamiliar) f.getValue()).itemid == 2870235) || (((StructFamiliar) f.getValue()).itemid == 2870019)) {
                            MonsterFamiliar mf = (MonsterFamiliar) chr.getFamiliars().get(f.getKey());
                            if (mf != null) {
                                if (mf.getVitality() >= 3) {
                                    mf.setExpiry(Math.min(System.currentTimeMillis() + 7776000000L, mf.getExpiry() + 2592000000L));
                                } else {
                                    mf.setVitality(mf.getVitality() + 1);
                                    mf.setExpiry(mf.getExpiry() + 2592000000L);
                                }
                            } else {
                                mf = new MonsterFamiliar(chr.getId(), (f.getKey()), System.currentTimeMillis() + 2592000000L);
                                chr.getFamiliars().put(f.getKey(), mf);
                            }
                            c.getSession().write(MaplePacketCreator.registerFamiliar(mf));
                        }
                    }
                } else if (itemId == 5220084) {
                    if (chr.getInventory(MapleInventoryType.USE).getNumFreeSlot() < 3) {
                        chr.dropMessage(5, "请确保您有足够的背包空间。");
                    } else {
                        used = true;
                        int[] familiars = new int[3];
                        while (true) {
                            for (int i = 0; i < familiars.length; i++) {
                                if (familiars[i] > 0) {
                                    continue;
                                }
                                for (Map.Entry f : ii.getFamiliars().entrySet()) {
                                    if ((Randomizer.nextInt(500) == 0) && (((i < 2) && (((StructFamiliar) f.getValue()).grade == 0)) || ((i == 2) && (((StructFamiliar) f.getValue()).grade != 0)))) {
                                        MapleInventoryManipulator.addById(c, ((StructFamiliar) f.getValue()).itemid, (short) 1, "Booster Pack");
                                        c.getSession().write(MTSCSPacket.getBoosterFamiliar(chr.getId(), ((Integer) f.getKey()), 0));
                                        familiars[i] = ((StructFamiliar) f.getValue()).itemid;
                                        break;
                                    }
                                }
                            }
                            if ((familiars[0] > 0) && (familiars[1] > 0) && (familiars[2] > 0)) {
                                break;
                            }
                        }
                        c.getSession().write(MTSCSPacket.getBoosterPack(familiars[0], familiars[1], familiars[2]));
                        c.getSession().write(MTSCSPacket.getBoosterPackClick());
                        c.getSession().write(MTSCSPacket.getBoosterPackReveal());
                    }
                } else {
                    chr.dropMessage(1, "暂时无法使用这个道具。");
                }
                break;
            case 523:
                int itemSearch = slea.readInt();
                List hms = c.getChannelServer().searchMerchant(itemSearch);
                if (hms.size() > 0) {
                    c.getSession().write(MaplePacketCreator.getOwlSearched(itemSearch, hms));
                    used = true;
                } else {
                    chr.dropMessage(1, "没有找到这个道具。");
                }
                MapleCharacterUtil.addToItemSearch(itemSearch);
                break;
            case 212:
                pet = null;
                MaplePet pets = chr.getSpawnPets();
                if (pet == null) {
                    chr.dropMessage(1, "没有可以喂食的宠物。\r\n请重新确认。");
                } else {
                    pet.setFullness(100);
                    if (pet.getCloseness() < 30000) {
                        if (pet.getCloseness() + 100 * c.getChannelServer().getTraitRate() > 30000) {
                            pet.setCloseness(30000);
                        } else {
                            pet.setCloseness(pet.getCloseness() + 100 * c.getChannelServer().getTraitRate());
                        }
                        if (pet.getCloseness() >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                            pet.setLevel(pet.getLevel() + 1);
                            c.getSession().write(PetPacket.showOwnPetLevelUp());
                            chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr));
                        }
                    }
                    c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition()), false));
                    chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, true, true), true);
                    used = true;
                }
                break;
            default:
                FileoutputUtil.log(new StringBuilder().append("使用未处理的商城道具 : ").append(itemId).toString());
                FileoutputUtil.log(slea.toString(true));
        }

        if ((itemType != 506) || (used)) {
            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, (short) slot, (short) 1, false, true);
        }
        c.getSession().write(MaplePacketCreator.enableActions());
        if (cc) {
            if ((!chr.isAlive()) || (chr.getEventInstance() != null) || (FieldLimitType.ChannelSwitch.check(chr.getMap().getFieldLimit()))) {
                chr.dropMessage(1, "刷新人物数据失败。");
                return;
            }
            chr.dropMessage(5, "正在刷新人数据.请等待...");
            chr.fakeRelog();
            if (chr.getScrolledPosition() != 0) {
                c.getSession().write(MaplePacketCreator.pamSongUI());
            }
        }
    }

    private static boolean getIncubatedItems(MapleClient c, int itemId) {
        if ((c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 2) || (c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 2) || (c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 2)) {
            c.getPlayer().dropMessage(5, "请确保你有足够的背包空间。");
            return false;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        int id1 = RandomRewards.getPeanutReward();
        int id2 = RandomRewards.getPeanutReward();
        while (!ii.itemExists(id1)) {
            id1 = RandomRewards.getPeanutReward();
        }
        while (!ii.itemExists(id2)) {
            id2 = RandomRewards.getPeanutReward();
        }
        c.getSession().write(MaplePacketCreator.getPeanutResult(id1, (short) 1, id2, (short) 1, itemId));
        MapleInventoryManipulator.addById(c, id1, (short) 1, new StringBuilder().append(ii.getName(itemId)).append(" 在 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        MapleInventoryManipulator.addById(c, id2, (short) 1, new StringBuilder().append(ii.getName(itemId)).append(" 在 ").append(FileoutputUtil.CurrentReadable_Date()).toString());
        return true;
    }

    private static boolean changeFace(MapleCharacter player, int color) {
        if (player.getFace() % 1000 < 100) {
            color += player.getFace();
        } else if ((player.getFace() % 1000 >= 100) && (player.getFace() % 1000 < 200)) {
            color += player.getFace() - 100;
        } else if ((player.getFace() % 1000 >= 200) && (player.getFace() % 1000 < 300)) {
            color += player.getFace() - 200;
        } else if ((player.getFace() % 1000 >= 300) && (player.getFace() % 1000 < 400)) {
            color += player.getFace() - 300;
        } else if ((player.getFace() % 1000 >= 400) && (player.getFace() % 1000 < 500)) {
            color += player.getFace() - 400;
        } else if ((player.getFace() % 1000 >= 500) && (player.getFace() % 1000 < 600)) {
            color += player.getFace() - 500;
        } else if ((player.getFace() % 1000 >= 600) && (player.getFace() % 1000 < 700)) {
            color += player.getFace() - 600;
        } else if ((player.getFace() % 1000 >= 700) && (player.getFace() % 1000 < 800)) {
            color += player.getFace() - 700;
        }
        if (MapleItemInformationProvider.getInstance().faceExists(color)) {
            return false;
        }
        player.setFace(color);
        player.updateSingleStat(MapleStat.脸型, player.getFace());
        player.equipChanged();
        return true;
    }
}
