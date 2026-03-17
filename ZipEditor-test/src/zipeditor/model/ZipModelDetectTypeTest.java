package zipeditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Test;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

/**
 * Test cases for {@link ZipModel#detectType}.
 */
public class ZipModelDetectTypeTest {

	@Test
	public void shouldReturnNullForEmptyStream() throws Exception {
		InputStream empty = new ByteArrayInputStream(new byte[0]);
		assertNull(ZipModel.detectType(empty));
	}

	@Test
	public void shouldDetectEmptyZip() throws Exception {
		byte[] emptyZip = { 0x50, 0x4b, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		assertEquals(ContentTypeId.ZIP_FILE, ZipModel.detectType(new ByteArrayInputStream(emptyZip)));
	}

	@Test
	public void shouldDetectZipFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.zip")) {
			assertEquals(ContentTypeId.ZIP_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectZipZstdFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/zstd.zip")) {
			assertEquals(ContentTypeId.ZIP_ZSTD_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectTarFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.tar")) {
			assertEquals(ContentTypeId.TAR_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectGzipFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.gz")) {
			assertEquals(ContentTypeId.GZ_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectTgzFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.tgz")) {
			assertEquals(ContentTypeId.TGZ_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectTbzFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.tbz")) {
			assertEquals(ContentTypeId.TBZ_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectRpmFile() throws Exception {
		try (InputStream in = new FileInputStream("resources/archive.rpm")) {
			assertEquals(ContentTypeId.RPM_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectZip64File() throws Exception {
		try (InputStream in = new FileInputStream("resources/invalid_zip64.zip")) {
			assertEquals(ContentTypeId.ZIP_FILE, ZipModel.detectType(in));
		}
	}

	@Test
	public void shouldDetectStreamWithoutMarkSupport() throws Exception {
		InputStream noMark = new InputStream() {
			@Override
			public int read() {
				return -1;
			}
			@Override
			public boolean markSupported() {
				return false;
			}
		};
		assertNull(ZipModel.detectType(noMark));
	}
}
