package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleDisease;
import client.Skill;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.PetCommand;
import client.inventory.PetDataFactory;
import client.inventory.PetFlag;
import constants.GameConstants;
import java.awt.Point;
import java.util.List;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.Randomizer;
import server.maps.FieldLimitType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class PetHandler {

    public static void SpawnPet(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        chr.spawnPet(slea.readByte(), slea.readByte() > 0);
    }

    public static void Pet_AutoPotion(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.skip(1);
        short slot = slea.readShort();
        if ((!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != slea.readInt())) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "暂时无法使用道具.");
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        if (!FieldLimitType.PotionUse.check(chr.getMap().getFieldLimit())) {
            if (MapleItemInformationProvider.getInstance().getItemEffect(toUse.getItemId()).applyTo(chr)) {
                MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, false);
                if (chr.getMap().getConsumeItemCoolTime() > 0) {
                    chr.setNextConsume(time + chr.getMap().getConsumeItemCoolTime() * 1000);
                }
            }
        } else {
            c.getSession().write(MaplePacketCreator.enableActions());
        }
    }

    public static void PetExcludeItems(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int petSlot = slea.readInt();
        MaplePet pet = chr.getSpawnPet();
        if ((pet == null) || (!PetFlag.PET_IGNORE_PICKUP.check(pet.getFlags()))) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        pet.clearExcluded();
        byte amount = slea.readByte();
        for (int i = 0; i < amount; i++) {
            pet.addExcluded(i, slea.readInt());
        }
    }

    public static void PetChat(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if (slea.available() < 12L) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        int petid = slea.readInt();
        if ((chr == null) || (chr.getMap() == null) || (chr.getSpawnPet() == null)) {
            return;
        }
        short command = slea.readShort();
        String text = slea.readMapleAsciiString();
        chr.getMap().broadcastMessage(chr, PetPacket.petChat(chr.getId(), command, text, (byte) petid), true);
    }

    public static void PetCommand(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int petId = slea.readInt();
        MaplePet pet;
        pet = chr.getSpawnPet();
        slea.readByte();
        if (pet == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getPetItemId(), command);
        if (petCommand == null) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        boolean success = false;
        if (Randomizer.nextInt(99) <= petCommand.getProbability()) {
            success = true;
            if (pet.getCloseness() < 30000) {
                int newCloseness = pet.getCloseness() + petCommand.getIncrease() * c.getChannelServer().getTraitRate();
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(PetPacket.showOwnPetLevelUp());
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr));
                }
                c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
            }
        }
        chr.getMap().broadcastMessage(PetPacket.commandResponse(chr.getId(), (byte) petCommand.getCommand(), success, false));
    }

    /**
     * 宠物食品
     * @param slea
     * @param c
     * @param chr
     */
    public static void PetFood(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        // 2A 05 00 40 59 20 00
        // 2A 19 00 40 59 20 00
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int previousFullness = 100;
        MaplePet pet = chr.getSpawnPet();
        short slot = slea.readShort();
        int itemId = slea.readInt();
        Item petFood = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((pet == null) || (petFood == null) || (petFood.getItemId() != itemId) || (petFood.getQuantity() <= 0) || (itemId / 10000 != 212)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        boolean gainCloseness = false;
        if (Randomizer.nextInt(101) > 50) {
            gainCloseness = true;
        }
        if (pet.getFullness() < 100) {
            int newFullness = pet.getFullness() + 30;
            if (newFullness > 100) {
                newFullness = 100;
            }
            pet.setFullness(newFullness);
            if ((gainCloseness) && (pet.getCloseness() < 30000)) {
                int newCloseness = pet.getCloseness() + 1;
                if (newCloseness > 30000) {
                    newCloseness = 30000;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness >= GameConstants.getClosenessNeededForLevel(pet.getLevel() + 1)) {
                    pet.setLevel(pet.getLevel() + 1);
                    c.getSession().write(PetPacket.showOwnPetLevelUp());
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr));
                }
            }
            c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
            chr.getMap().broadcastMessage(c.getPlayer(), PetPacket.commandResponse(chr.getId(), (byte) 1, true, true), true);
        } else {
            if (gainCloseness) {
                int newCloseness = pet.getCloseness() - 1;
                if (newCloseness < 0) {
                    newCloseness = 0;
                }
                pet.setCloseness(newCloseness);
                if (newCloseness < GameConstants.getClosenessNeededForLevel(pet.getLevel())) {
                    pet.setLevel(pet.getLevel() - 1);
                }
                chr.dropMessage(5, "您的宠物的饥饿感是满值，如果继续使用将会有50%的几率减少1点亲密度。");
            }
            c.getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, false, true), true);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, true, false);
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    /**
     * 宠物移动
     * @param slea
     * @param chr
     */
    public static void MovePet(SeekableLittleEndianAccessor slea, MapleCharacter chr) {
        // 4C
        // 0F 00 B3 00
        // 06 00 08 00 B3 00 83 FF 02 00 15 00 03 3C 00 00 07 00 B4 00 83 FF FE FF 15 00 03 0A 00 00 F6 FF B3 00 84 FF FE FF 14 00 03 8C 00 00 EE FF B3 00 CC FF FF FF 14 00 19 5A 00 00 EC FF B3 00 FC FF 00 00 14 00 19 3C 00 00 EC FF B3 00 00 00 00 00 14 00 19 96 00 00
        MaplePet pet = chr.getSpawnPet();
        if (pet == null) {
            return;
        }
        Point startPos = slea.readPos();
        List res = MovementParse.parseMovement(slea, 3);
        if ((res != null) && (chr != null) && (!res.isEmpty()) && (chr.getMap() != null)) {
            if (slea.available() != 1) {
                FileoutputUtil.log("slea.available != 1 (宠物移动出错) 剩余封包长度: " + slea.available());
                FileoutputUtil.log(FileoutputUtil.Movement_Pet, "slea.available != 1 (宠物移动出错) 封包: " + slea.toString(true));
                return;
            }
            chr.getSpawnPet().updatePosition(res);
//            chr.getClient().getSession().write(PetPacket.movePet(chr.getId(),1,startPos,res));
            chr.getMap().broadcastMessage(chr, PetPacket.movePet(chr.getId(), (byte)1, startPos, res), false);
        }
    }

    public static void AllowPetLoot(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        slea.skip(4);
        int data = slea.readShort();
        if (data > 0) {
            chr.getQuestNAdd(MapleQuest.getInstance(122902)).setCustomData(String.valueOf(data));
        } else {
            chr.getQuestRemove(MapleQuest.getInstance(122902));
        }
        MaplePet pet = c.getPlayer().getSpawnPets();
            if ((pet != null) && (pet.getSummoned())) {
                pet.setCanPickup(data > 0);
                chr.getClient().getSession().write(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
            }
        c.getSession().write(PetPacket.showPetPickUpMsg(data > 0, 1));
    }

    public static void AllowPetAutoEat(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            c.getSession().write(MaplePacketCreator.enableActions());
            return;
        }
        slea.skip(4);
        slea.skip(4);
        boolean data = slea.readByte() > 0;
        c.getSession().write(PetPacket.showPetAutoEatMsg());
    }
}
