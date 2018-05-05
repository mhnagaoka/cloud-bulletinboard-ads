package com.sap.bulletinboard.ads.services;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.sap.hcp.cf.logging.common.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

public class GetUserCommand extends HystrixCommand<UserServiceClient.User> {
    // Hystrix uses a default timeout of 1000 ms, increase in case you run into problems in remote locations
    private static final int DEFAULT_TIMEOUT_MS = 1000;
    private final Supplier<UserServiceClient.User> fallbackFunction;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String url;
    private RestTemplate restTemplate;

    public GetUserCommand(String url, RestTemplate restTemplate, Supplier<UserServiceClient.User> fallbackFunction) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("User.getById"))
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(DEFAULT_TIMEOUT_MS)));
        this.url = url;
        this.restTemplate = restTemplate;
        this.fallbackFunction = fallbackFunction;
    }

    @Override
    protected UserServiceClient.User run() throws Exception {
        logger.info("sending request {}", url);

        try {
            ResponseEntity<UserServiceClient.User> responseEntity = sendRequest();
            logger.info("received response, status code: {}", responseEntity.getStatusCode());
            return responseEntity.getBody();
        } catch(HttpServerErrorException error) {
            logger.warn("received HTTP status code: {}", error.getStatusCode());
            throw error;
        } catch(HttpClientErrorException error) {
            logger.error("received HTTP status code: {}", error.getStatusCode());
            throw new HystrixBadRequestException("Unsuccessful outgoing request", error);
        }
    }

    @Override
    protected UserServiceClient.User getFallback() {
        if (isResponseTimedOut()) {
            logger.error("execution timed out after {} ms (HystrixCommandKey:{})", getTimeoutInMs(),
                    this.getCommandKey().name());
        }
        if (isFailedExecution()) {
            logger.error("execution failed", getFailedExecutionException());
        }
        if (isResponseRejected()) {
            logger.warn("request was rejected");
        }
        return fallbackFunction.get();
    }

    protected ResponseEntity<UserServiceClient.User> sendRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(com.sap.hcp.cf.logging.common.HttpHeaders.CORRELATION_ID, LogContext.getCorrelationId());
        HttpEntity<UserServiceClient.User> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, UserServiceClient.User.class);
    }

    // this will be used in exercise 18
    protected int getTimeoutInMs() {
        return this.properties.executionTimeoutInMilliseconds().get();
    }
}
