/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.graphics.Image;

/**
 * The JobTreeElement is the abstract superclass of items
 * displayed in the tree.
 */
abstract class JobTreeElement implements Comparable {

    /**
     * Return the parent of this object.
     * @return Object
     */
    abstract Object getParent();

    /**
     * Return whether or not the receiver has children.
     * @return boolean
     */
    abstract boolean hasChildren();

    /**
     * Return the children of the receiver.
     * @return Object[]
     */
    abstract Object[] getChildren();

    /**
     * Return the displayString for the receiver.
     * @return
     */
    abstract String getDisplayString();

    /**
     * Get the image for the reciever. By default there is no image.
     * @return Image or <code>null</code>.
     */
    public Image getDisplayImage() {
        return null;
    }

    /**
     * Return the condensed version of the display string
     * @return
     */
    String getCondensedDisplayString() {
        return getDisplayString();
    }

    /**
     * Return whether or not the receiver is an info.
     * @return boolean
     */
    abstract boolean isJobInfo();

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        return getDisplayString().compareTo(
                ((JobTreeElement) arg0).getDisplayString());
    }

    /**
     * Return whether or not this is currently active.
     * @return
     */
    abstract boolean isActive();

    /**
     * Return whether or not the receiver can be cancelled.
     * @return boolean
     */
    public boolean isCancellable() {
        return false;
    }

    /**
     * Cancel the receiver.
     */
    public void cancel() {
        //By default do nothing.
    }
}