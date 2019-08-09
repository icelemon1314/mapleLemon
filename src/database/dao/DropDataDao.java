package database.dao;


import javax.persistence.Query;
import java.util.List;

public class DropDataDao extends BaseDao {

    public List getAllData()
    {
        Query query = this.em.createQuery("select p from DropDataPo as p");
        List result = query.getResultList();

        return result;
    }
}
