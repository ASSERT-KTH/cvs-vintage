package org.columba.mail.search;

import java.net.URI;
import java.util.Date;

import javax.swing.ImageIcon;

import org.columba.core.search.SearchResult;
import org.columba.ristretto.message.Address;

public class MailSearchResult extends SearchResult {

	private String date;

	private Address from;

	private ImageIcon statusIcon;

	private boolean flagged;
	
	public MailSearchResult(String title, String description, URI location,
			String date, Address from, ImageIcon statusIcon, boolean flagged) {
		super(title, description, location);
		this.date = date;
		this.from = from;
		this.statusIcon = statusIcon;
		this.flagged = flagged;
	}

	public String getDate() {
		return date;
	}

	public Address getFrom() {
		return from;
	}

	public ImageIcon getStatusIcon() {
		return statusIcon;
	}

	public boolean isFlagged() {
		return flagged;
	}

}
