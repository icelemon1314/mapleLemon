package database.dao;

import database.entity.AccountsPO;

import javax.persistence.Query;

public class AccountsDao extends BaseDao {

    public void save(AccountsPO account){

    }

    public void getAccountByName(String name) {
        Query query = this.em.createQuery("select p from accounts as p where name = :name");
        query.setParameter("name", name);
    }
}
