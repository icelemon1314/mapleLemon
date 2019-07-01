package com.sample;


public class Skills {

  private long id;
  private long skillid;
  private long characterid;
  private long skilllevel;
  private long masterlevel;
  private long expiration;
  private long teachId;
  private long position;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getSkillid() {
    return skillid;
  }

  public void setSkillid(long skillid) {
    this.skillid = skillid;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public long getSkilllevel() {
    return skilllevel;
  }

  public void setSkilllevel(long skilllevel) {
    this.skilllevel = skilllevel;
  }


  public long getMasterlevel() {
    return masterlevel;
  }

  public void setMasterlevel(long masterlevel) {
    this.masterlevel = masterlevel;
  }


  public long getExpiration() {
    return expiration;
  }

  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }


  public long getTeachId() {
    return teachId;
  }

  public void setTeachId(long teachId) {
    this.teachId = teachId;
  }


  public long getPosition() {
    return position;
  }

  public void setPosition(long position) {
    this.position = position;
  }

}
