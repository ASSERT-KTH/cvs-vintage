/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExtensionNode;
import org.eclipse.ui.internal.registry.CategorizedPageRegistryReader;

/**
 * A proxy for a preference page to avoid creation of preference page just to
 * show a node in the preference dialog tree.
 */
public class WorkbenchPreferenceNode extends WorkbenchPreferenceExtensionNode {

	/**
	 * The attribute for class name.
	 */
	public final static String ATT_CONTRIBUTOR_CLASS = "class"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver.
	 * @param nodeId
	 * @param element
	 */
	public WorkbenchPreferenceNode(String nodeId, IConfigurationElement element) {
		super(nodeId, element);
	}

	/**
	 * Creates the preference page this node stands for.
	 */
	public void createPage() {
		IWorkbenchPreferencePage page;
		try {
			page = (IWorkbenchPreferencePage) WorkbenchPlugin.createExtension(
					getConfigurationElement(), ATT_CONTRIBUTOR_CLASS);
		} catch (CoreException e) {
			// Just inform the user about the error. The details are
			// written to the log by now.
			ErrorDialog.openError((Shell) null, WorkbenchMessages
					.getString("PreferenceNode.errorTitle"), //$NON-NLS-1$
					WorkbenchMessages.getString("PreferenceNode.errorMessage"), //$NON-NLS-1$
					e.getStatus());
			page = new ErrorPreferencePage();
		}

		page.init(PlatformUI.getWorkbench());
		if (getLabelImage() != null)
			page.setImageDescriptor(getImageDescriptor());
		page.setTitle(getLabelText());
		setPage(page);
	}

	/**
	 * Return the category name for the node.
	 * @return java.lang.String
	 */
	public String getCategory() {
		return getConfigurationElement().getAttribute(
				CategorizedPageRegistryReader.ATT_CATEGORY);
	}	
}