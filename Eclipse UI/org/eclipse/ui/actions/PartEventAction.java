package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.Action;

/**
 * The abstract superclass for actions that listen to part activation and
 * open/close events. This implementation tracks the active part (see 
 * <code>getActivePart</code>) and provides a convenient place to monitor
 * part lifecycle events that could affect the availability of the action.
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
 */
public abstract class PartEventAction extends Action implements IPartListener {

	/**
	 * The active part, or <code>null</code> if none.
	 */
	private IWorkbenchPart activePart;
/**
 * Creates a new action with the given text.
 *
 * @param text the string used as the text for the action, 
 *   or <code>null</code> if there is no text
 */
protected PartEventAction(String text) {
	super(text);
}
/**
 * Returns the currently active part in the workbench.
 *
 * @return currently active part in the workbench, or <code>null</code> if none
 */
public IWorkbenchPart getActivePart() {
	return activePart;
}
/**
 * The <code>PartEventAction</code> implementation of this 
 * <code>IPartListener</code> method records that the given part is active.
 * Subclasses may extend this method if action availability has to be
 * recalculated.
 */
public void partActivated(IWorkbenchPart part) {
	activePart = part;
}
/**
 * The <code>PartEventAction</code> implementation of this 
 * <code>IPartListener</code> method does nothing. Subclasses should extend
 * this method if action availability has to be recalculated.
 */
public void partBroughtToTop(IWorkbenchPart part) {
}
/**
 * The <code>PartEventAction</code> implementation of this 
 * <code>IPartListener</code> method clears the active part if it just closed.
 * Subclasses may extend this method if action availability has to be
 * recalculated.
 */
public void partClosed(IWorkbenchPart part) {
	if (part == activePart)
		activePart = null;
}
/**
 * The <code>PartEventAction</code> implementation of this 
 * <code>IPartListener</code> method records that there is no active part.
 * Subclasses may extend this method if action availability has to be
 * recalculated.
 */
public void partDeactivated(IWorkbenchPart part) {
	activePart = null;
}
/**
 * The <code>PartEventAction</code> implementation of this 
 * <code>IPartListener</code> method does nothing. Subclasses should extend
 * this method if action availability has to be recalculated.
 */
public void partOpened(IWorkbenchPart part) {
}
}
