package com.example.demo.service.resource.ftp;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Stream;

import com.example.demo.exceptions.ResourceCollectionException;
import com.example.demo.service.ResourceUtils;
import com.example.demo.service.resource.IResourceProvider;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingPredicate;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.integration.sftp.session.SftpFileInfo;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value="scan.location.type", havingValue ="sftp")
@Primary
public class FtpResourceProvider implements IResourceProvider {

    @Value("${ftp.host}")
    private String ftpHost;

    @Autowired
    protected ResourceUtils utils;

    @Autowired
    private FtpGateway ftpGateway;

    @Override
    public Resource getResource(String url) throws ResourceCollectionException {
        try {
            return new FtpResource(ftpHost, url, ftpGateway, utils);
        } catch (MalformedURLException e) {
            throw new ResourceCollectionException("Malformed URL!", e);
        }
    }

    @Override
    public Resource[] list(Resource dir) throws ResourceCollectionException {
        return list(dir, false);
    }

    @Override
    public Resource[] list(Resource dir, boolean recursive) throws ResourceCollectionException {
        return list(dir, recursive, false);
    }

    @Override
    public Resource[] list(Resource dir, boolean recursive, boolean excludeDirs) throws ResourceCollectionException {

        try {
            String remoteDir = utils.remotePath(ftpHost, dir.getURL());

            List<SftpFileInfo> remoteFiles;
            if (recursive) {
                remoteFiles = ftpGateway.lsDirRecur(remoteDir);
            } else {
                remoteFiles = ftpGateway.lsDir(remoteDir);
            }

            Stream<SftpFileInfo> remoteFilesStream = remoteFiles.stream();
            if (excludeDirs)
                remoteFilesStream = remoteFilesStream.filter(ThrowingPredicate.sneaky(e -> ! e.isDirectory()));

            return remoteFilesStream
                .map(ThrowingFunction.sneaky(e -> utils.createRemoteURL("ftp", ftpHost, e)))
                .map(ThrowingFunction.sneaky(r -> new FtpResource(ftpHost, r, ftpGateway, utils)))
                .toArray(Resource[]::new);

        } catch (Exception e) {
            throw new ResourceCollectionException("Could not read from remote directory: " + dir.getFilename(), e);
        }
    }

    @Override
    public Resource copyFromRemote(Resource destinationDir, Resource remoteResource) throws ResourceCollectionException {
        try {
            if (remoteResource.getInputStream() == null) {
                remoteResource = getResource(remoteResource.getURL().toString());
            }
            return utils.copyResource(destinationDir, remoteResource, remoteResource.getFilename());
        } catch (Exception e) {
            throw new ResourceCollectionException("Cannot copy resource from remote.", e);
        }
    }

    @Override
    public Resource copyToRemote(Resource destinationDir, Resource localResource)
            throws ResourceCollectionException {
        try {
            String remoteFilePath = ftpGateway.put(
                IOUtils.toByteArray(localResource.getInputStream()),
                utils.remotePath(ftpHost, destinationDir.getURL()),
                localResource.getFilename()
            );

            return getResource(utils.createRemoteURL("ftp", ftpHost, remoteFilePath).toString());
        } catch (Exception e) {
            throw new ResourceCollectionException("Cannot copy resource to remote.", e);
        }
    }

}
