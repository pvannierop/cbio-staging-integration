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
package com.example.demo.service.load;

import java.util.HashMap;
import java.util.Map;

import com.example.demo.exceptions.LoaderException;
import com.example.demo.exceptions.ResourceUtilsException;
import com.example.demo.service.ExitStatus;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.resource.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

@Component
public class Loader {
    private static final Logger logger = LoggerFactory.getLogger(Loader.class);

    @Autowired
    private ILoaderService loaderService;

    @Autowired
    private ResourceUtils utils;

    private boolean areStudiesLoaded;

    final private Map<String, Resource> logFiles = new HashMap<>();

    @ServiceActivator(inputChannel = "load.channel", outputChannel = "report.channel")
    Map<String, ExitStatus> load(final Study[] studies) throws LoaderException {

        areStudiesLoaded = false;
        logFiles.clear();

        final Map<String, ExitStatus> loadResults = new HashMap<String, ExitStatus>();
        try {
            for (final Study study: studies) {

                String studyId = study.getStudyId();

                logger.info("Starting loading of study " + studyId + ". This can take some minutes.");
                final Resource studyPath = study.getStudyDir();
                Resource logFile;
                logFile = utils.createFileResource(studyPath, studyId + "_loading_log.txt");
                logFiles.put(studyId+" loading log", logFile);
                ExitStatus loadingStatus = loaderService.load(studyPath, logFile);
                //Add loading result for the email loading report
                if (loadingStatus == ExitStatus.SUCCESS) {
                    loadResults.put(studyId, ExitStatus.SUCCESS);
                    areStudiesLoaded = true;
                    logger.info("Loading of study "+studyId+" finished successfully.");
                } else {
                    loadResults.put(studyId, ExitStatus.ERROR);
                    logger.error("Loading process of study "+studyId+" failed.");
                }
            }
        } catch (ResourceUtilsException e) {
            throw new LoaderException("The Loader could not create a log file", e);
        }
        return loadResults;
    }

    Map<String, Resource> getLogFiles() {
        return logFiles;
    }

    boolean areStudiesLoaded() {
        return areStudiesLoaded;
	}
}
