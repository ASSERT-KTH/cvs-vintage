/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.menus;

/**
 * <p>
 * Provides a hook by which third-party code can modify the contents of a menu
 * or group before it is shown.  This is mainly used by the workbench to allow
 * third-party plug-ins to dynamic modify the contents of a menu.  For example,
 * a dynamic menu might be a list of recently opened files.
 * </p>
 * <p>
 * Clients may implement this interface, but they must not extend it.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 */
public interface IDynamicMenu {
	
	/**
	 * Called just before the given menu is about to show. This allows the
	 * implementor of this interface to modify the list of menu elements before
	 * the menu is actually shown.
	 * 
	 * @param menu
	 *            The menu that is about to show. This value is never null.
	 */
	public void aboutToShow(IMenuCollection menu);
}
