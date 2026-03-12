package zipeditor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

import zipeditor.model.Node;
import zipeditor.model.ZipModel;

public class ZipEditorDragAdapterTest {

	protected static <T extends TypedEvent> T createDNDEventWrapper(Widget widget, Class<T> eventType) {
		try {
			Class<?> dndEventClass = Class.forName("org.eclipse.swt.dnd.DNDEvent");
			Constructor<?> dndCtor = dndEventClass.getDeclaredConstructor();
			dndCtor.setAccessible(true);
			Object dndEvent = dndCtor.newInstance();
			((Event) dndEvent).widget = widget;
			Constructor<T> ctor = eventType.getDeclaredConstructor(dndEventClass);
			ctor.setAccessible(true);
			return ctor.newInstance(dndEvent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void dragSetDataExtractsNodes() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node node = model.findNode("about.html");
		assertNotNull(node);

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ISelectionProvider selectionProvider = new ISelectionProvider() {
					private StructuredSelection selection = new StructuredSelection(node);
					public void setSelection(ISelection s) { selection = (StructuredSelection) s; }
					public void removeSelectionChangedListener(ISelectionChangedListener l) {}
					public ISelection getSelection() { return selection; }
					public void addSelectionChangedListener(ISelectionChangedListener l) {}
				};

				ZipEditorDragAdapter adapter = new ZipEditorDragAdapter(selectionProvider);

				DragSource dragSource = new DragSource(shell, DND.DROP_COPY);
				DragSourceEvent event = createDNDEventWrapper(dragSource, DragSourceEvent.class);

				adapter.dragSetData(event);
				assertNotNull(event.data);
				assertTrue(event.data instanceof String[]);
				String[] paths = (String[]) event.data;
				assertTrue(paths.length == 1);
				assertTrue(paths[0].endsWith("about.html"));

				// Second call reuses temp paths
				DragSourceEvent event2 = createDNDEventWrapper(dragSource, DragSourceEvent.class);
				adapter.dragSetData(event2);
				assertNotNull(event2.data);

				adapter.dragFinished(event);

				// After dragFinished, temp paths are cleared; next dragSetData creates new ones
				DragSourceEvent event3 = createDNDEventWrapper(dragSource, DragSourceEvent.class);
				adapter.dragSetData(event3);
				assertNotNull(event3.data);

				dragSource.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void dragSetDataWithEmptySelection() throws Exception {
		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ISelectionProvider selectionProvider = new ISelectionProvider() {
					public void setSelection(ISelection s) {}
					public void removeSelectionChangedListener(ISelectionChangedListener l) {}
					public ISelection getSelection() { return StructuredSelection.EMPTY; }
					public void addSelectionChangedListener(ISelectionChangedListener l) {}
				};

				ZipEditorDragAdapter adapter = new ZipEditorDragAdapter(selectionProvider);

				DragSource dragSource = new DragSource(shell, DND.DROP_COPY);
				DragSourceEvent event = createDNDEventWrapper(dragSource, DragSourceEvent.class);

				adapter.dragSetData(event);
				assertNull(event.data);

				dragSource.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void dragSetDataWithFolderNode() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		Node folder = null;
		for (Node child : model.getRoot().getChildren()) {
			if (child.isFolder()) {
				folder = child;
				break;
			}
		}
		assertNotNull("Archive should contain a folder", folder);
		final Node folderNode = folder;

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ISelectionProvider selectionProvider = new ISelectionProvider() {
					public void setSelection(ISelection s) {}
					public void removeSelectionChangedListener(ISelectionChangedListener l) {}
					public ISelection getSelection() { return new StructuredSelection(folderNode); }
					public void addSelectionChangedListener(ISelectionChangedListener l) {}
				};

				ZipEditorDragAdapter adapter = new ZipEditorDragAdapter(selectionProvider);

				DragSource dragSource = new DragSource(shell, DND.DROP_COPY);
				DragSourceEvent event = createDNDEventWrapper(dragSource, DragSourceEvent.class);

				adapter.dragSetData(event);
				assertNotNull(event.data);
				String[] paths = (String[]) event.data;
				assertTrue(paths.length == 1);

				dragSource.dispose();
			} finally {
				shell.dispose();
			}
		});
	}

	@Test
	public void dragSetDataWithMultipleNodes() throws Exception {
		File path = new File("resources/archive.zip");
		final ZipModel model = new ZipModel(path, new FileInputStream(path), true);
		final Node[] children = model.getRoot().getChildren();
		assertTrue(children.length >= 2);

		final Node node1 = children[0];
		final Node node2 = children[1];

		syncExec(() -> {
			Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
			try {
				ISelectionProvider selectionProvider = new ISelectionProvider() {
					public void setSelection(ISelection s) {}
					public void removeSelectionChangedListener(ISelectionChangedListener l) {}
					public ISelection getSelection() { return new StructuredSelection(new Object[] { node1, node2 }); }
					public void addSelectionChangedListener(ISelectionChangedListener l) {}
				};

				ZipEditorDragAdapter adapter = new ZipEditorDragAdapter(selectionProvider);

				DragSource dragSource = new DragSource(shell, DND.DROP_COPY);
				DragSourceEvent event = createDNDEventWrapper(dragSource, DragSourceEvent.class);

				adapter.dragSetData(event);
				assertNotNull(event.data);
				String[] paths = (String[]) event.data;
				assertTrue(paths.length == 2);

				dragSource.dispose();
			} finally {
				shell.dispose();
			}
		});
	}
}
