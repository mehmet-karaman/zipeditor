package zipeditor.model;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Wrapper for ZIP file implementations that support enumeration of entries.
 */
public interface IZipFileWrapper {

	/**
	 * Returns an enumeration of the ZIP entries in this file.
	 */
	Enumeration entries();

	/**
	 * Closes this ZIP file and releases any system resources associated with it.
	 */
	void close() throws IOException;

}
