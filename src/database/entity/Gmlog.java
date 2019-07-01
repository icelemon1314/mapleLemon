package com.sample;


public class Gmlog {

  private long gmlogid;
  private long cid;
  private String name;
  private String command;
  private long mapid;
  private java.sql.Timestamp time;


  public long getGmlogid() {
    return gmlogid;
  }

  public void setGmlogid(long gmlogid) {
    this.gmlogid = gmlogid;
  }


  public long getCid() {
    return cid;
  }

  public void setCid(long cid) {
    this.cid = cid;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }


  public long getMapid() {
    return mapid;
  }

  public void setMapid(long mapid) {
    this.mapid = mapid;
  }


  public java.sql.Timestamp getTime() {
    return time;
  }

  public void setTime(java.sql.Timestamp time) {
    this.time = time;
  }

}
