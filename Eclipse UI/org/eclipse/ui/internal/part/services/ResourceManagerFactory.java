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
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.ui.components.ComponentFactory;
import org.eclipse.ui.components.ComponentHandle;
import org.eclipse.ui.components.IServiceProvider;
import org.eclipse.ui.internal.Workbench;

/**
 * @since 3.1
 */
public class ResourceManagerFactory extends ComponentFactory {

    /* (non-Javadoc)
     * @see org.eclipse.core.component.ComponentAdapter#createInstance(org.eclipse.core.component.IContainer)
     */
    public ComponentHandle createHandle(IServiceProvider availableServices) {
        
        ResourceManager registry = JFaceResources.getResources(Workbench.getInstance().getDisplay());
        
        LocalResourceManager manager = new LocalResourceManager(registry);
        
        return new ComponentHandle(manager) {
            /* (non-Javadoc)
             * @see org.eclipse.core.component.AbstractComponentHandle#dispose()
             */
            public void dispose() {
                ((LocalResourceManager)getInstance()).dispose();
            }
        };
    }
}
