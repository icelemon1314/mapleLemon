package database.entity;

import javax.persistence.*;

@Entity
@Table(name="shopitems")
public class ShopItemPo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int shopitemid;
  private int shopid;
  private int itemid;
  private int price;
  private int position;
  private int reqitem;
  private int reqitemq;
  private int period;
  private int state;
  private int rank;


  public int getShopitemid() {
    return shopitemid;
  }

  public void setShopitemid(int shopitemid) {
    this.shopitemid = shopitemid;
  }


  public int getShopid() {
    return shopid;
  }

  public void setShopid(int shopid) {
    this.shopid = shopid;
  }


  public int getItemid() {
    return itemid;
  }

  public void setItemid(int itemid) {
    this.itemid = itemid;
  }


  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }


  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }


  public int getReqitem() {
    return reqitem;
  }

  public void setReqitem(int reqitem) {
    this.reqitem = reqitem;
  }


  public int getReqitemq() {
    return reqitemq;
  }

  public void setReqitemq(int reqitemq) {
    this.reqitemq = reqitemq;
  }


  public int getPeriod() {
    return period;
  }

  public void setPeriod(int period) {
    this.period = period;
  }


  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }


  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

}
