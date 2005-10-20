/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.menus;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>SActionSet</code>.
 * <p>
 * Clients may implement this interface, but must not be extend.
 * </p>
 * 
 * @since 3.2
 * @see SActionSet#addListener(IActionSetListener)
 * @see SActionSet#removeListener(IActionSetListener)
 */
public interface IActionSetListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>SActionSet</code> have changed. Specific details are described in
	 * the <code>ActionSetEvent</code>.
	 * 
	 * @param event
	 *            The event; never <code>null</code>.
	 */
	void actionSetChanged(ActionSetEvent event);
}
