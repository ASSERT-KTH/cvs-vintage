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
package org.eclipse.ui.internal.part;

import org.eclipse.core.components.FactoryMap;
import org.eclipse.core.components.ComponentException;
import org.eclipse.core.components.ServiceFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.part.services.NullEditorInput;
import org.eclipse.ui.part.Part;
import org.eclipse.ui.part.services.IPartDescriptor;
import org.eclipse.ui.part.services.IWorkbenchPartFactory;

/**
 * Wraps a new-style Part in an IEditorPart. The wrapper creates and manages 
 * the lifecycle of the Part. If you are interested in adapting an existing
 * part, use <code>NewPartToOldAdapter</code> instead.
 * 
 * @since 3.1
 */
public class NewEditorToOldWrapper extends NewPartToOldWrapper implements
        IEditorPart {

    private IAdaptable additionalServices = new IAdaptable() {
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(Class adapter) {
            if (adapter == IEditorInput.class) {
                return getPropertyProvider().getEditorInput();
            }
            if (adapter == IActionBars.class) {
                return getEditorSite().getActionBars();
            }
            return null;
        }  
    };

    public NewEditorToOldWrapper(IPartDescriptor descriptor) {
        super(new PartPropertyProvider(null, null, null, descriptor, new NullEditorInput()));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#getMemento()
     */
    protected IMemento getMemento() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#createPart(org.eclipse.swt.widgets.Composite, org.eclipse.core.component.IContainerContext)
     */
    protected Part createPart(Composite parent, ServiceFactory args) throws ComponentException {
        IWorkbenchPartFactory factory = getFactory();
        return factory.createEditor(getSite().getId(), parent, getPropertyProvider().getEditorInput(), 
                getMemento(), args);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#getSecondaryId()
     */
    protected String getSecondaryId() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#getEditorSite()
     */
    public IEditorSite getEditorSite() {
        return (IEditorSite)getSite();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.part.compatibility.NewPartToOldAdapter#addServices(org.eclipse.core.component.ContainerContext)
     */
    protected void addServices(FactoryMap context) {
        super.addServices(context);
        
        context.addInstance(additionalServices);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {

        ((PartPropertyProvider)getPropertyProvider()).setEditorInput(input);
        setSite(site);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public void doSaveAs() {

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
        return false;
    }

}
