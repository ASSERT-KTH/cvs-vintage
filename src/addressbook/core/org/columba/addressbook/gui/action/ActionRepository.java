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
package org.columba.addressbook.gui.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.action.AbstractColumbaAction;

/**
 * Every action should register at this component, which enables/disables
 * actions according to the selection state of the table component.
 *
 * @author fdietz
 */
public class ActionRepository implements ListSelectionListener {

	private List list;

	private static ActionRepository instance;

	public ActionRepository() {

		list= new ArrayList();
	}

	public static ActionRepository getInstance() {
		if (instance == null)
			instance= new ActionRepository();

		return instance;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
			return;

		int start= e.getFirstIndex();
		if (start == -1)
			enableAllActions(false);
		else 
			enableAllActions(true);
	}

	public void enableAllActions(boolean enabled) {
		Iterator it= list.iterator();

		while (it.hasNext()) {
			((AbstractColumbaAction) it.next()).setEnabled(enabled);
		}
	}

	public void registerAction(AbstractColumbaAction action) {
		list.add(action);
	}

}
