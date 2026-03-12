package zipeditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

public class TarModelSaveTest {

	private ZipModel model;
	private File tempFile;

	@Before
	public void before() throws Exception {
		File path = new File("resources/archive.tar");
		model = new ZipModel(path, new FileInputStream(path), false);
	}

	@After
	public void after() {
		if (tempFile != null && tempFile.exists())
			tempFile.delete();
	}

	private File saveToTempFile(InputStream saved, String suffix) throws Exception {
		File file = File.createTempFile("test", suffix);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			byte[] buf = new byte[4096];
			int count;
			while ((count = saved.read(buf)) != -1)
				fos.write(buf, 0, count);
		}
		saved.close();
		return file;
	}

	@Test
	public void shouldSaveAsTar() throws Exception {
		InputStream saved = model.save(ContentTypeId.TAR_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tar");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(ContentTypeId.TAR_FILE, reloaded.getType());
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
		assertNotNull(reloaded.getRoot().getChildByName("folder", false)
				.getChildByName("about.html", false));
	}

	@Test
	public void shouldSaveAsTgz() throws Exception {
		InputStream saved = model.save(ContentTypeId.TGZ_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tgz");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(ContentTypeId.TGZ_FILE, reloaded.getType());
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
	}

	@Test
	public void shouldSaveAsTbz() throws Exception {
		InputStream saved = model.save(ContentTypeId.TBZ_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tbz");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(ContentTypeId.TBZ_FILE, reloaded.getType());
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
	}
}
