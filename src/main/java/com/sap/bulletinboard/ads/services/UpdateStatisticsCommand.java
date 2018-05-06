package com.sap.bulletinboard.ads.services;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.sap.hcp.cf.logging.common.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;

import static com.netflix.hystrix.HystrixCommandProperties.Setter;

public class UpdateStatisticsCommand extends HystrixCommand<Void> {

    public static final int DEFAULT_TIMEOUT_MS = 2000;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private long id;
    private AmqpTemplate rabbitTemplate;

    public UpdateStatisticsCommand(long id, AmqpTemplate rabbitTemplate) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("User"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("User.getById"))
                .andCommandPropertiesDefaults(Setter().withExecutionTimeoutInMilliseconds(DEFAULT_TIMEOUT_MS)));
        this.id = id;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    protected Void run() throws Exception {
        logger.info("Updating statistics for ad: {}", id);
        rabbitTemplate.convertAndSend(null, "statistics.adIsShown", String.valueOf(id), message -> {
            message.getMessageProperties().setCorrelationId(LogContext.getCorrelationId());
            return message;
        });
        return null;
    }
}
