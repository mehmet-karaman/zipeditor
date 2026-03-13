/*
 * (c) Copyright 2002, 2016 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IIndexableLazyContentProvider;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import zipeditor.model.Node;

public class LazyZipContentProvider extends ZipContentProvider implements IIndexableLazyContentProvider, ILazyTreeContentProvider {

	private ColumnViewer fViewer;
	private Object[] fRootChildren;

	public LazyZipContentProvider(int mode) {
		super(mode, true);
	}

	public int findElement(Object element) {
		IElementComparer comparer = fViewer.getComparer();
		Object[] children = getRootChildren(fViewer.getInput());
		for (int i = 0; i < children.length; i++) {
			if (comparer.equals(children[i], element))
				return i;
		}
		return -1;
	}

	public void updateElement(int index) {
		Object[] children = getRootChildren(fViewer.getInput());
		if (index < children.length)
			((TableViewer) fViewer).replace(children[index], index);
	}

	@Override
	public void updateElement(Object parent, int index) {
		if (parent instanceof Node) {
			Node[] children = ((Node) parent).getChildren();
			Node child = children[index];
			((TreeViewer) fViewer).replace(parent, index, child);
			updateChildCount(child, -1);
		}
	}

	@Override
	public void updateChildCount(Object element, int currentChildCount) {
		if (element instanceof Node) {
			int count = ((Node) element).getChildren().length;
			if (count != currentChildCount)
				((TreeViewer) fViewer).setChildCount(element, count);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);

		fRootChildren = null;
		if (viewer instanceof ColumnViewer && newInput != null) {
			fViewer = (ColumnViewer) viewer;
			reset(newInput);
		} else {
			fViewer = null;
		}
	}

	public void reset(Object input) {
		fRootChildren = null;
		getRootChildren(input);
	}

	private Object[] getRootChildren(Object input) {
		if (fRootChildren == null) {
			fRootChildren = getChildren(input);
			if (fViewer instanceof TableViewer) {
				ViewerFilter[] filters = fViewer.getFilters();
				for (int i = 0; i < filters.length; i++) {
					fRootChildren = filters[i].filter(fViewer, input, fRootChildren);
				}
				((TableViewer) fViewer).setItemCount(fRootChildren.length);
			}
		}
		return fRootChildren;
	}
}
