package database.dao;

import database.entity.AccountsPO;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import java.util.List;

public class AccountsDao extends BaseDao {

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

    public AccountsPO getAccountById(Integer id) {
        Query query = this.em.createQuery("select p from AccountsPO as p where id = :id");
        query.setParameter("id", id);
        List result = query.getResultList();
        if (!result.isEmpty()){
            return (AccountsPO)result.get(0);
        } else {
            return null;
        }
    }

    public void updateLoginStateToZero() {
        EntityTransaction transaction = this.em.getTransaction();
        Query query = this.em.createQuery("UPDATE AccountsPO SET loggedin = 0");
        transaction.begin();
        try {
            query.executeUpdate();
        } catch (QueryTimeoutException e) {
            transaction.rollback();
        } catch (PersistenceException e) {
            transaction.rollback();
        }
        transaction.commit();
    }
}
