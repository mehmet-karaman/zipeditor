package zipeditor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class NodePropertyPageTest {

	@Test
	public void zipNodePropertyPageCreateContents() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipNodePropertyPage page = new ZipNodePropertyPage();
				page.setElement(node);

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
	public void zipNodePropertyPagePerformOk() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), false);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipNodePropertyPage page = new ZipNodePropertyPage();
				page.setElement(node);

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				assertTrue(page.performOk());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void zipNodePropertyPageForFolder() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("folder");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ZipNodePropertyPage page = new ZipNodePropertyPage();
				page.setElement(node);

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);
				assertNotNull(page.getControl());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void tarNodePropertyPageCreateContents() throws Exception {
		File path = new File("resources/archive.tar");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node[] children = model.getRoot().getChildren();
		assertTrue(children.length > 0);
		final Node node = children[0].isFolder() ? children[0].getChildren()[0] : children[0];

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TarNodePropertyPage page = new TarNodePropertyPage();
				page.setElement(node);

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				assertNotNull(page.getControl());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void tarNodePropertyPagePerformOk() throws Exception {
		File path = new File("resources/archive.tar");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), false);
		Node[] children = model.getRoot().getChildren();
		assertTrue(children.length > 0);
		final Node node = children[0].isFolder() ? children[0].getChildren()[0] : children[0];

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				TarNodePropertyPage page = new TarNodePropertyPage();
				page.setElement(node);

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				assertTrue(page.performOk());
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void rpmNodePropertyPageCreateContents() throws Exception {
		File path = new File("resources/archive.rpm");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node[] children = model.getRoot().getChildren();
		assertTrue(children.length > 0);
		final Node node = children[0].isFolder() ? children[0].getChildren()[0] : children[0];

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				RpmNodePropertyPage page = new RpmNodePropertyPage();
				page.setElement(node);

				Composite parent = new Composite(shell, SWT.NONE);
				page.createControl(parent);

				assertNotNull(page.getControl());
			} finally {
				shell.dispose();
			}
		});
	}
}
