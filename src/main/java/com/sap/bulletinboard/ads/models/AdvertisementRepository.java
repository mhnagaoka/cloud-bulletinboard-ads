package com.sap.bulletinboard.ads.models;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface AdvertisementRepository extends AdvertisementRepositoryCustom, PagingAndSortingRepository<Advertisement, Long> {

}
