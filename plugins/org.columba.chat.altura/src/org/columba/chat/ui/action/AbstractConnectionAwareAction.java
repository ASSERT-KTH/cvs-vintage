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
package org.columba.chat.ui.action;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.chat.MainInterface;
import org.columba.chat.conn.api.ConnectionChangedEvent;
import org.columba.chat.conn.api.IConnectionChangedListener;
import org.columba.chat.conn.api.IConnection.STATUS;
import org.columba.core.gui.action.AbstractColumbaAction;

public abstract class AbstractConnectionAwareAction extends
		AbstractColumbaAction implements IConnectionChangedListener {

	public AbstractConnectionAwareAction(IFrameMediator frameMediator,
			String name) {
		super(frameMediator, name);
		
		setEnabled(false);
		
		MainInterface.connection.addConnectionChangedListener(this);
	}

	/**
	 * @see org.columba.chat.conn.api.IConnectionChangedListener#connectionChanged(org.columba.chat.conn.api.ConnectionChangedEvent)
	 */
	public void connectionChanged(ConnectionChangedEvent object) {
		STATUS status = object.getStatus();

		if (status == STATUS.ONLINE)
			setEnabled(true);
		else if (status == STATUS.OFFLINE)
			setEnabled(false);
	}

}
