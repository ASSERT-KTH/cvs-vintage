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
package org.eclipse.ui.part;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.CoolItemMultiToolBarManager;

/**
 * Standard implementation of <code>IEditorActionBarContributor</code>.
 * <p>
 * If instantiated and used as-is, nothing is contribututed. Clients should
 * subclass in order to contribute to some or all of the action bars.
 * <p>
 * Subclasses may reimplement the following methods:
 * <ul>
 *   <li><code>contributeToMenu</code> - reimplement to contribute to menu</li>
 *   <li><code>contributeToToolBar</code> - reimplement to contribute to tool
 *     bar</li>
 *   <li><code>contributeToStatusLine</code> - reimplement to contribute to 
 *     status line</li>
 *   <li><code>setActiveEditor</code> - reimplement to react to editor changes</li>
 * </ul>
 * </p>
 */
public class EditorActionBarContributor 
	implements IEditorActionBarContributor 
{
	/**
	 * The action bars; <code>null</code> until <code>init</code> is called.
	 */
	private IActionBars bars;
	/**
	 * The workbench page; <code>null</code> until <code>init</code> is called.
	 */
	private IWorkbenchPage page;
/**
 * Creates an empty editor action bar contributor. The action bars are
 * furnished later via the <code>init</code> method.
 */
public EditorActionBarContributor() {
}
/**
 * Contributes to the given menu.
 * <p>
 * The <code>EditorActionBarContributor</code> implementation of this method
 * does nothing. Subclasses may reimplement to add to the menu portion of this
 * contribution.
 * </p>
 *
 * @param menuManager the manager that controls the menu
 */
public void contributeToMenu(IMenuManager menuManager) {
}
/**
 * Contributes to the given status line.
 * <p>
 * The <code>EditorActionBarContributor</code> implementation of this method
 * does nothing. Subclasses may reimplement to add to the status line portion of
 * this contribution.
 * </p>
 *
 * @param statusLineManager the manager of the status line
 */
public void contributeToStatusLine(IStatusLineManager statusLineManager) {
}
/**
 * Contributes to the given tool bar.
 * <p>
 * The <code>EditorActionBarContributor</code> implementation of this method
 * does nothing. Subclasses may reimplement to add to the tool bar portion of
 * this contribution.
 * </p>
 *
 * @param toolBarManager the manager that controls the workbench tool bar
 */
public void contributeToToolBar(IToolBarManager toolBarManager) {
}
/**
 * Returns this contributor's action bars.
 *
 * @return the action bars
 */
public IActionBars getActionBars() {
	return bars;
}
/**
 * Returns this contributor's workbench page.
 *
 * @return the workbench page
 */
public IWorkbenchPage getPage() {
	return page;
}
/**
 * The <code>EditorActionBarContributor</code> implementation of this 
 * <code>IEditorActionBarContributor</code> method does nothing,
 * subclasses may override.
 */
public void dispose() {
}

/**
 * The <code>EditorActionBarContributor</code> implementation of this 
 * <code>IEditorActionBarContributor</code> method remembers the page
 * then forwards the call to <code>init(IActionBars)</code> for
 * backward compatibility
 */
public void init(IActionBars bars, IWorkbenchPage page) {
	this.page = page;
	init(bars);
}
/**
 * This method calls:
 * <ul>
 *  <li><code>contributeToMenu</code> with <code>bars</code>' menu manager</li>
 *  <li><code>contributeToToolBar</code> with <code>bars</code>' tool bar
 *    manager</li>
 *  <li><code>contributeToStatusLine</code> with <code>bars</code>' status line
 *    manager</li>
 * </ul>
 * The given action bars are also remembered and made accessible via 
 * <code>getActionBars</code>.
 * 
 * @param bars the action bars
 */
public void init(IActionBars bars) {
	this.bars = bars;
	contributeToMenu(bars.getMenuManager());
	contributeToToolBar(bars.getToolBarManager());
	contributeToStatusLine(bars.getStatusLineManager());
	// If the editor action bar supports multiple CoolItems, create
	// those cool items.  Workaround for [Bug 17477].
	IToolBarManager tBarMgr = bars.getToolBarManager();
	if (tBarMgr instanceof CoolItemMultiToolBarManager) {
		((CoolItemMultiToolBarManager)tBarMgr).createCoolBarContributionItems();
	}
		
}
/**
 * Sets the active editor for the contributor.
 * <p>
 * The <code>EditorActionBarContributor</code> implementation of this method does
 * nothing. Subclasses may reimplement. This generally entails disconnecting
 * from the old editor, connecting to the new editor, and updating the actions
 * to reflect the new editor.
 * </p>
 * 
 * @param targetEditor the new target editor
 */
public void setActiveEditor(IEditorPart targetEditor) {
}
}
