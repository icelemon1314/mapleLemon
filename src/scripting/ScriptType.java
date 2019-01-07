/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripting;

/**
 *
 * @author 7
 */
public enum ScriptType {

    NPC(-1),
    QUEST_START(0),
    QUEST_END(1),
    ITEM(-1),
    ON_FIRST_USER_ENTER(-1),
    ON_USER_ENTER(-1),
    PORTAL,
    REACTOR,
    EVENT;
    private byte code = -2;

    private ScriptType() {
    }

    private ScriptType(int value) {
        code = (byte) value;
    }

    public byte getValue() {
        return code;
    }
}
