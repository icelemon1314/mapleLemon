package database.dao;


import database.entity.ShopPo;

import javax.persistence.Query;
import java.util.List;

public class ShopItemDao extends BaseDao {

    public List getDataByShopId(int shopId)
    {
        Query query = this.em.createQuery("select p from ShopItemPo as p where shopid = :shopid ORDER BY position ASC");
        query.setParameter("shopid", shopId);
        List result = query.getResultList();

        return result;
    }

    public ShopPo getDataByNpcId(int npcId) {
        Query query = this.em.createQuery("select p from ShopPo as p where npcid = :npcid");
        query.setParameter("npcid", npcId);
        ShopPo result = (ShopPo)query.getSingleResult();

        return result;
    }
}
