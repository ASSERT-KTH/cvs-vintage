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
			if ( ((String)nextItem.getValue()).indexOf("Noselect") != -1) isSelectable = false;
			else isSelectable = true;
			
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
	
	public String getLastName()
	{
		String sub = mailboxName.substring( mailboxName.lastIndexOf(delimiter)+1, mailboxName.length() );
		
		return sub;
	}

	public boolean hasChildren() {
		return hasInferiors;
	}
	
	public String getDelimiter()
	{
		return delimiter;
	}

	/**
	 * @return boolean
	 */
	public boolean isSelectable() {
		return isSelectable;
	}

}
