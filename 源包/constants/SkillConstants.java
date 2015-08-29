package constants;

import client.MapleCharacter;

public class SkillConstants {

    public static final int[] 触发性冷却技能 = {2311012/*神圣保护*/};

    public static boolean is触发性冷却技能(int skillId) {
        for (int i : 触发性冷却技能) {
            if (i == skillId) {
                return true;
            }
        }
        return false;
    }

}
