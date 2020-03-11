package com.example.demo.service.studystrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.resource.IResourceProvider;
import com.example.demo.service.resource.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * YamlResourceStrategy
 *
 * Selects the newest Yaml file and extracts a
 * list of resources per study.
 */
@Component
@ConditionalOnProperty(value="scan.studyfiles.strategy", havingValue = "yaml")
public class YamlFileStudyResourceStrategy implements IStudyResourceStrategy {

    private static final Logger logger = LoggerFactory.getLogger(YamlFileStudyResourceStrategy.class);

    @Configuration
    static class MyConfiguration {

        @Bean
        public Yaml yamlParser() {
            return new Yaml();
        }

    }

    @Value("${scan.yaml.fileprefix:list_of_studies}")
    private String yamlPrefix;

    @Value("${scan.location}")
    private String scanLocation;

    @Autowired
    private IResourceProvider resourceProvider;

    @Autowired
    private Yaml yamlParser;

    @Autowired
    private ResourceUtils utils;

    @Override
    @ServiceActivator(inputChannel = "scan.resource.channel", outputChannel = "resolved.studies.channel")
    public Study[] resolveResources(Resource yamlFile) throws ResourceCollectionException {

        logger.info("Processing yaml file: " + yamlFile.getFilename());

        List<Study> out = new ArrayList<>();
        String timestamp = utils.getTimeStamp("yyyyMMdd-HHmmss");
        try {

            if (yamlFile != null) {

                Map<String, List<String>> parsedYaml = parseYaml((InputStreamSource) yamlFile);

                for (Entry<String, List<String>> entry : parsedYaml.entrySet()) {
                    List<Resource> collectedResources = new ArrayList<>();
                    for (String filePath : entry.getValue() ) {
                        String fullFilePath = filePath(filePath);
                        collectedResources.add(resourceProvider.getResource(fullFilePath));
                    }
                    out.add(new Study(entry.getKey(), yamlFile.getFilename(), timestamp, null, collectedResources.toArray(new Resource[0])));
                }
            }

        } catch (IOException e) {
            throw new ResourceCollectionException("Cannot read from yaml file.");
        }

        return out.toArray(new Study[0]);
    }

    private Map<String, List<String>> parseYaml(InputStreamSource resource) throws IOException {
		InputStream is = resource.getInputStream();
		@SuppressWarnings("unchecked")
        Map<String, List<String>> result = (Map<String, List<String>>) yamlParser.load(is);
        is.close();
		return result;
    }

    private String filePath(String filePath) {
        String trimmedScanLocation = utils.trimPathRight(scanLocation);
        String trimmedFilePath = filePath.replaceFirst("^\\/+", "");
        return trimmedScanLocation + "/" + trimmedFilePath;
    }

}