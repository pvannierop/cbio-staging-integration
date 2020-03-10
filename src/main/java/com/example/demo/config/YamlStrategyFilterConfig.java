package com.example.demo.config;

import java.io.File;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;

/**
 * YamlStrategy
 */
@Configuration
@ConditionalOnProperty(value = "scan.studyfiles.strategy", havingValue = "yaml")
public class YamlStrategyFilterConfig {

    @Bean
    public FileListFilter<File> fileListFilter() {
        return new RegexPatternFileListFilter(".*\\.ya*ml");
    }

}