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
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.model.AdaptableList;

/**
 *  Instances access the registry that is provided at creation time
 *  in order to determine the contained Wizards
 */
public class WizardsRegistryReader extends RegistryReader {
    private AdaptableList wizards;

    private String pluginPoint;

    public final static String TAG_WIZARD = "wizard";//$NON-NLS-1$

    public final static String ATT_NAME = "name";//$NON-NLS-1$

    // @issue we should have an IExtensionConstants class with all these attribute names, element names, attribute values (like true, false, etc).
    public final static String ATT_CLASS = "class";//$NON-NLS-1$

    public final static String ATT_ICON = "icon";//$NON-NLS-1$

    public final static String ATT_ID = "id";//$NON-NLS-1$

    public final static String trueString = "TRUE";//$NON-NLS-1$

    /**
     *	Create an instance of this class.
     *
     *	@param pluginPointId java.lang.String
     */
    public WizardsRegistryReader(String pluginPointId) {
        pluginPoint = pluginPointId;
    }

    /**
     * Adds new wizard to the provided collection. Override to
     * provide more logic.
     */
    protected void addNewElementToResult(WorkbenchWizardElement wizard,
            IConfigurationElement config) {
        wizards.add(wizard);
    }

    /**
     * Creates empty element collection. Overrider to fill
     * initial elements, if needed.
     */
    protected void createEmptyWizardCollection() {
        wizards = new AdaptableList();
    }

    /**
     * Returns a new WorkbenchWizardElement configured according to the parameters
     * contained in the passed Registry.  
     *
     * May answer null if there was not enough information in the Extension to create 
     * an adequate wizard
     */
    protected WorkbenchWizardElement createWizardElement(
            IConfigurationElement element) {
        // WizardElements must have a name attribute
        if (element.getAttribute(ATT_NAME) == null) {
            logMissingAttribute(element, ATT_NAME);
            return null;
        }
        
        if (element.getAttribute(ATT_CLASS) == null && element.getChildren(ATT_CLASS).length == 0) {       
            logMissingAttribute(element, ATT_CLASS);
            return null;
        }
        return new WorkbenchWizardElement(element);
    }

    /**
     *	Returns the first wizard with a given id.
     */
    public WorkbenchWizardElement findWizard(String id) {
        Object[] wizards = getWizardCollectionElements();
        for (int nX = 0; nX < wizards.length; nX++) {
            WizardCollectionElement collection = (WizardCollectionElement) wizards[nX];
            WorkbenchWizardElement element = collection.findWizard(id, true);
            if (element != null)
                return element;
        }
        return null;
    }

    /**
     * Returns a list of wizards, project and not.
     *
     * The return value for this method is cached since computing its value
     * requires non-trivial work.  
     */
    public AdaptableList getWizards() {
        if (!areWizardsRead()) {
            readWizards();
        }
        return wizards;
    }

    /**
     * Implement this method to read element attributes.
     */
    public boolean readElement(IConfigurationElement element) {
        if (!element.getName().equals(TAG_WIZARD))
            return false;
        WorkbenchWizardElement wizard = createWizardElement(element);
        if (wizard != null)
            addNewElementToResult(wizard, element);
        return true;
    }

    /**
     * Reads the wizards in a registry.  
     */
    protected void readWizards() {
        if (!areWizardsRead()) {
            createEmptyWizardCollection();
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            readRegistry(registry, WorkbenchPlugin.PI_WORKBENCH, pluginPoint);
        }
    }

    /**
     * Returns whether the wizards have been read already
     */
    protected boolean areWizardsRead() {
        return wizards != null;
    }

    protected Object[] getWizardCollectionElements() {
        if (!areWizardsRead()) {
            readWizards();
        }
        return wizards.getChildren();
    }
}