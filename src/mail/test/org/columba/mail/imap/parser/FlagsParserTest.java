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
public class FlagsParserTest extends TestCase {

	/**
	 * Constructor for FlagsParserTest.
	 * @param arg0
	 */
	public FlagsParserTest(String arg0) {
		super(arg0);
	}

	public void testParseFlags() {
		String testData = "testData";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);
		
		FlagsParser.parseFlags(r);
	}

}
