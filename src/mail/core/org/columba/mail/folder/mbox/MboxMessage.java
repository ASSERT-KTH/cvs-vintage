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

package org.columba.mail.folder.mbox;

public class MboxMessage {

	private Object uid;
	private long start;
	private long length;

	/**
	 * Constructs the MboxMessage.
	 * 
	 * @param uid
	 * @param start
	 * @param length
	 */
	public MboxMessage(Object uid, long start, long length) {
		this.uid = uid;
		this.start = start;
		this.length = length;
	}
	/**
	 * @return Returns the start.
	 */
	public long getStart() {
		return start;
	}
	/**
	 * @param start The start to set.
	 */
	public void setStart(long start) {
		this.start = start;
	}
	/**
	 * @return Returns the uid.
	 */
	public Object getUid() {
		return uid;
	}
	/**
	 * @param uid The uid to set.
	 */
	public void setUid(Object uid) {
		this.uid = uid;
	}
	/**
	 * @return Returns the length.
	 */
	public long getLength() {
		return length;
	}
	/**
	 * @param length The length to set.
	 */
	public void setLength(long length) {
		this.length = length;
	}
}
