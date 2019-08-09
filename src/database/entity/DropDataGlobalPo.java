package database.entity;

import javax.persistence.*;

@Entity
@Table(name="drop_data_global")
public class DropDataGlobalPo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private int continent;
  private byte dropType;
  private int itemid;
  private int minimumQuantity;
  private int maximumQuantity;
  private int questid;
  private int chance;
  private String comments;


  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }


  public int getContinent() {
    return continent;
  }

  public void setContinent(int continent) {
    this.continent = continent;
  }


  public byte getDropType() {
    return dropType;
  }

  public void setDropType(byte dropType) {
    this.dropType = dropType;
  }


  public int getItemid() {
    return itemid;
  }

  public void setItemid(int itemid) {
    this.itemid = itemid;
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


  public int getChance() {
    return chance;
  }

  public void setChance(int chance) {
    this.chance = chance;
  }


  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

}
