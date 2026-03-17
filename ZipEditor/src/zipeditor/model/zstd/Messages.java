package zipeditor.model.zstd;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "zipeditor.model.zstd" + ".messages"; //$NON-NLS-1$ //$NON-NLS-2$
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	public static String ZstdHandler_aircompLibNotAvailable;
	public static String ZstdHandler_jniLibNotAvailable;
	public static String ZstdHandler_notActive;
}
