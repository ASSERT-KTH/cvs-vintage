/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.activities;

import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * An instance of this interface provides support for managing 
 * <code>IWorkbench</code> activities.  An instance of this interface may be
 * obtained via {@link org.eclipse.ui.IWorkbench#getActivitySupport()}. 
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *  
 * @since 3.0
 */
public interface IWorkbenchActivitySupport {

    /**
     * Returns the activity manager for the workbench.
     * 
     * @return the activity manager for the workbench. Guaranteed not to be
     *         <code>null</code>.
     */
    IActivityManager getActivityManager();

    /**
     * Sets the set of identifiers to enabled activities.
     * 
     * @param enabledActivityIds
     *            the set of identifiers to enabled activities. This set may be
     *            empty, but it must not be <code>null</code>. If this set
     *            is not empty, it must only contain instances of <code>String</code>.
     */
    void setEnabledActivityIds(Set enabledActivityIds);
    
    /**
     * Return the image associated with this activity.
     * 
     * @param activity the activity
     * @return the image associated with this activity.  Never <code>null</code>.
     * @since 3.1
     */
    ImageDescriptor getImageDescriptor(IActivity activity);

    /**
     * Return the image associated with this category.
     * 
     * @param category the category
     * @return the image associated with this category.  Never <code>null</code>.
     * @since 3.1
     */
    ImageDescriptor getImageDescriptor(ICategory category);
	
	/**
	 * Return the trigger point manager for this instance.
	 * 
	 * @return the trigger point manager.  Never <code>null</code>.
	 * @since 3.1
	 */
	ITriggerPointManager getTriggerPointManager();
}