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
package com.example.demo.service.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.demo.exceptions.ConfigurationException;
import com.example.demo.exceptions.DirectoryCreatorException;
import com.example.demo.exceptions.ExtractionException;
import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.directory.IDirectoryCreator;
import com.example.demo.service.resource.IResourceProvider;
import com.example.demo.service.resource.Study;
import com.pivovarit.function.ThrowingFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/*
	Copies all files from scan.location to the etl.working.directory.
*/
@Component
class Extractor {
	private static final Logger logger = LoggerFactory.getLogger(Extractor.class);

	@Value("${scan.retry.time:5}")
	private Integer timeRetry;

	@Autowired
	private ResourceUtils utils;

	@Autowired
	private IDirectoryCreator directoryCreator;

	@Autowired
	private IResourceProvider resourceProvider;

	Map<String, List<String>> filesNotFound = new HashMap<>();

	// TODO: handle situation where stdy files could not be exracted
	@ServiceActivator(inputChannel = "extract.channel", outputChannel = "transform.channel")
	public Study run(Study study) throws ExtractionException {

		if (study == null) {
			throw new ExtractionException("Argument 'study' is null.");
		}

		try {
				String studyId = study.getStudyId();
				Resource studyDir = directoryCreator.createStudyExtractDir(study);

				String remoteBasePath = getBasePathResources(study.getResources());

				List<String> errorFiles = new ArrayList<>();
				List<Resource> files = new ArrayList<>();
				for (Resource remoteResource : study.getResources()) {

					String fullOriginalFilePath = remoteResource.getURL().toString();

					String path = fullOriginalFilePath.replaceFirst(remoteBasePath, "");
					path = path.substring(0, path.lastIndexOf("/"));

					Resource targetDir = resourceProvider.getResource(studyDir.getURL() + path);
					Resource localResource = attemptCopyResource(targetDir, remoteResource);
					if (localResource == null) {
						errorFiles.add(fullOriginalFilePath);
					} else {
						files.add(localResource);
					}
				}

				// return successfully extracted study
				if (errorFiles.isEmpty()) {
					logger.info("Extractor step finished");
					return new Study(study.getStudyId(), study.getVersion(), study.getTimestamp(), studyDir,
							files.toArray(new Resource[0]));
				} else {
					filesNotFound.put(studyId, errorFiles);
					return null;
				}

		} catch (IOException e) {
			throw new ExtractionException("Cannot access working ELT directory.", e);
		} catch (ConfigurationException e) {
			throw new ExtractionException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new ExtractionException("Timeout for resource downloads was interrupted.", e);
		} catch (ResourceCollectionException e) {
			throw new ExtractionException("Cannot copy Resource.", e);
		} catch (DirectoryCreatorException e) {
			throw new ExtractionException("Cannot create directory.", e);
		}

	}

	private Resource attemptCopyResource(Resource destination, Resource remoteResource)
			throws InterruptedException, ResourceCollectionException {
		int i = 1;
		int times = 5;
		Resource r = null;
		while (i++ <= times) {
			try {
				logger.info("Copying resource " + remoteResource.getURL() + " to "+ destination);
				r = resourceProvider.copyFromRemote(destination, remoteResource);
				logger.info("File has been copied successfully to "+ destination);
				break;
			} catch (IOException f) {
				if (i < times) {
					TimeUnit.MINUTES.sleep(timeRetry);
				}
			}
		}
		return r;
	}

	private String getBasePathResources(Resource[] resources) throws ConfigurationException {
		List<String> paths = Stream.of(resources)
			.map(ThrowingFunction.unchecked(e -> e.getURL().toString()))
			.collect(Collectors.toList());
		return utils.getBasePath(paths);
	}

	public Map<String, List<String>> errorFiles() {
		return filesNotFound;
	}

	public Integer getTimeRetry() {
		return timeRetry;
	}

}
