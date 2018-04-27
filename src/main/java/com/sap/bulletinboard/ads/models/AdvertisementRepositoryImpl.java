package com.sap.bulletinboard.ads.models;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.List;

public class AdvertisementRepositoryImpl implements AdvertisementRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<Advertisement> findByTitle(String title) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<Advertisement> criteriaQuery = criteriaBuilder.createQuery(Advertisement.class);
        Root<Advertisement> advertisement = criteriaQuery.from(Advertisement.class);
        ParameterExpression<String> titleParameter = criteriaBuilder.parameter(String.class);
        criteriaQuery.select(advertisement).where(criteriaBuilder.equal(advertisement.get("title"), titleParameter));

        TypedQuery<Advertisement> query = entityManager.createQuery(criteriaQuery);
        query.setParameter(titleParameter, title);

        return query.getResultList();
    }
}
