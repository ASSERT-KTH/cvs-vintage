/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.util.Util;

/**
 * A dynamic contribution item which supports to switch to other Contexts.
 */
public class ShowFastViewContribution extends ContributionItem {
	public static final String FAST_VIEW = "FastView"; //$NON-NLS-1$
	private IWorkbenchWindow window;
	/**
	 * Create a new menu item.
	 */
	public ShowFastViewContribution(IWorkbenchWindow window) {
		super("showFastViewContr"); //$NON-NLS-1$
		this.window = window;
	}
	
	private void updateItem(ToolItem item, IViewReference ref) {
		if (item.getImage() != ref.getTitleImage()) {
			item.setImage(ref.getTitleImage());			
		}

		if (!Util.equals(item.getToolTipText(), ref.getTitle())) {
			item.setToolTipText(ref.getTitle());		
		}
	}
	
	/**
	 * The default implementation of this <code>IContributionItem</code>
	 * method does nothing. Subclasses may override.
	 */
	public void fill(ToolBar parent, int index) {
		// Get page.
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page == null)
			return;

		// Get views.
		IViewReference[] refs = page.getFastViews();

		// Create tool item for each view.
		int size = refs.length;
		for (int nX = 0; nX < size; nX++) {
			final IViewReference ref = refs[nX];
			final ToolItem item = new ToolItem(parent, SWT.CHECK, index);
			updateItem(item, ref);
			item.setData(FAST_VIEW, ref);
			
			final IPropertyListener propertyListener = new IPropertyListener() {

				public void propertyChanged(Object source, int propId) {
					if (propId == IWorkbenchPartConstants.PROP_TITLE) {
						if (!item.isDisposed()) {
							updateItem(item, ref);
						}
					}
				}
				
			};
			
			ref.addPropertyListener(propertyListener);
			
			item.addDisposeListener(new DisposeListener() {
		        /* (non-Javadoc)
				 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
				 */
				public void widgetDisposed(DisposeEvent e) {
					ref.removePropertyListener(propertyListener);
				}				
			});
			
			// Select the active fast view's icon.
			if (ref == page.getActiveFastView()) {
				item.setSelection(true);
			} else {
				item.setSelection(false);
			}
			
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showView(ref);
				}
			});
			index++;
		}
	}
	/**
	 * Returns whether the contribution is dynamic.
	 */
	public boolean isDynamic() {
		return true;
	}
	/**
	 * Open a view.
	 */
	private void showView(IViewReference ref) {
		WorkbenchPage page = (WorkbenchPage)ref.getPage();
		page.toggleFastView(ref);
	}
}
