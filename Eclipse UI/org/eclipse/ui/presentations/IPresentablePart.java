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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This is a skin's interface to the contents of a view or editor. Note that this
 * is essentially the same as IWorkbenchPart, except it does not provide access
 * to lifecycle events and allows repositioning of the part.
 * 
 * Not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IPresentablePart {
	
	/**
	 * The property id for <code>isDirty</code>.
	 */
	public static final int PROP_DIRTY = IEditorPart.PROP_DIRTY;

	/**
	 * The property id for <code>getEditorInput</code>.
	 */
	public static final int PROP_INPUT = IEditorPart.PROP_INPUT;

	/**
	 * The property id for <code>getTitle</code>, <code>getTitleImage</code>
	 * and <code>getTitleToolTip</code>.
	 */
	public static final int PROP_TITLE = IWorkbenchPart.PROP_TITLE;
	
	/**
	 * The property id for <code>isBusy</code>.
	 */
	public static final int PROP_BUSY = 0x92;
		
	/**
	 * The property id for toolbar changes
	 */
	public static final int PROP_TOOLBAR = 0x93;
	
	/**
	 * The property id for pane menu changes
	 */
	public static final int PROP_PANE_MENU = 0x302;
	
	/**
	 * Sets the bounds of this part.
	 *  
	 * @param bounds
	 */
	public void setBounds(Rectangle bounds);
	
	/**
	 * Notifies the part whether or not it is visible in the current
	 * perspective. A part is visible iff any part of its widgetry can
	 * be seen.
	 * 
	 * @param isVisible true if the part has just become visible, false
	 * if the part has just become hidden
	 */
	public void setVisible(boolean isVisible);
	
	/**
	 * Forces this part to have focus.
	 */
	public void setFocus();
	
	/**
	 * Adds a listener for changes to properties of this workbench part.
	 * Has no effect if an identical listener is already registered.
	 * <p>
	 * The properties ids are as follows:
	 * <ul>
	 *   <li><code>PROP_TITLE</code> </li>
	 *   <li><code>PROP_INPUT</code> </li>
	 *   <li><code>PROP_DIRTY</code> </li>
	 *   <li><code>PROP_TOOLBAR</code> </li>
	 * </ul>
	 * </p>
	 *
	 * @param listener a property listener
	 */
	public void addPropertyListener(IPropertyListener listener);
	
	/**
	 * Remove a listener that was previously added using addPropertyListener.
	 *
	 * @param listener a property listener
	 */
	public void removePropertyListener(IPropertyListener listener);
	
	/**
	 * Returns the short name of the part. This is used as the text on
	 * the tab when this part is stacked on top of other parts.
	 * 
	 * @return the short name of the part
	 */
	public String getName();
	
	/**
	 * Returns the title of this workbench part. If this value changes 
	 * the part must fire a property listener event with 
	 * <code>PROP_TITLE</code>.
	 * <p>
	 * The title is used to populate the title bar of this part's visual
	 * container.  
	 * </p>
	 *
	 * @return the workbench part title
	 */
	public String getTitle();
	
	/**
	 * Returns the title image of this workbench part.  If this value changes 
	 * the part must fire a property listener event with 
	 * <code>PROP_TITLE</code>.
	 * <p>
	 * The title image is usually used to populate the title bar of this part's
	 * visual container. Since this image is managed by the part itself, callers
	 * must <b>not</b> dispose the returned image.
	 * </p>
	 *
	 * @return the title image
	 */
	public Image getTitleImage();
	
	/**
	 * Returns the title tool tip text of this workbench part. If this value 
	 * changes the part must fire a property listener event with 
	 * <code>PROP_TITLE</code>.
	 * <p>
	 * The tool tip text is used to populate the title bar of this part's 
	 * visual container.  
	 * </p>
	 *
	 * @return the workbench part title tool tip
	 */
	public String getTitleToolTip();
	
	/**
	 * Returns true iff the contents of this part have changed recently. For
	 * editors, this indicates that the part has changed since the last save.
	 * For views, this indicates that the view contains interesting changes
	 * that it wants to draw the user's attention to.
	 * 
	 * @return true iff the part is dirty
	 */
	public boolean isDirty();
	
	/**
	 * Return true if the the receiver is currently in a busy state.
	 * @return boolean true if busy
	 */
	public boolean isBusy();

	/**
	 * Returns the local toolbar for this part, or null if this part does not
	 * have a local toolbar. Callers must not dispose or downcast the return value.
	 * 
	 * @return the local toolbar for the part, or null if none
	 */
	public Control getToolBar();
	
	/**
	 * Returns the menu for this part or null if none
	 * 
	 * @return the menu for this part or null if none
	 */
	public IPartMenu getMenu();
	
}
