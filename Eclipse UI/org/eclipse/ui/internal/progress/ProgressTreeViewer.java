/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.Iterator;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;

/**
 * The ProgressTreeViewer is a tree viewer that handles the coloring of text.
 */
class ProgressTreeViewer extends TreeViewer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#doUpdateItem(org.eclipse.swt.widgets.Item,
	 *      java.lang.Object)
	 */
	protected void doUpdateItem(Item item, Object element) {
		super.doUpdateItem(item, element);
		if (element instanceof JobTreeElement) {
			if (item != null && item instanceof TreeItem) {
				TreeItem treeItem = (TreeItem) item;
				updateColors(treeItem, (JobTreeElement) element);
				if (treeItem.getItemCount() > 0)
					treeItem.setExpanded(true);
			}
		}
	}

	/**
	 * Update the colors for the treeItem.
	 * @param treeItem
	 * @param element
	 */

	private void updateColors(TreeItem treeItem, JobTreeElement element) {

		if (element.isJobInfo()) {
			JobInfo info = (JobInfo) element;
			if (info.getJob().getState() != Job.RUNNING) {
				treeItem.setForeground(JFaceColors.getActiveHyperlinkText(treeItem.getDisplay()));
				return;
			}
		}

		treeItem.setForeground(treeItem.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));

	}

	/**
	 * Create a new instance of the receiver with the supplied parent and
	 * style.
	 * 
	 * @param parent
	 * @param style
	 */
	public ProgressTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.AbstractTreeViewer#createChildren(org.eclipse.swt.widgets.Widget)
	 */
	protected void createChildren(Widget widget) {
		super.createChildren(widget);
		getTree().addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				//Bind escape to cancel
				if (e.keyCode == SWT.DEL) {
					ISelection selection = getSelection();
					if (selection instanceof IStructuredSelection) {
						IStructuredSelection structured = (IStructuredSelection) selection;
						Iterator elements = structured.iterator();
						while (elements.hasNext()) {
							Object next = elements.next();
							if (next instanceof JobInfo)
								 ((JobInfo) next).cancel();
						}
					}
				}
			}
		});
	}

}
