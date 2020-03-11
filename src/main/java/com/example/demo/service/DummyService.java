package com.example.demo.service;

import java.io.IOException;

import com.example.demo.service.resource.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * DummyService
 */
@Component
public class DummyService {

    private static final Logger logger = LoggerFactory.getLogger(DummyService.class);

    @ServiceActivator(inputChannel = "extract.channel")
    public void printMessage(Study study) throws IOException {
        logger.info("Dummy service recieved study:  " + study.getStudyId());
    }

}