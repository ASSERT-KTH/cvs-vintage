package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

/**
 * The abstract superclass for actions that listen to page activation and
 * open/close events. This implementation tracks the active page (see 
 * <code>getActivePage</code>) and provides a convenient place to monitor
 * page lifecycle events that could affect the availability of the action.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPartListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>partActivated</code></li> 
 *   <li><code>partDeactivated</code></li>
 *   <li><code>partOpened</code></li>
 *   <li><code>partClosed</code></li>
 *   <li><code>partBroughtToTop</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPageListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>pageActivated</code></li> 
 *   <li><code>pageClosed</code></li>
 *   <li><code>pageOpened</code></li>
 * </ul>
 * </p>
 */
public abstract class PageEventAction extends PartEventAction
	implements IPageListener
{
	/**
	 * The active page, or <code>null</code> if none.
	 */
	private IWorkbenchPage activePage;

	/**
	 * The workbench window this action is registered with.
	 */
	 private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new action with the given text. Register this
 * action with the workbench window for page lifecycle
 * events.
 *
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if there is no text
 * @param window the workbench window this action is
 *   registered with.
 */
protected PageEventAction(String text, IWorkbenchWindow window) {
	super(text);
	Assert.isNotNull(window);
	this.workbenchWindow = window;
	this.activePage = window.getActivePage();
	this.workbenchWindow.addPageListener(this);
}
/**
 * Returns the currently active page in the workbench window.
 *
 * @return currently active page in the workbench window, or <code>null</code> if none
 */
public IWorkbenchPage getActivePage() {
	return activePage;
}
/**
 * Returns the workbench window this action applies to.
 *
 * @return the workbench window
 */
public IWorkbenchWindow getWorkbenchWindow() {
	return workbenchWindow;
}
/**
 * The <code>PageEventAction</code> implementation of this 
 * <code>IPageListener</code> method records that the given page is active.
 * Subclasses may extend this method if action availability has to be
 * recalculated.
 */
public void pageActivated(IWorkbenchPage page) {
	this.activePage = page;
}
/**
 * The <code>PageEventAction</code> implementation of this 
 * <code>IPageListener</code> method clears the active page if it just closed.
 * Subclasses may extend this method if action availability has to be
 * recalculated.
 */
public void pageClosed(IWorkbenchPage page) {
	if (page == activePage)
		activePage = null;
}
/**
 * The <code>PageEventAction</code> implementation of this 
 * <code>IPageListener</code> method does nothing. Subclasses should extend
 * this method if action availability has to be recalculated.
 */
public void pageOpened(IWorkbenchPage page) {
}
}
