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
package org.columba.chat;

import javax.swing.event.EventListenerList;

import org.columba.chat.config.api.IAccount;
import org.columba.chat.conn.api.ConnectionChangedEvent;
import org.columba.chat.conn.api.IConnection;
import org.columba.chat.conn.api.IConnectionChangedListener;
import org.jivesoftware.smack.XMPPConnection;

public class Connection implements IConnection {

	private STATUS status;

	private EventListenerList listenerList = new EventListenerList();

	public static XMPPConnection XMPPConnection;

	public Connection() {
		super();
	}

	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;

		fireSelectionChanged(MainInterface.config.getAccount(), status);
	}

	/**
	 * Adds a listener.
	 */
	public void addConnectionChangedListener(IConnectionChangedListener listener) {
		listenerList.add(IConnectionChangedListener.class, listener);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeConnectionChangedListener(
			IConnectionChangedListener listener) {
		listenerList.remove(IConnectionChangedListener.class, listener);
	}

	/**
	 * Propagates an event to all registered listeners notifying them that the
	 * connection status has changed
	 */
	public void fireSelectionChanged(IAccount account, STATUS status) {
		ConnectionChangedEvent e = new ConnectionChangedEvent(this, account, status);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IConnectionChangedListener.class) {
				((IConnectionChangedListener) listeners[i + 1])
						.connectionChanged(e);
			}
		}
	}

}
