/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.application;

import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Interface providing special access for configuring workbench windows.
 * <p>
 * Window configurer objects are in 1-1 correspondence with the workbench
 * windows they configure. Clients may use <code>get/setData</code> to
 * associate arbitrary state with the window configurer object.
 * </p>
 * <p>
 * Note that these objects are only available to the main application
 * (the plug-in that creates and owns the workbench).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IWorkbenchConfigurer#getWorkbenchWindowConfigurer
 * @see WorkbenchAdvisor#preWindowOpen
 * @since 3.0
 */
public interface IWorkbenchWindowConfigurer {
	/**
	 * Returns the underlying workbench window.
	 * 
	 * @return the workbench window
	 */
	public IWorkbenchWindow getWindow();
	
	/**
	 * Returns the workbench configurer.
	 * 
	 * @return the workbench configurer
	 */
	public IWorkbenchConfigurer getWorkbenchConfigurer();
	
	/**
	 * Returns the action bar configurer for this workbench
	 * window.
	 * 
	 * @return the action bar configurer
	 */
	public IActionBarConfigurer getActionBarConfigurer();
	
	/**
	 * Returns the title of the underlying workbench window.
	 * 
	 * @return the window title
	 */
	public String getTitle();
		
	/**
	 * Sets the title of the underlying workbench window.
	 * <p>
	 * Note that the window can have a title even if the window's title bar
	 * is not visible.
	 * </p>
	 * 
	 * @param title the window title
	 * @see #showTitleBar
	 */
	public void setTitle(String title);
	
	/**
	 * Returns whether the underlying workbench window has a title bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences#SHOULD_SHOW_TITLE_BAR IWorkbenchPreferences.SHOULD_SHOW_TITLE_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public boolean getShowTitleBar();

	/**
	 * Sets whether the underlying workbench window has a title bar.
	 * 
	 * @param show <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public void setShowTitleBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a title bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences#SHOULD_SHOW_MENU_BAR IWorkbenchPreferences.SHOULD_SHOW_MENU_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a title bar, and <code>false</code>
	 * for no title bar
	 */
	public boolean getShowMenuBar();

	/**
	 * Sets whether the underlying workbench window has a menu bar.
	 * 
	 * @param show <code>true</code> for a menu bar, and <code>false</code>
	 * for no menu bar
	 */
	public void setShowMenuBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a tool bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_TOOL_BAR SHOULD_SHOW_TOOL_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a tool bar, and <code>false</code>
	 * for no tool bar
	 */
	public boolean getShowToolBar();

	/**
	 * Sets whether the underlying workbench window has a tool bar.
	 * 
	 * @param show <code>true</code> for a tool bar, and <code>false</code>
	 * for no tool bar
	 */
	public void setShowToolBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a shortcut bar.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_SHORTCUT_BAR SHOULD_SHOW_SHORTCUT_BAR}
	 * </p>
	 * 
	 * @return <code>true</code> for a shortcut bar, and <code>false</code>
	 * for no shortcut bar
	 */
	public boolean getShowShortcutBar();

	/**
	 * Sets whether the underlying workbench window has a shortcut bar.
	 * 
	 * @param show <code>true</code> for a shortcut bar, and <code>false</code>
	 * for no shortcut bar
	 * 
	 * @issue if the shortcut bar is hidden, the status line loses some space on the left
	 *   (presumably to the progress indicator)
	 */
	public void setShowShortcutBar(boolean show);

	/**
	 * Returns whether the underlying workbench window has a status line.
	 * <p>
	 * The initial value is controlled by the preference
	 * {@link IWorkbenchPreferences.SHOULD_SHOW_STATUS_LINE SHOULD_SHOW_STATUS_LINE}
	 * </p>
	 * 
	 * @return <code>true</code> for a status line, and <code>false</code>
	 * for no status line
	 */
	public boolean getShowStatusLine();

	/**
	 * Sets whether the underlying workbench window has a status line.
	 * 
	 * @param show <code>true</code> for a status line, and <code>false</code>
	 * for no status line
	 */
	public void setShowStatusLine(boolean show);
	
	/**
	 * Returns the data associated with this workbench window at the given key.
	 * 
	 * @param key the key
	 * @return the data, or <code>null</code> if there is no data at the given
	 * key
	 */
	public Object getData(String key);
	
	/**
	 * Sets the data associated with this workbench window at the given key.
	 * 
	 * @param key the key
	 * @param data the data, or <code>null</code> to delete existing data
	 */
	public void setData(String key, Object data);
	
	/**
	 * Adds the given drag and drop <code>Transfer</code> type to the ones
	 * supported for drag and drop on the editor area of this workbench window.
	 * <p>
	 * The workbench advisor would ordinarily call this method from the
	 * <code>preWindowOpen</code> callback.
	 * A newly-created workbench window supports no drag and drop transfer
	 * types. Adding <code>EditorInputTransfer.getInstance()</code>
	 * enables <code>IEditorInput</code>s to be transferred. 
	 * </p>
	 * <p>
	 * Note that drag and drop to the editor area requires adding one or more
	 * transfer types (using <code>addEditorAreaTransfer</code>) and 
	 * configuring a drop target listener
	 * (with <code>configureEditorAreaDropListener</code>)
	 * capable of handling any of those transfer types.
	 * </p>
	 * 
	 * @param transfer a drag and drop transfer object
	 * @see #configureEditorAreaDropListener
	 * @see org.eclipse.ui.part.EditorInputTransfer
	 */
	public void addEditorAreaTransfer(Transfer tranfer);
	
	/**
	 * Configures the drop target listener for the editor area of this workbench window.
	 * <p>
	 * The workbench advisor ordinarily calls this method from the
	 * <code>preWindowOpen</code> callback.
	 * A newly-created workbench window has no configured drop target listener for its
	 * editor area.
	 * </p>
	 * <p>
	 * Note that drag and drop to the editor area requires adding one or more
	 * transfer types (using <code>addEditorAreaTransfer</code>) and 
	 * configuring a drop target listener
	 * (with <code>configureEditorAreaDropListener</code>)
	 * capable of handling any of those transfer types.
	 * </p>
	 * 
	 * @param dropTargetListener the drop target listener that will handle
	 * requests to drop an object on to the editor area of this window
	 * 
	 * @see #addEditorAreaTransfer
	 */
	public void configureEditorAreaDropListener(DropTargetListener dropTargetListener);
}

