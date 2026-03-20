package zipeditor.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class CompressZipFileWrapper implements IZipFileWrapper {

	private ZipFile fZipFile;

	public CompressZipFileWrapper(File path) throws IOException {
		fZipFile = ZipFile.builder().setFile(path).get();
	}
	
	@Override
	public Enumeration entries() {
		List<ZipEntry> transformedEntries = new ArrayList<>();
		Enumeration<ZipArchiveEntry> entries = fZipFile.getEntries();
		while (entries.hasMoreElements()) {
			ZipArchiveEntry zipArchiveEntry = entries.nextElement();
			
			ZipEntry transformedEntry = ZipImplFacade.toZipEntry(zipArchiveEntry);
			transformedEntries.add(transformedEntry);
		}
		
		return Collections.enumeration(transformedEntries);
	}

	@Override
	public void close() throws IOException {
		fZipFile.close();
	}

}
