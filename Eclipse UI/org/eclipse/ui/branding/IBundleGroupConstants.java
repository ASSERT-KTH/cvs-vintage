/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.branding;

/**
 * These constants define the set of properties that the UI expects to
 * be available via <code>IBundleGroup.getProperty(String)</code>.
 * 
 * @since 3.0
 * @see org.eclipse.core.runtime.IBundleGroup#getProperty(String)
 */
public interface IBundleGroupConstants {
	/**
	 * An image which can be shown in an "about features" dialog (32x32).
	 */
	public static final String FEATURE_IMAGE = "featureImage"; //$NON-NLS-1$

	/**
	 * A help reference for the feature's tips and tricks page (optional).
	 */
	public static final String TIPS_AND_TRICKS_HREF = "tipsAndTricksHref"; //$NON-NLS-1$

	/**
	 * A URL for the feature's welcome page (special XML-based format)
     * ($nl$/ prefix to permit locale-specific translations of entire file).
	 * Products designed to run "headless" typically would not have such a page.
	 */
	public static final String WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$

	/**
	 * The id of a perspective in which to show the welcome page
	 * (optional).
	 */
	public static final String WELCOME_PERSPECTIVE = "welcomePerspective"; //$NON-NLS-1$
	
	/**
	 * The URL of the license page for the feature (optional).
	 */
	public static final String LICENSE_HREF = "licenseHref"; //$NON-NLS-1$
}
