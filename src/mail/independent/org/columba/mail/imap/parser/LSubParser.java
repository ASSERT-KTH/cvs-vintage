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

/**
 * @author fdietz
 *
 * See RFC 2060 IMAP4 (http://rfc-editor.org)
 * 
 */

//
//7.2.3.  LSUB Response
//
//   Contents:   name attributes
//			   hierarchy delimiter
//			   name
//
//	  The LSUB response occurs as a result of an LSUB command.  It
//	  returns a single name that matches the LSUB specification.  There
//	  can be multiple LSUB responses for a single LSUB command.  The
//	  data is identical in format to the LIST response.
//
//   Example:    S: * LSUB () "." #news.comp.mail.misc


public class LSubParser {
	
	public static List parseLsub(String s) {
		StringTokenizer tok = new StringTokenizer(s, "\n");

		String str;

		List v = new Vector();
		boolean firstTime = false;

		while (tok.hasMoreElements()) {
			str = (String) tok.nextElement();

			String path = null;

			path = extractPath(str);
			System.out.println("path:" + path);
			if (path != null)
				v.add(path);

		}

		return v;
	}

	protected static String extractPath(String s) {
		String result = new String();

		if (s.endsWith("\"")) {
			// string ends with "
			// this means the path is enclosed in ""
			// for example: "My Files"

			int index = s.lastIndexOf("\"", s.length() - 2);
			result = s.substring(index + 1, s.length() - 1);

		} else if (s.endsWith("}")) {
			// cyrus-imapd hack
			return null;
		} else {
			// path is not enclosed in ""
			// this means it does not contain whitespaces
			// for example: testorder but not "test order"

			result = s.substring(s.lastIndexOf(" ") + 1, s.length());
		}

		return result;
	}
}
