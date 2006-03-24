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
package org.columba.chat.model;

import org.columba.chat.model.api.IBuddyStatus;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author fdietz
 * 
 */
public class BuddyStatus implements IBuddyStatus {
	private String name;

	private String jabberId;

	private Presence.Mode presenceMode;

	private String statusMessage;

	private boolean signedOn;

	public BuddyStatus(String jabberId) {
		if (jabberId == null)
			throw new IllegalArgumentException("jabberId == null");

		this.jabberId = jabberId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#getJabberId()
	 */
	public String getJabberId() {
		return jabberId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#getPresenceMode()
	 */
	public Presence.Mode getPresenceMode() {
		return presenceMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#isSignedOn()
	 */
	public boolean isSignedOn() {
		return signedOn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#getStatusMessage()
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#getChatMediator()
	 */
	// public IChatMediator getChatMediator() {
	// return mediator;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#setChatMediator(org.columba.chat.api.IChatMediator)
	 */
	// public void setChatMediator(IChatMediator mediator) {
	// this.mediator = mediator;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#setPresenceMode(org.jivesoftware.smack.packet.Presence.Mode)
	 */
	public void setPresenceMode(Presence.Mode presenceMode) {
		this.presenceMode = presenceMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#setSignedOn(boolean)
	 */
	public void setSignedOn(boolean signedOn) {
		this.signedOn = signedOn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#setStatusMessage(java.lang.String)
	 */
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.chat.jabber.IBuddyStatus#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param user
	 *            The user to set.
	 */
	public void setName(String user) {
		this.name = user;
	}
}
