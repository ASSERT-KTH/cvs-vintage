/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ParserTestUtil {
	public static IMAPResponse[] fillIMAPResponse(String s) {
		List list = new Vector();

		StringTokenizer tok = new StringTokenizer(s, "\n");
		while (tok.hasMoreTokens()) {
			list.add((String) tok.nextToken());
		}

		IMAPResponse[] r = new IMAPResponse[list.size()];
		for (int i = 0; i < list.size(); i++) {
			r[i] = new IMAPResponse((String) list.get(i));
		}

		return r;
	}
}
