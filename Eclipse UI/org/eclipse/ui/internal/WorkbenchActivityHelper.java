/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IObjectActivityManager;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.ICategory;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * Utility class that manages the preservation of active activities as well as 
 * setting up various ObjectActivityManagers used throughout the workbench.
 * 
 * @since 3.0
 */
public class WorkbenchActivityHelper {

    /**
     * Prefix for all role preferences
     */
    private static String PREFIX = "UIRoles."; //$NON-NLS-1$    
    
    /**
     * 
     *Resource listener that reacts to new projects (and associated natures) 
     * coming into the workspace.
     */
    private IResourceChangeListener listener;

    /**
     * Singleton instance.
     */
    private static WorkbenchActivityHelper singleton;
    
    /**
     * Get the singleton instance of this class.
     * @return the singleton instance of this class.
     * @since 3.0
     */
    public static WorkbenchActivityHelper getInstance() {
        if (singleton == null) {
            singleton = new WorkbenchActivityHelper();            
        }
        return singleton;
    }
    
    /**
     * Create a new <code>WorkbenchActivityHelper</code> which will populate 
     * the various <code>ObjectActivityManagers</code> with Workbench 
     * contributions.
     */
    private WorkbenchActivityHelper() {
        listener = getChangeListener();
        WorkbenchPlugin.getPluginWorkspace().addResourceChangeListener(listener);
        loadEnabledStates();
        
        createPreferenceMappings();
        createNewWizardMappings();
        createPerspectiveMappings();
        createViewMappings();  
    }
    
    /**
     * Get a change listener for listening to resource changes.
     * 
     * @return
     */
    private IResourceChangeListener getChangeListener() {
        return new IResourceChangeListener() {
            /*
             * (non-Javadoc) @see
             * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
             */
            public void resourceChanged(IResourceChangeEvent event) {

                IResourceDelta mainDelta = event.getDelta();

                if (mainDelta == null)
                    return;
                //Has the root changed?
                if (mainDelta.getKind() == IResourceDelta.CHANGED
                    && mainDelta.getResource().getType() == IResource.ROOT) {

                    try {
                        IResourceDelta[] children = mainDelta.getAffectedChildren();
                        for (int i = 0; i < children.length; i++) {
                            IResourceDelta delta = children[i];
                            if (delta.getResource().getType() == IResource.PROJECT) {
                                IProject project = (IProject) delta.getResource();
                                String[] ids = project.getDescription().getNatureIds();
                                for (int j = 0; j < ids.length; j++) {
                                    enableActivities(ids[j]);
                                }
                            }
                        }

                    } catch (CoreException exception) {
                        //Do nothing if there is a CoreException
                    }
                }
            }
        };
    }

    /**
     * Enable all IActivity objects that match the given id.
     * 
     * @param id the id to match.
     * @since 3.0
     */
    public static void enableActivities(String id) {
        IActivityManager activityManager = ((Workbench)PlatformUI.getWorkbench())
            .getActivityManager();
        Set activities = new HashSet(activityManager.getEnabledActivityIds());
        for (Iterator i = activityManager.getDefinedActivityIds().iterator(); i.hasNext(); ) {
            String activityId = (String) i.next();
            IActivity activity = activityManager.getActivity(activityId);
            if (activity.match(id)) {
                activities.add(activityId);
            }
        }
        activityManager.setEnabledActivityIds(activities);
    }
    
    /**
     * Save the enabled state of all Activities and unhook the 
     * <code>IResourceChangeListener</code>.
     */ 
    public void shutdown() {
        saveEnabledStates();
        if (listener != null) {
            WorkbenchPlugin.getPluginWorkspace().removeResourceChangeListener(listener);
        }        
    }

    
    /**
     * Create the mappings for the new wizard object activity manager.
     * Objects of interest in this manager are Strings (wizard IDs).
     */
    private void createNewWizardMappings() {
        NewWizardsRegistryReader reader = new NewWizardsRegistryReader(false);
        WizardCollectionElement wizardCollection = (WizardCollectionElement)reader.getWizards();
        IObjectActivityManager manager = PlatformUI.getWorkbench().getObjectActivityManager(IWorkbenchConstants.PL_NEW, true);
        Object [] wizards = flattenWizards(wizardCollection);
        for (int i = 0; i < wizards.length; i++) {
            WorkbenchWizardElement element = (WorkbenchWizardElement)wizards[i];
            manager.addObject(element.getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), element.getID(), element.getID());
            
        }
        manager.applyPatternBindings();        
    }
    
    /**
     * Create the mappings for the perspective object activity manager.  
     * Objects of interest in this manager are Strings (perspective IDs).
     */
    private void createPerspectiveMappings() {
        IPerspectiveRegistry registry = WorkbenchPlugin.getDefault().getPerspectiveRegistry();
        IPerspectiveDescriptor [] descriptors = registry.getPerspectives();
        IObjectActivityManager manager = PlatformUI.getWorkbench().getObjectActivityManager(IWorkbenchConstants.PL_PERSPECTIVES, true);
        for (int i = 0; i < descriptors.length; i++) {
            String localId = descriptors[i].getId();
            if (!(descriptors[i] instanceof PerspectiveDescriptor)) {
                // this situation doesn't currently occur.  
                // All of our IPerspectiveDescriptors are PerspectiveDescriptors
                // give it a plugin ID of * to represent internal "plugins" (custom perspectives)
                // These objects will always be "active".
                manager.addObject("*", localId, localId); //$NON-NLS-1$
                continue;
            }
            IConfigurationElement element = ((PerspectiveDescriptor)descriptors[i]).getConfigElement();
            if (element == null) {
                // Custom perspective
                // Give it a plugin ID of * to represent internal "plugins" (custom perspectives)
                // These objects will always be "active".                
                manager.addObject("*", localId, localId); //$NON-NLS-1$
                continue;
            }
            String pluginId = element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier();
            manager.addObject(pluginId, localId, localId);              
        }
       manager.applyPatternBindings();        
    }    

    /**
     * Create the mappings for the preference page object activity manager.
     * Objects of interest in this manager are WorkbenchPreferenceNodes. 
     */
    private void createPreferenceMappings() {       
        PreferenceManager preferenceManager = WorkbenchPlugin.getDefault().getPreferenceManager();
        //add all WorkbenchPreferenceNodes to the manager
        IObjectActivityManager objectManager =
            PlatformUI.getWorkbench().getObjectActivityManager(IWorkbenchConstants.PL_PREFERENCES, true);
        for (Iterator i = preferenceManager.getElements(PreferenceManager.PRE_ORDER).iterator(); i.hasNext();) {
            IPreferenceNode node = (IPreferenceNode) i.next();
            if (node instanceof WorkbenchPreferenceNode) {
                WorkbenchPreferenceNode workbenchNode = ((WorkbenchPreferenceNode) node);
                objectManager.addObject(workbenchNode.getPluginId(), workbenchNode.getExtensionLocalId(), node);
            }
        }
        // and then apply the default bindings
        objectManager.applyPatternBindings();
    }
   
    /**
     * Create the mappings for the perspective object activity manager.  
     * Objects of interest in this manager are Strings (view IDs as well as view
     * category IDs (in the form "{ID}*").
     */
    private void createViewMappings() {
        IViewRegistry viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
        IObjectActivityManager objectManager =
            PlatformUI.getWorkbench().getObjectActivityManager(IWorkbenchConstants.PL_VIEWS, true);        
        
        IViewDescriptor [] viewDescriptors = viewRegistry.getViews();
        for (int i = 0; i < viewDescriptors.length; i++) {
            IConfigurationElement element = viewDescriptors[i].getConfigurationElement();
            objectManager.addObject(element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), viewDescriptors[i].getId(), viewDescriptors[i].getId());
        }
        
        // this is a temporary hack until we decide whether categories warrent their own
        // object manager.  
        ICategory[] categories = viewRegistry.getCategories();
        for (int i = 0; i < categories.length; i++) {
            IConfigurationElement element = (IConfigurationElement) categories[i].getAdapter(IConfigurationElement.class);
            if (element != null) {
                String categoryId = createViewCategoryIdKey(categories[i].getId());
                objectManager.addObject(element.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), categoryId, categoryId);
            }
        }
        
        // and then apply the default bindings
       objectManager.applyPatternBindings();        
    }
    
    /**
     * Utility method to create a key/object value from a given view 
     * category ID.
     * 
     * @param id
     * @return the value of id + '*'
     * @since 3.0
     */
    public static String createViewCategoryIdKey(String id) {
        return id + '*';
    }
       
    /**
     * Take the tree WizardCollecitonElement structure and flatten it into a list
     * of WorkbenchWizardElements. 
     * 
     * @param wizardCollection the collection to flatten.
     * @return Object [] the flattened wizards.
     * @since 3.0
     */
    private Object[] flattenWizards(WizardCollectionElement wizardCollection) {
        return flattenWizards(wizardCollection, new HashSet());
    }

    /**
     * Recursivly take a <code>WizardCollectionElement</code> and flatten it 
     * into an array of all contained wizards.
     *
     * @param wizardCollection the collection to flatten.
     * @param list the list of currently flattened wizards.
     * @return Object [] the flattened wizards.
     * @since 3.0
     */
    private Object[] flattenWizards(WizardCollectionElement wizardCollection, Collection wizards) {
        wizards.addAll(Arrays.asList(wizardCollection.getWizards()));
        for (int i = 0; i < wizardCollection.getChildren().length; i++) {
            WizardCollectionElement child = (WizardCollectionElement) wizardCollection.getChildren()[i];
            wizards.addAll(Arrays.asList(flattenWizards(child, wizards)));            
        }
        return wizards.toArray();
    }
    
    /**
     * Create the preference key for the activity.
     * 
     * @param activity the activity.
     * @return String a preference key representing the activity.
     */
    private String createPreferenceKey(IActivity activity) {
        return PREFIX + activity.getId();
    }    
    
    /**
     * Loads the enabled states from the preference store. 
     */
    void loadEnabledStates() {
        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();

        //Do not set it if the store is not set so as to
        //allow for switching off and on of roles
//        if (!store.isDefault(PREFIX + FILTERING_ENABLED))
//            setFiltering(store.getBoolean(PREFIX + FILTERING_ENABLED));
        IActivityManager activityManager = ((Workbench)PlatformUI.getWorkbench())
            .getActivityManager();
        Iterator values = activityManager.getDefinedActivityIds().iterator();
        Set enabledActivities = new HashSet();
        while (values.hasNext()) {
            IActivity activity = activityManager.getActivity((String) values.next());
            if (store.getBoolean(createPreferenceKey(activity))) {
                enabledActivities.add(activity.getId());
            }
        }
        
        activityManager.setEnabledActivityIds(enabledActivities);
    }
    
    
    /**
     * Save the enabled states in he preference store. 
     */
    private void saveEnabledStates() {
        IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
//        store.setValue(PREFIX + FILTERING_ENABLED, isFiltering());
        IActivityManager activityManager = ((Workbench)PlatformUI.getWorkbench())
            .getActivityManager();
        Iterator values = activityManager.getDefinedActivityIds().iterator();
        while (values.hasNext()) {
            IActivity activity = activityManager.getActivity((String) values.next());

            store.setValue(createPreferenceKey(activity), activity.isEnabled());
        }
    }    
}
