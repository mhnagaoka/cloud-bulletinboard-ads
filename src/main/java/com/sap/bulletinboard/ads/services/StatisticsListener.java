package com.sap.bulletinboard.ads.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
@Profile("cloud")
public class StatisticsListener implements MessageListener {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onMessage(Message message) {
        logger.info("got statistics: {}", new String(message.getBody(), Charset.forName("UTF-8")));
    }
}
