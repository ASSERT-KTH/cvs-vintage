/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.util.StringTokenizer;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.folder.MessageFolderInfo;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageFolderInfoParser {

	public static MessageFolderInfo parseMessageFolderInfo(String s) {
		MessageFolderInfo info = new MessageFolderInfo();

		StringTokenizer tok = new StringTokenizer(s, "\n");

		String str, str2;
		Integer i;

		while (tok.hasMoreElements()) {
			str = (String) tok.nextElement();

			if (str.indexOf("EXISTS") != -1) {
				ColumbaLogger.log.debug("exists_string=" + str);
				str2 =
					str.substring(
						str.indexOf("*") + 2,
						str.indexOf("EXISTS") - 1);

				ColumbaLogger.log.debug("exists_substring=" + str2);

				i = new Integer(str2);

				info.setExists(i.intValue());

			} else if (str.indexOf("RECENT") != -1) {

				str2 =
					str.substring(
						str.indexOf("*") + 2,
						str.indexOf("RECENT") - 1);

				i = new Integer(str2);

				//info.setRecent(i.intValue());

			} else if (str.indexOf("UIDVALIDITY") != -1) {

				str2 =
					str.substring(
						str.indexOf("UIDVALIDITY") + 11 + 1,
						str.indexOf("]"));

				i = new Integer(str2);

				info.setUidValidity(i.intValue());

			} else if (str.indexOf("UIDNEXT") != -1) {

				str2 =
					str.substring(
						str.indexOf("UIDNEXT") + 7 + 1,
						str.indexOf("]"));

				i = new Integer(str2);

				info.setUidNext(i.intValue());

			} else if (str.indexOf("FLAGS") != -1) {

				str2 = str.substring(str.indexOf("(") + 1, str.indexOf(")"));

				//info.setFlags( parseFlags(str2) );
			} else if (str.indexOf("PERMANENTFLAGS") != -1) {

				str2 = str.substring(str.indexOf("(") + 1, str.indexOf(")"));

				//info.setPermanentFlags(parseFlags(str2) );
			} else if (str.indexOf("UNSEEN") != -1) {

				str2 =
					str.substring(
						str.indexOf("UNSEEN") + 6 + 1,
						str.indexOf("]"));

				i = new Integer(str2);

				//info.setUnseen(i.intValue());

			} else if (str.indexOf("READ-WRITE") != -1) {

				info.setReadWrite(true);

			}

		}

		return info;
	}
}
