package com.sap.bulletinboard.ads.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bulletinboard.ads.models.Advertisement;
import com.sap.bulletinboard.ads.models.AdvertisementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Validated
@RequestScope
@RestController
@RequestMapping(path = AdvertisementController.PATH, produces = {"application/json"})
public class AdvertisementController {
    public static final String PATH = "/api/v1/ads";

    private AdvertisementRepository advertisementRepository;
    Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public AdvertisementController(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    @GetMapping
    public ResponseEntity<AdvertisementList> advertisements(@RequestParam(value = "pageId", defaultValue = "0") final int pageId,
                                            @RequestParam(value = "pageSize", defaultValue = "5") final int pageSize) {
        Pageable pageable = new PageRequest(pageId, pageSize);
        Page<Advertisement> page = advertisementRepository.findAll(pageable);
        return new ResponseEntity<>(new AdvertisementList(page), buildLinkHeader(page, PATH), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public Advertisement advertisementById(@Min(0) @PathVariable("id") Long id) {
        Advertisement ad = advertisementRepository.findOne(id);
        if (ad != null) {
            logger.info("Retrieving advertisement: {}", ad);
            return ad;
        }
        throw new NotFoundException("No such ad: " + id);
    }

    /**
     * @RequestBody is bound to the method argument. HttpMessageConverter resolves method argument depending on the
     * content type.
     */
    @PostMapping
    public ResponseEntity<Advertisement> add(@RequestBody @Valid Advertisement advertisement,
                                             UriComponentsBuilder uriComponentsBuilder) {

        try {

            if (advertisement.getId() != null) {
                String message = String
                        .format("Remove 'id' property from request or use PUT method to update resource with id = %d", advertisement.getId());
                throw new BadRequestException(message);
            }
            Advertisement savedAd = advertisementRepository.save(advertisement);

            UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}").buildAndExpand(savedAd.getId());
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(uriComponents.getPath()));

            ResponseEntity<Advertisement> result = new ResponseEntity<>(advertisement, headers, HttpStatus.CREATED);
            return result; // return ResponseEntity with advertisement in the body, location header and
            // HttpStatus.CREATED status code
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping(path = "/{id}", produces = "application/json")
    public Advertisement updateById(@PathVariable("id") Long id, @RequestBody @Valid Advertisement updatedAdvertisement) {
        if (updatedAdvertisement.getId() != null && !updatedAdvertisement.getId().equals(id)) {
            String message = String
                    .format("Remove 'id' property from request make it match the resource URI with id = %d", id);
            throw new BadRequestException(message);
        }
        if (!advertisementRepository.exists(id)) {
            throw new NotFoundException("No such id: " + id);
        }
        updatedAdvertisement.setId(id);
        return advertisementRepository.save(updatedAdvertisement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        if (advertisementRepository.exists(id)) {
            advertisementRepository.delete(id);
            return;
        }
        throw new NotFoundException("No such id: " + id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        advertisementRepository.deleteAll();
    }

    public static HttpHeaders buildLinkHeader(Page<?> page, String path) {
        StringBuilder linkHeader = new StringBuilder();
        int pageSize = page.getSize();
        if (page.hasPrevious()) {
            int prevNumber = page.getNumber() - 1;
            linkHeader.append(String.format("<%s?pageId=%d&pageSize=%d>;rel=\"previous\"", path, prevNumber, pageSize));
            if (!page.isLast())
                linkHeader.append(", ");
        }
        if (page.hasNext()) {
            int nextNumber = page.getNumber() + 1;
            linkHeader.append(String.format("<%s?pageId=%d&pageSize=%d>;rel=\"next\"", path, nextNumber, pageSize));
        }
        HttpHeaders headers = new HttpHeaders();
        if (linkHeader.length() > 0) {
            headers.add(HttpHeaders.LINK, linkHeader.toString());
        }
        return headers;
    }

    public static class AdvertisementList {
        @JsonProperty("value")
        public List<Advertisement> advertisements = new ArrayList<>();

        public AdvertisementList(Iterable<Advertisement> ads) {
            ads.forEach(advertisements::add);
        }
    }
}
