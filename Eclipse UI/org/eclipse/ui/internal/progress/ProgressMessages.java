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
package org.eclipse.ui.internal.progress;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * ProgressMessages is the class for the externalized strings for 
 * the progress support.
 */
public class ProgressMessages {

	private static final String BUNDLE_NAME = "org.eclipse.ui.internal.progress.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE =
		ResourceBundle.getBundle(BUNDLE_NAME);

	private ProgressMessages() {
		//prevent instantiation of class
	}
	/**
	 * Returns the resource object with the given key in
	 * the resource bundle. If there isn't any value under
	 * the given key, the key is returned.
	 *
	 * @param key the resource name
	 * @return the string
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}
}
