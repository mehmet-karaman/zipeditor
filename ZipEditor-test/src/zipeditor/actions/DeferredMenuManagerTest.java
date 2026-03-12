package zipeditor.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static zipeditor.TestUtils.syncExec;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

public class DeferredMenuManagerTest {

	private static final String FAMILY = "testFamily";

	private DeferredMenuManager.MenuJob createMenuJob(final Object family, final Object property, final IStatus resultStatus) {
		return new DeferredMenuManager.MenuJob(family, property) {
			protected IStatus addToMenu(IProgressMonitor monitor, IMenuManager menu) {
				menu.add(new Action("Added by job") {});
				return resultStatus;
			}
		};
	}

	@Test
	public void constructorAddsPendingAction() {
		syncExec(() -> {
			MenuManager parent = new MenuManager();
			DeferredMenuManager manager = new DeferredMenuManager(parent, "Test Menu", "testId");

			IContributionItem[] items = manager.getItems();
			assertEquals(1, items.length);
			assertEquals(manager.getPendingId(), items[0].getId());
		});
	}

	@Test
	public void addRemovesPendingAction() {
		syncExec(() -> {
			MenuManager parent = new MenuManager();
			DeferredMenuManager manager = new DeferredMenuManager(parent, "Test Menu", "testId");

			Action action = new Action("Real Action") {};
			manager.add(action);

			IContributionItem[] items = manager.getItems();
			assertEquals(1, items.length);
			assertFalse(manager.getPendingId().equals(items[0].getId()));
		});
	}

	@Test
	public void addToMenuSchedulesJobAndAddsSubMenu() {
		syncExec(() -> {
			MenuManager parentMenu = new MenuManager();
			DeferredMenuManager.MenuJob job = createMenuJob(FAMILY, "prop1", Status.OK_STATUS);

			DeferredMenuManager.addToMenu(parentMenu, null, "Deferred", "deferredId", job);

			IContributionItem[] items = parentMenu.getItems();
			assertTrue(items.length > 0);
			boolean found = false;
			for (IContributionItem item : items) {
				if ("deferredId".equals(item.getId())) {
					found = true;
					break;
				}
			}
			assertTrue("Sub menu should be added to parent", found);

			// Wait for the job to complete
			try {
				job.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}
		});
	}

	@Test
	public void addToMenuWithParentId() {
		syncExec(() -> {
			MenuManager parentMenu = new MenuManager();
			parentMenu.add(new GroupMarker("group1"));

			DeferredMenuManager.MenuJob job = createMenuJob(FAMILY, "prop2", Status.OK_STATUS);
			DeferredMenuManager.addToMenu(parentMenu, "group1", "Deferred", "deferredId2", job);

			IContributionItem[] items = parentMenu.getItems();
			assertTrue(items.length >= 2);

			try {
				job.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}
		});
	}

	@Test
	public void isRunningReturnsFalseWhenNoJobScheduled() {
		assertFalse(DeferredMenuManager.isRunning("noSuchFamily", "noSuchProp"));
	}

	@Test
	public void menuJobBelongsToFamily() {
		DeferredMenuManager.MenuJob job = createMenuJob("myFamily", "myProp", Status.OK_STATUS);
		assertTrue(job.belongsTo("myFamily"));
		assertFalse(job.belongsTo("otherFamily"));
	}

	@Test
	public void finishRemovesPendingWhenActionsExist() {
		syncExec(() -> {
			MenuManager parent = new MenuManager();
			DeferredMenuManager manager = new DeferredMenuManager(parent, "Test", "testId");

			// Add a real action so finish doesn't remove the whole menu
			Action action = new Action("Real") {};
			manager.add(action);

			manager.finish();

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}

			IContributionItem[] items = manager.getItems();
			for (IContributionItem item : items) {
				assertFalse("Pending action should be removed", manager.getPendingId().equals(item.getId()));
			}
		});
	}

	@Test
	public void getPendingIdIsStable() {
		syncExec(() -> {
			MenuManager parent = new MenuManager();
			DeferredMenuManager manager = new DeferredMenuManager(parent, "Test", "testId");

			String id1 = manager.getPendingId();
			String id2 = manager.getPendingId();
			assertNotNull(id1);
			assertEquals(id1, id2);
		});
	}

	@Test
	public void isVisibleDelegatesToSuper() {
		syncExec(() -> {
			MenuManager parent = new MenuManager();
			DeferredMenuManager manager = new DeferredMenuManager(parent, "Test", "testId");

			// With pending action, should be visible
			assertTrue(manager.isVisible());
		});
	}

	@Test
	public void jobAddsActionToMenu() throws InterruptedException {
		final boolean[] added = { false };
		syncExec(() -> {
			MenuManager parentMenu = new MenuManager();
			DeferredMenuManager.MenuJob job = new DeferredMenuManager.MenuJob(FAMILY, "propJobAdds") {
				protected IStatus addToMenu(IProgressMonitor monitor, IMenuManager menu) {
					menu.add(new Action("Dynamic Action") {});
					added[0] = true;
					return Status.OK_STATUS;
				}
			};

			DeferredMenuManager.addToMenu(parentMenu, null, "Deferred", "deferredJobAdds", job);

			try {
				job.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			Display display = PlatformUI.getWorkbench().getDisplay();
			while (display.readAndDispatch()) {}
		});
		assertTrue("Job should have added action to menu", added[0]);
	}
}
