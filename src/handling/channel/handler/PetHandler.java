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

import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class PetHandler {

    public static void SpawnPet(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {

    }

    public static void Pet_AutoPotion(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        slea.skip(1);
        short slot = slea.readShort();
        if ((!chr.isAlive()) || (chr.getMapId() == 749040100) || (chr.getMap() == null)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        Item toUse = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((toUse == null) || (toUse.getQuantity() < 1) || (toUse.getItemId() != slea.readInt())) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        long time = System.currentTimeMillis();
        if (chr.getNextConsume() > time) {
            chr.dropMessage(5, "暂时无法使用道具.");
            c.sendPacket(MaplePacketCreator.enableActions());
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
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }

    public static void PetExcludeItems(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        int petSlot = slea.readInt();
        MaplePet pet = chr.getSpawnPet();
        if ((pet == null) || (!PetFlag.PET_IGNORE_PICKUP.check(pet.getFlags()))) {
            c.sendPacket(MaplePacketCreator.enableActions());
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
            c.sendPacket(MaplePacketCreator.enableActions());
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
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        byte command = slea.readByte();
        PetCommand petCommand = PetDataFactory.getPetCommand(pet.getPetItemId(), command);
        if (petCommand == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
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
                    c.sendPacket(PetPacket.showOwnPetLevelUp());
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr));
                }
                c.sendPacket(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
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

    }

    /**
     * 宠物移动
     * @param slea
     * @param chr
     */
    public static void MovePet(SeekableLittleEndianAccessor slea, MapleCharacter chr) {

    }

    public static void AllowPetLoot(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            c.sendPacket(MaplePacketCreator.enableActions());
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
                chr.getClient().sendPacket(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
            }
//        c.sendPacket(PetPacket.showPetPickUpMsg(data > 0, 1));
    }

    public static void AllowPetAutoEat(SeekableLittleEndianAccessor slea, MapleClient c, MapleCharacter chr) {
        if ((chr == null) || (chr.getMap() == null)) {
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        slea.skip(4);
        slea.skip(4);
        boolean data = slea.readByte() > 0;
//        c.sendPacket(PetPacket.showPetAutoEatMsg());
    }
}
