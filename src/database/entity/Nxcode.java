package com.sample;


public class Nxcode {

  private String code;
  private long valid;
  private long type;
  private long item;
  private String user;
  private java.sql.Timestamp time;


  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }


  public long getValid() {
    return valid;
  }

  public void setValid(long valid) {
    this.valid = valid;
  }


  public long getType() {
    return type;
  }

  public void setType(long type) {
    this.type = type;
  }


  public long getItem() {
    return item;
  }

  public void setItem(long item) {
    this.item = item;
  }


  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }


  public java.sql.Timestamp getTime() {
    return time;
  }

  public void setTime(java.sql.Timestamp time) {
    this.time = time;
  }

}
