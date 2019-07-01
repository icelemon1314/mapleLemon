package com.sample;


public class Cheatlog {

  private long id;
  private long characterid;
  private String offense;
  private long count;
  private java.sql.Timestamp lastoffensetime;
  private String param;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public String getOffense() {
    return offense;
  }

  public void setOffense(String offense) {
    this.offense = offense;
  }


  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }


  public java.sql.Timestamp getLastoffensetime() {
    return lastoffensetime;
  }

  public void setLastoffensetime(java.sql.Timestamp lastoffensetime) {
    this.lastoffensetime = lastoffensetime;
  }


  public String getParam() {
    return param;
  }

  public void setParam(String param) {
    this.param = param;
  }

}
