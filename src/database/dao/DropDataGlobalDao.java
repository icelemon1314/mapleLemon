package database.dao;


import javax.persistence.Query;
import java.util.List;

public class DropDataGlobalDao extends BaseDao {

    public List getAllData()
    {
        Query query = this.em.createQuery("select p from DropDataGlobalPo as p");
        List result = query.getResultList();

        return result;
    }
}
