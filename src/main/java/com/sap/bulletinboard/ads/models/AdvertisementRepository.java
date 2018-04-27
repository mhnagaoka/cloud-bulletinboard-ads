package com.sap.bulletinboard.ads.models;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AdvertisementRepository extends PagingAndSortingRepository<Advertisement, Long> {
    List<Advertisement> findByTitle(String title);
}
