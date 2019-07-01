package com.sample;


public class CashshopLog {

  private long id;
  private long accId;
  private long chrId;
  private String name;
  private long sn;
  private long itemId;
  private long type;
  private long price;
  private long count;
  private long cash;
  private long points;
  private String itemlog;
  private java.sql.Timestamp time;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getAccId() {
    return accId;
  }

  public void setAccId(long accId) {
    this.accId = accId;
  }


  public long getChrId() {
    return chrId;
  }

  public void setChrId(long chrId) {
    this.chrId = chrId;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getSn() {
    return sn;
  }

  public void setSn(long sn) {
    this.sn = sn;
  }


  public long getItemId() {
    return itemId;
  }

  public void setItemId(long itemId) {
    this.itemId = itemId;
  }


  public long getType() {
    return type;
  }

  public void setType(long type) {
    this.type = type;
  }


  public long getPrice() {
    return price;
  }

  public void setPrice(long price) {
    this.price = price;
  }


  public long getCount() {
    return count;
  }

  public void setCount(long count) {
    this.count = count;
  }


  public long getCash() {
    return cash;
  }

  public void setCash(long cash) {
    this.cash = cash;
  }


  public long getPoints() {
    return points;
  }

  public void setPoints(long points) {
    this.points = points;
  }


  public String getItemlog() {
    return itemlog;
  }

  public void setItemlog(String itemlog) {
    this.itemlog = itemlog;
  }


  public java.sql.Timestamp getTime() {
    return time;
  }

  public void setTime(java.sql.Timestamp time) {
    this.time = time;
  }

}
