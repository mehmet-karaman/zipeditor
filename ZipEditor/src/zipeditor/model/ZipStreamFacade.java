package zipeditor.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipMethod;

/**
 * Facade for sequential ZIP reading that hides the underlying ZIP stream implementation.
 * <p>
 * Outside this facade, no {@link ZipInputStream}/{@link ZipArchiveInputStream} methods are used.
 */
final class ZipImplFacade extends InputStream {

	private interface IZipImplWrapper {
		InputStream stream();

		ZipEntry getNextEntry() throws IOException;

		void closeEntry() throws IOException;
	}

	private final IZipImplWrapper implWrapper;

	private ZipImplFacade(IZipImplWrapper implWrapper) {
		this.implWrapper = implWrapper;
	}

	static ZipImplFacade openForModel(ZipModel model, InputStream in) {
		if (model.getType() == ZipContentDescriber.ContentTypeId.ZIP_ZSTD_FILE) {
			return new ZipImplFacade(new ZipArchiveInputStreamWrapper(in));
		}
		return new ZipImplFacade(new ZipInputStreamWrapper(in));
	}

	static ZipImplFacade openForModelPath(ZipModel model) throws IOException {
		return openForModel(model, new BufferedInputStream(new FileInputStream(model.getZipPath())));
	}

	/**
	 * Detect whether any entry in the stream uses Zstd compression.
	 * <p>
	 * The provided stream must support {@code mark/reset} (caller wraps if needed).
	 */
	static boolean containsZstdEntry(InputStream contents) throws IOException {
		ZipInputStream zip = new ZipInputStream(contents);
		for (ZipEntry e = zip.getNextEntry(); e != null; e = zip.getNextEntry()) {
			int method = e.getMethod();
			if (method == ZipMethod.ZSTD.getCode() || method == ZipMethod.ZSTD_DEPRECATED.getCode()) {
				return true;
			}
		}
		return false;
	}

	ZipEntry getNextEntry() throws IOException {
		return implWrapper.getNextEntry();
	}

	void closeEntry() throws IOException {
		implWrapper.closeEntry();
	}

	@Override
	public int read() throws IOException {
		return implWrapper.stream().read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return implWrapper.stream().read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		implWrapper.stream().close();
	}
	
	private static final class ZipInputStreamWrapper implements IZipImplWrapper {
		private final ZipInputStream in;

		private ZipInputStreamWrapper(InputStream in) {
			this.in = new ZipInputStream(in);
		}

		@Override
		public InputStream stream() {
			return in;
		}

		@Override
		public ZipEntry getNextEntry() throws IOException {
			return in.getNextEntry();
		}

		@Override
		public void closeEntry() throws IOException {
			in.closeEntry();
		}
	}

	private static final class ZipArchiveInputStreamWrapper implements IZipImplWrapper {
		private final ZipArchiveInputStream in;

		private ZipArchiveInputStreamWrapper(InputStream in) {
			this.in = new ZstdAwareZipArchiveInputStream(in);
		}

		@Override
		public InputStream stream() {
			return in;
		}

		@Override
		public ZipEntry getNextEntry() throws IOException {
			ZipArchiveEntry e = in.getNextEntry();
			return e != null ? toZipEntry(e) : null;
		}

		@Override
		public void closeEntry() throws IOException {
			// ZipArchiveInputStream doesn't expose a public per-entry close API; advancing is sufficient.
		}

	}

	public static ZipEntry toZipEntry(ZipArchiveEntry zipArchiveEntry) {
		ZipEntry e = new ZipEntry(zipArchiveEntry.getName()) {
			
			private int pMethod;

			public void setMethod(int method) {
				this.pMethod = method;
			}
			
			@Override
			public int getMethod() {
				return pMethod;
			}
		};
		e.setMethod(zipArchiveEntry.getMethod());
		e.setTime(zipArchiveEntry.getTime());
		if (zipArchiveEntry.getSize() >= 0) {
			e.setSize(zipArchiveEntry.getSize());
		}
		if (zipArchiveEntry.getComment() != null) {
			e.setComment(zipArchiveEntry.getComment());
		}
		return e;
	}
}
