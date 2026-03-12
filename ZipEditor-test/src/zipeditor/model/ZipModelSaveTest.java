package zipeditor.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import zipeditor.model.ZipContentDescriber.ContentTypeId;

public class ZipModelSaveTest {

	private ZipModel model;
	private File tempFile;

	@Before
	public void before() throws Exception {
		File path = new File("resources/archive.zip");
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
	public void shouldSaveAndReloadZip() throws Exception {
		InputStream saved = model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".zip");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(ContentTypeId.ZIP_FILE, reloaded.getType());
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
		assertNotNull(reloaded.getRoot().getChildByName("folder", false)
				.getChildByName("about.html", false));
	}

	@Test
	public void shouldSaveAsTar() throws Exception {
		InputStream saved = model.save(ContentTypeId.TAR_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tar");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
	}

	@Test
	public void shouldSaveAsTgz() throws Exception {
		InputStream saved = model.save(ContentTypeId.TGZ_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tgz");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(2, reloaded.getRoot().getChildren().length);
	}

	@Test
	public void shouldSaveAsTbz() throws Exception {
		InputStream saved = model.save(ContentTypeId.TBZ_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".tbz");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertEquals(2, reloaded.getRoot().getChildren().length);
		assertNotNull(reloaded.getRoot().getChildByName("folder", false));
		assertNotNull(reloaded.getRoot().getChildByName("about.html", false));
	}

	@Test
	public void shouldSaveAsGz() throws Exception {
		InputStream saved = model.save(ContentTypeId.GZ_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".gz");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertNotNull(reloaded.getRoot());
	}

	@Test
	public void shouldSaveAsBz2() throws Exception {
		InputStream saved = model.save(ContentTypeId.BZ2_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".bz2");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertNotNull(reloaded.getRoot());
	}

	@Test
	public void shouldSaveStoredEntryUnmodified() throws Exception {
		ZipNode node = (ZipNode) model.findNode("about.html");
		node.setMethod(ZipEntry.STORED);

		InputStream saved = model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".zip");

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertNotNull(reloaded.findNode("about.html"));
	}

	@Test
	public void shouldSaveStoredEntryModified() throws Exception {
		ZipNode node = (ZipNode) model.findNode("about.html");
		node.setMethod(ZipEntry.STORED);
		File contentFile = File.createTempFile("content", ".html");
		try (FileOutputStream fos = new FileOutputStream(contentFile)) {
			fos.write("<html>modified</html>".getBytes());
		}
		node.updateContent(contentFile);

		InputStream saved = model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		tempFile = saveToTempFile(saved, ".zip");
		contentFile.delete();

		ZipModel reloaded = new ZipModel(tempFile, new FileInputStream(tempFile), true);
		assertNotNull(reloaded.findNode("about.html"));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldRejectSaveAsRpm() throws Exception {
		model.save(ContentTypeId.RPM_FILE, new NullProgressMonitor());
	}

	@Test
	public void shouldFindNodeByPath() throws Exception {
		assertNotNull(model.findNode("about.html"));
		assertNotNull(model.findNode("folder/about.html"));
		assertNotNull(model.findNode("folder"));
		assertNull(model.findNode("nonexistent"));
	}

	@Test
	public void shouldCreateFolderNode() throws Exception {
		Node folder = model.createFolderNode(model.getRoot(), "newFolder");
		assertNotNull(folder);
		assertTrue(folder.isFolder());
		assertEquals("newFolder", folder.getName());
		assertNotNull(model.getRoot().getChildByName("newFolder", false));
	}

	@Test
	public void shouldCreateNestedFolderNode() throws Exception {
		Node folder = model.createFolderNode(model.getRoot(), "a/b/c");
		assertNotNull(folder);
		assertEquals("c", folder.getName());
		assertNotNull(model.getRoot().getChildByName("a", false));
		assertNotNull(model.getRoot().getChildByName("a", false).getChildByName("b", false));
		assertNotNull(model.getRoot().getChildByName("a", false).getChildByName("b", false)
				.getChildByName("c", false));
	}

	@Test
	public void shouldDisposeTempDir() throws Exception {
		model.save(ContentTypeId.ZIP_FILE, new NullProgressMonitor());
		File tmpDir = model.getTempDir();
		assertTrue(tmpDir.exists());

		model.dispose();
		assertFalse(tmpDir.exists());
	}

	@Test
	public void shouldTrackDirtyState() throws Exception {
		assertFalse(model.isDirty());
		model.setDirty(true);
		assertTrue(model.isDirty());
		model.setDirty(false);
		assertFalse(model.isDirty());
	}

	@Test
	public void shouldBeReadonly() throws Exception {
		File path = new File("resources/archive.zip");
		ZipModel readonlyModel = new ZipModel(path, new FileInputStream(path), true);
		assertTrue(readonlyModel.isReadonly());
		assertFalse(model.isReadonly());
	}

	@Test
	public void shouldNotBeInitializing() throws Exception {
		assertFalse(model.isInitializing());
	}
}
