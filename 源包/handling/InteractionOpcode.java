package handling;

import constants.ServerConstants;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import tools.EncodingDetect;

public enum InteractionOpcode implements WritableIntValueHolder {

    设置物品,
    设置物品_001,
    设置物品_002,
    设置物品_003,
    设置金币,
    设置金币_005,
    设置金币_006,
    设置金币_007,
    确认交易,
    确认交易_009,
    确认交易_00A,
    确认交易_00B,
    创建,
    访问,
    房间,
    交易邀请,
    拒绝邀请,
    聊天,
    聊天事件,
    打开,
    退出,
    玩家商店_添加道具,
    玩家商店_购买道具,
    雇佣商店_维护,
    添加物品,
    添加物品_0020,
    添加物品_0021,
    添加物品_0022,
    BUY_ITEM_STORE,
    雇佣商店_购买道具,
    雇佣商店_购买道具0024,
    雇佣商店_购买道具0025,
    雇佣商店_购买道具0026,
    雇佣商店_求购道具,
    移除物品,
    雇佣商店_开启,
    雇佣商店_整理,
    雇佣商店_关闭,
    雇佣商店_关闭完成,
    管理员修改雇佣商店名称,
    雇佣商店_查看访问名单,
    雇佣商店_查看黑名单,
    雇佣商店_添加黑名单,
    雇佣商店_移除黑名单,
    雇佣商店_修改商店名称,
    雇佣商店_错误提示,
    雇佣商店_更新信息,
    雇佣商店_维护开启,
    REQUEST_TIE,
    ANSWER_TIE,
    GIVE_UP,
    REQUEST_REDO,
    ANSWER_REDO,
    EXIT_AFTER_GAME,
    CANCEL_EXIT,
    READY,
    UN_READY,
    EXPEL,
    START,
    SKIP,
    MOVE_OMOK,
    SELECT_CARD;

    private byte code = -2;

    @Override
    public void setValue(byte code) {
        this.code = code;
    }

    @Override
    public byte getValue() {
        return this.code;
    }

    public static InteractionOpcode getByAction(int packetId) {
        for (InteractionOpcode interaction : values()) {
            if (interaction.getValue() == packetId) {
                return interaction;
            }
        }
        return null;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("Interaction.properties")) {
            props.load(new BufferedReader(new InputStreamReader(fileInputStream, EncodingDetect.getJavaEncode("Interaction.properties"))));
        }
        return props;
    }

    public static void reloadValues() {
        try {
            if (ServerConstants.loadop) {
                Properties props = new Properties();
                props.load(InteractionOpcode.class.getClassLoader().getResourceAsStream("Interaction.ini"));
                ExternalCodeTableGetter.populateValues(props, values());
            } else {
                ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
            }
        } catch (IOException e) {
            throw new RuntimeException("加载 Interaction.properties 文件出现错误", e);
        }
    }

    static {
        reloadValues();
    }
}
