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
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Contains static methods for manipulating SWT controls
 * 
 * @since 3.0
 */
public class SwtUtil {
	
	private SwtUtil() {
		
	}
	
	/**
	 * Determines if one control is a child of another. Returns true iff the second
	 * argument is a child of the first (or the same object).
	 * 
	 * @param potentialParent
	 * @param childToTest
	 * @return
	 */
	public static boolean isChild(Control potentialParent, Control childToTest) {
		if (childToTest == null) {
			return false;
		}
		
		if (childToTest == potentialParent) {
			return true;
		}
		
		return isChild(potentialParent, childToTest.getParent());
	}
	
	/**
	 * Finds and returns the most specific SWT control at the given location. 
	 * (Note: this does a DFS on the SWT widget hierarchy, which is very slow).
	 * 
	 * @param displayToSearch
	 * @param locationToFind
	 * @return
	 */
	public static Control findControl(Display displayToSearch, Point locationToFind) {
		Shell[] shells = displayToSearch.getShells();
		
		return findControl(shells, locationToFind);
	}
	
	/**
	 * Searches the given list of controls for a control containing the given point.
	 * If the array contains any composites, those composites will be recursively
	 * searched to find the most specific child that contains the point.
	 * 
	 * @param toSearch an array of composites 
	 * @param locationToFind a point (in the same coordinate system as the parent of
	 * the 
	 * @return
	 */
	public static Control findControl(Control[] toSearch, Point locationToFind) {
		for (int idx = toSearch.length - 1; idx >= 0; idx--) {
			Control next = toSearch[idx];
			
			if (!next.isDisposed() && next.isVisible()) {
			
				Rectangle bounds = next.getBounds();
				
				if (bounds.contains(locationToFind)) {
					if (next instanceof Composite) {
						locationToFind.x -= bounds.x;
						locationToFind.y -= bounds.y;
						
						Control result = findControl((Composite)next, locationToFind);
					
						locationToFind.x += bounds.x;
						locationToFind.y += bounds.y;
						
						if (result != null) {
							return result;
						}
					}
					
					return next;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Finds the control in the given location
	 * 
	 * @param toSearch
	 * @param locationToFind location (in coordinates relative to the given composite) 
	 * @return
	 */
	public static Control findControl(Composite toSearch, Point locationToFind) {
		Control[] children = toSearch.getChildren();
		
		return findControl(children, locationToFind);
	}
	
}
