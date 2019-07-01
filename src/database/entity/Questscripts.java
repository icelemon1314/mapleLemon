package com.sample;


public class Questscripts {

  private long id;
  private long characterid;
  private String quest;
  private long status;
  private long time;


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


  public String getQuest() {
    return quest;
  }

  public void setQuest(String quest) {
    this.quest = quest;
  }


  public long getStatus() {
    return status;
  }

  public void setStatus(long status) {
    this.status = status;
  }


  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

}
