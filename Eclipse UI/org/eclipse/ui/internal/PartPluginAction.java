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
package org.eclipse.ui.internal;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;

/**
 * This class adds to the PluginAction support by
 * setting itself up for work within a WorkbenchPart.
 * The main difference is that it is capable of
 * processing local selection changes within a part.
 */
public class PartPluginAction extends PluginAction {
	/**
	 * PartPluginAction constructor.
	 *
	 */
	public PartPluginAction(IConfigurationElement actionElement, String runAttribute, String definitionId, int style) {
		super(actionElement, runAttribute, definitionId, style);
	}
	/**
	 * Registers this action as a listener of the workbench part.
	 */
	protected void registerSelectionListener(IWorkbenchPart aPart) {
		ISelectionProvider selectionProvider = aPart.getSite().getSelectionProvider();
		if (selectionProvider != null) {
			selectionProvider.addSelectionChangedListener(this);
			selectionChanged(selectionProvider.getSelection());
		}
	}
	/**
	 * Unregisters this action as a listener of the workbench part.
	 */
	protected void unregisterSelectionListener(IWorkbenchPart aPart) {
		ISelectionProvider selectionProvider = aPart.getSite().getSelectionProvider();
		if (selectionProvider != null) {
			selectionProvider.removeSelectionChangedListener(this);
		}
	}
}
