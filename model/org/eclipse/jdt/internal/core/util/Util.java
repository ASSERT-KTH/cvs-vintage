/**********************************************************************
 * Copyright (c) 2000, 2001, 2002 IBM Corp. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *********************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Provides convenient utility methods to other types in this package.
 */
public class Util {
	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName =
		"org.eclipse.jdt.internal.core.util.messages";	//$NON-NLS-1$

	static {
			/**
			 * Creates a NLS catalog for the given locale.
			 */
			bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	}

	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id) {
		if (id == null)
			return "No message available"; //$NON-NLS-1$
		String message = null;
		try {
			message = bundle.getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
		}
		// for compatibility with MessageFormat which eliminates double quotes in original message
		char[] messageWithNoDoubleQuotes =
			CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
		return new String(messageWithNoDoubleQuotes);
	}
}