package com.sample;


public class Shopranks {

  private long id;
  private long shopid;
  private long rank;
  private String name;
  private long itemid;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public long getShopid() {
    return shopid;
  }

  public void setShopid(long shopid) {
    this.shopid = shopid;
  }


  public long getRank() {
    return rank;
  }

  public void setRank(long rank) {
    this.rank = rank;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public long getItemid() {
    return itemid;
  }

  public void setItemid(long itemid) {
    this.itemid = itemid;
  }

}
