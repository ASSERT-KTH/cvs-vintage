// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.gui.action;

import org.columba.addressbook.gui.frame.AddressbookFrameMediator;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Adds a table selection listener to DefaultTreeAction.
 * <p>
 * Table selection listener additionally disables this action if no
 * contact/group card is selected.
 * 
 * @author fdietz
 */
public abstract class DefaultTableAction extends AbstractColumbaAction
		implements ListSelectionListener {
	/**
	 * @param frameMediator
	 * @param name
	 */
	public DefaultTableAction(FrameMediator frameMediator, String name) {
		super(frameMediator, name);

		// register interest on table selection changes
		((AddressbookFrameMediator) frameMediator)
				.addTableSelectionListener(this);
	}

	/**
	 * Enable or disable action on selection change.
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent event) {
		// return if selection change is in flux
		if (event.getValueIsAdjusting()) {
			return;
		}

		Object[] uids = ((AddressbookFrameMediator) frameMediator).getTable()
				.getUids();

		if (uids.length > 0) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}
}