/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.TestCase;

import org.columba.mail.imap.IMAPResponse;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MimePartParserTest extends TestCase {

	/**
	 * Constructor for MimePartParserTest.
	 * @param arg0
	 */
	public MimePartParserTest(String arg0) {
		super(arg0);
	}

	//	one-liners don't work correctly right now
	// parser has to be fixed
	public void testParse() {
		String testData = "testData";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);

		String messageSource = MimePartParser.parse(r);
	}

}
