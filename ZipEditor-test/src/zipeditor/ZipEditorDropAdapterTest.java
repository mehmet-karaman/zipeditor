package zipeditor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class ZipEditorDropAdapterTest {

	@Test
	public void validateDropAcceptsFileTransfer() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				ZipEditorDropAdapter adapter = new ZipEditorDropAdapter(viewer);

				TransferData transferData = FileTransfer.getInstance().getSupportedTypes()[0];
				assertTrue(adapter.validateDrop(null, DND.DROP_COPY, transferData));
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void validateDropRejectsUnsupportedTransfer() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				ZipEditorDropAdapter adapter = new ZipEditorDropAdapter(viewer);

				TransferData transferData = new TransferData();
				assertFalse(adapter.validateDrop(null, DND.DROP_COPY, transferData));
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void performDropReturnsFalseForNonStringArray() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				ZipEditorDropAdapter adapter = new ZipEditorDropAdapter(viewer);

				assertFalse(adapter.performDrop("not a string array"));
				assertFalse(adapter.performDrop(null));
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void dragEnterSetsDropCopyForFileTransfer() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				ZipEditorDropAdapter adapter = new ZipEditorDropAdapter(viewer);

				DropTarget dropTarget = new DropTarget(viewer.getControl(), DND.DROP_COPY | DND.DROP_DEFAULT);
				DropTargetEvent dropEvent = ZipEditorDragAdapterTest.createDNDEventWrapper(dropTarget, DropTargetEvent.class);
				dropEvent.currentDataType = FileTransfer.getInstance().getSupportedTypes()[0];
				dropEvent.detail = DND.DROP_DEFAULT;

				adapter.dragEnter(dropEvent);
				assertTrue(dropEvent.detail == DND.DROP_COPY);

				dropTarget.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void performDropOnFolderNode() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), false);
		Node folder = null;
		for (Node child : model.getRoot().getChildren()) {
			if (child.isFolder()) {
				folder = child;
				break;
			}
		}
		assertNotNull("Archive should contain a folder", folder);
		final Node folderNode = folder;

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				viewer.setContentProvider(new ITreeContentProvider() {
					public Object[] getElements(Object input) { return input instanceof Node ? ((Node) input).getChildren() : new Object[0]; }
					public Object[] getChildren(Object element) { return element instanceof Node ? ((Node) element).getChildren() : new Object[0]; }
					public Object getParent(Object element) { return element instanceof Node ? ((Node) element).getParent() : null; }
					public boolean hasChildren(Object element) { return element instanceof Node && ((Node) element).getChildren().length > 0; }
					public void dispose() {}
					public void inputChanged(Viewer v, Object o, Object n) {}
				});
				viewer.setInput(model.getRoot());

				// Select the folder so getCurrentTarget returns it
				viewer.setSelection(new StructuredSelection(folderNode), true);

				ZipEditorDropAdapter adapter = new ZipEditorDropAdapter(viewer);

				// Drop a temp file onto the folder
				File tempFile = File.createTempFile("droptest", ".txt");
				tempFile.deleteOnExit();
				assertTrue(adapter.performDrop(new String[] { tempFile.getAbsolutePath() }));
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				shell.dispose();
			}
		});
	}
}
