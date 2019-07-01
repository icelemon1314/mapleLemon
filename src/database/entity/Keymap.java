package com.sample;


public class Keymap {

  private long id;
  private long characterid;
  private long key;
  private long type;
  private long action;


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


  public long getKey() {
    return key;
  }

  public void setKey(long key) {
    this.key = key;
  }


  public long getType() {
    return type;
  }

  public void setType(long type) {
    this.type = type;
  }


  public long getAction() {
    return action;
  }

  public void setAction(long action) {
    this.action = action;
  }

}
