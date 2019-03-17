package database.dao;

import javax.persistence.*;
import javax.transaction.Transactional;

public class BaseDao {

    protected EntityManagerFactory factory = Persistence.createEntityManagerFactory("MapleLemonJPA");
    protected EntityManager em = factory.createEntityManager();
    private EntityTransaction transaction;

//    public void flush() {
//        this.em.flush();
//    }

    public EntityManager getEm() {
        return this.em;
    }

    public void transactionStart() {
        this.transaction = this.em.getTransaction();
        this.transaction.begin();
    }

    public void transactionCommit() {
        this.transaction.commit();
    }

    public void transactionRollback() {
        this.transaction.rollback();
    }

    public void persist(Object entity) {
        this.em.persist(entity);
    }

    public void save(Object entity) {
        EntityTransaction trans = this.em.getTransaction();
        trans.begin();

        this.em.persist(entity);
        this.em.flush();

        trans.commit();
    }
}
