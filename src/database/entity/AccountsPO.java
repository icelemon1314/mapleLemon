package database.entity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "accounts")
public class AccountsPO {
    public AccountsPO(){}

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public void setPoints(Integer points) {
        this.points = points;
    }
}
