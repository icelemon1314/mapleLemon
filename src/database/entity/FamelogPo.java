package database.entity;

import javax.persistence.*;

@Entity
@Table(name="famelog")
public class FamelogPo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int famelogid;
  private int characterid;
  private int characteridTo;
  @Column(name="\"when\"")
  private java.sql.Timestamp createDate;


  public int getFamelogid() {
    return famelogid;
  }

  public void setFamelogid(int famelogid) {
    this.famelogid = famelogid;
  }


  public int getCharacterid() {
    return characterid;
  }

  public void setCharacterid(int characterid) {
    this.characterid = characterid;
  }


  public int getCharacteridTo() {
    return characteridTo;
  }

  public void setCharacteridTo(int characteridTo) {
    this.characteridTo = characteridTo;
  }


  public java.sql.Timestamp getCreateDate() {
    return createDate;
  }

  public void setCreateDate(java.sql.Timestamp when) {
    this.createDate = when;
  }

}
