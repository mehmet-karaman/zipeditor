package zipeditor.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;

import org.eclipse.ui.model.IWorkbenchAdapter;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class PlainNodeTest {

	@Test
	public void shouldAdaptToElement() throws Exception {
		File path = new File("resources/about.html");
		ZipModel model = new ZipModel(path, new FileInputStream(path));

		PlainNode node = new PlainNode(model, path.getName());
		Object adapted = node.getAdapter(Element.class);

		assertNotNull(adapted);
		assertTrue(adapted instanceof Element);
		Element element = (Element) adapted;
		assertEquals("about.html", element.getFileName());
		assertEquals(path.getAbsolutePath(), element.getPath());
		assertEquals(Element.UNKNOWN, element.getType());
		assertEquals(Long.valueOf(path.length()), element.getSize());
	}

	@Test
	public void shouldAdaptToWorkbenchAdapter() throws Exception {
		File path = new File("resources/about.html");
		ZipModel model = new ZipModel(path, new FileInputStream(path));

		PlainNode node = new PlainNode(model, path.getName());
		Object adapted = node.getAdapter(IWorkbenchAdapter.class);

		assertNotNull(adapted);
	}

	@Test
	public void shouldAdaptToNode() throws Exception {
		File path = new File("resources/about.html");
		ZipModel model = new ZipModel(path, new FileInputStream(path));

		PlainNode node = new PlainNode(model, path.getName());
		Object adapted = node.getAdapter(Node.class);

		assertEquals(node, adapted);
	}

	@Test
	public void shouldReturnNullForUnknownAdapter() throws Exception {
		File path = new File("resources/about.html");
		ZipModel model = new ZipModel(path, new FileInputStream(path));

		PlainNode node = new PlainNode(model, path.getName());
		Object adapted = node.getAdapter(String.class);

		assertNull(adapted);
	}

	@Test
	public void shouldCreatePlainNode() throws Exception {
		File path = new File("resources/about.html");
		ZipModel model = new ZipModel(path, new FileInputStream(path));

		PlainNode node = new PlainNode(model, path.getName());
		Node created = node.create(model, "test.txt", false);

		assertNotNull(created);
		assertTrue(created instanceof PlainNode);
		assertEquals("test.txt", created.getName());
	}
}
