//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.EventListenerList;

/**
 * 
 * Adds an Actionlistener list to AbstractAction. 
 * 
 * This makes it possible to add ActionListeners to this Action.
 * When you execute the actionPerformed()-method this will be
 * passed on to all listeners.
 * 
 * @author fdietz
 */
public abstract class JAbstractAction extends AbstractAction {

	private EventListenerList actionListeners;

	/**
	 * default constructor
	 *
	 */
	public JAbstractAction()
	{
	}
	
	/**
	 * default constructor
	 * 
	 * @param s		name of action
	 */
	public JAbstractAction( String s )
	{
		super(s);
	}
	
	public void setName(String s) {
		putValue(Action.NAME, s);
	}

	public String getName() {
		return (String) getValue(Action.NAME);
	}

	public String getActionCommand() {
		return (String) getValue(Action.ACTION_COMMAND_KEY);
	}
	
	public void setActionCommand( String s )
	{
		putValue(Action.ACTION_COMMAND_KEY, s);
	}

	// ActionListener registration and invocation.

	public void actionPerformed(ActionEvent evt) {
		if (actionListeners != null) {
			Object[] listenerList = actionListeners.getListenerList();

			// Recreate the ActionEvent and stuff the value of the ACTION_COMMAND_KEY
			ActionEvent e =
				new ActionEvent(
					evt.getSource(),
					evt.getID(),
					(String) getValue(Action.ACTION_COMMAND_KEY));
			for (int i = 0; i <= listenerList.length - 2; i += 2) {
				((ActionListener) listenerList[i + 1]).actionPerformed(e);
			}
		}
	}

	public void addActionListener(ActionListener l) {
		if (actionListeners == null) {
			actionListeners = new EventListenerList();
		}
		actionListeners.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		if (actionListeners == null) {
			return;
		}
		actionListeners.remove(ActionListener.class, l);
	}

}
