/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.roles;

import java.util.EventObject;

/**
 * Event generated by an Activity change.  Contains a reference to the changed
 * activity (but not an itemized description of what has changed).
 */
public class ActivityEvent extends EventObject {

	/**
	 * @param activity
	 */
	public ActivityEvent(Activity activity) {
        super(activity);
	}
    
    /**
     * @return the source Activity
     */
    public Activity getActivity() {
        return (Activity) getSource();
    }
}
