package zipeditor.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;

public class ZipSearchLabelProviderTest {

	private static final IRunnableContext RUNNABLE_CONTEXT = new IRunnableContext() {
		public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			runnable.run(new NullProgressMonitor());
		}
	};

	private ZipSearchQuery createAndRunQuery() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		options.setElements(files);
		final ZipSearchQuery query = new ZipSearchQuery(options, files);
		syncExec(() -> {
			NewSearchUI.runQueryInForeground(RUNNABLE_CONTEXT, query);
		});
		return query;
	}

	private ZipSearchResultPage getPage() {
		ISearchResultViewPart view = NewSearchUI.activateSearchResultView();
		assertNotNull(view);
		ZipSearchResultPage page = (ZipSearchResultPage) view.getActivePage();
		assertNotNull(page);
		return page;
	}

	private Node findNode(Object[] elements, String name) {
		for (Object element : elements) {
			if (element instanceof Node) {
				Node node = (Node) element;
				if (name.equals(node.getName()) && name.equals(node.getFullPath())) {
					return node;
				}
			}
		}
		return null;
	}

	@Test
	public void getTextForNodeInFlatLayout() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			Node node = findNode(elements, "about.html");
			assertNotNull(node);
			assertEquals("about.html [archive.zip] - 3", provider.getText(node));
		});
	}

	@Test
	public void getTextForNodeInTreeLayout() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			Display display = PlatformUI.getWorkbench().getDisplay();

			page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
			while (display.readAndDispatch()) {}

			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);
			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			Node node = findNode(elements, "about.html");
			assertNotNull(node);
			assertEquals("about.html - 3", provider.getText(node));
		});
	}

	@Test
	public void getTextForElement() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			Element element = new Element(null, "/path/to/archive.zip", "archive.zip", null, null, Element.ZIP);
			assertEquals("archive.zip", provider.getText(element));
		});
	}

	@Test
	public void getColumnTextDelegatesToGetText() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			Element element = new Element(null, "/path", "test.zip", null, null, Element.ZIP);
			assertEquals(provider.getText(element), provider.getColumnText(element, 0));
		});
	}

	@Test
	public void getTextForNonNodeNonElement() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			assertNull(provider.getText("a string"));
		});
	}

	@Test
	public void getImageForFolderElement() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			Element folder = new Element(null, "/path", "folder", null, null, Element.FOLDER);
			Image image = provider.getImage(folder);
			assertNotNull(image);
		});
	}

	@Test
	public void getImageForZipElement() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			Element zip = new Element(null, "/path", "archive.zip", null, null, Element.ZIP);
			Image image = provider.getImage(zip);
			assertNotNull(image);
		});
	}

	@Test
	public void getImageForNode() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			Image image = provider.getImage(elements[0]);
			assertNotNull(image);
		});
	}

	@Test
	public void getImageForUnknownElementTypeThrows() {
		createAndRunQuery();
		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);

			Element unknown = new Element(null, "/path", "file", null, null, Element.UNKNOWN);
			try {
				provider.getImage(unknown);
				fail("Expected IllegalArgumentException");
			} catch (IllegalArgumentException e) {
				// expected
			}
		});
	}

	@Test
	public void getTextForNodeWithParentNodes() {
		File path = new File("resources/nested.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		options.setElements(files);
		final ZipSearchQuery query = new ZipSearchQuery(options, files);

		syncExec(() -> {
			NewSearchUI.runQueryInForeground(RUNNABLE_CONTEXT, query);
		});

		syncExec(() -> {
			ZipSearchResultPage page = getPage();
			Display display = PlatformUI.getWorkbench().getDisplay();

			page.setLayout(AbstractTextSearchViewPage.FLAG_LAYOUT_FLAT);
			while (display.readAndDispatch()) {}

			ZipSearchLabelProvider provider = new ZipSearchLabelProvider(page);
			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			for (Object element : elements) {
				if (element instanceof Node) {
					Node node = (Node) element;
					List<?> parentNodes = node.getParentNodes();
					if (parentNodes != null && parentNodes.size() > 0) {
						String text = provider.getText(node);
						assertNotNull(text);
						assertTrue(text.contains("["));
						assertTrue(text.contains(">"));
						return;
					}
				}
			}
		});
	}
}
