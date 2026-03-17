package zipeditor.model;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JavaUtilZipFileWrapper implements IZipFileWrapper {

	private ZipFile fZipFile;

	public JavaUtilZipFileWrapper(File path) throws IOException {
		fZipFile = new ZipFile(path);
	}

	@Override
	public Enumeration<? extends ZipEntry> entries() {
		return fZipFile.entries();
	}

	@Override
	public void close() throws IOException {
		fZipFile.close();
	}

}
