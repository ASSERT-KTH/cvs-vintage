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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.ExtensionEventHandlerMessages;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The registry of action set extensions.
 */
public class ActionSetRegistry extends RegistryManager implements
        IRegistryChangeListener {
    public static final String OTHER_CATEGORY = "org.eclipse.ui.actionSetCategory";//$NON-NLS-1$

    private ArrayList children = new ArrayList();

    private ArrayList categories = new ArrayList(1);

    private Map mapPartToActionSets = new HashMap();

    // for dynamic UI - store cache for removal
    private Map mapCacheToActionSets = new HashMap();

    /**
     * Creates the action set registry.
     */
    public ActionSetRegistry() {
        super(WorkbenchPlugin.PI_WORKBENCH, IWorkbenchConstants.PL_ACTION_SETS);
        Platform.getExtensionRegistry().addRegistryChangeListener(this);
        readFromRegistry();
    }

    /**
     * Adds an action set.
     */
    public void addActionSet(ActionSetDescriptor desc) {
        children.add(desc);
    }

    /**
     * Adds an association between an action set an a part.
     */
    public void addAssociation(String actionSetId, String partId) {
        // get the action set ids for this part
        ArrayList actionSets = (ArrayList) mapPartToActionSets.get(partId);
        if (actionSets == null) {
            actionSets = new ArrayList();
            mapPartToActionSets.put(partId, actionSets);
        }
        // get the action set
        IActionSetDescriptor desc = findActionSet(actionSetId);
        if (desc == null) {
            WorkbenchPlugin.log("Unable to associate action set with part: " + //$NON-NLS-1$
                    partId + ". Action set " + actionSetId + " not found."); //$NON-NLS-2$ //$NON-NLS-1$
            return;
        }
        // add the action set if it is not already present
        if (!actionSets.contains(desc))
            actionSets.add(desc);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.RegistryManager#buildNewCacheObject(org.eclipse.core.runtime.IExtensionDelta)
     */
    public Object[] buildNewCacheObject(IExtensionDelta delta) {
        IExtension extension = delta.getExtension();
        if (extension == null)
            return null;
        IConfigurationElement[] elements = extension.getConfigurationElements();
        if (elements == null || elements.length == 0)
            return null;
        Object[] retArray = new Object[elements.length];

        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            if (element == null)
                break;
            try {
                ActionSetDescriptor desc = new ActionSetDescriptor(element);
                addActionSet(desc);
                retArray[i] = desc;
            } catch (CoreException e) {
                // log an error since its not safe to open a dialog here
                WorkbenchPlugin
                        .log(
                                "Unable to create action set descriptor.", e.getStatus());//$NON-NLS-1$
            }
        }
        if (retArray != null && retArray.length != 0) {
            addResetMessage(MessageFormat
                    .format(
                            ExtensionEventHandlerMessages
                                    .getString("ExtensionEventHandler.change_format"), //$NON-NLS-1$ 
                            new Object[] {
                                    extension.getNamespace(),
                                    ExtensionEventHandlerMessages
                                            .getString("ExtensionEventHandler.new_action_set") })); //$NON-NLS-1$ 
        }
        return retArray;
    }

    /**
     * Finds and returns the registered action set with the given id.
     *
     * @param id the action set id 
     * @return the action set, or <code>null</code> if none
     * @see IActionSetDescriptor#getId
     */
    public IActionSetDescriptor findActionSet(String id) {
        Iterator enum = children.iterator();
        while (enum.hasNext()) {
            IActionSetDescriptor desc = (IActionSetDescriptor) enum.next();
            if (desc.getId().equals(id))
                return desc;
        }
        return null;
    }

    /**
     * Find a category with a given id.
     */
    public ActionSetCategory findCategory(String id) {
        Iterator enum = categories.iterator();
        while (enum.hasNext()) {
            ActionSetCategory cat = (ActionSetCategory) enum.next();
            if (id.equals(cat.getId()))
                return cat;
        }
        return null;
    }

    /**
     * Returns a list of the action sets known to the workbench.
     *
     * @return a list of action sets
     */
    public IActionSetDescriptor[] getActionSets() {
        int count = children.size();
        IActionSetDescriptor[] array = new IActionSetDescriptor[count];
        for (int nX = 0; nX < count; nX++) {
            array[nX] = (IActionSetDescriptor) children.get(nX);
        }
        return array;
    }

    /**
     * Returns a list of the action sets associated with the given part id.
     *
     * @return a list of action sets
     */
    public IActionSetDescriptor[] getActionSetsFor(String partId) {
        // get the action set ids for this part
        ArrayList actionSets = (ArrayList) mapPartToActionSets.get(partId);
        if (actionSets == null)
            return new IActionSetDescriptor[0];
        return (IActionSetDescriptor[]) actionSets
                .toArray(new IActionSetDescriptor[actionSets.size()]);
    }

    /**
     * Returns a list of action set categories.
     *
     * @return a list of action sets categories
     */
    public ActionSetCategory[] getCategories() {
        int count = categories.size();
        ActionSetCategory[] array = new ActionSetCategory[count];
        for (int i = 0; i < count; i++) {
            array[i] = (ActionSetCategory) categories.get(i);
        }
        return array;
    }

    /**
     * Adds each action set in the registry to a particular category.
     * For now, everything goes into the OTHER_CATEGORY.
     */
    public void mapActionSetsToCategories() {
        // Create "other" category.
        ActionSetCategory cat = new ActionSetCategory(OTHER_CATEGORY,
                WorkbenchMessages.getString("ActionSetRegistry.otherCategory")); //$NON-NLS-1$
        categories.add(cat);

        // Add everything to it.
        Iterator enum = children.iterator();
        while (enum.hasNext()) {
            IActionSetDescriptor desc = (IActionSetDescriptor) enum.next();
            cat.addActionSet(desc);
        }
    }

    /**
     * Reads the registry.
     */
    public void readFromRegistry() {
        ActionSetRegistryReader reader = new ActionSetRegistryReader();
        reader.readRegistry(Platform.getExtensionRegistry(), this);

        ActionSetPartAssociationsReader assocReader = new ActionSetPartAssociationsReader();
        assocReader.readRegistry(Platform.getExtensionRegistry(), this);
    }

    //for dynamic UI
    // Commented out because the cache is broken -- it does not understand multiple windows.
    // See bug 66374.
    //public void addCache(String actionSetId, Object cache) {
    //	mapCacheToActionSets.put(actionSetId, cache);
    //}

    //for dynamic UI
    public Object removeCache(String actionSetId) {
        return mapCacheToActionSets.remove(actionSetId);
    }

    //for dynamic UI
    public void remove(String id) {
        IActionSetDescriptor desc = findActionSet(id);
        if (id != null) {
            children.remove(desc);
            categories.remove(desc);
        }
    }

    //for dynamic UI
    public void removeAssociation(String actionSetId, String partId) {
        IActionSetDescriptor desc = findActionSet(actionSetId);
        if (desc == null)
            return;
        ArrayList actionSets = (ArrayList) mapPartToActionSets.get(partId);
        if (actionSets == null)
            return;
        if (actionSets.contains(desc))
            actionSets.remove(desc);
        if (actionSets.size() == 0)
            mapPartToActionSets.remove(partId);

    }
}