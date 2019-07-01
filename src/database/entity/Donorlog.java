package com.sample;


public class Donorlog {

  private long id;
  private String accname;
  private long accId;
  private String chrname;
  private long chrId;
  private String log;
  private String time;
  private long previousPoints;
  private long currentPoints;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getAccname() {
    return accname;
  }

  public void setAccname(String accname) {
    this.accname = accname;
  }


  public long getAccId() {
    return accId;
  }

  public void setAccId(long accId) {
    this.accId = accId;
  }


  public String getChrname() {
    return chrname;
  }

  public void setChrname(String chrname) {
    this.chrname = chrname;
  }


  public long getChrId() {
    return chrId;
  }

  public void setChrId(long chrId) {
    this.chrId = chrId;
  }


  public String getLog() {
    return log;
  }

  public void setLog(String log) {
    this.log = log;
  }


  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }


  public long getPreviousPoints() {
    return previousPoints;
  }

  public void setPreviousPoints(long previousPoints) {
    this.previousPoints = previousPoints;
  }


  public long getCurrentPoints() {
    return currentPoints;
  }

  public void setCurrentPoints(long currentPoints) {
    this.currentPoints = currentPoints;
  }

}
