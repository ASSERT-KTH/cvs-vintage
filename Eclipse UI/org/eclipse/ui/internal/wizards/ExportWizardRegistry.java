/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * Registry that contains wizards contributed via the <code>exportWizards</code>
 * extension point.
 * 
 * @since 3.1
 */
public class ExportWizardRegistry extends AbstractExtensionWizardRegistry {

	private static ExportWizardRegistry singleton;
	
	/**
	 * Return the singleton instance of this class.
	 * 
	 * @return the singleton instance of this class
	 */
	public static synchronized ExportWizardRegistry getInstance() {		
		if (singleton == null) {
			singleton = new ExportWizardRegistry();
		}
		return singleton;
	}
	
	/**
	 * 
	 */
	public ExportWizardRegistry() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getExtensionPoint()
	 */
	protected String getExtensionPoint() {
		return IWorkbenchConstants.PL_EXPORT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getPlugin()
	 */
	protected String getPlugin() {
		return PlatformUI.PLUGIN_ID;
	}
}
