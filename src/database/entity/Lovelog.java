package com.sample;


public class Lovelog {

  private long id;
  private long characterid;
  private long characteridTo;
  private java.sql.Timestamp when;


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
