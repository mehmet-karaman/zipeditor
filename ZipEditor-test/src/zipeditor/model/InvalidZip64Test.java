package zipeditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

public class InvalidZip64Test {

	private ZipModel model;
	private File tempFile;

	@Before
	public void before() throws Exception {
		File path = new File("resources/invalid_zip64.zip");
		model = new ZipModel(path, new FileInputStream(path), false);
	}

	@After
	public void after() {
		if (model != null)
			model.dispose();
		if (tempFile != null && tempFile.exists())
			tempFile.delete();
	}

	@Test
	public void shouldOpenWithZipInputStreamFallback() throws Exception {
		assertEquals(ContentTypeId.ZIP_FILE, model.getType());
		Method createZipFile = ZipModel.class.getDeclaredMethod("createZipFile", File.class);
		createZipFile.setAccessible(true);
		assertNull(createZipFile.invoke(model, model.getZipPath()));
		assertEquals(1, model.getRoot().getChildren().length);
		Node node = model.getRoot().getChildByName("test.txt", false);
		assertNotNull(node);
		assertEquals("test.txt", node.getName());
		assertEquals(13, node.getSize());
	}

	@Test
	public void shouldReadContent() throws Exception {
		Node node = model.findNode("test.txt");
		assertNotNull(node);
		InputStream content = node.getContent();
		assertNotNull(content);
		byte[] buf = new byte[100];
		int count = content.read(buf);
		content.close();
		assertEquals("Hello, World!", new String(buf, 0, count));
	}

	@Test
	public void shouldSaveAndReload() throws Exception {
		InputStream saved = model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".zip");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(ContentTypeId.ZIP_FILE, reloaded.getType());
		assertEquals(1, reloaded.getRoot().getChildren().length);
		Node node = reloaded.getRoot().getChildByName("test.txt", false);
		assertNotNull(node);
		assertEquals(13, node.getSize());
	}

	@Test
	public void shouldSaveAndReloadWithCorrectContent() throws Exception {
		InputStream saved = model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".zip");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		Node node = reloaded.findNode("test.txt");
		assertNotNull(node);
		InputStream content = node.getContent();
		assertNotNull(content);
		byte[] buf = new byte[100];
		int count = content.read(buf);
		content.close();
		assertEquals("Hello, World!", new String(buf, 0, count));
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
}
