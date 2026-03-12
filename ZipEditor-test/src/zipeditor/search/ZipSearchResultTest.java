package zipeditor.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ZipSearchResultTest {

	@Test
	public void shouldFormatLabelWithTextPattern() {
		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		List<File> elements = new ArrayList<>();
		elements.add(new File("archive.zip"));
		options.setElements(elements);

		ZipSearchQuery query = new ZipSearchQuery(options, elements);
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		String label = result.getLabel();
		assertNotNull(label);
		assertTrue(label.contains("Uwe Voigt"));
		assertTrue(label.contains("archive.zip"));
	}

	@Test
	public void shouldFormatLabelWithNodeNamePattern() {
		ZipSearchOptions options = new ZipSearchOptions("*.html", "", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		List<File> elements = new ArrayList<>();
		elements.add(new File("archive.zip"));
		options.setElements(elements);

		ZipSearchQuery query = new ZipSearchQuery(options, elements);
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		String label = result.getLabel();
		assertNotNull(label);
		assertTrue(label.contains("*.html"));
	}

	@Test
	public void shouldFormatLabelWithMultipleElements() {
		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		List<File> elements = new ArrayList<>();
		elements.add(new File("a.zip"));
		elements.add(new File("b.zip"));
		elements.add(new File("c.zip"));
		options.setElements(elements);

		ZipSearchQuery query = new ZipSearchQuery(options, elements);
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		String label = result.getLabel();
		assertTrue(label.contains("a.zip"));
		assertTrue(label.contains("b.zip"));
		assertTrue(label.contains("..."));
	}

	@Test
	public void shouldFormatLabelWithEmptyElements() {
		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		options.setElements(new ArrayList<>());

		ZipSearchQuery query = new ZipSearchQuery(options, new ArrayList<>());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		String label = result.getLabel();
		assertNotNull(label);
	}

	@Test
	public void shouldReturnNullTooltip() {
		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		options.setElements(new ArrayList<>());
		ZipSearchQuery query = new ZipSearchQuery(options, new ArrayList<>());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		assertNull(result.getTooltip());
	}

	@Test
	public void shouldReturnNullAdapters() {
		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		options.setElements(new ArrayList<>());
		ZipSearchQuery query = new ZipSearchQuery(options, new ArrayList<>());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		assertNull(result.getEditorMatchAdapter());
		assertNull(result.getFileMatchAdapter());
	}

	@Test
	public void shouldReturnImageDescriptor() {
		ZipSearchOptions options = new ZipSearchOptions("", "test", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		options.setElements(new ArrayList<>());
		ZipSearchQuery query = new ZipSearchQuery(options, new ArrayList<>());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		assertNotNull(result.getImageDescriptor());
	}

	@Test
	public void shouldReturnMatchCountInLabel() {
		File path = new File("resources/archive.zip");
		List<File> files = new ArrayList<>();
		files.add(path);

		ZipSearchOptions options = new ZipSearchOptions("", "Uwe Voigt", "Cp1252", false, ZipSearchOptions.SCOPE_WORKSPACE, false);
		options.setElements(files);

		ZipSearchQuery query = new ZipSearchQuery(options, files);
		query.run(new org.eclipse.core.runtime.NullProgressMonitor());
		ZipSearchResult result = (ZipSearchResult) query.getSearchResult();

		String label = result.getLabel();
		assertTrue(result.getMatchCount() > 0);
		assertTrue(label.contains(String.valueOf(result.getMatchCount())));
	}
}
