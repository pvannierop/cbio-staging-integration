package com.example.demo.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * DummyService
 */
@Component
public class DummyService {

    private static final Logger logger = LoggerFactory.getLogger(DummyService.class);

    @ServiceActivator(inputChannel = "scan.resource.channel")
    public void printMessage(Resource res) throws IOException {
        logger.info("Resource has URL:  " + res.getURL().toString());
    }

}