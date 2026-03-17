package zipeditor.model;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import zipeditor.model.zstd.ZstdUtilities;

/**
 * Small facade to read ZIP entry streams.
 * <p>
 * Uses {@link java.util.zip.ZipFile} for standard ZIPs and Apache Commons Compress
 * for ZIPs containing Zstd entries (method 93 / deprecated 20).
 */
final class ZipReadFacade implements Closeable {

	private final ZipFile javaZipFile;
	private final org.apache.commons.compress.archivers.zip.ZipFile commonsZipFile;

	private ZipReadFacade(ZipFile javaZipFile, org.apache.commons.compress.archivers.zip.ZipFile commonsZipFile) {
		this.javaZipFile = javaZipFile;
		this.commonsZipFile = commonsZipFile;
	}

	static ZipReadFacade open(ZipModel model) throws IOException {
		File path = model.getZipPath();
		if (path == null) {
			throw new IOException("No zip path available"); //$NON-NLS-1$
		}
		if (model.getType() == ZipContentDescriber.ContentTypeId.ZIP_ZSTD_FILE) {
			try {
				return new ZipReadFacade(null, ZstdUtilities.getZipFileBuilder().setFile(path).get());
			} catch (ZipEditorZstdException e) {
				// Keep error semantics similar to java.util.zip.ZipFile open failures.
				IOException ioe = new IOException(e.getMessage());
				ioe.initCause(e);
				throw ioe;
			}
		}
		return new ZipReadFacade(new ZipFile(path), null);
	}

	InputStream getInputStream(ZipEntry entry) throws IOException {
		if (entry == null) {
			return null;
		}
		if (javaZipFile != null) {
			return javaZipFile.getInputStream(entry);
		}
		ZipArchiveEntry commonsEntry = commonsZipFile.getEntry(entry.getName());
		return commonsEntry != null ? commonsZipFile.getInputStream(commonsEntry) : null;
	}

	@Override
	public void close() throws IOException {
		if (javaZipFile != null) {
			javaZipFile.close();
		}
		if (commonsZipFile != null) {
			commonsZipFile.close();
		}
	}
}
