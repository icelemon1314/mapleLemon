package handling.channel.handler;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.GameConstants;
import handling.MaplePacketHandler;
import handling.vo.recv.UsePetFoodRecvVO;
import server.MapleInventoryManipulator;
import server.Randomizer;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packet.PetPacket;

public class UsePetFoodHandler extends MaplePacketHandler<UsePetFoodRecvVO> {


    @Override
    public void handlePacket(UsePetFoodRecvVO recvVO, MapleClient c) {
        // 2A 05 00 40 59 20 00
        // 2A 19 00 40 59 20 00
        MapleCharacter chr = c.getPlayer();
        if ((chr == null) || (chr.getMap() == null)) {
            return;
        }
        int previousFullness = 100;
        MaplePet pet = chr.getSpawnPet();
        short slot = recvVO.getSlot();
        int itemId = recvVO.getItemId();
        Item petFood = chr.getInventory(MapleInventoryType.USE).getItem(slot);
        if ((pet == null) || (petFood == null) || (petFood.getItemId() != itemId) || (petFood.getQuantity() <= 0) || (itemId / 10000 != 212)) {
            c.sendPacket(MaplePacketCreator.enableActions());
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
                    c.sendPacket(PetPacket.showOwnPetLevelUp());
                    chr.getMap().broadcastMessage(PetPacket.showPetLevelUp(chr));
                }
            }
            c.sendPacket(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), false));
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
            c.sendPacket(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem((short) (byte) pet.getInventoryPosition()), true));
            chr.getMap().broadcastMessage(chr, PetPacket.commandResponse(chr.getId(), (byte) 1, false, true), true);
        }
        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, slot, (short) 1, true, false);
        c.sendPacket(MaplePacketCreator.enableActions());
    }
}
