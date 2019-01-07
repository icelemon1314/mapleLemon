package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StructItemOption {

    public static String[] types = {
        "incSTR", 
        "incDEX", 
        "incINT", 
        "incLUK", 
        "incACC", 
        "incEVA", 
        "incPAD", 
        "incMAD", 
        "incPDD", 
        "incMDD", 
        "incMHP", 
        "incMMP", 
        "incSTRr", 
        "incDEXr", 
        "incINTr", 
        "incLUKr", 
        "incACCr", 
        "incEVAr", 
        "incPADr", 
        "incMADr", 
        "incPDDr", 
        "incMDDr", 
        "incMHPr", 
        "incMMPr", 
        "incSTRlv", 
        "incDEXlv", 
        "incINTlv", 
        "incLUKlv", 
        "incPADlv", 
        "incMADlv", 
        "incSpeed", 
        "incJump", 
        "incCr", 
        "incDAMr", 
        "incTerR", 
        "incAsrR", 
        "incEXPr", 
        "incMaxDamage", 
        "HP", 
        "MP", 
        "RecoveryHP",
        "RecoveryMP", 
        "level", 
        "prop", 
        "time", 
        "ignoreTargetDEF", 
        "ignoreDAM", 
        "incAllskill", 
        "ignoreDAMr", 
        "RecoveryUP", 
        "incCriticaldamageMin", 
        "incCriticaldamageMax", 
        "DAMreflect", 
        "mpconReduce", 
        "reduceCooltime", 
        "incMesoProp", 
        "incRewardProp", 
        "boss", 
        "attackType"
    };
    public int optionType;
    public int reqLevel;
    public int opID;
    public String face;
    public String opString;
    public Map<String, Integer> data = new HashMap();

    public int get(String type) {
        return this.data.get(type) != null ? this.data.get(type) : 0;
    }

    public String[] get潜能属性() {
        ArrayList<String> ss = new ArrayList();
        for (String s : StructItemOption.types) {
            if (get(s) > 0) {
                ss.add(s);
            }
        }
        String[] s = new String[ss.size()];
        for (int i = 0; i < ss.size() ; i++) {
            s[i] = ss.get(i);
        }
        return s;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (get("incMesoProp") > 0) {
            ret.append("金币获得量 : +");
            ret.append(get("incMesoProp"));
            ret.append("% ");
        }
        if (get("incRewardProp") > 0) {
            ret.append("物品获得概率 : +");
            ret.append(get("incRewardProp"));
            ret.append("% ");
        }
        if (get("incSTR") > 0) {
            ret.append("力量 : +");
            ret.append(get("incSTR"));
            ret.append(" ");
        }
        if (get("incDEX") > 0) {
            ret.append("敏捷 : +");
            ret.append(get("incDEX"));
            ret.append(" ");
        }
        if (get("incINT") > 0) {
            ret.append("智力 : +");
            ret.append(get("incINT"));
            ret.append(" ");
        }
        if (get("incLUK") > 0) {
            ret.append("运气 : +");
            ret.append(get("incLUK"));
            ret.append(" ");
        }
        if (get("incSTRr") > 0) {
            ret.append("力量 : +");
            ret.append(get("incSTRr"));
            ret.append("% ");
        }
        if (get("incDEXr") > 0) {
            ret.append("敏捷 : +");
            ret.append(get("incDEXr"));
            ret.append("% ");
        }
        if (get("incINTr") > 0) {
            ret.append("智力 : +");
            ret.append(get("incINTr"));
            ret.append("% ");
        }
        if (get("incLUKr") > 0) {
            ret.append("运气 : +");
            ret.append(get("incLUKr"));
            ret.append("% ");
        }
        return ret.toString();
    }
}
