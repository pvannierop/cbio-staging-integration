package com.example.demo.service.studystrategy;

import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.service.resource.Study;

import org.springframework.core.io.Resource;

public interface IStudyResourceStrategy {

    Study[] resolveResources(Resource resource) throws ResourceCollectionException;

}
