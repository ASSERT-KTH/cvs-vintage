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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.PerspectiveMenu;

/**
 * Change the perspective of the active page in the window
 * to the selected one.
 */
public class ChangeToPerspectiveMenu extends PerspectiveMenu {

	/**
	 * Constructor for ChangeToPerspectiveMenu.
	 * 
	 * @param window the workbench window this action applies to.
	 */
	public ChangeToPerspectiveMenu(IWorkbenchWindow window, String id) {
		super(window, id);
		showActive(true);
	}

	/* (non-Javadoc)
	 * @see PerspectiveMenu#run(IPerspectiveDescriptor)
	 */
	protected void run(IPerspectiveDescriptor desc) {
		IPreferenceStore store = PlatformUI.getWorkbench().getPreferenceStore();
		int mode = store.getInt(IPreferenceConstants.OPEN_PERSP_MODE);
		IWorkbenchPage page = getWindow().getActivePage();
		IPerspectiveDescriptor persp = null;
		if (page != null)
			persp = page.getPerspective();
		
		// Only open a new window if user preference is set and the window
		// has an active perspective.
		if (IPreferenceConstants.OPM_NEW_WINDOW == mode && persp != null) {
			try {
				IWorkbench workbench = getWindow().getWorkbench();
				IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
				workbench.openWorkbenchWindow(desc.getId(), input);
			} catch (WorkbenchException e) {
				handleWorkbenchException(e);
			}
		} else {
			if (page != null) {
				page.setPerspective(desc);
			} else {
				try {
					IWorkbench workbench = getWindow().getWorkbench();
					IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
					getWindow().openPage(desc.getId(), input);
				} catch(WorkbenchException e) {
					handleWorkbenchException(e);
				}
			}
		}
	}
	
	/**
	 * Handles workbench exception
	 */
	private void handleWorkbenchException(WorkbenchException e) {
		ErrorDialog.openError(
			getWindow().getShell(),
			WorkbenchMessages.getString("ChangeToPerspectiveMenu.errorTitle"), //$NON-NLS-1$,
			e.getMessage(),
			e.getStatus());
	}
}
