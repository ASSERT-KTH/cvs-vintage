package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 *	Closes all active editors
 */
public class CloseAllAction extends PartEventAction implements IPageListener {
	private IWorkbenchWindow workbench;	
/**
 *	Create an instance of this class
 */
public CloseAllAction(IWorkbenchWindow aWorkbench) {
	super(WorkbenchMessages.getString("CloseAllAction.text")); //$NON-NLS-1$
	this.workbench = aWorkbench;
	setToolTipText(WorkbenchMessages.getString("CloseAllAction.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	setId(IWorkbenchActionConstants.CLOSE_ALL);
	updateState();
	aWorkbench.addPageListener(this);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_ACTION);
	setAccelerator(SWT.CTRL | SWT.SHIFT | SWT.F4);
}
/**
 * Notifies this listener that the given page has been activated.
 *
 * @param page the page that was activated
 * @see IWorkbenchWindow#setActivePage
 */
public void pageActivated(org.eclipse.ui.IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies this listener that the given page has been closed.
 *
 * @param page the page that was closed
 * @see IWorkbenchPage#close
 */
public void pageClosed(org.eclipse.ui.IWorkbenchPage page) {
	updateState();
}
/**
 * Notifies this listener that the given page has been opened.
 *
 * @param page the page that was opened
 * @see IWorkbenchWindow#openPage
 */
public void pageOpened(org.eclipse.ui.IWorkbenchPage page) {}
/**
 * A part has been closed.
 */
public void partClosed(IWorkbenchPart part) {
	updateState();
}
/**
 * A part has been opened.
 */
public void partOpened(IWorkbenchPart part) {	
	updateState();
}
/**
 *	The user has invoked this action
 */
public void run() {
	IWorkbenchPage page = workbench.getActivePage();
	if (page != null)
		page.closeAllEditors(true);
}
/**
 * Enable the action if there at least one editor open.
 */
private void updateState() {
	WorkbenchPage page = (WorkbenchPage)workbench.getActivePage();
	if (page != null) {
		setEnabled(page.getSortedEditors().length >= 1);
	} else {
		setEnabled(false);
	}
}
}
