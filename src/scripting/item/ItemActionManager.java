package scripting.item;

import client.MapleClient;
import client.Skill;
import client.SkillEntry;
import client.SkillFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import java.util.Map;
import javax.script.Invocable;
import scripting.ScriptType;
import scripting.npc.NPCConversationManager;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;

public class ItemActionManager extends NPCConversationManager {

    private final Item item;

    public ItemActionManager(MapleClient c, int npc, Item item, Invocable iv) {
        super(c, npc, String.valueOf(item.getItemId()), ScriptType.ITEM, iv);
        this.item = item;
    }

    public String getSkillMenu(int skillMaster) {
        String menu = "";
        for (Map.Entry<Skill, SkillEntry> ret : c.getPlayer().getSkills().entrySet()) {
            if (GameConstants.getSkillBookBySkill(ret.getKey().getId()) > 2 && ret.getKey().getMaxLevel() > 10 && ret.getValue().masterlevel < ret.getKey().getMaxLevel()) {
                if (skillMaster > 20) {
                    if (ret.getValue().masterlevel < 30 && ret.getValue().masterlevel >= 20 && ret.getKey().getMaxLevel() > 20) {
                        menu += "\r\n#L" + ret.getKey().getId() + "# #s" + ret.getKey().getId() + "# #fn黑体##fs14##e#q" + ret.getKey().getId() + "##n#fs##fn##l";
                    }
                } else {
                    if (ret.getValue().masterlevel < 20) {
                        menu += "\r\n#L" + ret.getKey().getId() + "# #s" + ret.getKey().getId() + "# #fn黑体##fs14##e#q" + ret.getKey().getId() + "##n#fs##fn##l";
                    }
                }
            }
        }
        return menu;
    }

    public boolean canUseSkillBook(int skillId, int masterLevel) {
        if (masterLevel > 0) {
            final Skill CurrSkillData = SkillFactory.getSkill(skillId);
            if (c.getPlayer().getSkillLevel(CurrSkillData) >= CurrSkillData.getMaxLevel()) {
                return false;
            }
            int a = c.getPlayer().getSkillLevel(CurrSkillData);
            if ((c.getPlayer().getSkillLevel(CurrSkillData) >= 5 && masterLevel == 20) || (c.getPlayer().getSkillLevel(CurrSkillData) >= 15 && masterLevel == 30)) {
                return true;
            }
        }
        return false;
    }

    public void useSkillBook(int skillId, int masterLevel) {
        final Skill CurrSkillData = SkillFactory.getSkill(skillId);
        masterLevel = masterLevel > CurrSkillData.getMaxLevel() ? CurrSkillData.getMaxLevel() : masterLevel;
        c.getPlayer().changeSingleSkillLevel(CurrSkillData, c.getPlayer().getSkillLevel(CurrSkillData), (byte) masterLevel);
        c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.useSkillBook(c.getPlayer(), 0, 0, true, true));
        c.getSession().write(MaplePacketCreator.enableActions());
    }

    public Item getItem() {
        return item;
    }

    public int getItemId() {
        return item.getItemId();
    }

    public int getPosition() {
        return item.getPosition();
    }

    public boolean used() {
        return used(1);
    }

    public boolean used(int q) {
        return MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(item.getType()), item.getPosition(), (short) q, true, false);
    }

    public boolean usedAll() {
        return MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(item.getType()), item.getPosition(), (short) item.getQuantity(), true, false);
    }

    public void dispose(int remove) {
        if (remove == 0) {
            usedAll();
        } else if (remove > 0) {
            used(remove);
        }
        ItemScriptManager.getInstance().dispose(this, getClient());
    }

    @Override
    public void dispose() {
        dispose(-1);
    }
}
