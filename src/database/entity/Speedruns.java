package com.sample;


public class Speedruns {

  private long id;
  private String type;
  private String leader;
  private String timestring;
  private long time;
  private String members;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }


  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  public String getLeader() {
    return leader;
  }

  public void setLeader(String leader) {
    this.leader = leader;
  }


  public String getTimestring() {
    return timestring;
  }

  public void setTimestring(String timestring) {
    this.timestring = timestring;
  }


  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }


  public String getMembers() {
    return members;
  }

  public void setMembers(String members) {
    this.members = members;
  }

}
