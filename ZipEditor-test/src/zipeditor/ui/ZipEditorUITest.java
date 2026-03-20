package zipeditor.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import zipeditor.ZipEditor;
import zipeditor.model.Node;


@RunWith(SWTBotJunit4ClassRunner.class)
public class ZipEditorUITest {

	private static SWTWorkbenchBot bot;
	private static SWTBotEditor editor;
	private static File tempArchive;
	private static boolean isTreeMode;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot = new SWTWorkbenchBot();
		SWTBotPreferences.TIMEOUT = 5000;

		// Close Welcome view if present
		try {
			bot.viewByTitle("Welcome").close();
		} catch (Exception e) {
			// no welcome view
		}

		// Create a temp copy of the test archive
		File source = new File("resources/archive.zip");
		Path tempPath = Files.createTempFile("swtbot-test-", ".zip");
		Files.copy(source.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);
		tempArchive = tempPath.toFile();
		tempArchive.deleteOnExit();

		// Open the editor once
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(tempArchive.toURI());
		IEditorInput input = new FileStoreEditorInput(fileStore);
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			try {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				page.openEditor(input, "zipeditor.ZipEditor");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		editor = bot.activeEditor();

		// Detect view mode with a short timeout
		long savedTimeout = SWTBotPreferences.TIMEOUT;
		SWTBotPreferences.TIMEOUT = 1000;
		try {
			editor.bot().tree();
			isTreeMode = true;
		} catch (Exception e) {
			isTreeMode = false;
		}
		SWTBotPreferences.TIMEOUT = savedTimeout;
	}

	private static ZipEditor getZipEditor() {
		final ZipEditor[] result = new ZipEditor[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().getActiveEditor();
			result[0] = (ZipEditor) part;
		});
		return result[0];
	}

	@AfterClass
	public static void afterClass() {
		if (editor != null) {
			editor.close();
		}
		// Don't delete here, background jobs (DeferredMenuManager) may still
		// access the file. deleteOnExit() from beforeClass handles cleanup.
	}

	@Test
	public void openArchiveShowsEditor() {
		assertNotNull(editor);
		assertTrue(editor.getTitle().contains(".zip"));
	}

	@Test
	public void editorNotDirtyAfterOpen() {
		assertFalse("Editor should not be dirty after opening", editor.isDirty());
	}

	@Test
	public void editorContainsViewerWidget() {
		if (isTreeMode) {
			assertNotNull(editor.bot().tree());
		} else {
			assertNotNull(editor.bot().table());
		}
	}

	@Test
	public void viewerHasEntries() {
		if (isTreeMode) {
			SWTBotTree tree = editor.bot().tree();
			assertTrue("Tree should have items", tree.getAllItems().length > 0);
		} else {
			SWTBotTable table = editor.bot().table();
			assertTrue("Table should have rows", table.rowCount() > 0);
		}
	}

	@Test
	public void viewerContainsExpectedEntries() {
		if (isTreeMode) {
			SWTBotTree tree = editor.bot().tree();
			boolean foundAbout = false;
			boolean foundFolder = false;
			for (SWTBotTreeItem item : tree.getAllItems()) {
				String text = item.getText();
				if (text.contains("about.html"))
					foundAbout = true;
				if (text.contains("folder"))
					foundFolder = true;
			}
			assertTrue("Should contain about.html", foundAbout);
			assertTrue("Should contain folder", foundFolder);
		} else {
			SWTBotTable table = editor.bot().table();
			assertTrue("Table should have at least 2 rows", table.rowCount() >= 2);
			StringBuilder entries = new StringBuilder();
			boolean foundAbout = false;
			for (int i = 0; i < table.rowCount(); i++) {
				String text = table.cell(i, 0);
				entries.append("[").append(text).append("] ");
				if (text.contains("about.html"))
					foundAbout = true;
			}
			assertTrue("Should contain about.html, found: " + entries, foundAbout);
		}
	}

	@Test
	public void selectItem() {
		if (isTreeMode) {
			SWTBotTree tree = editor.bot().tree();
			tree.getAllItems()[0].select();
			assertTrue(tree.selection().rowCount() > 0);
		} else {
			SWTBotTable table = editor.bot().table();
			table.select(0);
			assertTrue(table.selection().rowCount() > 0);
		}
	}

	@Test
	public void expandFolderInTree() {
		if (!isTreeMode)
			return;
		SWTBotTree tree = editor.bot().tree();
		for (SWTBotTreeItem item : tree.getAllItems()) {
			if (item.getText().contains("folder")) {
				item.expand();
				assertTrue("Folder should be expandable", item.isExpanded());
				assertTrue("Folder should have children", item.getItems().length > 0);
				return;
			}
		}
	}

	@Test
	public void contextMenuHasExpectedActions() {
		if (isTreeMode) {
			SWTBotTree tree = editor.bot().tree();
			tree.getAllItems()[0].select();
			assertNotNull(tree.getAllItems()[0].contextMenu("Add..."));
			assertNotNull(tree.getAllItems()[0].contextMenu("Extract..."));
			assertNotNull(tree.getAllItems()[0].contextMenu("Delete"));
		} else {
			SWTBotTable table = editor.bot().table();
			table.select(0);
			assertNotNull(table.contextMenu("Add..."));
			assertNotNull(table.contextMenu("Extract..."));
			assertNotNull(table.contextMenu("Delete"));
		}
	}

	@Test
	public void addFileUpdatesModel() throws Exception {
		// Create a temp file to add
		Path fileToAdd = Files.createTempFile("added-", ".txt");
		Files.writeString(fileToAdd, "test content for add");
		fileToAdd.toFile().deleteOnExit();

		ZipEditor zipEditor = getZipEditor();
		assertNotNull(zipEditor);

		// Count model children before add
		Node rootNode = zipEditor.getRootNode();
		int childCountBefore = rootNode.getChildren().length;

		// Add file directly to the model (bypassing the async AddOperation)
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			rootNode.add(fileToAdd.toFile(), null, new org.eclipse.core.runtime.NullProgressMonitor(), false);
		});

		// Verify the model was updated
		int childCountAfter = rootNode.getChildren().length;
		assertTrue("Model child count should increase, was " + childCountBefore + " now " + childCountAfter,
				childCountAfter > childCountBefore);

		// Verify the added node exists in the model
		boolean found = false;
		for (Node child : rootNode.getChildren()) {
			if (child.getName().equals(fileToAdd.toFile().getName())) {
				found = true;
				break;
			}
		}
		assertTrue("Added file should be in model", found);
	}

	@Test
	public void deleteNodeUpdatesModel() throws Exception {
		ZipEditor zipEditor = getZipEditor();
		assertNotNull(zipEditor);

		Node rootNode = zipEditor.getRootNode();
		int childCountBefore = rootNode.getChildren().length;
		assertTrue("Root should have children to delete", childCountBefore > 0);

		// Remember the first child's name
		Node nodeToDelete = rootNode.getChildren()[0];
		String deletedName = nodeToDelete.getName();

		// Delete the node
		rootNode.remove(nodeToDelete);

		// Verify model is updated
		int childCountAfter = rootNode.getChildren().length;
		assertEquals("Child count should decrease by 1", childCountBefore - 1, childCountAfter);

		// Verify the node is no longer findable by name
		boolean found = false;
		for (Node child : rootNode.getChildren()) {
			if (child.getName().equals(deletedName)) {
				found = true;
				break;
			}
		}
		assertFalse("Deleted node should not be in children", found);
	}

	@Test
	public void deleteNodeUpdatesViewer() throws Exception {
		ZipEditor zipEditor = getZipEditor();
		assertNotNull(zipEditor);

		// First add a file so we have something to delete without affecting other tests
		Path fileToAdd = Files.createTempFile("to-delete-", ".txt");
		Files.writeString(fileToAdd, "will be deleted");
		fileToAdd.toFile().deleteOnExit();

		final Node rootNode = zipEditor.getRootNode();
		final StructuredViewer viewer = zipEditor.getViewer();

		// Add and refresh on UI thread
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			rootNode.add(fileToAdd.toFile(), null, new org.eclipse.core.runtime.NullProgressMonitor(), false);
			viewer.setInput(viewer.getInput());
		});
		Thread.sleep(500);

		// Count viewer entries after add
		final int[] countAfterAdd = new int[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			if (isTreeMode) {
				countAfterAdd[0] = editor.bot().tree().getAllItems().length;
			} else {
				countAfterAdd[0] = editor.bot().table().rowCount();
			}
		});

		// Find the node we just added
		Node nodeToDelete = null;
		for (Node child : rootNode.getChildren()) {
			if (child.getName().equals(fileToAdd.toFile().getName())) {
				nodeToDelete = child;
				break;
			}
		}
		assertNotNull("Should find the added node", nodeToDelete);

		// Delete and refresh on UI thread
		final Node toDelete = nodeToDelete;
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			rootNode.remove(toDelete);
			viewer.setInput(viewer.getInput());
		});
		Thread.sleep(500);

		// Verify viewer count decreased
		final int[] countAfterDelete = new int[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			if (isTreeMode) {
				countAfterDelete[0] = editor.bot().tree().getAllItems().length;
			} else {
				countAfterDelete[0] = editor.bot().table().rowCount();
			}
		});
		assertTrue("Viewer count should decrease after delete, was " + countAfterAdd[0] + " now " + countAfterDelete[0],
				countAfterDelete[0] < countAfterAdd[0]);
	}

	@Test
	public void deleteActionViaContextMenu() throws Exception {
		ZipEditor zipEditor = getZipEditor();
		assertNotNull(zipEditor);

		// First add a file so we have something safe to delete
		Path fileToAdd = Files.createTempFile("ctx-delete-", ".txt");
		Files.writeString(fileToAdd, "context menu delete test");
		fileToAdd.toFile().deleteOnExit();

		final Node rootNode = zipEditor.getRootNode();
		final StructuredViewer viewer = zipEditor.getViewer();
		final String fileName = fileToAdd.toFile().getName();

		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
			rootNode.add(fileToAdd.toFile(), null, new org.eclipse.core.runtime.NullProgressMonitor(), false);
			viewer.setInput(viewer.getInput());
		});
		Thread.sleep(500);

		if (isTreeMode) {
			SWTBotTree tree = editor.bot().tree();
			for (SWTBotTreeItem item : tree.getAllItems()) {
				if (item.getText().contains(fileName)) {
					item.select();
					break;
				}
			}
			// Delete via context menu
			tree.contextMenu("Delete").click();
		} else {
			SWTBotTable table = editor.bot().table();
			for (int i = 0; i < table.rowCount(); i++) {
				if (table.cell(i, 0).contains(fileName)) {
					table.select(i);
					break;
				}
			}
			// Delete via context menu
			table.contextMenu("Delete").click();
		}

		// Wait for refresh
		Thread.sleep(500);
		PlatformUI.getWorkbench().getDisplay().syncExec(() -> {});

		// Verify the node was removed from the model
		boolean found = false;
		for (Node child : rootNode.getChildren()) {
			if (child.getName().equals(fileName)) {
				found = true;
				break;
			}
		}
		assertFalse("Deleted node should not be in model", found);
	}
}
