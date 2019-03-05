package database.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

public class BaseDao {

    protected EntityManagerFactory factory = Persistence.createEntityManagerFactory("MapleLemonJPA");
    @PersistenceContext
    protected EntityManager em = factory.createEntityManager();

}
