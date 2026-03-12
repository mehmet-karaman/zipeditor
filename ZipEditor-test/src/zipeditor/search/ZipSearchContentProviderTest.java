package zipeditor.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.PreferenceConstants;

public class ZipSearchContentProviderTest {

	private ZipSearchResult runSearch() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);
		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		query.run(new NullProgressMonitor());
		return (ZipSearchResult) query.getSearchResult();
	}

	@Test
	public void tableViewerGetChildren() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				provider.inputChanged(viewer, null, result);

				Object[] children = provider.getChildren(result);
				assertNotNull(children);
				assertTrue(children.length > 0);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void treeViewerGetChildren() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
				TreeViewer viewer = new TreeViewer(shell);
				provider.inputChanged(viewer, null, result);

				Object[] children = provider.getChildren(result);
				assertNotNull(children);
				assertTrue(children.length > 0);

				for (Object child : children) {
					assertTrue(child instanceof Element);
				}

				Element element = (Element) children[0];
				Object[] elementChildren = provider.getChildren(element);
				assertNotNull(elementChildren);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void elementsChangedAdd() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				provider.inputChanged(viewer, null, result);

				Object[] elements = result.getElements();
				assertTrue(elements.length > 0);

				provider.elementsChanged(elements);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void elementsChangedRemove() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				viewer.setContentProvider(provider);
				viewer.setInput(result);

				Object[] elements = result.getElements();
				assertTrue(elements.length > 0);

				Object element = elements[0];
				result.removeAll();

				provider.elementsChanged(new Object[] { element });

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void treeViewerElementsChanged() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
				TreeViewer viewer = new TreeViewer(shell);
				provider.inputChanged(viewer, null, result);

				Object[] elements = result.getElements();
				assertTrue(elements.length > 0);

				provider.elementsChanged(elements);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void clear() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				viewer.setContentProvider(provider);
				viewer.setInput(result);

				provider.clear();

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void inputChangedToNull() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				provider.inputChanged(viewer, null, result);

				provider.inputChanged(viewer, result, null);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void disposeModels() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER);
				TableViewer viewer = new TableViewer(shell);
				provider.inputChanged(viewer, null, result);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void treeViewerClear() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
				TreeViewer viewer = new TreeViewer(shell);
				viewer.setContentProvider(provider);
				viewer.setInput(result);

				provider.clear();

				Object[] children = provider.getChildren(result);
				assertEquals(0, children.length);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	private IProject createTestProject() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("contentProviderTestProject");
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
	public void treeViewerWorkspaceScopeCreatesRootElement() throws Exception {
		final IProject project = createTestProject();
		try {
			File archiveFile = new File("resources/archive.zip").getAbsoluteFile();
			List<File> files = new ArrayList<File>();
			files.add(archiveFile);

			ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
			ZipSearchQuery query = new ZipSearchQuery(options, files);
			query.run(new NullProgressMonitor());
			final ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
			assertTrue(result.getMatchCount() > 0);

			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
				try {
					ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
					TreeViewer viewer = new TreeViewer(shell);
					provider.inputChanged(viewer, null, result);

					Object[] children = provider.getChildren(result);
					assertNotNull(children);
					assertTrue(children.length > 0);

					// Root element should be created via workspace file lookup (Zeile 144-158)
					assertTrue(children[0] instanceof Element);
					Element root = (Element) children[0];
					assertNotNull(root.getFileName());

					provider.dispose();
				} finally {
					shell.dispose();
				}
			});
		} finally {
			project.delete(true, new NullProgressMonitor());
		}
	}

	@Test
	public void treeViewerPlainFileCreatesFolder() {
		// Search with searchPlainFile=true to produce PlainNode results (type=FOLDER)
		File path = new File("resources");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, true);
		options.setPath(files);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		query.run(new NullProgressMonitor());
		final ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		assertTrue(result.getMatchCount() > 0);

		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
				TreeViewer viewer = new TreeViewer(shell);
				provider.inputChanged(viewer, null, result);

				Object[] children = provider.getChildren(result);
				assertNotNull(children);
				assertTrue(children.length > 0);

				// PlainNode results should create FOLDER root elements (Zeile 160-163)
				boolean hasFolderElement = false;
				for (Object child : children) {
					if (child instanceof Element && ((Element) child).getType() == Element.FOLDER) {
						hasFolderElement = true;
						break;
					}
				}
				assertTrue("Should have a FOLDER element for plain file results", hasFolderElement);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void treeViewerRemoveChild() {
		final ZipSearchResult result = runSearch();
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchContentProvider provider = new ZipSearchContentProvider(PreferenceConstants.VIEW_MODE_TREE);
				TreeViewer viewer = new TreeViewer(shell);
				viewer.setContentProvider(provider);
				viewer.setInput(result);

				Object[] elements = result.getElements();
				assertTrue(elements.length > 0);

				result.removeAll();
				provider.elementsChanged(elements);

				provider.dispose();
			} finally {
				shell.dispose();
			}
		});
	}
}
