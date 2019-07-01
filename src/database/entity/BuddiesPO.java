package database.entity;

import tools.DateUtil;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name="buddies")
public class BuddiesPO {
    public BuddiesPO(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer characterid;
    private String groupname;
    private Integer buddyid;
    private Integer pending;



}
