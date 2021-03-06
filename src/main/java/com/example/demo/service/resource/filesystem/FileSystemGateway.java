package com.example.demo.service.resource.filesystem;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Header;

@MessagingGateway
@ConditionalOnProperty(value="scan.location.type", havingValue ="filesystem")
public interface FileSystemGateway {

    @Gateway(requestChannel = "resource.ls")
    public List<File> ls(String dir);

    @Gateway(requestChannel = "resource.ls.dir")
    public List<File> lsDir(String dir);

    @Gateway(requestChannel = "resource.ls.dir.recur")
    public List<File> lsDirRecur(String dir);

    @Gateway(requestChannel = "resource.get.stream")
    public InputStream getStream(String file);

    @Gateway(requestChannel = "resource.put")
    public String put(byte[] bytes, @Header("dest.dir") String destinationDir, @Header("filename") String fileName);

    @Gateway(requestChannel = "resource.rm")
    public String rm(String file);

}