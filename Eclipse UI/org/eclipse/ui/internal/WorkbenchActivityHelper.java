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

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IMutableActivityManager;
import org.eclipse.ui.internal.activities.IObjectActivityManager;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.registry.IPluginContribution;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

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
	 * Singleton instance.
	 */
    private static WorkbenchActivityHelper singleton;

    /**
	 * Get the singleton instance of this class.
	 * 
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
	 * Calls <code>isEnabled(IActivityManager, String)</code> with the Workbench 
     * <code>IActivityManager</code>.
     * 
	 * @param idToMatchAgainst
	 *            the <code>String</code> to match against
	 * @return <code>false</code> if the <code>String</code> matches only
	 *         disabled <code>IActivity</code> objects (based on pattern
	 *         bindings), <code>true</code> otherwise.
     * @see isEnabled(org.eclipse.ui.activities.IActivityManager, java.lang.String)
	 * @since 3.0
	 */
    public static boolean isEnabled(String idToMatchAgainst) {
        return isEnabled(PlatformUI.getWorkbench().getActivityManager(), idToMatchAgainst);
    }

    /**
	 * Determines whether the provided id matches <em>any</em> enabled 
     * <code>IActivity</code> object (based on pattern bindings). If the id does
     * not match any binding, then it is considered enabled.
     * 
	 * @param activityManager
	 *            the <code>IActivityManager</code> to work with
	 * @param idToMatchAgainst
	 *            the <code>String</code> to match against
	 * @return <code>false</code> if the <code>String</code> matches only
	 *         disabled <code>IActivity</code> objects (based on pattern
	 *         bindings), <code>true</code> otherwise.
	 * @since 3.0
	 */
    public static boolean isEnabled(IActivityManager activityManager, String idToMatchAgainst) {
        boolean match = false, enabled = false;
        Set activityIds = activityManager.getDefinedActivityIds();

        for (Iterator iterator = activityIds.iterator(); iterator.hasNext();) {
            IActivity activity = activityManager.getActivity((String) iterator.next());

            if (activity.match(idToMatchAgainst)) {
                match = true;
                if (activity.isEnabled()) {
                    enabled = true;
                    break;
                }
            }
        }

        return match ? enabled : true;
    }

    /**
	 * Create a new <code>WorkbenchActivityHelper</code> which will populate
	 * the various <code>ObjectActivityManagers</code> with Workbench
	 * contributions.
	 */
    private WorkbenchActivityHelper() {
        loadEnabledStates();

        // TODO kim: shouldn't you want to check for any activities (not categories)?        
        boolean noRoles = PlatformUI.getWorkbench().getActivityManager().getDefinedActivityIds().isEmpty();
        
        if (noRoles) {
        	// TODO cast
            IMutableActivityManager activityManager = (IMutableActivityManager) PlatformUI.getWorkbench().getActivityManager();
            activityManager.setEnabledActivityIds(activityManager.getDefinedActivityIds());
        }

        createNewWizardMappings();
    }
    
    /**
	 * Enable all IActivity objects that match the given id.
	 * 
	 * @param id
	 *            the id to match.
	 * @since 3.0
	 */
    public static void enableActivities(String id) {
        // TODO cast
    	IMutableActivityManager activityManager = (IMutableActivityManager) PlatformUI.getWorkbench().getActivityManager();
        Set activities = new HashSet(activityManager.getEnabledActivityIds());
        for (Iterator i = activityManager.getDefinedActivityIds().iterator(); i.hasNext();) {
            String activityId = (String) i.next();
            IActivity activity = activityManager.getActivity(activityId);
            if (activity.match(id)) {
                activities.add(activityId);
            }
        }
        activityManager.setEnabledActivityIds(activities);
    }

    /**
	 * Save the enabled state of all Activities.
	 */
    public void shutdown() {
        saveEnabledStates();
    }

    /**
	 * Create the mappings for the new wizard object activity manager. Objects
	 * of interest in this manager are Strings (wizard IDs).
	 */
    private void createNewWizardMappings() {
        NewWizardsRegistryReader reader = new NewWizardsRegistryReader(false);
        WizardCollectionElement wizardCollection = reader.getWizardElements();
        IObjectActivityManager manager = (/* TODO bad cast */ (Workbench) PlatformUI.getWorkbench()).getObjectActivityManager(IWorkbenchConstants.PL_NEW, true);
        Object[] wizards = flattenWizards(wizardCollection);
        for (int i = 0; i < wizards.length; i++) {
            WorkbenchWizardElement element = (WorkbenchWizardElement) wizards[i];
            manager.addObject(
                element.getConfigurationElement().getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(),
                element.getID(),
                element.getID());

        }
        manager.applyPatternBindings();
    }
    
    /**
     * Utility method to create a String containing the plugin and local ids of 
     * a contribution.
     * 
     * @param contribution the contribution to use.
     * @return the unified id.
     * @since 3.0
     */
    public static final String createUnifiedId(IPluginContribution contribution) {
        if (contribution.fromPlugin()) 
            return contribution.getPluginId() + '/' + contribution.getLocalId();
        return contribution.getLocalId();
    }    
    
    /**
	 * Utility method to create a key/object value from a given view category
	 * ID.
	 * 
	 * @param id
	 * @return the value of id + '*'
	 * @since 3.0
	 */
    public static String createViewCategoryIdKey(String id) {
        return id + '*';
    }

    /**
	 * Take the tree WizardCollecitonElement structure and flatten it into a
	 * list of WorkbenchWizardElements.
	 * 
	 * @param wizardCollection
	 *            the collection to flatten.
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
	 * @param wizardCollection
	 *            the collection to flatten.
	 * @param list
	 *            the list of currently flattened wizards.
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
	 * @param activity
	 *            the activity.
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

        // TODO cast
        IMutableActivityManager activityManager = (IMutableActivityManager) PlatformUI.getWorkbench().getActivityManager();
        
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
        IActivityManager activityManager = PlatformUI.getWorkbench().getActivityManager();
        Iterator values = activityManager.getDefinedActivityIds().iterator();
        while (values.hasNext()) {
            IActivity activity = activityManager.getActivity((String) values.next());

            store.setValue(createPreferenceKey(activity), activity.isEnabled());
        }
    }
}
