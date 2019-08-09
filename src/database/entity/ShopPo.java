package database.entity;

import javax.persistence.*;

@Entity
@Table(name="shops")
public class ShopPo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int shopid;
  private int npcid;
  private String shopname;

  public int getShopid() {
    return shopid;
  }

  public void setShopid(int shopid) {
    this.shopid = shopid;
  }

  public int getNpcid() {
    return npcid;
  }

  public void setNpcid(int npcid) {
    this.npcid = npcid;
  }

  public String getShopname() {
    return shopname;
  }

  public void setShopname(String shopname) {
    this.shopname = shopname;
  }

}
