package client.messages.commands;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.messages.PlayerGMRank;
import constants.BattleConstants;
import constants.ItemConstants;
import scripting.npc.NPCScriptManager;
import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import tools.FileoutputUtil;
import tools.StringUtil;

public class SuperDonatorCommand {

    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.SUPERDONATOR;
    }

    public static class OfferCash extends SuperDonatorCommand.OfferCommand {

        public OfferCash() {
            this.invType = 5;
        }
    }

    public static class OfferEtc extends SuperDonatorCommand.OfferCommand {

        public OfferEtc() {
            this.invType = 4;
        }
    }

    public static class OfferSetup extends SuperDonatorCommand.OfferCommand {

        public OfferSetup() {
            this.invType = 3;
        }
    }

    public static class OfferUse extends SuperDonatorCommand.OfferCommand {

        public OfferUse() {
            this.invType = 2;
        }
    }

    public static class OfferEquip extends SuperDonatorCommand.OfferCommand {

        public OfferEquip() {
            this.invType = 1;
        }
    }

    public static abstract class OfferCommand extends CommandExecute.TradeExecute {

        protected int invType = -1;

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length < 3) {
                c.getPlayer().dropMessage(-2, "[错误] : <数量> <道具名称>");
            } else if (c.getPlayer().getLevel() < 70) {
                c.getPlayer().dropMessage(-2, "[错误] : 只有等级达到70级以上的玩家才能使用这个命令");
            } else {
                int quantity = 1;
                try {
                    quantity = Integer.parseInt(splitted[1]);
                } catch (NumberFormatException e) {
                }
                String search = StringUtil.joinStringFrom(splitted, 2).toLowerCase();
                Item found = null;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                for (Item inv : c.getPlayer().getInventory(MapleInventoryType.getByType((byte) this.invType))) {
                    if ((ii.getName(inv.getItemId()) != null) && (ii.getName(inv.getItemId()).toLowerCase().contains(search))) {
                        found = inv;
                        break;
                    }
                }
                if (found == null) {
                    c.getPlayer().dropMessage(-2, "[错误] : 没有找到该道具 (" + search + ")");
                    return 0;
                }
                if ((ItemConstants.isPet(found.getItemId())) || (ItemConstants.isRechargable(found.getItemId()))) {
                    c.getPlayer().dropMessage(-2, "[错误] : 这个道具无法使用这个命令来进行交易");
                    return 0;
                }
                if ((quantity > found.getQuantity()) || (quantity <= 0) || (quantity > ii.getSlotMax(found.getItemId()))) {
                    c.getPlayer().dropMessage(-2, "[错误] : 输入的数量无效");
                    return 0;
                }
                if (!c.getPlayer().getTrade().setItems(c, found, (byte) -1, quantity)) {
                    c.getPlayer().dropMessage(-2, "[错误] : 放入道具失败");
                    return 0;
                }
                c.getPlayer().getTrade().chatAuto("[系统提示] : " + c.getPlayer().getName() + " offered " + ii.getName(found.getItemId()) + " x " + quantity);
            }

            return 1;
        }
    }

    public static class TradeHelp extends CommandExecute.TradeExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(-2, "[系统提示] : <@offerequip, @offeruse, @offersetup, @offeretc, @offercash> <数量> <道具名称>");
            return 1;
        }
    }

    public static class Check extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getCSPoints(1) + " Cash.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getPoints() + " donation points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getVPoints() + " voting points.");
            c.getPlayer().dropMessage(6, "You currently have " + c.getPlayer().getIntNoRecord(150001) + " Boss Party Quest points.");
            c.getPlayer().dropMessage(6, "当前时间: " + FileoutputUtil.CurrentReadable_TimeGMT());
            return 1;
        }
    }

    public static class TSmega extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            c.getPlayer().setSmega();
            return 1;
        }
    }

    public static class Pokemon extends SuperDonatorCommand.OpenNPCCommand {

        public Pokemon() {
            this.npc = 6;
        }
    }

    public static class Pokedex extends SuperDonatorCommand.OpenNPCCommand {

        public Pokedex() {
            this.npc = 5;
        }
    }

    public static class CheckDrop extends SuperDonatorCommand.OpenNPCCommand {

        public CheckDrop() {
            this.npc = 4;
        }
    }

    public static class Event extends SuperDonatorCommand.OpenNPCCommand {

        public Event() {
            this.npc = 2;
        }
    }

    public static class DCash extends SuperDonatorCommand.OpenNPCCommand {

        public DCash() {
            this.npc = 1;
        }
    }

    public static class Npc extends SuperDonatorCommand.OpenNPCCommand {

        public Npc() {
            this.npc = 0;
        }
    }

    public static abstract class OpenNPCCommand extends CommandExecute {

        protected int npc = -1;
        private static final int[] npcs = {9270035, 9270035, 9270035, 9270035, 9270035, 9270035, 9270035};

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if ((this.npc != 6) && (this.npc != 5) && (this.npc != 4) && (this.npc != 3) && (this.npc != 1) && (c.getPlayer().getMapId() != 910000000)) {
                if ((c.getPlayer().getLevel() < 10) && (c.getPlayer().getJob() != 200)) {
                    c.getPlayer().dropMessage(5, "等级达到10级才可以使用这个命令.");
                    return 0;
                }
                if (c.getPlayer().isInBlockedMap()) {
                    c.getPlayer().dropMessage(5, "无法在这里使用这个命令.");
                    return 0;
                }
            } else if ((this.npc == 1)
                    && (c.getPlayer().getLevel() < 70)) {
                c.getPlayer().dropMessage(5, "等级达到70级才可以使用这个命令.");
                return 0;
            }

            if (c.getPlayer().hasBlockedInventory()) {
                c.getPlayer().dropMessage(5, "无法在这里使用这个命令.");
                return 0;
            }
            NPCScriptManager.getInstance().start(c, npcs[this.npc]);
            return 1;
        }
    }

    public static class Challenge extends CommandExecute {

        @Override
        public int execute(MapleClient c, String[] splitted) {
            if (splitted.length <= 1) {
                c.getPlayer().dropMessage(6, "@challenge [playername OR accept/decline OR block/unblock]");
                return 0;
            }
            if (c.getPlayer().getBattler(0) == null) {
                c.getPlayer().dropMessage(6, "You have no monsters!");
                return 0;
            }
            if (splitted[1].equalsIgnoreCase("accept")) {
                if (c.getPlayer().getChallenge() > 0) {
                    MapleCharacter chr = c.getPlayer().getMap().getCharacterById(c.getPlayer().getChallenge());
                    if (chr != null) {
                        if (((c.getPlayer().isInTownMap()) || (c.getPlayer().isGM()) || (chr.isInTownMap()) || (chr.isGM())) && (chr.getBattler(0) != null) && (chr.getChallenge() == c.getPlayer().getId())) {
                            if (c.getPlayer().getPosition().y != chr.getPosition().y) {
                                c.getPlayer().dropMessage(6, "Please be near them.");
                                return 0;
                            }
                            if ((c.getPlayer().getPosition().distance(chr.getPosition()) > 600.0D) || (c.getPlayer().getPosition().distance(chr.getPosition()) < 400.0D)) {
                                c.getPlayer().dropMessage(6, "Please be at a moderate distance from them.");
                                return 0;
                            }
                            chr.setChallenge(0);
                            chr.dropMessage(6, c.getPlayer().getName() + " has accepted!");
                            c.getPlayer().setChallenge(0);
                        } else {
                            c.getPlayer().dropMessage(6, "You may only use it in towns, or the other character has no monsters, or something failed.");
                        }
                    } else {
                        c.getPlayer().dropMessage(6, "They do not exist in the map.");
                    }
                } else {
                    c.getPlayer().dropMessage(6, "You don't have a challenge.");
                }
            } else if (splitted[1].equalsIgnoreCase("decline")) {
                if (c.getPlayer().getChallenge() > 0) {
                    c.getPlayer().cancelChallenge();
                } else {
                    c.getPlayer().dropMessage(6, "You don't have a challenge.");
                }
            } else if (splitted[1].equalsIgnoreCase("block")) {
                if (c.getPlayer().getChallenge() == 0) {
                    c.getPlayer().setChallenge(-1);
                    c.getPlayer().dropMessage(6, "You have blocked challenges.");
                } else {
                    c.getPlayer().dropMessage(6, "You have a challenge or they are already blocked.");
                }
            } else if (splitted[1].equalsIgnoreCase("unblock")) {
                if (c.getPlayer().getChallenge() < 0) {
                    c.getPlayer().setChallenge(0);
                    c.getPlayer().dropMessage(6, "You have unblocked challenges.");
                } else {
                    c.getPlayer().dropMessage(6, "You didn't block challenges.");
                }
            } else if (c.getPlayer().getChallenge() == 0) {
                MapleCharacter chr = c.getChannelServer().getPlayerStorage().getCharacterByName(splitted[1]);
                if ((chr != null) && (chr.getMap() == c.getPlayer().getMap()) && (chr.getId() != c.getPlayer().getId())) {
                    if (((c.getPlayer().isInTownMap()) || (c.getPlayer().isGM()) || (chr.isInTownMap()) || (chr.isGM())) && (chr.getBattler(0) != null) && (chr.getChallenge() == 0)) {
                        chr.setChallenge(c.getPlayer().getId());
                        chr.dropMessage(6, c.getPlayer().getName() + " has challenged you! Type @challenge [accept/decline] to answer!");
                        c.getPlayer().setChallenge(chr.getId());
                        c.getPlayer().dropMessage(6, "Successfully sent the request.");
                    } else {
                        c.getPlayer().dropMessage(6, "You may only use it in towns, or the other character has no monsters, or they have a challenge.");
                    }
                } else {
                    c.getPlayer().dropMessage(6, splitted[1] + " does not exist in the map.");
                }
            } else {
                c.getPlayer().dropMessage(6, "You have a challenge or you have blocked them.");
            }

            return 1;
        }
    }
}
