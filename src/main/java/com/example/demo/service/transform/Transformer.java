/*
* Copyright (c) 2018 The Hyve B.V.
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.example.demo.service.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.example.demo.exceptions.ReporterException;
import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.service.ExitStatus;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.directory.IDirectoryCreator;
import com.example.demo.service.resource.IResourceProvider;
import com.example.demo.service.resource.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class Transformer {
    private static final Logger logger = LoggerFactory.getLogger(Transformer.class);

    @Autowired
    private ITransformerService transformerService;

    @Autowired
    private ResourceUtils utils;

    @Autowired
    private IResourceProvider provider;

    @Autowired
	private IDirectoryCreator directoryCreator;

    final private Map<String, Resource> logFiles = new HashMap<>();
    final private List<Study> validStudies = new ArrayList<>();

    @ServiceActivator(inputChannel = "transform.channel", outputChannel = "load.channel")
    public Map<String, ExitStatus> transform(Study[] studies) throws ReporterException {

        logFiles.clear();
        validStudies.clear();

        Map<String, ExitStatus> statusStudies = new HashMap<String, ExitStatus>();

        try {
            for (Study study: studies) {

                String studyId = study.getStudyId();
                ExitStatus transformationStatus = ExitStatus.SUCCESS;
                Resource transformedFilesPath;
                try {
                    Resource untransformedFilesPath = study.getStudyDir();
                    transformedFilesPath = directoryCreator.createTransformedStudyDir(study, untransformedFilesPath);

                    Resource logFile = utils.createFileResource(transformedFilesPath, study.getStudyId() + "_transformation_log.txt");
                    logFiles.put(studyId + " transformation log", logFile);

                    if (metaFileExists(untransformedFilesPath)) {
                        utils.copyDirectory(untransformedFilesPath, transformedFilesPath);
                        transformationStatus = ExitStatus.SKIPPED;
                    } else {
                        transformationStatus = transformerService.transform(untransformedFilesPath, transformedFilesPath, logFile);
                    }

                } catch (Exception e) {
                    throw new ReporterException(e);
                }

                Resource[] resources;
                    resources = provider.list(transformedFilesPath);
                    Study transformedStudy = new Study(studyId, study.getVersion(), study.getTimestamp(), transformedFilesPath, resources);

                //Add status of the validation for the study
                statusStudies.put(studyId, transformationStatus);
                if (transformationStatus == ExitStatus.SUCCESS) {
                    validStudies.add(transformedStudy);
                    logger.info("Transformation of study "+studyId+" finished successfully.");
                } else if (transformationStatus == ExitStatus.WARNING) {
                    validStudies.add(transformedStudy);
                    logger.info("Transformation of study "+studyId+" finished successfully with warnings.");
                } else if (transformationStatus == ExitStatus.SKIPPED) {
                    validStudies.add(transformedStudy);
                    logger.info("Study "+studyId+" does contain a meta file, so the transformation step is skipped.");
                } else {
                    logger.error("Transformation process of study "+studyId+" failed.");
                }
            }

        } catch (ResourceCollectionException e) {
            throw new ReporterException(e);
        }

        logger.info("Transformation step finished.");
        return statusStudies;
    }

    private boolean metaFileExists(Resource originPath) throws ResourceCollectionException {
        Resource[] studyFiles = provider.list(originPath);
        return Stream.of(studyFiles).anyMatch(f -> f.getFilename().contains("meta_study.txt"));
    }

    public Map<String, Resource> getLogFiles() {
        return logFiles;
    }

    public Study[] getValidStudies() {
        return validStudies.toArray(new Study[0]);
    }
}
