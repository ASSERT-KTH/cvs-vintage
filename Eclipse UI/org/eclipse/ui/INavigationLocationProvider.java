/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui;

/**
 * 2.1 - WORK_IN_PROGRESS do not use.
 */
public interface INavigationLocationProvider {
	
	/**
	 * Creates a navigation location describing the current state.	 * @return NavigationLocation	 */
	NavigationLocation createNavigationLocation();
}
