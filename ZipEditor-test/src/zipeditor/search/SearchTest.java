package zipeditor.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.search.ui.text.Match;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class SearchTest {

	@Test
	public void gzSearch() throws Exception {
		ZipSearchResult result = search("archive.gz");
		Assert(result, 1, 9);
	}

	@Test
	public void tarSearch() throws Exception {
		ZipSearchResult result = search("archive.tar");
		Assert(result, 2, 3);
	}

	@Test
	public void tarBz2Search() throws Exception {
		ZipSearchResult result = search("archive.tar.bz2");
		Assert(result, 2, 3);
	}

	@Test
	public void tarGzSearch() throws Exception {
		ZipSearchResult result = search("archive.tar.gz");
		Assert(result, 2, 3);
	}

	@Test
	public void tbzSearch() throws Exception {
		ZipSearchResult result = search("archive.tbz");
		Assert(result, 2, 3);
	}

	@Test
	public void tgzSearch() throws Exception {
		ZipSearchResult result = search("archive.tgz");
		Assert(result, 2, 3);
	}

	@Test
	public void zipSearch() throws Exception {
		ZipSearchResult result = search("archive.zip");
		Assert(result, 2, 3);
	}

	@Test
	public void searchZipAndOtherFiles() {
		File path = new File("resources");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, true);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());

		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		assertNotNull(result);
		int archives = 3 * 2 * 6;
		int gz = 9;
		int large = 1;
		int plain = 2 * 3;
		assertEquals(archives + gz + plain + large, result.getMatchCount());
		Object[] elements = result.getElements();
		assertEquals(16, elements.length);
	}

	@Test
	public void largeSearch() throws Exception {
		File path = new File("resources/large.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", true, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertNotNull(result);
		assertEquals(1, result.getMatchCount());
		Object[] elements = result.getElements();
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			int count = result.getMatchCount(element);
			assertEquals(1, count);
			Match[] matches = result.getMatches(element);
			assertEquals(8190, matches[0].getOffset());
			assertEquals(9, matches[0].getLength());
		}
	}

	@Test
	public void utf8Search() throws Exception {
		File path = new File("resources/utf8.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", new String(new byte[] {(byte) 0xe8, (byte)0xa1, (byte)0x8b}, "UTF8"), "UTF8", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertEquals(1, result.getMatchCount());
		Object[] elements = result.getElements();
		assertEquals(1, elements.length);
		Object element = elements[0];
		assertEquals(1, result.getMatchCount(element));
		Match[] matches = result.getMatches(element);
		assertEquals(2123, matches[0].getOffset());
		assertEquals(1, matches[0].getLength());
	}

	@Test
	public void rpmSearch() throws Exception {
		File path = new File("resources/archive.rpm");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertNotNull(result);
		assertTrue(result.getMatchCount() > 0);
	}

	@Test
	public void caseSensitiveSearchNoMatch() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "uwe voigt", "Cp1252", true, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertEquals(0, result.getMatchCount());
	}

	@Test
	public void nodeNamePatternWithComma() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("*.html,*.txt", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertEquals(2 * 3, result.getMatchCount());
	}

	@Test
	public void noMatchSearch() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "NONEXISTENT_XYZ_12345", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertEquals(0, result.getMatchCount());
	}

	@Test
	public void cancelledSearch() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		NullProgressMonitor monitor = new NullProgressMonitor();
		monitor.setCanceled(true);
		IStatus status = query.run(monitor);

		assertEquals(IStatus.CANCEL, status.getSeverity());
	}

	@Test
	public void nestedArchiveSearch() {
		File path = new File("resources/nested.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertTrue("nested archive should find matches", result.getMatchCount() > 0);
	}

	private IProject createTestProject() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
		if (!project.exists())
			project.create(new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		File archiveFile = new File("resources/archive.zip").getAbsoluteFile();
		IFile file = project.getFile("archive.zip");
		if (!file.exists())
			file.createLink(new Path(archiveFile.getAbsolutePath()), 0, new NullProgressMonitor());
		return project;
	}

	@Test
	public void iFileElementSearch() throws Exception {
		IProject project = createTestProject();
		try {
			IFile file = project.getFile("archive.zip");

			List<IFile> elements = new ArrayList<>();
			elements.add(file);

			ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
			ZipSearchQuery query = new ZipSearchQuery(options, elements);
			IStatus status = query.run(new NullProgressMonitor());

			assertEquals(IStatus.OK, status.getCode());
			ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
			assertTrue(result.getMatchCount() > 0);
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void iResourceElementSearch() throws Exception {
		IProject project = createTestProject();
		try {
			List<IProject> elements = new ArrayList<>();
			elements.add(project);

			ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
			ZipSearchQuery query = new ZipSearchQuery(options, elements);
			IStatus status = query.run(new NullProgressMonitor());

			assertEquals(IStatus.OK, status.getCode());
			ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
			assertTrue(result.getMatchCount() > 0);
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void adaptableElementSearch() throws Exception {
		IProject project = createTestProject();
		try {
			final IFile resource = project.getFile("archive.zip");
			Object adaptable = new IAdaptable() {
				public Object getAdapter(Class adapter) {
					if (adapter == IResource.class)
						return resource;
					return null;
				}
			};

			List<Object> elements = new ArrayList<>();
			elements.add(adaptable);

			ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
			ZipSearchQuery query = new ZipSearchQuery(options, elements);
			IStatus status = query.run(new NullProgressMonitor());

			assertEquals(IStatus.OK, status.getCode());
			ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
			assertTrue(result.getMatchCount() > 0);
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void nodeElementSearch() throws Exception {
		File path = new File("resources/archive.zip");
		ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node node = model.findNode("about.html");

		List<Node> elements = new ArrayList<>();
		elements.add(node);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, elements);
		IStatus status = query.run(new NullProgressMonitor());

		assertEquals(IStatus.OK, status.getCode());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertTrue(result.getMatchCount() > 0);
	}

	protected ZipSearchResult search(String archiveName) {
		File path = new File("resources/" + archiveName);
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		IStatus status = query.run(new NullProgressMonitor());

		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());

		return (ZipSearchResult) query.getSearchResult();
	}

	protected void Assert(ZipSearchResult result, int expectedElements, int expectedMatchCount) {
		assertNotNull(result);
		assertEquals(expectedElements * expectedMatchCount, result.getMatchCount());
		Object[] elements = result.getElements();
		assertEquals(expectedElements, elements.length);

		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			int count = result.getMatchCount(element);
			assertEquals(expectedMatchCount, count);
			Match[] matches = result.getMatches(element);
			for (int j = 0; j < expectedMatchCount / 3; j++) {
				int index = 3 * j;
				int contentOffset = j * 1144;
				assertEquals(contentOffset + 223, matches[index].getOffset());
				assertEquals(9, matches[index].getLength());
				assertEquals(contentOffset + 975, matches[++index].getOffset());
				assertEquals(9, matches[index].getLength());
				assertEquals(contentOffset + 1037, matches[++index].getOffset());
				assertEquals(9, matches[index].getLength());
			}
		}
	}
}
