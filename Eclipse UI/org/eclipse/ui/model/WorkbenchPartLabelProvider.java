/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.model;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A table label provider implementation for showing workbench views and
 * editors (objects of type <code>IWorkbenchPart</code>) in tree- and
 * table-structured viewers.
 * <p>
 * Clients may instantiate this class. It is not intended to be subclassed.
 * </p>
 * 
 * @since 3.0
 */
public final class WorkbenchPartLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {

	/**
	 * Creates a new label provider for workbench parts.
	 */
	public WorkbenchPartLabelProvider() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	public final Image getImage(Object element) {
		if (element instanceof IWorkbenchPart) {
			return ((IWorkbenchPart) element).getTitleImage();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider
	 */
	public final String getText(Object element) {
		if (element instanceof IWorkbenchPart) {
			IWorkbenchPart part = (IWorkbenchPart) element;
			String path = part.getTitleToolTip();
			if (path.length() == 0) {
				return part.getTitle();
			} else {
				return part.getTitle() + "  [" + part.getTitleToolTip() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return null;
	}

	/**
	 * @see ITableLabelProvider#getColumnImage
	 */
	public final Image getColumnImage(Object element, int columnIndex) {
		return getImage(element);
	}

	/**
	 * @see ITableLabelProvider#getColumnText
	 */
	public final String getColumnText(Object element, int columnIndex) {
		return getText(element);
	}
}
