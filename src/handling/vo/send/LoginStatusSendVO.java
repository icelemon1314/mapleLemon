package handling.vo.send;

import handling.SendPacketOpcode;
import handling.vo.MaplePacketSendVO;

public class LoginStatusSendVO extends MaplePacketSendVO {

    private Byte state; // 登录状态
    private Byte unknow2 = 0;
    private Long banTimestamp = 0L;
    private Byte unknow = 0;
    private Integer unknow1 = 0;
    private Integer accountId;
    private Byte gender;
    private Boolean isGm;
    private String accountName;

    /*登录操作码
         0 - 成功登录
         1 - 刷新
         2 - 封号
         3 - 屏蔽了账号登录功能或者已经被删除、终止的账号
         4 - 屏蔽了静态密码或密码输入错误
         5 - 未登录的账号
         6 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         7 - 正在登录中的账号
         8 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         9 - 当前连接不稳定。请更换其它频道或世界。为您带来不便，请谅解。
         10 - 目前因链接邀请过多 服务器未能处理。
         */
    public static final Byte LOGIN_STATE_OK = 0;
    public static final Byte LOGIN_STATE_BANNED = 3;
    public static final Byte LOGIN_STATE_WRONG_PASSWORD = 4;
    public static final Byte LOGIN_STATE_UNKNOW_ACCOUNT = 5;
    public static final Byte LOGIN_STATE_LOGINNED = 7;


    public byte[] encodePacket() {
        opcode = SendPacketOpcode.LOGIN_STATUS.getValue();
        super.encodeOpcode();

        mplew.write(state);
        if (state == 2) {
            mplew.write(unknow2);
            mplew.writeLong(banTimestamp);
        } else if (state == 0) {
            mplew.writeInt(accountId);
            mplew.write(gender); // 早期版本角色性别由帐号控制
            mplew.write(isGm ? 1 : 0);//给客户端判断是否GM,是GM客户端会给/找人命令加地图ID,有删除人物按钮,被封印后能使用技能,其他未知
            mplew.writeMapleAsciiString(accountName);
            mplew.writeInt(accountId);
            mplew.write(0);
        }

        return mplew.getPacket();
    }

    public Byte getOpcode() {
        return opcode;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Byte getUnknow2() {
        return unknow2;
    }

    public void setUnknow2(Byte unknow2) {
        this.unknow2 = unknow2;
    }

    public Long getBanTimestamp() {
        return banTimestamp;
    }

    public void setBanTimestamp(Long timestamp) {
        this.banTimestamp = timestamp;
    }

    public Byte getUnknow() {
        return unknow;
    }

    public void setUnknow(Byte unknow) {
        this.unknow = unknow;
    }

    public Integer getUnknow1() {
        return unknow1;
    }

    public void setUnknow1(Integer unknow1) {
        this.unknow1 = unknow1;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public Boolean getGm() {
        return isGm;
    }

    public void setGm(Boolean gm) {
        isGm = gm;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
