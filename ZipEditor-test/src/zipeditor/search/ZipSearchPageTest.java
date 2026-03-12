package zipeditor.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.util.Collections;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class ZipSearchPageTest {

	private ISearchPageContainer createContainer(final ISelection selection) {
		return new ISearchPageContainer() {
			public ISelection getSelection() {
				return selection;
			}
			public IRunnableContext getRunnableContext() {
				return null;
			}
			public void setPerformActionEnabled(boolean state) {
			}
			public int getSelectedScope() {
				return ISearchPageContainer.WORKSPACE_SCOPE;
			}
			public void setSelectedScope(int scope) {
			}
			public boolean hasValidScope() {
				return true;
			}
			public void setActiveEditorCanProvideScopeSelection(boolean state) {
			}
			public IWorkingSet[] getSelectedWorkingSets() {
				return new IWorkingSet[0];
			}
			public IEditorInput getActiveEditorInput() {
				return null;
			}
			public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
			}
			public String[] getSelectedProjectNames() {
				return null;
			}
		};
	}

	@Test
	public void createControlCreatesWidgets() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchPage page = new ZipSearchPage();
				page.setContainer(createContainer(StructuredSelection.EMPTY));

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				Control control = page.getControl();
				assertNotNull(control);
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void performActionWithWorkspaceScope() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchPage page = new ZipSearchPage();
				page.setContainer(createContainer(StructuredSelection.EMPTY));

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				boolean result = page.performAction();
				assertTrue(result);

				while (display.readAndDispatch()) {}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void setVisibleTrue() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchPage page = new ZipSearchPage();
				page.setContainer(createContainer(StructuredSelection.EMPTY));

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				page.setVisible(true);
				page.setVisible(false);
			} finally {
				shell.dispose();
			}
		});
	}

	private void selectRadio(Control control, int index) {
		// The scope group is a Group inside the page control's children
		// Find the Group, then select the radio button at the given index
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				if (child instanceof Group) {
					Control[] groupChildren = ((Group) child).getChildren();
					int radioIndex = 0;
					for (Control gc : groupChildren) {
						if (gc instanceof Button && (gc.getStyle() & SWT.RADIO) != 0) {
							((Button) gc).setSelection(radioIndex == index);
							radioIndex++;
						}
					}
					return;
				}
				selectRadio(child, index);
			}
		}
	}

	@Test
	public void performActionWithSelectedScope() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				File file = new File("resources/archive.zip").getAbsoluteFile();
				ISelection selection = new StructuredSelection(Collections.singletonList(file));
				ZipSearchPage page = new ZipSearchPage();
				page.setContainer(createContainer(selection));

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// Select the "Selected" radio (index 1: Workspace=0, Selected=1, FileSystem=2)
				selectRadio(page.getControl(), 1);

				boolean result = page.performAction();
				assertTrue(result);

				while (display.readAndDispatch()) {}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void performActionWithFileSystemScope() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipSearchPage page = new ZipSearchPage();
				page.setContainer(createContainer(StructuredSelection.EMPTY));

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				Display display = PlatformUI.getWorkbench().getDisplay();
				while (display.readAndDispatch()) {}

				// Select the "File System" radio (index 2)
				selectRadio(page.getControl(), 2);

				boolean result = page.performAction();
				assertTrue(result);

				while (display.readAndDispatch()) {}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void idConstant() {
		assertNotNull(ZipSearchPage.ID);
		assertTrue(ZipSearchPage.ID.length() > 0);
	}
}
