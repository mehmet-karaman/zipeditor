package zipeditor.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;

import zipeditor.PreferenceConstants;
import zipeditor.ZipEditorPlugin;
import zipeditor.model.zstd.ZstdUtilities;

public class PreferenceUtils {

	public enum ZstdLibrary {

		JNI(Messages.PreferenceUtils_ZSTDJniLibraryLabel, JNI_LIBRARY_ID),
		AIRCOMPRESSOR(Messages.PreferenceUtils_AircompressorLabel, AIRCOMPRESSOR_ID);

		private String label;
		private String id;

		ZstdLibrary(String label, String id) {
			this.label = label;
			this.id = id;
		}

		public String getLabel() {
			return label;
		}

		public String getID() {
			return id;
		}
	}

	/**
	 * Preference value for the aircompressor library.
	 */
	public static final String AIRCOMPRESSOR_ID = "aircompressor"; //$NON-NLS-1$

	/**
	 * Preference value for the zstd-jni library.
	 */
	public static final String JNI_LIBRARY_ID = "jniLibrary"; //$NON-NLS-1$

	private static String getSelectedZstdLib() {
		IPreferenceStore preferenceStore = ZipEditorPlugin.getDefault().getPreferenceStore();
		String selectedLib = preferenceStore
				.getString(PreferenceConstants.PREFIX_EDITOR + PreferenceConstants.SELECTED_ZSTD_LIB);
		return selectedLib;
	}

	/**
	 * Returns the available libraries.
	 * 
	 * @return a {@link List} of available {@link ZstdLibrary} objects.
	 */
	public static List<ZstdLibrary> getAvailableLibraries() {
		List<ZstdLibrary> availableLibs = new ArrayList<ZstdLibrary>();
		for (ZstdLibrary libData : ZstdLibrary.values()) {
			if (libData.id.equals(JNI_LIBRARY_ID) && ZstdUtilities.isZstdJniCompressionAvailable()) {
				availableLibs.add(libData);
			} else if (libData.id.equals(AIRCOMPRESSOR_ID) && ZstdUtilities.isAircompressorAvailable()) {
				availableLibs.add(libData);
			}
		}
		return availableLibs;
	}

	/**
	 * Returns the available libraries.
	 * 
	 * @return a {@link List} of available {@link ZstdLibrary} objects.
	 */
	public static String[][] getAvailableLibrariesForPreferenceUI() {
		List<ZstdLibrary> availableLibraries = getAvailableLibraries();
		String[][] libs = new String[availableLibraries.size()][2];
		for (int i = 0; i < availableLibraries.size(); i++) {
			ZstdLibrary zstdLibrary = availableLibraries.get(i);
			libs[i] = new String[] { zstdLibrary.label, zstdLibrary.id };
		}
		return libs;
	}

	/**
	 * @return true, if the zstd handling is active.
	 */
	static boolean isZstdActive() {
		IPreferenceStore preferenceStore = ZipEditorPlugin.getDefault().getPreferenceStore();
		return preferenceStore.getBoolean(PreferenceConstants.PREFIX_EDITOR + PreferenceConstants.ACTIVATE_ZSTD_LIB);
	}

	/**
	 * @return true, if the zstd-jni library is selected for zstd handling.
	 */
	public static boolean isJNIZstdSelected() {
		String selectedLib = getSelectedZstdLib();

		return JNI_LIBRARY_ID.equals(selectedLib);
	}

	/**
	 * @return true, if the aircompressor library is selected for zstd handling.
	 */
	public static boolean isAircompressorSelected() {
		String selectedLib = getSelectedZstdLib();

		return AIRCOMPRESSOR_ID.equals(selectedLib);
	}

	/**
	 * Checks if any zstd library is available and the zstd active pref is true.
	 * 
	 * @return
	 */
	public static boolean isZstdAvailableAndActive() {
		return isZstdActive() && getAvailableLibraries().size() > 0;
	}

	/**
	 * Returns the preference value for default zstd compression
	 * 
	 * @return true if zstd can be used as default or it returns false.
	 */
	public static boolean isZstdDefaultCompression() {
		IPreferenceStore preferenceStore = ZipEditorPlugin.getDefault().getPreferenceStore();
		return preferenceStore.getBoolean(PreferenceConstants.PREFIX_EDITOR + PreferenceConstants.USE_ZSTD_AS_DEFAULT);
	}

	/**
	 * Returns the preference value for the zstd compression level.
	 * 
	 * @return the value of the compression level preference.
	 */
	public static int getCompressionLevel() {
		IPreferenceStore preferenceStore = ZipEditorPlugin.getDefault().getPreferenceStore();
		return preferenceStore.getInt(PreferenceConstants.PREFIX_EDITOR + PreferenceConstants.COMPRESSION_LEVEL);
	}

	/**
	 * Checks if the selected library is available and returns it. If the selected
	 * library is not available it returns the available one.
	 * 
	 * @return the selected or available library. identifier of the library which is
	 *         written in the constants JNI_LIBRARY or AIRCOMPRESSOR or null if
	 *         there is no library available.
	 */
	public static String getSelectedOrAvailableLibrary() {
		List<ZstdLibrary> availableLibraries = getAvailableLibraries();
		if (availableLibraries.size() == 0) {
			return null;
		}
		String selectedZstdLibIdentifier = getSelectedZstdLib();
		for (ZstdLibrary library : availableLibraries) {
			if (selectedZstdLibIdentifier.equals(library.id)) {
				return selectedZstdLibIdentifier;
			}
		}

		return availableLibraries.get(0).id;
	}
}
