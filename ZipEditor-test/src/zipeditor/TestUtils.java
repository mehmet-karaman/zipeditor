package zipeditor;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class TestUtils {

	public static void syncExec(Runnable runnable) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		final Throwable[] error = new Throwable[1];
		display.syncExec(() -> {
			try {
				runnable.run();
			} catch (Throwable t) {
				error[0] = t;
			}
		});
		if (error[0] instanceof AssertionError)
			throw (AssertionError) error[0];
		if (error[0] instanceof RuntimeException)
			throw (RuntimeException) error[0];
		if (error[0] != null)
			throw new RuntimeException(error[0]);
	}
}
