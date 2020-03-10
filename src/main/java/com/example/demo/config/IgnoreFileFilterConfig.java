package com.example.demo.config;

import java.io.File;
import java.io.IOException;

import com.example.demo.integration.ModificationInsensitivePersistentFileListFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;
import org.springframework.integration.metadata.PropertiesPersistingMetadataStore;

/**
 * IgnoreFileConfig
 */
@Configuration
@ConditionalOnProperty(value = "scan.ignore.file", havingValue = "")
public class IgnoreFileFilterConfig {

    @Value("${scan.ignore.file:}")
    private Resource ignoreFile;

    @Bean
    @Order(1)
    public FileListFilter<File> fileSystemPersistentAcceptOnceFileListFilter() throws IOException {
        return new ModificationInsensitivePersistentFileListFilter(metadataStore(), "scan.ignore");
    }

    @Bean
    public ConcurrentMetadataStore metadataStore() throws IOException {
        PropertiesPersistingMetadataStore store = new PropertiesPersistingMetadataStore();
        String path = ignoreFile.getFile().getAbsolutePath();
        String dir = path.substring(0, path.lastIndexOf("/"));
        store.setBaseDirectory(dir);
        store.setFileName(ignoreFile.getFilename());
        return store;
    }

}