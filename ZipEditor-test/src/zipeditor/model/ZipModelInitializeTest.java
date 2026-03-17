package zipeditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Test;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

/**
 * Test cases for {@link ZipModel#initialize}: new files, empty files,
 * and existing zip files with content.
 */
public class ZipModelInitializeTest {

	private ZipModel model;
	private File tempFile;

	@After
	public void after() {
		if (model != null) {
			model.dispose();
			model = null;
		}
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
			tempFile = null;
		}
	}

	@Test
	public void shouldInitializeWithEmptyFile() throws Exception {
		tempFile = File.createTempFile("zipeditor-empty", ".zip");
		// Empty file: 0 bytes
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			// nothing written
		}

		model = new ZipModel(tempFile, new FileInputStream(tempFile), true);

		assertEquals(ContentTypeId.ZIP_FILE, model.getType());
		assertNotNull(model.getRoot());
		assertTrue(model.getRoot().getChildren() == null || model.getRoot().getChildren().length == 0);
	}

	@Test
	public void shouldInitializeWithEmptyStreamWhenPathIsNull() throws Exception {
		InputStream emptyStream = new ByteArrayInputStream(new byte[0]);

		model = new ZipModel(null, emptyStream, true);

		assertEquals(ContentTypeId.ZIP_FILE, model.getType());
		assertNotNull(model.getRoot());
		assertTrue(model.getRoot().getChildren() == null || model.getRoot().getChildren().length == 0);
	}

	@Test
	public void shouldInitializeWithExistingZipWithContent() throws Exception {
		File path = new File("resources/archive.zip");
		model = new ZipModel(path, new FileInputStream(path), true);

		assertEquals(ContentTypeId.ZIP_FILE, model.getType());
		assertNotNull(model.getRoot());
		assertEquals(2, model.getRoot().getChildren().length);
		assertNotNull(model.getRoot().getChildByName("folder", false));
		assertNotNull(model.getRoot().getChildByName("about.html", false));
		assertNotNull(model.getRoot().getChildByName("folder", false)
				.getChildByName("about.html", false));
	}

	@Test
	public void shouldInitializeWithEmptyZipFile() throws Exception {
		// Minimal valid empty zip (End-of-Central-Directory record only)
		byte[] emptyZip = { 0x50, 0x4b, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		tempFile = File.createTempFile("zipeditor-emptyzip", ".zip");
		try (FileOutputStream fos = new FileOutputStream(tempFile)) {
			fos.write(emptyZip);
		}

		model = new ZipModel(tempFile, new FileInputStream(tempFile), true);

		assertEquals(ContentTypeId.ZIP_FILE, model.getType());
		assertNotNull(model.getRoot());
		assertTrue(model.getRoot().getChildren() == null || model.getRoot().getChildren().length == 0);
	}

}
