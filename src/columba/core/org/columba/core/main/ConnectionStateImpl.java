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
package org.columba.core.main;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;


/**
 * Default implementation for ConnectionState.
 *
 * @author javaprog
 * @author fdietz
 */
public class ConnectionStateImpl implements ConnectionState {
	protected boolean online = false;
	protected EventListenerList listenerList = new EventListenerList();
	protected ChangeEvent e;
	private static ConnectionStateImpl instance;

	public ConnectionStateImpl() {
		e = new ChangeEvent(this);
	}
	
	public static ConnectionStateImpl getInstance() {
		if ( instance == null ) instance = new ConnectionStateImpl();
		
		return instance;
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public synchronized boolean isOnline() {
		return online;
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public synchronized void setOnline(boolean b) {
		if (online != b) {
			online = b;
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					((ChangeListener) listeners[i + 1]).stateChanged(e);
				}
			}
		}
	}
}