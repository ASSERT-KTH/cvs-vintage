package org.columba.mail.imap.parser;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ListInfo {

	String mailboxName;
	String delimiter;
	boolean hasInferiors;
	boolean isSelectable;
	String source;

	public ListInfo() {
		isSelectable = true;
		hasInferiors = true;
	}

	public void parse(IMAPResponse response) {

		this.source = response.getSource();
		
		System.out.println("parsing--------------->" + source);

		/*		
		source = source.toLowerCase();
		
		if (source.indexOf("noselect") != -1)
			isSelectable = false;
		if (source.indexOf("noinferiors") != -1)
			hasInferiors = false;
			*/

		try {
			ListTokenizer tok = new ListTokenizer(source);
			ListTokenizer.Item nextItem = null;
			
			// skip "*"
			nextItem = tok.getNextItem();
			
			// skip "LSUB" or "LIST"
			nextItem = tok.getNextItem();
			
			// (\NoInferiors \hasChildren)
			nextItem = tok.getNextItem();
			
			// delimiter "." or "/"
			nextItem = tok.getNextItem();
			delimiter = (String) nextItem.getValue();
			
			// folder path 
			nextItem = tok.getNextItem();
			
			mailboxName = (String) nextItem.getValue();
			mailboxName.trim();
			
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public String getSource() {
		return source;
	}

	public String getName() {
		return mailboxName;
	}

	public boolean hasChildren() {
		return hasInferiors;
	}
	
	public String getDelimiter()
	{
		return delimiter;
	}

}
