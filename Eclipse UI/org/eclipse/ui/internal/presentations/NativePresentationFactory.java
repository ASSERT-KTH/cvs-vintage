/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * A presentation factory using native widgets.
 * <p>
 * EXPERIMENTAL
 * </p>
 * 
 * @since 3.0
 */
public class NativePresentationFactory extends WorkbenchPresentationFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.presentations.AbstractPresentationFactory
     */
    public StackPresentation createPresentation(Composite parent,
            IStackPresentationSite site, int role,
            String perspectiveId, String folderId) {
        switch (role) {
        default:
            return new NativeStackPresentation(parent, site);
        }
    }

}