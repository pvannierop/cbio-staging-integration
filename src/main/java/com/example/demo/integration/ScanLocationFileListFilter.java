package com.example.demo.integration;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.FileListFilter;
import org.springframework.stereotype.Component;

/**
 * ScanLocationFileListFilter
 *
 * Slurps up all FileListFilter<File> from appliction context.
 */
@Component
public class ScanLocationFileListFilter extends CompositeFileListFilter<File> {

    @Autowired
    public ScanLocationFileListFilter(List<FileListFilter<File>> fileFilters) {
        super(fileFilters);
    }

}