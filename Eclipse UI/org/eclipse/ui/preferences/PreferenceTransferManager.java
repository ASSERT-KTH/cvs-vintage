/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.preferences;

import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.registry.PreferenceTransferRegistryReader;

/**
 * Manages preference transfer support for the workbench
 *
 * @since 3.1
 */
public class PreferenceTransferManager {

    /**
     * Return an array of <code>IPreferenceTransfer</code> objects
     * @return an array of <code>IPreferenceTransfer</code> objects
     */
    public static PreferenceTransferElement[] getPreferenceTransfers() {
        return new PreferenceTransferRegistryReader(
                    IWorkbenchConstants.PL_PREFERENCE_TRANSFER)
                    .getPreferenceTransfers();
    }
}
