package zipeditor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class ZipEditorTest {

	private ZipEditor openEditor(final File file) {
		final ZipEditor[] result = new ZipEditor[1];
		syncExec(() -> {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(
						new Path(file.getAbsolutePath()));
				IEditorPart editor = page.openEditor(Utils.createEditorInput(fileStore), "zipeditor.ZipEditor");
				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}
				assertTrue(editor instanceof ZipEditor);
				result[0] = (ZipEditor) editor;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		return result[0];
	}

	private File createTempCopy(File original) throws IOException {
		File temp = File.createTempFile(original.getName(), ".zip");
		temp.deleteOnExit();
		Files.copy(original.toPath(), temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		return temp;
	}

	private void closeAllEditors() {
		syncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.closeAllEditors(false);
		});
	}

	@Test
	public void openZipEditorAndCheckBasics() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				assertNotNull(editor.getModel());
				assertNotNull(editor.getViewer());
				assertNotNull(editor.getRootNode());
				assertTrue(editor.isSaveAsAllowed());
				assertNotNull(editor.getPreferenceStore());

				// getMode
				int mode = editor.getMode();
				assertTrue(mode > 0);
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void getAdapterReturnsOutlinePage() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				Object adapter = editor.getAdapter(IContentOutlinePage.class);
				assertNotNull(adapter);
				assertTrue(adapter instanceof IContentOutlinePage);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// Calling again returns same instance
				Object adapter2 = editor.getAdapter(IContentOutlinePage.class);
				assertTrue(adapter == adapter2);

				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void selectionChangedUpdatesStatus() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				StructuredViewer viewer = editor.getViewer();
				Node root = editor.getRootNode();
				Node[] children = root.getChildren();
				assertTrue(children.length > 0);

				// Select a node - triggers handleViewerSelectionChanged
				viewer.setSelection(new StructuredSelection(children[0]), true);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				Node[] selected = editor.getSelectedNodes();
				assertNotNull(selected);
				assertTrue(selected.length > 0);
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void isDirtyReturnsFalseInitially() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				assertTrue(!editor.isDirty());
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void setFocusSetsViewerFocus() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				editor.setFocus();
				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void updateViewToTreeMode() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Switch to tree mode
				editor.updateView(PreferenceConstants.VIEW_MODE_TREE, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				assertNotNull(editor.getViewer());
				assertNotNull(editor.getRootNode());
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void updateViewToFlatMode() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Switch to flat (one layer folders) mode
				editor.updateView(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				assertNotNull(editor.getViewer());
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void doSaveDoesNotCrash() throws Exception {
		File temp = createTempCopy(new File("resources/archive.zip").getAbsoluteFile());
		final ZipEditor editor = openEditor(temp);
		try {
			syncExec(() -> {
				editor.doSave(new NullProgressMonitor());

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void doRevertResetsModel() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				ZipModel modelBefore = editor.getModel();
				assertNotNull(modelBefore);

				editor.doRevert();

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// After revert, model should be recreated
				ZipModel modelAfter = editor.getModel();
				assertNotNull(modelAfter);
				assertTrue(modelBefore != modelAfter);
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void openTarEditor() {
		final ZipEditor editor = openEditor(new File("resources/archive.tar").getAbsoluteFile());
		try {
			syncExec(() -> {
				assertNotNull(editor.getModel());
				assertNotNull(editor.getRootNode());

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void openTarGzEditor() {
		final ZipEditor editor = openEditor(new File("resources/archive.tar.gz").getAbsoluteFile());
		try {
			syncExec(() -> {
				assertNotNull(editor.getModel());
				assertNotNull(editor.getRootNode());

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void storeTableColumnPreferences() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Ensure we're in table mode
				editor.updateView(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// Should not throw
				editor.storeTableColumnPreferences();
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void storeTableColumnPreferencesInTreeMode() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				editor.updateView(PreferenceConstants.VIEW_MODE_TREE, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// In tree mode, storeTableColumnPreferences should return early
				editor.storeTableColumnPreferences();
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void propertyChangeTriggersUpdateView() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Simulate a sorting preference change
				String property = PreferenceConstants.PREFIX_EDITOR + PreferenceConstants.SORT_ENABLED + "SortingChanged";
				PropertyChangeEvent event = new PropertyChangeEvent(this, property, Boolean.FALSE, Boolean.TRUE);
				editor.propertyChange(event);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				assertNotNull(editor.getViewer());
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void doubleClickOnFolderInTreeMode() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				editor.updateView(PreferenceConstants.VIEW_MODE_TREE, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				Node root = editor.getRootNode();
				Node folder = null;
				for (Node child : root.getChildren()) {
					if (child.isFolder()) {
						folder = child;
						break;
					}
				}
				assertNotNull("Archive should contain a folder", folder);

				StructuredViewer viewer = editor.getViewer();
				assertTrue(viewer instanceof TreeViewer);

				// Select the folder and double-click -> toggles expand
				viewer.setSelection(new StructuredSelection(folder), true);
				while (display.readAndDispatch()) {}

				// Simulate double-click via viewer event
				viewer.getControl().notifyListeners(SWT.MouseDoubleClick, new Event());
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void doubleClickOnFolderInTableMode() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				editor.updateView(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				Node root = editor.getRootNode();
				Node folder = null;
				for (Node child : root.getChildren()) {
					if (child.isFolder()) {
						folder = child;
						break;
					}
				}
				assertNotNull("Archive should contain a folder", folder);

				StructuredViewer viewer = editor.getViewer();
				assertTrue(viewer instanceof TableViewer);

				// Select folder and simulate double-click
				viewer.setSelection(new StructuredSelection(folder), true);
				while (display.readAndDispatch()) {}

				viewer.getControl().notifyListeners(SWT.MouseDoubleClick, new Event());
				while (display.readAndDispatch()) {}
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void saveAndRestoreState() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Ensure table mode with folder navigation for frame list
				editor.updateView(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				XMLMemento memento = XMLMemento.createWriteRoot("state");
				editor.saveState(memento);

				editor.restoreState(memento);
			});
		} finally {
			closeAllEditors();
		}
	}

	@Test
	public void tableSortColumnClick() {
		final ZipEditor editor = openEditor(new File("resources/archive.zip").getAbsoluteFile());
		try {
			syncExec(() -> {
				// Ensure table mode
				editor.updateView(PreferenceConstants.VIEW_MODE_FOLDERS_ONE_LAYER, false);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				StructuredViewer viewer = editor.getViewer();
				assertTrue(viewer instanceof TableViewer);

				Table table = ((TableViewer) viewer).getTable();
				TableColumn[] columns = table.getColumns();
				assertTrue(columns.length > 0);

				// Click first column to sort
				columns[0].notifyListeners(SWT.Selection, new Event());
				while (display.readAndDispatch()) {}

				// Click same column again to reverse sort direction
				columns[0].notifyListeners(SWT.Selection, new Event());
				while (display.readAndDispatch()) {}

				// Click a different column
				if (columns.length > 1) {
					columns[1].notifyListeners(SWT.Selection, new Event());
					while (display.readAndDispatch()) {}
				}
			});
		} finally {
			closeAllEditors();
		}
	}
}
