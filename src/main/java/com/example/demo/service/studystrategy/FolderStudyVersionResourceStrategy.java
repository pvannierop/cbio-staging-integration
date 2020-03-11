package com.example.demo.service.studystrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.exceptions.ResourceUtilsException;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.resource.IResourceProvider;
import com.example.demo.service.resource.Study;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 *
 * Recieves a list of directories and recursively extracts a list of
 * resources for each study. The files/resources are returned keyed
 * by study id. When available the study indentifier is extracted from
 * the meta_study.txt file. If not, study identifier is set to the name
 * of the study folder.
 *
 */
@Component
@Primary
@ConditionalOnProperty(value="scan.studyfiles.strategy", havingValue = "versiondir")
public class FolderStudyVersionResourceStrategy implements IStudyResourceStrategy {

    private static final Logger logger = LoggerFactory.getLogger(FolderStudyVersionResourceStrategy.class);

    @Autowired
    private IResourceProvider resourceProvider;

    @Autowired
    private ResourceUtils utils;

    @PostConstruct
    public void init() {
        logger.debug("Activated FolderStudyVersionResourceResolver from spring profile.");
    }

    @Override
    @ServiceActivator(inputChannel = "scan.resource.channel", outputChannel = "resolved.studies.channel")
    public Study[] resolveResources(Resource studyVersionDir) throws ResourceCollectionException {

        logger.info("Processing study directory: " + studyVersionDir.getFilename());

        List<Study> out = new ArrayList<>();
        String timestamp = utils.getTimeStamp("yyyyMMdd-HHmmss");
        if (studyVersionDir != null) {

            try {
                String studyPath = getStudyPath(studyVersionDir);
                String studyVersion = getStudyVersion(studyVersionDir);

                Resource[] studyResources = resourceProvider.list(studyVersionDir, true, true);

                String studyId = getStudyId(studyResources, studyPath);

                out.add(new Study(studyId, studyVersion, timestamp, studyVersionDir, studyResources));

            } catch (ResourceUtilsException e) {
                throw new ResourceCollectionException("Cannot read from study directory:" + studyVersionDir.getFilename(), e);
            }
        }

        return out.toArray(new Study[0]);
    }

    private String getStudyId(Resource[] resources, String studyFolder) throws ResourceUtilsException {
        // find study meta file and if found get the studyId from the meta file (transformed studies)
        Optional<Resource> studyMetaFile = Stream.of(resources).filter(e -> e.getFilename().matches(".*meta_study.txt$")).findAny();
        if (studyMetaFile.isPresent()) {
            return utils.readMetaFile(studyMetaFile.get()).get("cancer_study_identifier");
        }
        // if not meta file found use the study folder name as studyId (folder previous to the "version" folder)
        studyFolder = utils.trimPathRight(studyFolder);
        return studyFolder.substring(studyFolder.lastIndexOf("/") + 1);
    }

    private String getStudyVersion(Resource dir) throws ResourceUtilsException {
        String url = utils.trimPathRight(utils.getURL(dir).toString());
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private String getStudyPath(Resource studyVersionDir) throws ResourceUtilsException {
        String studyVersionPath = utils.trimPathRight(utils.getURL(studyVersionDir).toString());
        return studyVersionPath.substring(0, studyVersionPath.lastIndexOf("/"));
    }

}