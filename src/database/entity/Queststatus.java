package com.sample;


public class Queststatus {

  private long queststatusid;
  private long characterid;
  private long quest;
  private long status;
  private long time;
  private long forfeited;
  private String customData;


  public long getQueststatusid() {
    return queststatusid;
  }

  public void setQueststatusid(long queststatusid) {
    this.queststatusid = queststatusid;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public long getQuest() {
    return quest;
  }

  public void setQuest(long quest) {
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


  public long getForfeited() {
    return forfeited;
  }

  public void setForfeited(long forfeited) {
    this.forfeited = forfeited;
  }


  public String getCustomData() {
    return customData;
  }

  public void setCustomData(String customData) {
    this.customData = customData;
  }

}
