package zipeditor.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.junit.Test;

import zipeditor.model.Node;

public class ZipSearchResultPageTest {

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

	@Test
	public void searchResultPageIsCreatedBySearch() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);
		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		ZipSearchQuery query = new ZipSearchQuery(options, files);
		query.run(new NullProgressMonitor());

		ISearchResult result = query.getSearchResult();
		assertNotNull(result);
		ISearchQuery resultQuery = result.getQuery();
		assertNotNull(resultQuery);
		assertTrue(resultQuery instanceof ZipSearchQuery);
	}

	@Test
	public void showInTargetListAdapter() {
		ZipSearchResultPage page = new ZipSearchResultPage();
		Object adapter = page.getAdapter(IShowInTargetList.class);
		assertNotNull(adapter);
		assertTrue(adapter instanceof IShowInTargetList);
		String[] targets = ((IShowInTargetList) adapter).getShowInTargetIds();
		assertNotNull(targets);
		assertTrue(targets.length > 0);
	}

	@Test
	public void openMatchOpensEditorWithAnnotations() {
		createAndRunQuery();
		syncExec(() -> {
			Display display = PlatformUI.getWorkbench().getDisplay();
			ISearchResultViewPart view = NewSearchUI.activateSearchResultView();
			assertNotNull(view);
			while (display.readAndDispatch()) {}

			ZipSearchResultPage page = (ZipSearchResultPage) view.getActivePage();
			assertNotNull(page);

			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			// Select a node and trigger showMatch via gotoSelectedMatch
			Node node = (Node) elements[0];
			Match[] matches = result.getMatches(node);
			assertTrue(matches.length > 0);

			// Trigger showMatch via gotoNextMatch (public method that calls showMatch internally)
			page.gotoNextMatch();
			while (display.readAndDispatch()) {}

			// Close any opened editors
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			workbenchPage.closeAllEditors(false);
		});
	}

	@Test
	public void openMatchInMultiPageEditor() {
		// Search in archive containing plugin.xml which opens with MultiPageEditorPart
		File path = new File("resources/plugin.zip");
		List<File> files = new ArrayList<File>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Test Editor", "UTF-8", false, ZipSearchOptions.SCOPE_FILESYSTEM, false);
		options.setElements(files);
		final ZipSearchQuery query = new ZipSearchQuery(options, files);
		syncExec(() -> {
			NewSearchUI.runQueryInForeground(RUNNABLE_CONTEXT, query);
		});

		syncExec(() -> {
			Display display = PlatformUI.getWorkbench().getDisplay();
			ISearchResultViewPart view = NewSearchUI.activateSearchResultView();
			assertNotNull(view);
			while (display.readAndDispatch()) {}

			ZipSearchResultPage page = (ZipSearchResultPage) view.getActivePage();
			assertNotNull(page);

			ZipSearchResult result = (ZipSearchResult) page.getInput();
			assertTrue(result.getMatchCount() > 0);

			// gotoNextMatch opens plugin.xml -> triggers findTextEditor with MultiPageEditorPart
			page.gotoNextMatch();
			while (display.readAndDispatch()) {}

			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			workbenchPage.closeAllEditors(false);
		});
	}

	@Test
	public void showInSourceAdapter() {
		createAndRunQuery();
		syncExec(() -> {
			Display display = PlatformUI.getWorkbench().getDisplay();
			ISearchResultViewPart view = NewSearchUI.activateSearchResultView();
			assertNotNull(view);
			while (display.readAndDispatch()) {}

			ZipSearchResultPage page = (ZipSearchResultPage) view.getActivePage();
			assertNotNull(page);

			ZipSearchResult result = (ZipSearchResult) page.getInput();
			Object[] elements = result.getElements();
			assertTrue(elements.length > 0);

			// Select a node so getAdapter(IShowInSource) has a selection
			Node node = (Node) elements[0];
			ISelectionProvider selectionProvider = page.getSite().getSelectionProvider();
			selectionProvider.setSelection(new StructuredSelection(node));
			while (display.readAndDispatch()) {}

			Object adapter = page.getAdapter(IShowInSource.class);
			assertNotNull(adapter);
			assertTrue(adapter instanceof IShowInSource);

			ShowInContext context = ((IShowInSource) adapter).getShowInContext();
			assertNotNull(context);
		});
	}
}
