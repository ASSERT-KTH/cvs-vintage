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
public class SearchResultParserTest extends TestCase {

	/**
	 * Constructor for SearchResultParserTest.
	 * @param arg0
	 */
	public SearchResultParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		String testData = "testData";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);

		SearchResultParser.parse(r);
	}

}
