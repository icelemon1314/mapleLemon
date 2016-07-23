package handling;

import constants.ServerConstants;
import tools.EncodingDetect;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * 现金商城相关
 * @author 7
 */
public enum CashShopOpcode implements WritableIntValueHolder {

    加载道具栏,
    加载礼物,
    加载购物车,
    更新购物车,
    购买道具成功,
    购买道具失败,
    使用优惠券成功,
    礼物优惠券成功,
    使用优惠券失败,
    商城送礼,
    错误提示,
    扩充道具栏成功,
    扩充道具栏失败,
    扩充仓库,
    商城到背包,
    背包到商城,
    删除道具,
    道具到期,
    换购道具,
    购买礼包,
    商城送礼包,
    购买任务道具,
    领奖卡提示,
    注册商城,
    打开箱子,
    商城提示;

    private byte code = -2;

    @Override
    public void setValue(byte code) {
        this.code = code;
    }

    @Override
    public byte getValue() {
        return this.code;
    }

    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
        Properties props = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("cashops.properties")) {
            props.load(new BufferedReader(new InputStreamReader(fileInputStream, EncodingDetect.getJavaEncode("cashops.properties"))));
        }
        return props;
    }

    public static void reloadValues() {
        try {
            if (ServerConstants.loadop) {
                Properties props = new Properties();
                props.load(CashShopOpcode.class.getClassLoader().getResourceAsStream("cashops.ini"));
                ExternalCodeTableGetter.populateValues(props, values());
            } else {
                ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
            }
        } catch (IOException e) {
            throw new RuntimeException("加载 cashops.properties 文件出现错误", e);
        }
    }

    static {
        reloadValues();
    }
}
