/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageParserUtils {
	
	public static String[] divideMessage(String input) {
		String[] output = new String[2];
		int emptyLinePos;

		if (input.length() == 0)
			return null;

		if (input.charAt(0) == '\n') {
			output[0] = new String();
			output[1] = input;
			return output;
		}

		emptyLinePos = input.indexOf("\n\n");

		if (input.indexOf("\n\n") != -1) {
			output[0] = input.substring(0, emptyLinePos + 1);
			output[1] = input.substring(emptyLinePos + 2);
		} else {
			output[0] = input;
			output[1] = new String();
		}

		return output;
	}
}
