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
 * @author fdietz
 *
 * See RFC 2060 IMAP4 (http://rfc-editor.org)
 * 
 * fetch list of folders
 * 
 * example:
 * 
 * S: * LIST (\Noselect) "/" ~/Mail/foo
 */

//  from RFC 2060:
//
//   Contents:  name attributes
//			    hierarchy delimiter
//			    name
//
//	 The LIST response occurs as a result of a LIST command.  It
//	 returns a single name that matches the LIST specification.  There
//	 can be multiple LIST responses for a single LIST command.
//
//	 Four name attributes are defined:
//
//	 \Noinferiors   It is not possible for any child levels of
//					hierarchy to exist under this name; no child levels
//					exist now and none can be created in the future.
//
//	 \Noselect      It is not possible to use this name as a selectable
//					mailbox.
//
//	 \Marked        The mailbox has been marked "interesting" by the
//					server; the mailbox probably contains messages that
//					have been added since the last time the mailbox was
//					selected.
//
//	 \Unmarked      The mailbox does not contain any additional
//					messages since the last time the mailbox was
//					selected.
//
//	 If it is not feasible for the server to determine whether the
//	 mailbox is "interesting" or not, or if the name is a \Noselect
//	 name, the server SHOULD NOT send either \Marked or \Unmarked.
//   The hierarchy delimiter is a character used to delimit levels of
//	  hierarchy in a mailbox name.  A client can use it to create child
//	  mailboxes, and to search higher or lower levels of naming
//	  hierarchy.  All children of a top-level hierarchy node MUST use
//	  the same separator character.  A NIL hierarchy delimiter means
//	  that no hierarchy exists; the name is a "flat" name.
//
//	  The name represents an unambiguous left-to-right hierarchy, and
//	  MUST be valid for use as a reference in LIST and LSUB commands.
//	  Unless \Noselect is indicated, the name MUST also be valid as an
//			argument for commands, such as SELECT, that accept mailbox
//	  names.  
 
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
