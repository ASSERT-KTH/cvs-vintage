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
package org.columba.mail.imap.parser;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class IMAPFlags {
	boolean seen;
	boolean answered;
	boolean recent;
	boolean flagged;
	boolean deleted;
	
	Object uid;
	
	/**
	 * Constructor for IMAPFlags.
	 */
	public IMAPFlags() {
		super();
	}

	/**
	 * Returns the answered.
	 * @return boolean
	 */
	public boolean isAnswered() {
		return answered;
	}

	

	/**
	 * Returns the flagged.
	 * @return boolean
	 */
	public boolean isFlagged() {
		return flagged;
	}

	/**
	 * Returns the recent.
	 * @return boolean
	 */
	public boolean isRecent() {
		return recent;
	}

	/**
	 * Returns the seen.
	 * @return boolean
	 */
	public boolean isSeen() {
		return seen;
	}

	/**
	 * Sets the answered.
	 * @param answered The answered to set
	 */
	public void setAnswered(boolean answered) {
		this.answered = answered;
	}

	
	/**
	 * Sets the flagged.
	 * @param flagged The flagged to set
	 */
	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	/**
	 * Sets the recent.
	 * @param recent The recent to set
	 */
	public void setRecent(boolean recent) {
		this.recent = recent;
	}

	/**
	 * Sets the seen.
	 * @param seen The seen to set
	 */
	public void setSeen(boolean seen) {
		this.seen = seen;
	}

	/**
	 * Returns the deleted.
	 * @return boolean
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Sets the deleted.
	 * @param deleted The deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * Returns the uid.
	 * @return Object
	 */
	public Object getUid() {
		return uid;
	}

	/**
	 * Sets the uid.
	 * @param uid The uid to set
	 */
	public void setUid(Object uid) {
		this.uid = uid;
	}

}
