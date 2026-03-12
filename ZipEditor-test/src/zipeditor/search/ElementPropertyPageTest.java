package zipeditor.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class ElementPropertyPageTest {

	private Control createPage(Shell shell, Element element) {
		ElementPropertyPage page = new ElementPropertyPage();
		page.setElement(element);

		Composite parent = new Composite(shell, SWT.NONE);
		page.createControl(parent);

		Control control = page.getControl();
		assertNotNull(control);
		return control;
	}

	@Test
	public void createContentsWithSizeAndDate() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				Element element = new Element(null, "/path/to/archive.zip", "archive.zip", 12345L, System.currentTimeMillis(), Element.ZIP);
				Control control = createPage(shell, element);

				// Find the content area within the page control hierarchy
				boolean foundFileName = findStyledText(control, "archive.zip");
				assertTrue("File name should appear in a StyledText widget", foundFileName);
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void createContentsWithNullSizeAndDate() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				Element element = new Element(null, "/path/to/file.txt", "file.txt", null, null, Element.UNKNOWN);
				createPage(shell, element);
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void createContentsForFolderElement() {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				Element element = new Element(null, "/path/to/folder", "myFolder", null, null, Element.FOLDER);
				createPage(shell, element);
			} finally {
				shell.dispose();
			}
		});
	}

	private boolean findStyledText(Control control, String text) {
		if (control instanceof StyledText && text.equals(((StyledText) control).getText()))
			return true;
		if (control instanceof Composite) {
			for (Control child : ((Composite) control).getChildren()) {
				if (findStyledText(child, text))
					return true;
			}
		}
		return false;
	}
}
