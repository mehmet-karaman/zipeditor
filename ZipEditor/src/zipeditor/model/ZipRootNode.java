/*
 * (c) Copyright 2002, 2017 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import zipeditor.Utils;

public class ZipRootNode extends RootNode {

	private ZipFile zipFile;
	private Map entryToContent;

	public ZipRootNode(ZipModel model) {
		super(model);
	}

	InputStream getFor(ZipEntry zipEntry) throws IOException {
		if (zipFile != null)
			return zipFile.getInputStream(zipEntry);
		if (entryToContent != null) {
			byte[] content = (byte[]) entryToContent.get(zipEntry.getName());
			if (content != null)
				return new ByteArrayInputStream(content);
			}
		return null;
	}

	public Object accept(NodeVisitor visitor, Object argument) throws IOException {
		// this is relevant when saving to a previously empty file
		if (model.getZipPath().length() > 0) {
			zipFile = model.createZipFile(model.getZipPath());
			if (zipFile == null) {
				entryToContent = new HashMap();
				ZipInputStream in = new ZipInputStream(new FileInputStream(model.getZipPath()));
				for (ZipEntry entry; (entry = in.getNextEntry()) != null;) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					Utils.readAndWrite(in, bos, false, true);
					entryToContent.put(entry.getName(), bos.toByteArray());
				}
			}
		}
		try {
			return super.accept(visitor, argument);
		} finally {
			if (zipFile != null)
				zipFile.close();
			zipFile = null;
			if (entryToContent != null)
				entryToContent.clear();
			entryToContent = null;
		}
	}

	public Node create(ZipModel model, String name, boolean isFolder) {
		return new ZipNode(model, name, isFolder);
	}
}
