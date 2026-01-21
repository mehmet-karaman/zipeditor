package zipeditor.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import zipeditor.model.zstd.ZstdUtilities;

public final class ZstdAwareZipArchiveInputStream extends ZipArchiveInputStream {
	public ZstdAwareZipArchiveInputStream(InputStream inputStream) {
		super(inputStream, StandardCharsets.UTF_8.name(), true, true);
	}

	@Override
	protected InputStream createZstdInputStream(InputStream in) throws IOException {
		return ZstdUtilities.getInputStream(in);
	}
}