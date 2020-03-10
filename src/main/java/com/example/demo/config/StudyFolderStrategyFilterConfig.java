package com.example.demo.config;

import java.io.File;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.file.filters.ExpressionFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;

/**
 * StudyFolderStrategy
 */
@Configuration
@ConditionalOnExpression(value = "'${properties.scan.studyfiles.strategy}'.equals('studydir') or '${properties.scan.studyfiles.strategy}'.equals('versiondir')")
public class StudyFolderStrategyFilterConfig {

    @Bean
    public FileListFilter<File> fileListFilter() {
        return new ExpressionFileListFilter<File>("isDirectory()");
    }


}