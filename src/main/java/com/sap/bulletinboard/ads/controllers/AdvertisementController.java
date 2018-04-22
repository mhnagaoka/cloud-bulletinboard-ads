package com.sap.bulletinboard.ads.controllers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bulletinboard.ads.models.Advertisement;

@Validated
@RequestScope
@RestController
@RequestMapping(path = AdvertisementController.PATH, produces = { "application/json" })
public class AdvertisementController {
    public static final String PATH = "/api/v1/ads";
    private static final AtomicLong ID = new AtomicLong(0L);
    private static final Map<Long, Advertisement> ads = new HashMap<>(); // temporary data storage, key represents the ID

    @GetMapping
    public AdvertisementList advertisements() {
        return new AdvertisementList(ads.values());
    }

    @GetMapping("/{id}")
    public Advertisement advertisementById(@Min(0) @PathVariable("id") Long id) {
        if (ads.containsKey(id)) {
            return ads.get(id);
        }
        throw new NotFoundException("No such ad: " + id);
    }

    /**
     * @RequestBody is bound to the method argument. HttpMessageConverter resolves method argument depending on the
     *              content type.
     */
    @PostMapping
    public ResponseEntity<Advertisement> add(@RequestBody @Valid Advertisement advertisement,
            UriComponentsBuilder uriComponentsBuilder) {

        try {
            Long id = ID.getAndAdd(1L);
            ads.put(id, advertisement);

            UriComponents uriComponents = uriComponentsBuilder.path(PATH + "/{id}").buildAndExpand(id);
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
        if (ads.containsKey(id)) {
            ads.put(id, updatedAdvertisement);
            return updatedAdvertisement;
        }
        throw new NotFoundException("No such id: " + id);
    }
    
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        if (ads.containsKey(id)) {
            ads.remove(id);
            return;
        }
        throw new NotFoundException("No such id: " + id);
    }

    @DeleteMapping
    public void deleteAll() {
        ads.clear();
    }

    public static class AdvertisementList {
        @JsonProperty("value")
        public List<Advertisement> advertisements = new ArrayList<>();

        public AdvertisementList(Iterable<Advertisement> ads) {
            ads.forEach(advertisements::add);
        }
    }
}
