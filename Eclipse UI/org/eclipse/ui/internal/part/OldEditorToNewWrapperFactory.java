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
package org.eclipse.ui.internal.part;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.ComponentFactory;
import org.eclipse.ui.components.ComponentHandle;
import org.eclipse.ui.components.Components;
import org.eclipse.ui.components.IServiceProvider;

/**
 * @since 3.1
 */
public class OldEditorToNewWrapperFactory extends ComponentFactory {
    /* (non-Javadoc)
     * @see org.eclipse.core.components.ComponentFactory#getHandle(org.eclipse.core.components.IComponentProvider)
     */
    public ComponentHandle createHandle(IServiceProvider availableServices)
            throws ComponentException {
        IEditorPart part = (IEditorPart) Components.queryInterface(availableServices,
                IEditorPart.class);
        
        StandardWorkbenchServices services = new StandardWorkbenchServices(availableServices);
        
        try {
            return new ComponentHandle(new OldEditorToNewWrapper(part, services));
//            bundle, composite, 
//                    new ProviderToAdaptableAdapter(availableServices), input, page, actionBars, selectionHandler,
//                    name, state, status, partDescriptor), true);
        } catch (CoreException e) {
            throw new ComponentException(part.getClass(), e);
        } 

    }
}
