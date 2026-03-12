package zipeditor.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FileOpenerTest {

	private static class TestEditor extends FileOpener.Editor {
		TestEditor(String label, String path) {
			super(label, path);
		}
	}

	@Test
	public void editorFromStringWithExternalPath() {
		String input = "My Editor" + (char) 255 + "/usr/bin/vim";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		assertEquals("My Editor", editor.getLabel());
		assertEquals("/usr/bin/vim", editor.getPath());
		assertNull(editor.getDescriptor());
	}

	@Test
	public void editorFromStringWithDescriptorId() {
		String input = "Text Editor" + (char) 254 + "org.eclipse.ui.DefaultTextEditor";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		assertEquals("Text Editor", editor.getLabel());
		assertNull(editor.getPath());
		// descriptor may or may not be found depending on registered editors
	}

	@Test
	public void editorFromStringWithoutDelimiter() {
		String input = "org.eclipse.ui.DefaultTextEditor";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		// Without delimiter, label is null and full string is treated as editor ID
		assertNull(editor.getLabel());
		assertNull(editor.getPath());
	}

	@Test
	public void toStringWithExternalPath() {
		String input = "My Editor" + (char) 255 + "/usr/bin/vim";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		assertEquals(input, editor.toString());
	}

	@Test
	public void toStringRoundTrip() {
		String input = "My Editor" + (char) 255 + "/usr/bin/vim";
		FileOpener.Editor editor1 = new FileOpener.Editor(input);
		FileOpener.Editor editor2 = new FileOpener.Editor(editor1.toString());

		assertEquals(editor1.getLabel(), editor2.getLabel());
		assertEquals(editor1.getPath(), editor2.getPath());
	}

	@Test
	public void equalsWithSameString() {
		String input = "My Editor" + (char) 255 + "/usr/bin/vim";
		FileOpener.Editor editor1 = new FileOpener.Editor(input);
		FileOpener.Editor editor2 = new FileOpener.Editor(input);

		assertTrue(editor1.equals(editor2));
		assertTrue(editor2.equals(editor1));
	}

	@Test
	public void equalsWithDifferentEditors() {
		FileOpener.Editor editor1 = new FileOpener.Editor("Editor1" + (char) 255 + "/bin/ed");
		FileOpener.Editor editor2 = new FileOpener.Editor("Editor2" + (char) 255 + "/bin/vi");

		assertFalse(editor1.equals(editor2));
	}

	@Test
	public void equalsWithNull() {
		FileOpener.Editor editor = new FileOpener.Editor("Ed" + (char) 255 + "/bin/ed");
		assertFalse(editor.equals(null));
	}

	@Test
	public void setLabelChangesLabel() {
		FileOpener.Editor editor = new FileOpener.Editor("Old" + (char) 255 + "/bin/ed");
		editor.setLabel("New");
		assertEquals("New", editor.getLabel());
	}

	@Test
	public void setPathChangesPath() {
		FileOpener.Editor editor = new FileOpener.Editor("Ed" + (char) 255 + "/bin/ed");
		editor.setPath("/usr/bin/vim");
		assertEquals("/usr/bin/vim", editor.getPath());
	}

	@Test
	public void editorFromStringWithPathContainingSpaces() {
		String input = "My Editor" + (char) 255 + "/usr/local/my app/editor";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		assertEquals("My Editor", editor.getLabel());
		assertEquals("/usr/local/my app/editor", editor.getPath());
	}

	@Test
	public void editorFromStringWithEmptyLabel() {
		String input = "" + (char) 255 + "/usr/bin/vim";
		FileOpener.Editor editor = new FileOpener.Editor(input);

		assertEquals("", editor.getLabel());
		assertEquals("/usr/bin/vim", editor.getPath());
	}

	@Test
	public void protectedConstructorWithLabelAndPath() {
		FileOpener.Editor editor = new TestEditor("Label", "/path/to/editor");

		assertEquals("Label", editor.getLabel());
		assertEquals("/path/to/editor", editor.getPath());
		assertNull(editor.getDescriptor());
	}
}
