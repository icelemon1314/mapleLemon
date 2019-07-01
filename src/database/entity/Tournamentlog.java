package com.sample;


public class Tournamentlog {

  private long logid;
  private long winnerid;
  private long numContestants;
  private java.sql.Timestamp when;


  public long getLogid() {
    return logid;
  }

  public void setLogid(long logid) {
    this.logid = logid;
  }


  public long getWinnerid() {
    return winnerid;
  }

  public void setWinnerid(long winnerid) {
    this.winnerid = winnerid;
  }


  public long getNumContestants() {
    return numContestants;
  }

  public void setNumContestants(long numContestants) {
    this.numContestants = numContestants;
  }


  public java.sql.Timestamp getWhen() {
    return when;
  }

  public void setWhen(java.sql.Timestamp when) {
    this.when = when;
  }

}
