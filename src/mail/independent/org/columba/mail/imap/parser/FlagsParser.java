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

import java.util.List;
import java.util.Vector;

import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.imap.IMAPResponse;

/**
 * @author fdietz
 *
 * See RFC 2060 IMAP4 (http://rfc-editor.org)
 * 
 * fetch list of message flags
 * 
 * example:
 *  
 * C: A999 UID FETCH 4827313:4828442 FLAGS
 * S: * 23 FETCH (FLAGS (\Seen) UID 4827313)
 * S: * 24 FETCH (FLAGS (\Seen) UID 4827943)
 * S: * 25 FETCH (FLAGS (\Seen) UID 4828442)
 * S: A999 UID FETCH completed
 * 
 */


//7.2.6.  FLAGS Response
//
//   Contents:   flag parenthesized list
//
//	  The FLAGS response occurs as a result of a SELECT or EXAMINE
//	  command.  The flag parenthesized list identifies the flags (at a
//	  minimum, the system-defined flags) that are applicable for this
//	  mailbox.  Flags other than the system flags can also exist,
//	  depending on server implementation.
//
//	  The update from the FLAGS response MUST be recorded by the client.
//
//   Example:    S: * FLAGS (\Answered \Flagged \Deleted \Seen \Draft)
//
public class FlagsParser {

	

	public static IMAPFlags[] parseFlags(IMAPResponse[] responses) {
		List v = new Vector();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			// for example: * 149 FETCH (UID 149 FLAGS (\Seen \Answered))
            //              * 406 FETCH (FLAGS (\Seen) UID 57088)
			String data = ImapParserUtils.parseData(responses[i].getSource());
			//ColumbaLogger.log.debug("IMAP Flags: answer=" + data);
			//System.out.println("IMAP Flags: answer=" + data);
            IMAPFlags flags;
			if ( data.indexOf("()") == -1){
              // parse=(\Seen \Answered)
              flags = parseFlagsLine(ImapParserUtils.parseData(data));
            }else{
              // empty "()" so just create a flags object with no flags set
              flags = new IMAPFlags();
            }

            // parse UID portion
			String uid = parseUidsLine(data);

			flags.setUid(uid);

			v.add(flags);
		}

		IMAPFlags[] flags = new IMAPFlags[v.size()];
		((Vector)v).copyInto(flags);

		return flags;
	}

	protected static IMAPFlags parseFlagsLine(String str) {
		IMAPFlags flags = new IMAPFlags();

		if (str.indexOf("Seen") != -1) {
			//System.out.println("seen is true ");
			flags.setSeen(true);
		}
		if (str.indexOf("Answered") != -1) {
			//System.out.println("answered is true ");
			flags.setAnswered(true);
		}
		if (str.indexOf("Flagged") != -1) {
			//System.out.println("flagged is true ");
			flags.setFlagged(true);
		}
		if (str.indexOf("Deleted") != -1) {
			//System.out.println("deleted is true ");
			flags.setDeleted(true);
		}

		if (str.indexOf("Recent") != -1) {
			//System.out.println("deleted is true ");
			flags.setRecent(true);
		}

		return flags;
	}

	protected static String parseUidsLine(String data) {

      // Find the start of the UID portion. Look for UID... as the UID
      // portion isn't guarenteed to be at the beginning of the line.
      int leftIndex  = data.indexOf("UID ") + 4;
      int rightIndex = data.indexOf(" ", leftIndex);

      if(rightIndex == -1){
        // No rightIndex, therefore you went to the end of line, so just
        // return from the left index to the end of the line
        return data.substring(leftIndex);
      }else{
        // Return the sub string you found
        return data.substring(leftIndex, rightIndex);
      }
	}

	public static String parseVariant(int variant) {
		StringBuffer buf = new StringBuffer();
		List arg = new Vector();
		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
			case MarkMessageCommand.MARK_AS_UNREAD :
				{
					arg.add("\\Seen");
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
			case MarkMessageCommand.MARK_AS_UNFLAGGED :
				{
					arg.add("\\Flagged");
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
			case MarkMessageCommand.MARK_AS_UNEXPUNGED :
				{
					arg.add("\\Deleted");
					break;
				}
			case MarkMessageCommand.MARK_AS_ANSWERED :
				{
					arg.add("\\Answered");
					break;
				}
		}

		//if (arg.size() > 1)
		buf.append("(");
		for (int i = 0; i < arg.size(); i++) {
			buf.append((String) arg.get(i));
			if (i != arg.size() - 1)
				buf.append(" ");
		}
		//if (arg.size() > 1)
		buf.append(")");

		return buf.toString();
	}

}
