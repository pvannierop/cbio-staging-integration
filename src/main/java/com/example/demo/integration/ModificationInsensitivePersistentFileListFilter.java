package com.example.demo.integration;

import java.io.File;

import org.springframework.integration.file.filters.FileSystemPersistentAcceptOnceFileListFilter;
import org.springframework.integration.metadata.ConcurrentMetadataStore;

// TODO add proper explanation for the existence of this class
/**
 * ModificationInsensitivePersistentFileListFilter
 *
 * Variant of FileSystemPersistentAcceptOnceFileListFilter that does not
 * allow a file/dir when it was updated.
 *
 * Note: a dir is considered updated when its contents were updated.
 */
public class ModificationInsensitivePersistentFileListFilter extends FileSystemPersistentAcceptOnceFileListFilter {

	public ModificationInsensitivePersistentFileListFilter(ConcurrentMetadataStore store, String prefix) {
		super(store, prefix);
	}

	@Override
	protected long modified(File file) {
		return 0L;
	}

}