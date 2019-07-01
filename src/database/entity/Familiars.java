package com.sample;


public class Familiars {

  private long id;
  private long characterid;
  private long familiar;
  private String name;
  private long fatigue;
  private long expiry;
  private long vitality;


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


  public long getFamiliar() {
    return familiar;
  }

  public void setFamiliar(long familiar) {
    this.familiar = familiar;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getFatigue() {
    return fatigue;
  }

  public void setFatigue(long fatigue) {
    this.fatigue = fatigue;
  }


  public long getExpiry() {
    return expiry;
  }

  public void setExpiry(long expiry) {
    this.expiry = expiry;
  }


  public long getVitality() {
    return vitality;
  }

  public void setVitality(long vitality) {
    this.vitality = vitality;
  }

}
