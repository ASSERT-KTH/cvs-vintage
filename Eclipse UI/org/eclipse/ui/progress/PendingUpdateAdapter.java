/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The PendingUpdateAdapter is a convenience object that can be used
 * by a BaseWorkbenchContentProvider that wants to show a pending update.
 * 
 * @since 3.2
 */
public class PendingUpdateAdapter implements IWorkbenchAdapter, IAdaptable {

    private boolean removed = false;

    /**
     * Return whether or not this has been removed from the tree.
     * @return boolean
     */
    protected boolean isRemoved() {
        return removed;
    }

    /**
     * Set whether or not this has been removed from the tree.
     * @param removedValue boolean
     */
    protected void setRemoved(boolean removedValue) {
        this.removed = removedValue;
    }

    /**
     * Create a new instance of the receiver.
     */
    public PendingUpdateAdapter() {
        //No initial behavior
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return ProgressMessages.PendingUpdateAdapter_PendingLabel;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }
}
