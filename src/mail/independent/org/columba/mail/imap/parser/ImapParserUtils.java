package org.columba.mail.imap.parser;


/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class ImapParserUtils {

	public static String parseData(String line) {
		int left = line.indexOf("(");
		int right = getClosingParenthesis(line, left + 1);

		return line.substring(left + 1, right);
	}

	protected static int getClosingParenthesis(String s, int openPos) {
		int nextOpenPos = s.indexOf("(", openPos + 1);
		int nextClosePos = s.indexOf(")", openPos + 1);

		while ((nextOpenPos < nextClosePos) & (nextOpenPos != -1)) {
			nextClosePos = s.indexOf(")", nextClosePos + 1);
			nextOpenPos = s.indexOf("(", nextOpenPos + 1);
		}
		return nextClosePos;
	}
}
