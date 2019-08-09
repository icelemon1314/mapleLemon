package database.entity;

import javax.persistence.*;

@Entity
@Table(name="drop_data")
public class DropDataPo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private int mobId;
  private int itemid;
  private int chance;
  private int minimumQuantity;
  private int maximumQuantity;
  private int questid;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public int getMobId() {
    return mobId;
  }

  public void setMobId(int mobId) {
    this.mobId = mobId;
  }


  public int getItemid() {
    return itemid;
  }

  public void setItemid(int itemid) {
    this.itemid = itemid;
  }


  public int getChance() {
    return chance;
  }

  public void setChance(int chance) {
    this.chance = chance;
  }


  public int getMinimumQuantity() {
    return minimumQuantity;
  }

  public void setMinimumQuantity(int minimumQuantity) {
    this.minimumQuantity = minimumQuantity;
  }


  public int getMaximumQuantity() {
    return maximumQuantity;
  }

  public void setMaximumQuantity(int maximumQuantity) {
    this.maximumQuantity = maximumQuantity;
  }


  public int getQuestid() {
    return questid;
  }

  public void setQuestid(int questid) {
    this.questid = questid;
  }

}
