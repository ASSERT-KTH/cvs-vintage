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

/**
 * Interface to a menu created by a part that will be displayed in a presentation.
 * 
 * @since 3.0
 */
public interface IPartMenu {
	/**
	 * Displays the local menu for this part as a popup at the given location.
	 * 
	 * @param location position to display the menu at (display coordinates, not null)
	 * @since 3.0
	 */
	public void showMenu(Point location);
}
