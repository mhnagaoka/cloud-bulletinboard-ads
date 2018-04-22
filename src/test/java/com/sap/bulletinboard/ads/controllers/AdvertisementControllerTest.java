package com.sap.bulletinboard.ads.controllers;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bulletinboard.ads.config.WebAppContextConfig;
import com.sap.bulletinboard.ads.models.Advertisement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebAppContextConfig.class })
@WebAppConfiguration
//@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
//@formatter:off
public class AdvertisementControllerTest {
    
    private static final String LOCATION = "Location";
    private static final String SOME_TITLE = "MyNewAdvertisement";

    @Inject
    WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void create() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE))
                .andExpect(status().isCreated())
                .andExpect(header().string(LOCATION, is(not(""))))
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE))); // requires com.jayway.jsonpath:json-path
    }

    @Test
    public void readAll() throws Exception {
        mockMvc.perform(buildPostRequest(SOME_TITLE + "ReadAll"))
                .andExpect(status().isCreated());
        mockMvc.perform(buildGetRequest(""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.value[*].title", hasItem(SOME_TITLE + "ReadAll")))
                .andExpect(jsonPath("$.value.length()", both(greaterThan(0)).and(lessThan(10))));
    }

    @Test
    public void readByIdNotFound() throws Exception {
        // try to retrieve object with nonexisting ID using GET request to /4711
        mockMvc.perform(buildGetRequest("4711"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void readById() throws Exception {
        // create new advertisement using POST, then retrieve it using GET {/id}
        String location = mockMvc.perform(buildPostRequest(SOME_TITLE + "ReadById"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
        String id = getIdFromLocation(location);
        mockMvc.perform(buildGetRequest(id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE + "ReadById"))); // requires com.jayway.jsonpath:json-path

    }
    
    @Test
    public void updateById() throws Exception {
        String location = mockMvc.perform(buildPostRequest(SOME_TITLE + "BeforeUpdateById"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
        String id = getIdFromLocation(location);
        mockMvc.perform(buildPutRequest(id, SOME_TITLE + "AfterUpdateById"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title", is(SOME_TITLE + "AfterUpdateById"))); // requires com.jayway.jsonpath:json-path
    }

    
    @Test
    public void updateByIdNotFound() throws Exception {
        mockMvc.perform(buildPutRequest("-4711", "AfterUpdateNotFound"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateAllAdsNotSupportede() throws Exception {
        mockMvc.perform(buildPutRequest("", "AfterUpdateNotFound"))
                .andExpect(status().isMethodNotAllowed());
    }
    
    @Test
    public void deleteById() throws Exception {
        String location = mockMvc.perform(buildPostRequest(SOME_TITLE + "BeforeDeleteById"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader(LOCATION);
        String id = getIdFromLocation(location);
        mockMvc.perform(buildDeleteRequest(id))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteByIdNotFound() throws Exception {
        mockMvc.perform(buildDeleteRequest("-4711"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteAll() throws Exception {
        mockMvc.perform(buildDeleteRequest(""))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder buildPostRequest(String adsTitle) throws Exception {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(adsTitle);

        // post the advertisement as a JSON entity in the request body
        return post(AdvertisementController.PATH).content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildPutRequest(String id, String adsTitle) throws Exception {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(adsTitle);

        // post the advertisement as a JSON entity in the request body
        return put(AdvertisementController.PATH + "/" + id)
                .content(toJson(advertisement)).contentType(APPLICATION_JSON_UTF8);
    }

    private MockHttpServletRequestBuilder buildGetRequest(final String someId) {
        return get(AdvertisementController.PATH + "/" + someId).accept(APPLICATION_JSON);
    }

    private MockHttpServletRequestBuilder buildDeleteRequest(final String someId) {
        return delete(AdvertisementController.PATH + "/" + someId).accept(APPLICATION_JSON);
    }

    private String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

    private String getIdFromLocation(String location) {
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private <T> T convertJsonContent(MockHttpServletResponse response, Class<T> clazz) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String contentString = response.getContentAsString();
        return objectMapper.readValue(contentString, clazz);
    }
}
