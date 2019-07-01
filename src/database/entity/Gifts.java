package com.sample;


public class Gifts {

  private long giftid;
  private long recipient;
  private String from;
  private String message;
  private long sn;
  private long uniqueid;


  public long getGiftid() {
    return giftid;
  }

  public void setGiftid(long giftid) {
    this.giftid = giftid;
  }


  public long getRecipient() {
    return recipient;
  }

  public void setRecipient(long recipient) {
    this.recipient = recipient;
  }


  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }


  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }


  public long getSn() {
    return sn;
  }

  public void setSn(long sn) {
    this.sn = sn;
  }


  public long getUniqueid() {
    return uniqueid;
  }

  public void setUniqueid(long uniqueid) {
    this.uniqueid = uniqueid;
  }

}
