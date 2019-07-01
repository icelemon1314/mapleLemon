package com.sample;


public class CharacterWork {

  private long id;
  private long accid;
  private long worldid;
  private long characterid;
  private long worktype;
  private java.sql.Timestamp worktime;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getAccid() {
    return accid;
  }

  public void setAccid(long accid) {
    this.accid = accid;
  }


  public long getWorldid() {
    return worldid;
  }

  public void setWorldid(long worldid) {
    this.worldid = worldid;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public long getWorktype() {
    return worktype;
  }

  public void setWorktype(long worktype) {
    this.worktype = worktype;
  }


  public java.sql.Timestamp getWorktime() {
    return worktime;
  }

  public void setWorktime(java.sql.Timestamp worktime) {
    this.worktime = worktime;
  }

}
