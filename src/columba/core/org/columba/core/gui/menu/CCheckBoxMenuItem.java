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

package org.columba.core.gui.menu;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.action.SelectionStateObservable;
import org.columba.core.gui.util.MnemonicSetter;

/**
 * Adds an Observer to JCheckBoxMenuItem in order to make it possible
 * to receive selection state changes from its underlying action.
 *
 * @author fdietz
 */
public class CCheckBoxMenuItem extends JCheckBoxMenuItem implements Observer{

	/**
	 * default constructor
	 */
	public CCheckBoxMenuItem() {
		super();
	}

	

	/**
	 * Creates a checkbox menu item with a given action attached.
	 * <br>
	 * If the name of the action contains &, the next character is used as
	 * mnemonic. If not, the fall-back solution is to use default behaviour,
	 * i.e. the mnemonic defined using setMnemonic on the action.
	 *  
	 * @param action	The action to attach to the menu item
	 */
	public CCheckBoxMenuItem(Action action) {
		super(action);
		
		CheckBoxAction cbAction = (CheckBoxAction) getAction();

		// register as observer on the action
		cbAction.getObservable().addObserver(this);
		
		// Set text, possibly with a mnemonic if defined using &
		MnemonicSetter.setTextWithMnemonic(this,
                        (String)cbAction.getValue(Action.NAME));
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable obs, Object arg1) {
		SelectionStateObservable o = (SelectionStateObservable) obs;
		
		boolean selectionState = o.isSelected();
		setSelected(selectionState);
	}
}
