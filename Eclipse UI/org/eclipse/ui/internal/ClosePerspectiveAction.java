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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * The <code>ClosePerspectiveAction</code> is used to close the
 * active perspective in the workbench window's active page.
 */
public class ClosePerspectiveAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of <code>ClosePerspectiveAction</code>
     * 
     * @param window the workbench window this action applies to
     */
    public ClosePerspectiveAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("ClosePerspectiveAction.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.closePerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages
                .getString("ClosePerspectiveAction.toolTip")); //$NON-NLS-1$
        setEnabled(false);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.CLOSE_PAGE_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page != null) {
            Perspective persp = ((WorkbenchPage) page).getActivePerspective();
            if (persp != null) {
                closePerspective((WorkbenchPage) page, persp);
            }
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // already disposed
            return;
        }
        workbenchWindow = null;
    }

    /**
     * Close the argument perspective in the argument page.  Do nothing if the page or
     * perspective are null.
     * 
     * @param page the page
     * @param persp the perspective
	 * @since 3.1
     */
    public static void closePerspective(WorkbenchPage page, Perspective persp) {
        if (page != null && persp != null)
            page.closePerspective(persp, true, true);
    }
}
