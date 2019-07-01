package com.sample;


public class Pqlog {

  private long pqid;
  private long characterid;
  private String pqname;
  private long count;
  private long type;
  private java.sql.Timestamp time;


  public long getPqid() {
    return pqid;
  }

  public void setPqid(long pqid) {
    this.pqid = pqid;
  }


  public long getCharacterid() {
    return characterid;
  }

  public void setCharacterid(long characterid) {
    this.characterid = characterid;
  }


  public String getPqname() {
    return pqname;
  }

  public void setPqname(String pqname) {
    this.pqname = pqname;
  }


  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }


  public long getType() {
    return type;
  }

  public void setType(long type) {
    this.type = type;
  }


  public java.sql.Timestamp getTime() {
    return time;
  }

  public void setTime(java.sql.Timestamp time) {
    this.time = time;
  }

}
