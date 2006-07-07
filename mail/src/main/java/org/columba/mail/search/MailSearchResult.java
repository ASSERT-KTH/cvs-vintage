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
package org.columba.mail.search;

import java.net.URI;

import javax.swing.ImageIcon;

import org.columba.core.search.SearchResult;
import org.columba.ristretto.message.Address;

public class MailSearchResult extends SearchResult {

	private String date;

	private Address from;

	private ImageIcon statusIcon;

	private boolean flagged;

	/**
	 * @param title
	 * @param description
	 * @param location
	 * @param date
	 * @param from
	 * @param statusIcon
	 * @param flagged
	 */
	public MailSearchResult(String title, String description, URI location,
			String date, Address from, ImageIcon statusIcon, boolean flagged) {
		super(title, description, location);
		this.date = date;
		this.from = from;
		this.statusIcon = statusIcon;
		this.flagged = flagged;
	}

	/**
	 * @return date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return from
	 */
	public Address getFrom() {
		return from;
	}

	/**
	 * @return status Icon
	 */
	public ImageIcon getStatusIcon() {
		return statusIcon;
	}

	/**
	 * @return flagged
	 */
	public boolean isFlagged() {
		return flagged;
	}
}
