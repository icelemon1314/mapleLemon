package com.sample;


public class Pets {

  private long petid;
  private String name;
  private long level;
  private long closeness;
  private long fullness;
  private long seconds;
  private long flags;
  private long skillid;
  private String excluded;


  public long getPetid() {
    return petid;
  }

  public void setPetid(long petid) {
    this.petid = petid;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getLevel() {
    return level;
  }

  public void setLevel(long level) {
    this.level = level;
  }


  public long getCloseness() {
    return closeness;
  }

  public void setCloseness(long closeness) {
    this.closeness = closeness;
  }


  public long getFullness() {
    return fullness;
  }

  public void setFullness(long fullness) {
    this.fullness = fullness;
  }


  public long getSeconds() {
    return seconds;
  }

  public void setSeconds(long seconds) {
    this.seconds = seconds;
  }


  public long getFlags() {
    return flags;
  }

  public void setFlags(long flags) {
    this.flags = flags;
  }


  public long getSkillid() {
    return skillid;
  }

  public void setSkillid(long skillid) {
    this.skillid = skillid;
  }


  public String getExcluded() {
    return excluded;
  }

  public void setExcluded(String excluded) {
    this.excluded = excluded;
  }

}
