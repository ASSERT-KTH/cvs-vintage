/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.presentations;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Point;


/**
 * Represents the main interface between an StackPresentation and the workbench.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IStackPresentationSite {
	public static int STATE_MINIMIZED = 0;
	public static int STATE_MAXIMIZED = 1;
	public static int STATE_RESTORED = 2;

	/**
	 * Sets the state of the container. Called by the presentation when the
	 * user causes the the container to be minimized, maximized, etc.
	 * 
	 * @param newState one of the STATE_* constants
	 */
	public void setState(int newState);
	
	/**
	 * Returns the current state of the site (one of the STATE_* constants)
	 * 
	 * @return the current state of the site (one of the STATE_* constants)
	 */
	public int getState();

	/**
	 * Returns true iff the site supports the given state
	 * 
	 * @param state one of the STATE_* constants, above
	 * @return true iff the site supports the given state
	 */
	public boolean supportsState(int state);
	
	/**
	 * Begins dragging the given part
	 * 
	 * @param beingDragged the part to drag (not null)
	 * @param initialPosition the mouse position at the time of the initial mousedown 
	 * (display coordinates, not null)
	 * @param keyboard true iff the drag was initiated via mouse dragging,
	 * and false if the drag may be using the keyboard
	 */
	public void dragStart(IPresentablePart beingDragged, Point initialPosition, boolean keyboard);
	
	/**
	 * Closes the given part.
	 * 
	 * @param toClose the part to close (not null)
	 */
	public void close(IPresentablePart toClose);
	
	/**
	 * Begins dragging the entire stack of parts
	 * 
	 * @param initialLocation the mouse position at the time of the initial mousedown (display coordinates, 
	 * not null)
	 * @param keyboard true iff the drag was initiated via mouse dragging,
	 * and false if the drag may be using the keyboard	 
	 */
	public void dragStart(Point initialPosition, boolean keyboard);

	/**
	 * Returns true iff this site will allow the given part to be closed
	 * 
	 * @param toClose part to test (not null)
	 * @return true iff the part may be closed
	 */
	public boolean isCloseable(IPresentablePart toClose);
	
	/**
	 * Returns true iff the given part can be dragged. If this
	 * returns false, the given part should not trigger a drag.
	 * 
	 * @param toMove part to test (not null)
	 * @return true iff this part is a valid drag source
	 */
	public boolean isPartMoveable(IPresentablePart toMove);
	
	/**
	 * Returns true iff this entire stack can be dragged
	 * 
	 * @return tre iff the stack can be dragged
	 */
	public boolean isStackMoveable();
	
	/**
	 * Returns true iff this site will allow the given part to be moved.
	 * If the argument is null, this returns whether dragging should
	 * be enabled for the entire stack
	 *
	 * @deprecated use isPartMoveable(...) or isStackMoveable() instead
	 *
	 * @param toMove part to test, or null if we're testing the entire stack
	 * @return true iff the part may be moved
	 */
	public boolean isMoveable(IPresentablePart toMove);
	
	/**
	 * Makes the given part active
	 * 
	 * @param toSelect
	 */
	public void selectPart(IPresentablePart toSelect);
	
	/**
	 * Returns the currently selected part or null if the stack is empty 
	 * 
	 * @return the currently selected part or null if the stack is empty
	 */
	public IPresentablePart getSelectedPart();
	
	/**
	 * Adds system actions to the given menu manager. The site may
	 * make use of the following group ids:
	 * <ul>
	 * <li><code>close</code>, for close actions</li>
	 * <li><code>size</code>, for resize actions</li>
	 * <li><code>misc</code>, for miscellaneous actions</li>
	 * </ul>
	 * The presentation can control the insertion position by creating
	 * these group IDs where appropriate. 
	 * 
	 * @param menuManager the menu manager to populate
	 */
	public void addSystemActions(IMenuManager menuManager);
	
}
