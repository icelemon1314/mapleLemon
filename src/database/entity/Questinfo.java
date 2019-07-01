package com.sample;


public class Questinfo {

  private long questinfoid;
  private long characterid;
  private long quest;
  private String customData;


  public long getQuestinfoid() {
    return questinfoid;
  }

  public void setQuestinfoid(long questinfoid) {
    this.questinfoid = questinfoid;
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


  public String getCustomData() {
    return customData;
  }

  public void setCustomData(String customData) {
    this.customData = customData;
  }

}
