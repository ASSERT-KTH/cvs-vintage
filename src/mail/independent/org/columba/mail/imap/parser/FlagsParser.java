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

import java.util.Vector;

import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.imap.IMAPResponse;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FlagsParser {

	public static Object[] parseUids(IMAPResponse[] responses) {
		Vector v = new Vector();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			String data = responses[i].getSource();
			//ColumbaLogger.log.debug("answer=" + data);

			String uid = parseUidsLine(data);
			//String source = responses[i].getSource();
			//ColumbaLogger.log.debug("index=" + uid);

			v.add(uid);
			//System.out.println("line["+i+"]="+source);
		}

		Object[] uids = new Object[v.size()];
		v.copyInto(uids);

		return uids;
	}

	public static IMAPFlags[] parseFlags(IMAPResponse[] responses) {
		Vector v = new Vector();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			// for example: * 149 FETCH (UID 149 FLAGS (\Seen \Answered))
			String data = ImapParserUtils.parseData(responses[i].getSource());
			//ColumbaLogger.log.debug("answer=" + data);
			// answer=UID 149 FLAGS (\Seen \Answered)

			// empty "()"
			if ( data.indexOf("()") != -1) continue;
			
			// parse=(\Seen \Answered)
			IMAPFlags flags = parseFlagsLine(ImapParserUtils.parseData(data));

			int leftIndex = data.indexOf(" ");
			int rightIndex = data.indexOf(" ", leftIndex + 1);

			//ColumbaLogger.log.debug("left=" + leftIndex);
			//ColumbaLogger.log.debug("right=" + rightIndex);

			String uid = data.substring(leftIndex + 1, rightIndex);
			//ColumbaLogger.log.debug("uid=<" + uid + ">");

			flags.setUid(uid);

			v.add(flags);

		}

		IMAPFlags[] flags = new IMAPFlags[v.size()];
		v.copyInto(flags);

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

		int leftIndex = data.indexOf(" ");
		int rightIndex = data.indexOf(" ", leftIndex + 1);

		//ColumbaLogger.log.debug("left=" + leftIndex);
		//ColumbaLogger.log.debug("right=" + rightIndex);

		return data.substring(leftIndex + 1, rightIndex);
	}

	public static String parseVariant(int variant) {
		StringBuffer buf = new StringBuffer();
		Vector arg = new Vector();
		switch (variant) {
			case MarkMessageCommand.MARK_AS_READ :
				{
					arg.add("\\Seen");
					break;
				}
			case MarkMessageCommand.MARK_AS_FLAGGED :
				{
					arg.add("\\Flagged");
					break;
				}
			case MarkMessageCommand.MARK_AS_EXPUNGED :
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
