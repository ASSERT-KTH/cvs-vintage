/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Try to quit the application.
 */
public class QuitAction extends Action implements ActionFactory.IWorkbenchAction {

	/**
	 * Whether this action has been disposed yet.
	 */
	private boolean disposed = false;

	/**
	 * Creates a new <code>QuitAction</code>. The action is initialized from
	 * the <code>JFaceResources</code> bundle.
	 */
	public QuitAction() {
		setText(WorkbenchMessages.getString("Exit.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("Exit.toolTip")); //$NON-NLS-1$
		setId("quit"); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.QUIT_ACTION);
	}

	/*
	 * (non-Javadoc) Method declared on ActionFactory.IWorkbenchAction.
	 */
	public void dispose() {
		disposed = true;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void run() {
		if (disposed) {
			return;
		}

		PlatformUI.getWorkbench().close();
	}

}
