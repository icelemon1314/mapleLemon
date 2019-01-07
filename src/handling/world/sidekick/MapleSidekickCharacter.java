  package handling.world.sidekick;
  
  import client.MapleCharacter;
  import java.io.Serializable;
  
  public class MapleSidekickCharacter
    implements Serializable
  {
    private static final long serialVersionUID = 6215463252132450750L;
    private String name;
    private int id;
    private int level;
    private int jobid;
    private int mapid;
  
    public MapleSidekickCharacter(MapleCharacter maplechar)
    {
      update(maplechar);
    }
  
    public MapleSidekickCharacter(int id, String name, int level, int jobid, int mapid) {
      this.name = name;
      this.id = id;
      this.level = level;
      this.jobid = jobid;
      this.mapid = mapid;
    }
  
    public final void update(MapleCharacter maplechar) {
      this.name = maplechar.getName();
      this.level = maplechar.getLevel();
      this.id = maplechar.getId();
      this.jobid = maplechar.getJob();
      this.mapid = maplechar.getMapId();
    }
  
    public int getLevel() {
      return this.level;
    }
  
    public int getMapid() {
      return this.mapid;
    }
  
    public String getName() {
      return this.name;
    }
  
    public int getId() {
      return this.id;
    }
  
    public int getJobId() {
      return this.jobid;
    }
  
    @Override
    public int hashCode()
    {
      int prime = 31;
      int result = 1;
      result = prime * result + this.id;
      return result;
    }
  
    @Override
    public boolean equals(Object obj)
    {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MapleSidekickCharacter other = (MapleSidekickCharacter)obj;
      if (this.name == null) {
        if (other.name != null) {
            return false;
        }
      }
      else if (!this.name.equals(other.name)) {
       return false;
      }
     return true;
    }
  }

