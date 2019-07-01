package com.sample;


public class Notes {

  private long id;
  private String to;
  private String from;
  private String message;
  private long timestamp;
  private long gift;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
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


  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }


  public long getGift() {
    return gift;
  }

  public void setGift(long gift) {
    this.gift = gift;
  }

}
