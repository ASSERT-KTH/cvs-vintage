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
package org.columba.core.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import org.columba.core.action.BasicAction;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FocusManager implements FocusListener {

	Vector list;
	BasicAction cutAction;
	BasicAction copyAction;
	BasicAction pasteAction;
	BasicAction deleteAction;
	
	FocusOwner lastFocusGained = null;
	FocusOwner lastFocusLost = null;

	public FocusManager() {
		list = new Vector();

	}

	public void setActions(
		BasicAction cutAction,
		BasicAction copyAction,
		BasicAction pasteAction,
		BasicAction deleteAction) {

		this.cutAction = cutAction;
		this.copyAction = copyAction;
		this.pasteAction = pasteAction;
		this.deleteAction = deleteAction;
	}

	public void registerComponent(FocusOwner c) {
		list.add(c);
		c.getComponent().addFocusListener(this);
	}

	protected FocusOwner searchOwner(Object component) {
		for (int i = 0; i < list.size(); i++) {
			FocusOwner owner = (FocusOwner) list.get(i);
			Object  c = owner.getComponent();

			if (c.equals(component))
				return owner;
		}

		return null;
	}

	protected void enableActions(boolean b) {
		/*
		if (b) {
			cutAction.setEnabled(true);
			copyAction.setEnabled(true);
			pasteAction.setEnabled(true);
			deleteAction.setEnabled(true);
		} else {
			cutAction.setEnabled(false);
			copyAction.setEnabled(false);
			pasteAction.setEnabled(false);
			deleteAction.setEnabled(false);
		}
		*/

	}

	public void focusGained(FocusEvent event) {
		//System.out.println("focus gained:" + event.getSource().toString());

		lastFocusGained = searchOwner(event.getSource());

		
		if (lastFocusGained.enableAction() == true)
			enableActions(true);
		
	}

	public void focusLost(FocusEvent event) {
		//System.out.println("focus lost:" + event.getSource().toString());
		
		lastFocusLost = searchOwner(event.getSource());
		
		if ( !lastFocusLost.equals(lastFocusGained) )
		{
			enableActions(false);
		}

	}

}