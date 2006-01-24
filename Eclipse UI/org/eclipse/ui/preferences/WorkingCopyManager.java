/*******************************************************************************
 * Copyright (c) 2005,2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.internal.preferences.WorkingCopyPreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * WorkingCopyManager is a concrete implementation of an
 * IWorkingCopyManager.
 * <p>
 * This class is not intended to be sub-classed by clients.
 * </p>
 * @since 3.2
 */
public class WorkingCopyManager implements IWorkingCopyManager{

	// all working copies - maps absolute path to PreferencesWorkingCopy instance
	private Map workingCopies = new HashMap();

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.preferences.IWorkingCopyManager#getWorkingCopy(org.eclipse.core.runtime.preferences.IEclipsePreferences)
	 */
	public IEclipsePreferences getWorkingCopy(IEclipsePreferences original) {
		if (original instanceof WorkingCopyPreferences)
			throw new IllegalArgumentException("Trying to get a working copy of a working copy"); //$NON-NLS-1$
		String absolutePath = original.absolutePath();
		IEclipsePreferences preferences = (IEclipsePreferences) workingCopies.get(absolutePath);
		if (preferences == null) {
			preferences = new WorkingCopyPreferences(original, this);
			workingCopies.put(absolutePath, preferences);
		}
		return preferences;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.preferences.IWorkingCopyManager#applyChanges()
	 */
	public void applyChanges() throws BackingStoreException {
		for (Iterator i = workingCopies.values().iterator(); i.hasNext();)
			((WorkingCopyPreferences) i.next()).flush();
	}

}
