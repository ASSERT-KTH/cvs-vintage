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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.misc.Assert;


/**
 * This represents an object that can supply trim around a IPresentablePart. 
 * Clients can implement subclasses to provide the appearance for editor workbooks,
 * view folders, fast views, and detached windows.
 * <p>
 * StackPresentations do not store any persistent state and cannot
 * directly make changes to the workbench. They are given an IStackPresentationSite 
 * reference on creation, which allows them to send events and requests to the workbench.
 * However, the workbench is free to ignore these requests. The workbench will call one
 * of the public methods on StackPresentation when (and if) the presentation is expected to 
 * change state. 
 * </p>
 * <p>
 * For example, if the user clicks a button that is intended to close a part, the
 * StackPresentation will send a close request to its site, but should not assume
 * that the part has been closed until the workbench responds with a call 
 * <code>StackPresentation.remove</code>. 
 * </p>
 * 
 * @since 3.0
 */
public abstract class StackPresentation {

	/**
	 * Inactive state. This is the default state for deselected presentations.
	 */
	public static final int AS_INACTIVE = 0;
	
	/**
	 * Activation state indicating that one of the parts in the presentation currently has focus
	 */
	public static final int AS_ACTIVE_FOCUS = 1;
	
	/**
	 * Activation state indicating that none of the parts in the presentation have focus, but
	 * one of the parts is being used as the context for global menus and toolbars
	 */
	public static final int AS_ACTIVE_NOFOCUS = 2;
	
    /**
     * The presentation site.
     */
	private IStackPresentationSite site;
	
	/**
	 * Constructs a new stack presentation with the given site.
	 * 
	 * @param stackSite the stack site
	 */
	protected StackPresentation(IStackPresentationSite stackSite) {
	    Assert.isNotNull(stackSite);
	    site = stackSite;
	}
	
	/**
	 * Returns the presentation site (not null).
	 */
	protected IStackPresentationSite getSite() {
	    return site;
	}
	
	/**
	 * Sets the bounding rectangle for this presentation. 
	 * 
	 * @param bounds new bounding rectangle (not null)
	 */
	public abstract void setBounds(Rectangle bounds);
	
	/**
	 * Returns the minimum size for this stack. The stack is prevented
	 * from being resized smaller than this amount, and this is used as
	 * the default size for the stack when it is minimized. Typically,
	 * this is the amount of space required to fit the minimize, close,
	 * and maximize buttons and one tab. 
	 * 
	 * @return the minimum size for this stack (not null)
	 */
	public abstract Point computeMinimumSize();
	
	/**
	 * Disposes all SWT resources being used by the stack. This is the
	 * last method that will be invoked on the stack. 
	 */
	public abstract void dispose();

	/**
	 * This is invoked to notify the presentation that its activation
	 * state has changed. StackPresentations can have three possible activation
	 * states (see the AS_* constants above)
	 * 
	 * @param isActive one of AS_INACTIVE, AS_ACTIVE, or AS_ACTIVE_NOFOCUS
	 */
	public abstract void setActive(int newState);
	
	/**
	 * This causes the presentation to become visible or invisible. 
	 * When a presentation is invisible, it must not respond to user
	 * input or modify its parts. For example, a presentations will 
	 * be made invisible if it belongs to a perspective and the user
	 * switches to another perspective.
	 * 
	 * @since 3.0
	 */
	public abstract void setVisible(boolean isVisible);
	
	/**
	 * Sets the state of the presentation. That is, notifies the presentation
	 * that is has been minimized, maximized, or restored. Note that this method
	 * is the only way that a presentation is allowed to change its state.
	 * <p>
	 * If a presentation wishes to minimize itself, it must call setState
	 * on its associated IPresentationSite. If the site chooses to respond
	 * to the state change, it will call this method at the correct time.
	 * The presentation should not call this method directly. 
	 * </p>
	 * 
	 * @param state one of the IPresentationSite.STATE_* constants.
	 */
	public abstract void setState(int state);
	
	/**
	 * Returns the control for this presentation
	 * 
	 * @return the control for this presentation (not null)
	 */
	public abstract Control getControl();	
	
	/**
	 * Adds the given part to the stack. The presentations' interpretation of the
	 * part order must be consistent. For example,
	 * {addPart(a, null); addPart(b, null); addPart(c, null);} should have exactly 
	 * the same effect as {addPart(c, null); addPart(a, c); addPart(b, c);}; 
	 * 
	 * @param newPart the new part to add (not null)
	 * @param position the position to insert the part. The new part will
	 * occupy the tab location currently occupied by the "position" part, and the
	 * "position" part will be moved to a new location. If null, the tab should be
	 * last position.
	 */
	public abstract void addPart(IPresentablePart newPart, IPresentablePart position);
	
	/**
	 * Removes the given part from the stack.
	 * 
	 * @param oldPart the part to remove (not null)
	 */
	public abstract void removePart(IPresentablePart oldPart);
	
	/**
	 * Brings the specified part to the foreground. This should not affect
	 * the current focus.
	 * 
	 * @param toSelect the new active part (not null)
	 */
	public abstract void selectPart(IPresentablePart toSelect);
		
	/**
	 * This method is invoked whenever a part is dragged over the stack's control.
	 * It returns a StackDropResult if and only if the part may be dropped in this
	 * location.
	 *
	 * @param currentControl the control being dragged over
	 * @param location cursor location (display coordinates)
	 * @return a StackDropResult or null if the presentation does not have
	 * a drop target in this location.
	 */
	public abstract StackDropResult dragOver(Control currentControl, Point location);
	
	/**
	 * Instructs the presentation to display the system menu
	 *
	 */
	public abstract void showSystemMenu();

	/**
	 * Instructs the presentation to display the pane menu 
	 */
	public abstract void showPaneMenu();

	/**
	 * Instructs the presentation to display a list of all parts in the stack, and
	 * allow the user to change the selection using the keyboard.
	 */
	public void showPartList() {
		
	}
	
	/**
	 * Returns the tab-key traversal order for the given <code>IPresentablePart</code>.
	 * 
	 * @param part the part
	 * @return the tab-key traversal order
	 */
	public abstract Control[] getTabList(IPresentablePart part);
}
