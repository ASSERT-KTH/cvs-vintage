package org.eclipse.ui.internal;

/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */

import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Activates the most recently used editor in the current window.
 */
public class ActivateEditorAction extends PageEventAction {

/**
 * Creates an ActivateEditorAction.
 */
protected ActivateEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ActivateEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ActivateEditorAction.toolTip")); //$NON-NLS-1$
	setAccelerator(SWT.F12);
	window.getPartService().addPartListener(this);
	updateState();
	WorkbenchHelp.setHelp(this, IHelpContextIds.ACTIVATE_EDITOR_ACTION);
}

public void pageActivated(IWorkbenchPage page) {
	super.pageActivated(page);
	updateState();
}

public void pageClosed(IWorkbenchPage page) {
	super.pageClosed(page);
	updateState();
}

public void partOpened(IWorkbenchPart part) {
	super.partOpened(part);
	if (part instanceof IEditorPart) {
		updateState();
	}
}

public void partClosed(IWorkbenchPart part) {
	super.partClosed(part);
	if (part instanceof IEditorPart) {
		updateState();
	}
}

/**
 * @see Action#run()
 */
public void run() {
	IWorkbenchPage page = getActivePage();
	if (page != null) {
		IEditorPart editor = page.getActiveEditor(); // may not actually be active
		if (editor != null) {
			page.activate(editor);
		}
	}
}

/**
 * Updates the enabled state.
 */
public void updateState() {
	WorkbenchPage page = (WorkbenchPage)getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	setEnabled(page.getSortedEditors().length >= 1);
}
 
}

