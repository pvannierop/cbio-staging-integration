package com.example.demo.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;

import com.example.demo.integration.ScanLocationFileListFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;

/**
 * IntegrationConfig
 */
@Configuration
public class IntegrationConfig {

    @Transformer(inputChannel = "scan.channel", outputChannel = "scan.resource.channel")
    public Resource toResource(File file) {
        return new FileSystemResource(file);
    }

    @Value("${scan.location:}")
    private Resource scanLocation;

    @Autowired
    private ScanLocationFileListFilter remoteFileFilter;

    @Bean
    @InboundChannelAdapter(value = "scan.channel", poller = @Poller(cron = "${scan.cron:* * * * * *}"))
    public MessageSource<File> fileReadingMessageSource() throws IOException {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(scanLocation.getFile());
        source.setScanner(directoryScanner());
        return source;
    }

    @Bean
    public DirectoryScanner directoryScanner() {
        RecursiveDirectoryScanner scanner = new RecursiveDirectoryScanner();
        scanner.setFilter(remoteFileFilter);
        scanner.setFileVisitOptions(FileVisitOption.values());
        return scanner;
    }

}