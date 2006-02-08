/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.presentations;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.presentations.WorkbenchPresentationFactory;

/**
 * The intention of this class is to allow for replacing the implementation
 * of the toolbars in the workbench by providing a subclass of this presentation 
 * factory.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public abstract class InternalPresentationFactory extends WorkbenchPresentationFactory {

    /**
     * Creates an action presentation for a window
     * @param window 
     * @return ActionPresentation
     */
    public abstract ActionBarPresentation createActionBarPresentation(IWorkbenchWindow window);

}
