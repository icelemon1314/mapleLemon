package tools.wztosql;

import provider.*;
import tools.FileoutputUtil;
import tools.Pair;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Icelemon1314
 */
public class DumpSkillInfo {
    private final MapleDataProvider string = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath", "wz") + "/wz/String.wz"));
    protected final  MapleData skillInfoData = string.getData("Skill.img");

    public static long start, end;
    public static List<Exception> exceptions = new LinkedList();
    public static List<Pair<Integer, Character>> a = new LinkedList();
    public static List<Character> b = new LinkedList();

    public void dumpSkill() throws Exception {
        for (MapleData data : skillInfoData.getChildren()) {
            if (data.getName().length() <= 4) {
                for (MapleData jobSkill : data.getChildren()) {
                    System.out.println(jobSkill.getData());
                }
            } else {
                String skillName = MapleDataTool.getString(data.getChildByPath("name"),"默认");
                FileoutputUtil.log("public static final int "+skillName+" = "+data.getName()+";");
            }
        }
    }

    public static void main(String[] args) {
        final DumpSkillInfo dq = new DumpSkillInfo();
        try {
            dq.dumpSkill();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
