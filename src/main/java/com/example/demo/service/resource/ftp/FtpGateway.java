package com.example.demo.service.resource.ftp;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.sftp.session.SftpFileInfo;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway
@ConditionalOnProperty(value="scan.location.type", havingValue ="sftp")
public interface FtpGateway {

    @Gateway(requestChannel = "resource.ls")
    public List<SftpFileInfo> ls(String dir);

    @Gateway(requestChannel = "resource.ls.dir")
    public List<SftpFileInfo> lsDir(String dir);

    @Gateway(requestChannel = "resource.ls.dir.recur")
    public List<SftpFileInfo> lsDirRecur(String dir);

    @Gateway(requestChannel = "resource.get.stream")
    public InputStream getStream(String file);

    @Gateway(requestChannel = "resource.put")
    public String put(byte[] bytes, @Header("dest.dir") String destinationDir, @Header("filename") String fileName);

    @Gateway(requestChannel = "resource.rm")
    public String rm(String file);

}