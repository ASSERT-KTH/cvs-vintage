/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui;
/**
 * 2.1 - WORK_IN_PROGRESS. Do not use.
 */
/**
 * Represents the context marked for the user in the navigation history.
 * 
 * Not intended to be implemented by clients. Clients should subclass NavigationLocation
 * instead.
 */
public interface INavigationLocation {
	/**
	 * Disposes of this location and frees any allocated resource.
	 */	
	public void dispose();
	/**
	 * Release any state kept by this location. Any relevant state should be
	 * saved by the previous call of saveState(IMemento). This object will
	 * not be used until restoreState is called again. 	 */
	public void releaseState();
	/**
	 * Persists the state of this location into the <code>memento</code>
	 * 	 * @param memento the storage were the state should be saved into.	 */
	public void saveState(IMemento memento);
	/**
	 * Restore the state of this location from the <code>memento</code>
	 * 
	 * @param memento the storage were the state was saved into.
	 */	
	public void restoreState(IMemento memento);
	/**
	 * Restore the context saved by this location.	 */
	public void restoreLocation();
	/**
	 * Merge the receiver into <code>currentLocation</code>. Return true if
	 * the two locations could be merged otherwise return false.
	 * 
	 * This message is sent to all locations before being added to the history;
	 * given the change to the new location to merge itself into the current
	 * location minimizing the number of entries in the navigation history.
	 * 	 * @param currentLocation where the receiver should be merged into	 * @return boolean true if the merge was possible	 */
	public boolean mergeInto(INavigationLocation currentLocation);
	/**
	 * Returns the input used for this location. Returns null if the
	 * receivers state has been released.
	 * 	 * @return Object the input	 */
	public Object getInput();
	/**
	 * Returns the display name for this location.  This name is used in the
	 * navigation history list.
	 * 
	 * @return String the display text
	 */
	public String getText();
	/**
	 * Sets the location's input.
	 *
	 * Should not be called by clients.
	 * @param input the editor input.
	 */
	public void setInput(Object input);
	/**
	 * Sets the display name for this location.  This name is used in the
	 * navigation history list. 
	 *
	 * @return String the display text
	 */
	public void setText(String text);
	/**
	 * The message <code>update</code> is sent to the active location before
	 * another location becomes active.
	 */	
	public void update();
}