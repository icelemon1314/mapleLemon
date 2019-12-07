package database.entity;

import tools.DateUtil;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="accounts")
@NamedQueries({
        @NamedQuery(name = "AccountsPO.findByName",
                query = "SELECT b FROM AccountsPO b WHERE b.name = :name"),
        @NamedQuery(name = "AccountsPO.findAll",
                query = "SELECT b FROM AccountsPO b")
})
public class AccountsPO {
    public AccountsPO(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String password;
    private String salt;
    private String salt2;
    private Byte gm;
    private Byte loggedin;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastlogin;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdat;
    @Temporal(TemporalType.DATE)
    private Calendar birthday;
    private String email;
    private String qq;
    private Byte banned;
    private String banreason;
    private String macs;
    private String maclist;
    @Temporal(TemporalType.TIMESTAMP)
    private Date tempban;
    private Byte greason;
    private Byte gender;
    private String SessionIP;
    private Integer points;
    private Integer vpoints;
    private Integer totalvoteip;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastlogon;
    private String lastvoteip;
    private String lastknownip;
    private Integer ACash;
    private Integer mPoints;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getSalt2() {
        return salt2;
    }

    public void setSalt2(String salt2) {
        this.salt2 = salt2;
    }

    public Byte getGm() {
        return gm;
    }

    public void setGm(Byte gm) {
        this.gm = gm;
    }

    public Byte getLoggedin() {
        return loggedin;
    }

    public void setLoggedin(Byte loggedin) {
        this.loggedin = loggedin;
    }

    public Date getLastlogin() {
        return lastlogin;
    }

    public void setLastlogin(Date lastlogin) {
        this.lastlogin = lastlogin;
    }

    public Date getCreatedat() {
        return createdat;
    }

    public void setCreatedat(Date createdat) {
        this.createdat = createdat;
    }

    public String getBirthday() {
        return DateUtil.getFormatDate(birthday);
    }

    public void setBirthday(Calendar birthday) {

        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public Byte getBanned() {
        return banned;
    }

    public void setBanned(Byte banned) {
        this.banned = banned;
    }

    public String getBanreason() {
        return banreason;
    }

    public void setBanreason(String banreason) {
        this.banreason = banreason;
    }

    public String getMacs() {
        return macs;
    }

    public void setMacs(String macs) {
        this.macs = macs;
    }

    public String getMaclist() {
        return maclist;
    }

    public void setMaclist(String maclist) {
        this.maclist = maclist;
    }

    public Date getTempban() {
        return tempban;
    }

    public void setTempban(Date tempban) {
        this.tempban = tempban;
    }

    public Byte getGreason() {
        return greason;
    }

    public void setGreason(Byte greason) {
        this.greason = greason;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public String getSessionIP() {
        return SessionIP;
    }

    public void setSessionIP(String sessionIP) {
        SessionIP = sessionIP;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getVpoints() {
        return vpoints;
    }

    public void setVpoints(Integer vpoints) {
        this.vpoints = vpoints;
    }

    public Integer getTotalvoteip() {
        return totalvoteip;
    }

    public void setTotalvoteip(Integer totalvoteip) {
        this.totalvoteip = totalvoteip;
    }

    public Date getLastlogon() {
        return lastlogon;
    }

    public void setLastlogon(Date lastlogon) {
        this.lastlogon = lastlogon;
    }

    public String getLastvoteip() {
        return lastvoteip;
    }

    public void setLastvoteip(String lastvoteip) {
        this.lastvoteip = lastvoteip;
    }

    public String getLastknownip() {
        return lastknownip;
    }

    public void setLastknownip(String lastknownip) {
        this.lastknownip = lastknownip;
    }

    public Integer getACash() {
        return ACash;
    }

    public void setACash(Integer ACash) {
        this.ACash = ACash;
    }

    public Integer getmPoints() {
        return mPoints;
    }

    public void setmPoints(Integer mPoints) {
        this.mPoints = mPoints;
    }
}
