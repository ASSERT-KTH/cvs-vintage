/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.TestCase;

import org.columba.mail.imap.IMAPResponse;
import org.columba.mail.message.MimePartTree;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MimePartTreeParserTest extends TestCase {

	/**
	 * Constructor for MimePartTreeParserTest.
	 * @param arg0
	 */
	public MimePartTreeParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		String testData = "fehler";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);

		MimePartTree result = MimePartTreeParser.parse(r);

		assertNull(result);
	}

	public void testParse2() {
		String testData =
			"BODYSTRUCTURE (\"text\" \"plain\" NIL NIL NIL NIL NIL)";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);

		MimePartTree result = MimePartTreeParser.parse(r);

	}

}
