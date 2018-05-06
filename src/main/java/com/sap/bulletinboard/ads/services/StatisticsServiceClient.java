package com.sap.bulletinboard.ads.services;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class StatisticsServiceClient {

    private AmqpTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public StatisticsServiceClient(AmqpTemplate amqpTemplate) {
        this.rabbitTemplate = amqpTemplate;
    }

    public void advertisementIsShown(long id) {
        try {
            new UpdateStatisticsCommand(id, rabbitTemplate).queue();
        } catch (HystrixRuntimeException ex) {
            logger.warn("[HystrixFailure:" + ex.getFailureType().toString() + "] " + ex.getMessage());
        }
    }
}
