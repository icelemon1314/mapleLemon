package database.dao;


import database.entity.ShopPo;

import javax.persistence.Query;
import java.util.List;

public class ShopDao extends BaseDao {

    public ShopPo getDataByShopId(int shopId)
    {
        Query query = this.em.createQuery("select p from ShopPo as p where shopid = :shopid");
        query.setParameter("shopid", shopId);
        ShopPo result = (ShopPo)query.getSingleResult();

        return result;
    }

    public ShopPo getDataByNpcId(int npcId) {
        Query query = this.em.createQuery("select p from ShopPo as p where npcid = :npcid");
        query.setParameter("npcid", npcId);
        ShopPo result = (ShopPo)query.getSingleResult();

        return result;
    }
}
