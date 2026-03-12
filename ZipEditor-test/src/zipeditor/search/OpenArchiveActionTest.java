package zipeditor.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.ZipEditor;
import zipeditor.model.Node;

public class OpenArchiveActionTest {

	@Test
	public void constructorSetsTextAndImage() {
		syncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			File file = new File("resources/archive.zip");
			OpenArchiveAction action = new OpenArchiveAction(page, file, null);
			assertNotNull(action.getText());
			assertTrue(action.getText().length() > 0);
			assertNotNull(action.getImageDescriptor());
		});
	}

	@Test
	public void runOpensZipEditor() {
		syncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			File file = new File("resources/archive.zip").getAbsoluteFile();
			OpenArchiveAction action = new OpenArchiveAction(page, file, null);
			action.run();

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}

			IEditorPart editor = page.getActiveEditor();
			assertNotNull(editor);
			assertTrue(editor instanceof ZipEditor);

			page.closeAllEditors(false);
		});
	}

	@Test
	public void runWithNodeSelectionSelectsNode() {
		// First run a search to get a Node
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);
		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		options.setElements(files);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		query.run(new NullProgressMonitor());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();
		final Object[] elements = result.getElements();
		assertTrue(elements.length > 0);
		final Node node = (Node) elements[0];

		syncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			File file = new File("resources/archive.zip").getAbsoluteFile();
			OpenArchiveAction action = new OpenArchiveAction(page, file, node);
			action.run();

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}

			IEditorPart editor = page.getActiveEditor();
			assertNotNull(editor);
			assertTrue(editor instanceof ZipEditor);

			page.closeAllEditors(false);
		});
	}
}
