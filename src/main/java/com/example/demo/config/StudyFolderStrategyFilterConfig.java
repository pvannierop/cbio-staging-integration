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
@ConditionalOnExpression("${properties.scan.studyfiles.strategy:studydir} || ${properties.scan.studyfiles.strategy:versiondir}")
public class StudyFolderStrategyFilterConfig {

    @Bean
    public FileListFilter<File> fileListFilter() {
        return new ExpressionFileListFilter<File>("isDirectory()");
    }


}