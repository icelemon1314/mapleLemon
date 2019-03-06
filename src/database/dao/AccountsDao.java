package database.dao;

import database.entity.AccountsPO;

import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;

public class AccountsDao extends BaseDao {

    public void save(AccountsPO account){

    }

    /**
     *
     * @param name
     * @return
     */
    public AccountsPO getAccountByName(String name) {
        Query query = this.em.createQuery("select p from AccountsPO as p where name = :name");
        query.setParameter("name", name);
        List result = query.getResultList();
        if (!result.isEmpty()){
            return (AccountsPO)result.get(0);
        } else {
            return null;
        }
    }
}
