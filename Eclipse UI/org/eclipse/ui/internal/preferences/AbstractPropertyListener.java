/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

/**
 * @since 3.1
 */
public abstract class AbstractPropertyListener implements IPropertyMapListener {

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMapListener#propertyChanged(java.lang.String[])
     */
    public void propertyChanged(String[] propertyIds) {
        update();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.IPropertyMapListener#listenerAttached()
     */
    public void listenerAttached() {
        update();
    }

    protected abstract void update();
}
