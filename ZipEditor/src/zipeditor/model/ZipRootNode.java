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

import zipeditor.Utils;

public class ZipRootNode extends RootNode {

	private ZipReadFacade zipFile;
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
			try {
				zipFile = ZipReadFacade.open(model);
			} catch (IOException e) {
				zipFile = null;
			}
			if (zipFile == null) {
				entryToContent = new HashMap();
				ZipImplFacade in = ZipImplFacade.openForModel(model, new FileInputStream(model.getZipPath()));
				for (ZipEntry entry; (entry = in.getNextEntry()) != null;) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					Utils.readAndWrite(in, bos, false, true);
					entryToContent.put(entry.getName(), bos.toByteArray());
				}
				in.close();
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
	
	/**
	 * This method can be used to check if new compression methods can be used without doubts.
	 *  
	 * @param zipMethod the compression method to check
	 * @return true if the given zipMethod was already used in this node model, else false.
	 */
	public boolean hasContentWithCompression(int zipMethod) {
		return hasContentWithCompression(this, zipMethod);
	}

	private boolean hasContentWithCompression(Node parentNode, int zipMethod) {
		for (Node node : parentNode.getChildren()) {
			if (!(node instanceof ZipNode)) {
				continue;
			}
			ZipNode zipNode = (ZipNode) node;
			if (zipNode.getMethod() == zipMethod) {
				return true;
			}
			if (hasContentWithCompression(zipNode, zipMethod)) {
				return true;
			}
		}
		return false;
	}
}