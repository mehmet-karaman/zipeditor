package zipeditor.actions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class RenameNodeActionTest {

	private Text findTextWidget(Control control) {
		if (control instanceof Text)
			return (Text) control;
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				Text found = findTextWidget(child);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	@Test
	public void constructorWithTreeViewer() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				RenameNodeAction action = new RenameNodeAction(viewer);
				assertNotNull(action.getText());
				assertNotNull(action.getToolTipText());
				assertNotNull(action.getId());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void constructorWithTableViewer() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TableViewer viewer = new TableViewer(shell, SWT.NONE);
				RenameNodeAction action = new RenameNodeAction(viewer);
				assertNotNull(action.getText());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorWithUnsupportedViewerThrows() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ListViewer viewer = new ListViewer(shell, SWT.NONE);
				new RenameNodeAction(viewer);
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void runWithNoSelectionDoesNothing() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				RenameNodeAction action = new RenameNodeAction(viewer);
				// No selection set - should not throw
				action.run();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void runWithSelectionOpensInlineEditor() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			shell.setSize(400, 300);
			shell.open();
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				viewer.setContentProvider(new ITreeContentProvider() {
					public Object[] getElements(Object input) { return ((Node) input).getChildren(); }
					public Object[] getChildren(Object element) { return ((Node) element).getChildren(); }
					public Object getParent(Object element) { return ((Node) element).getParent(); }
					public boolean hasChildren(Object element) { return ((Node) element).getChildren().length > 0; }
					public void dispose() {}
					public void inputChanged(Viewer v, Object o, Object n) {}
				});
				viewer.setLabelProvider(new LabelProvider() {
					public String getText(Object element) { return ((Node) element).getName(); }
				});
				viewer.setInput(model.getRoot());
				viewer.setSelection(new StructuredSelection(node), true);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				RenameNodeAction action = new RenameNodeAction(viewer);
				action.run();

				while (display.readAndDispatch()) {}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void runWithSelectionInTableViewer() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			shell.setSize(400, 300);
			shell.open();
			try {
				TableViewer viewer = new TableViewer(shell, SWT.NONE);
				viewer.setContentProvider(new ArrayContentProvider());
				viewer.setLabelProvider(new LabelProvider() {
					public String getText(Object element) { return ((Node) element).getName(); }
				});
				viewer.setInput(model.getRoot().getChildren());
				viewer.setSelection(new StructuredSelection(node), true);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				RenameNodeAction action = new RenameNodeAction(viewer);
				action.run();

				while (display.readAndDispatch()) {}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void modelProviderIds() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				RenameNodeAction action = new RenameNodeAction(viewer);

				assertNull(action.getModelProviderIds());

				String[] ids = { "provider1", "provider2" };
				action.setModelProviderIds(ids);
				assertArrayEquals(ids, action.getModelProviderIds());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void doOperationRenamesNode() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			shell.setSize(400, 300);
			shell.open();
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				viewer.setContentProvider(new ITreeContentProvider() {
					public Object[] getElements(Object input) { return ((Node) input).getChildren(); }
					public Object[] getChildren(Object element) { return ((Node) element).getChildren(); }
					public Object getParent(Object element) { return ((Node) element).getParent(); }
					public boolean hasChildren(Object element) { return ((Node) element).getChildren().length > 0; }
					public void dispose() {}
					public void inputChanged(Viewer v, Object o, Object n) {}
				});
				viewer.setLabelProvider(new LabelProvider() {
					public String getText(Object element) { return ((Node) element).getName(); }
				});
				viewer.setInput(model.getRoot());
				viewer.setSelection(new StructuredSelection(node), true);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				RenameNodeAction action = new RenameNodeAction(viewer);
				action.run();
				while (display.readAndDispatch()) {}

				// Find the inline Text editor and type a new name
				Text textEditor = findTextWidget(viewer.getControl());
				assertNotNull("Inline text editor should be created", textEditor);
				textEditor.setText("renamed.html");

				// Press Enter to confirm
				Event event = new Event();
				event.detail = SWT.TRAVERSE_RETURN;
				textEditor.notifyListeners(SWT.Traverse, event);
				while (display.readAndDispatch()) {}

				assertEquals("renamed.html", node.getName());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void runWithNoSelectionInTreeDoesNotCreateEditor() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			shell.setSize(400, 300);
			shell.open();
			try {
				TreeViewer viewer = new TreeViewer(shell, SWT.NONE);
				RenameNodeAction action = new RenameNodeAction(viewer);
				// No selection - run should not create a text editor
				action.run();

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				Text textEditor = findTextWidget(viewer.getControl());
				assertNull("No text editor should be created without selection", textEditor);
			} finally {
				shell.dispose();
			}
		});
	}
}
