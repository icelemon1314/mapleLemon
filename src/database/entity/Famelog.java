package com.sample;


public class Famelog {

  private long famelogid;
  private long characterid;
  private long characteridTo;
  private java.sql.Timestamp when;


  public long getFamelogid() {
    return famelogid;
  }

  public void setFamelogid(long famelogid) {
    this.famelogid = famelogid;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public long getCharacteridTo() {
    return characteridTo;
  }

  public void setCharacteridTo(long characteridTo) {
    this.characteridTo = characteridTo;
  }


  public java.sql.Timestamp getWhen() {
    return when;
  }

  public void setWhen(java.sql.Timestamp when) {
    this.when = when;
  }

}
