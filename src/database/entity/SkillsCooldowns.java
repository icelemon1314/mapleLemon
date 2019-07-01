package com.sample;


public class SkillsCooldowns {

  private long id;
  private long charid;
  private long skillId;
  private long length;
  private long startTime;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getCharid() {
    return charid;
  }

  public void setCharid(long charid) {
    this.charid = charid;
  }


  public long getSkillId() {
    return skillId;
  }

  public void setSkillId(long skillId) {
    this.skillId = skillId;
  }


  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }


  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

}
