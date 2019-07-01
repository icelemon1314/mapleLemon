package database.dao;

import database.entity.CharacterPO;

import javax.persistence.Query;
import java.util.List;

public class CharacterDao extends BaseDao {

    /**
     *
     * @param accountId
     * @param worldId
     * @return
     */
    public List<CharacterPO> getCharacterByNameAndWorld(int accountId, int worldId) {
        Query query = this.em.createQuery("select p from CharacterPO as p where account.id = :accountId and world = :worldId");
        query.setParameter("accountId", accountId);
        query.setParameter("worldId", worldId);
        List result = query.getResultList();
        return result;
    }

    /**
     *
     * @param name
     * @return
     */
    public CharacterPO getCharacterByName(String name) {
        Query query = this.em.createQuery("select p from CharacterPO as p where name = :name");
        query.setParameter("name", name);
        Object result = query.getSingleResult();
        return (CharacterPO) result;
    }

    /**
     *
     * @param id
     * @return
     */
    public CharacterPO getCharacterById(Integer id) {
        Query query = this.em.createQuery("select p from CharacterPO as p where id = :id");
        query.setParameter("id", id);
        List result = query.getResultList();
        if (!result.isEmpty()){
            return (CharacterPO)result.get(0);
        } else {
            return null;
        }
    }

    public List<CharacterPO> getCharacterByAccountId(Integer accountId) {
        Query query = this.em.createQuery("select p from CharacterPO as p where account.id = :id");
        query.setParameter("id", accountId);
        List result = query.getResultList();
        if (!result.isEmpty()){
            return result;
        } else {
            return null;
        }
    }
}
