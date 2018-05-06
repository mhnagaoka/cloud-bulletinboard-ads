package com.sap.bulletinboard.ads.services;

import com.sap.hcp.cf.logging.common.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class StatisticsServiceClient {

    private AmqpTemplate rabbitTemplate;
    private final Logger logger;

    @Inject
    public StatisticsServiceClient(AmqpTemplate amqpTemplate) {
        this.rabbitTemplate = amqpTemplate;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public void advertisementIsShown(long id) {
        logger.info("Updating statistics for ad: {}", id);
        rabbitTemplate.convertAndSend(null, "statistics.adIsShown", String.valueOf(id), message -> {
            message.getMessageProperties().setCorrelationId(LogContext.getCorrelationId());
            return message;
        });
    }
}
