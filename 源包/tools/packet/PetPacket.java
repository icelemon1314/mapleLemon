package tools.packet;

import client.MapleCharacter;
import client.MapleStat;
import client.inventory.Item;
import client.inventory.MaplePet;
import handling.SendPacketOpcode;
import java.awt.Point;
import java.util.List;
import server.movement.LifeMovementFragment;
import tools.data.output.MaplePacketLittleEndianWriter;

public class PetPacket {

    public static byte[] updatePet(MaplePet pet, Item item, boolean active) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(0);
        mplew.write(2);
        mplew.write(0);
        mplew.write(3);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(0);
        mplew.write(5);
        mplew.writeShort(pet.getInventoryPosition());
        mplew.write(3);
        mplew.writeInt(pet.getPetItemId());
        mplew.write(1);
        mplew.writeLong(pet.getUniqueId());
        PacketHelper.addPetItemInfo(mplew, item, pet, active);

        return mplew.getPacket();
    }

    public static byte[] showPetPickUpMsg(boolean canPickup, int pets) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_PICKUP_MSG.getValue());
        mplew.write(canPickup ? 1 : 0);
        mplew.write(pets);

        return mplew.getPacket();
    }

    public static byte[] showPetAutoEatMsg() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_AUTO_EAT_MSG.getValue());

        return mplew.getPacket();
    }

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger) {
        return showPet(chr, pet, remove, hunger, false);
    }

    public static byte[] showPet(MapleCharacter chr, MaplePet pet, boolean remove, boolean hunger, boolean show) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(show ? SendPacketOpcode.SHOW_PET.getValue() : SendPacketOpcode.SPAWN_PET.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getPetIndex(pet));
        mplew.write(remove ? 0 : 1);
        mplew.write(hunger ? 1 : 0);
        if (!remove) {
            addPetInfo(mplew, chr, pet, false);
        }

        return mplew.getPacket();
    }

    public static void addPetInfo(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, MaplePet pet, boolean showpet) {
        if (showpet) {
            mplew.write(1);
            mplew.writeInt(chr.getPetIndex(pet));
        }
        mplew.writeInt(pet.getPetItemId());
        mplew.writeMapleAsciiString(pet.getName());
        mplew.writeLong(pet.getUniqueId());
        mplew.writePos(pet.getPos());
        mplew.write(pet.getStance());
        mplew.writeShort(pet.getFh());
        mplew.writeInt(-1);
        mplew.writeInt(100);
    }

    public static byte[] movePet(int chrId, int slot, Point startPos, List<LifeMovementFragment> moves) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.MOVE_PET.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(slot);
        mplew.writeInt(0);
        mplew.writePos(startPos);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);

        return mplew.getPacket();
    }

    public static byte[] petChat(int chaId, int un, String text, byte slot) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_CHAT.getValue());
        mplew.writeInt(chaId);
        mplew.writeInt(slot);
        mplew.writeShort(un);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }

    public static byte[] commandResponse(int chrId, byte command, byte slot, boolean success, boolean food) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_COMMAND.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(slot);
        mplew.write(food ? 2 : 1);
        if (food) {
            mplew.write(success ? 1 : 0);
            mplew.writeInt(0);
        } else {
            mplew.write(command);
            mplew.writeShort(0);
        }
        return mplew.getPacket();
    }

    public static byte[] showOwnPetLevelUp(byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_SPECIAL_EFFECT.getValue());
        mplew.write(0x7);
        mplew.write(0);
        mplew.writeInt(index);
        mplew.writeZero(3);

        return mplew.getPacket();
    }

    public static byte[] showPetLevelUp(MapleCharacter chr, byte index) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(6);
        mplew.write(0);
        mplew.writeInt(index);
        mplew.writeZero(3);

        return mplew.getPacket();
    }

    public static byte[] loadExceptionList(MapleCharacter chr, MaplePet pet) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.PET_EXCEPTION_LIST.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getPetIndex(pet));
        mplew.writeLong(pet.getUniqueId());
        List excluded = pet.getExcluded();
        mplew.write(excluded.size());
        for (Object excluded1 : excluded) {
            mplew.writeInt(((Integer) excluded1));
        }

        return mplew.getPacket();
    }

    public static byte[] petStatUpdate(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.write(SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write(0);
        mplew.writeLong(MapleStat.宠物.getValue());
        MaplePet[] pets = chr.getSpawnPets();
        for (int i = 0; i < 3; i++) {
            if (pets[i] != null) {
                mplew.writeLong(pets[i].getUniqueId());
            } else {
                mplew.writeLong(0L);
            }
        }
        mplew.write(0);
        mplew.writeShort(0);

        return mplew.getPacket();
    }
}
