package zipeditor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class MultiPreferenceDialogTest {

	@Test
	public void createDialogWithSingleElement() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				PreferenceManager manager = new PreferenceManager();
				MultiPreferenceDialog dialog = new MultiPreferenceDialog(shell, manager, new Node[] { node });
				dialog.setBlockOnOpen(false);
				dialog.open();
				try {
					Shell dialogShell = dialog.getShell();
					assertNotNull(dialogShell);
					String text = dialogShell.getText();
					assertNotNull(text);
					assertTrue("Shell text should contain 'Properties'", text.contains("Properties"));
				} finally {
					dialog.close();
				}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void createDialogWithMultipleElements() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node1 = model.findNode("about.html");
		final Node node2 = model.getRoot().getChildren()[0];
		assertNotNull(node1);
		assertNotNull(node2);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				PreferenceManager manager = new PreferenceManager();
				MultiPreferenceDialog dialog = new MultiPreferenceDialog(shell, manager, new Node[] { node1, node2 });
				dialog.setBlockOnOpen(false);
				dialog.open();
				try {
					Shell dialogShell = dialog.getShell();
					String text = dialogShell.getText();
					assertNotNull(text);
					assertTrue("Shell text should mention element count", text.contains("2"));
				} finally {
					dialog.close();
				}
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void createDialogWithNullElements() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				PreferenceManager manager = new PreferenceManager();
				MultiPreferenceDialog dialog = new MultiPreferenceDialog(shell, manager, null);
				dialog.setBlockOnOpen(false);
				dialog.open();
				try {
					Shell dialogShell = dialog.getShell();
					assertNotNull(dialogShell);
				} finally {
					dialog.close();
				}
			} finally {
				shell.dispose();
			}
		});
	}
}
