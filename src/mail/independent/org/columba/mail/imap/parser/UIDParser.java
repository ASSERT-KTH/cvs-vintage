package org.columba.mail.imap.parser;

import java.util.Vector;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class UIDParser {

	public static Vector parse(IMAPResponse[] responses) {
		Vector v = new Vector();

		for (int i = 0; i < responses.length - 1; i++) {
			if (responses[i] == null)
				continue;

			String data = ImapParserUtils.parseData(responses[i].getSource());

			String uid = parseLine(data);
			
			v.add(uid);

		}

		return v;
	}

	protected static String parseLine(String data) {

		int index = data.indexOf(" ");

		return data.substring(index + 1, data.length());
	}

}
