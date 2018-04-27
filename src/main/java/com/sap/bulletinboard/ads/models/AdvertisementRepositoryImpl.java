package com.sap.bulletinboard.ads.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class AdvertisementRepositoryImpl implements AdvertisementRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<Advertisement> findByTitle(String title) {
        String qlString = "SELECT ads FROM Advertisement ads WHERE ads.title = :title";
        TypedQuery<Advertisement> query = entityManager.createQuery(qlString, Advertisement.class);
        query.setParameter("title", title);
        return query.getResultList();
    }
}
