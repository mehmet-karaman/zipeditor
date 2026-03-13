/*
 * (c) Copyright 2002, 2005 Uwe Voigt
 * All Rights Reserved.
 */
package zipeditor.actions;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;

import zipeditor.ZipTableViewer;

public class SelectAllAction extends ViewerAction {
	public SelectAllAction(StructuredViewer viewer) {
		super(ActionMessages.getString("SelectAllAction.0"), viewer); //$NON-NLS-1$
	}

	public void run() {
		StructuredViewer viewer = getViewer();
		if (viewer instanceof TreeViewer)
			((TreeViewer) viewer).getTree().selectAll();
		else if (viewer instanceof TableViewer)
			((TableViewer) viewer).getTable().selectAll();
		if (viewer instanceof ZipTableViewer) {
			IStructuredContentProvider contentProvider = (IStructuredContentProvider) viewer.getContentProvider();
			((ZipTableViewer) viewer).fireSelectionChanged(new StructuredSelection(contentProvider.getElements(viewer.getInput())));
		} else
			viewer.setSelection(viewer.getSelection());
	}
}
