/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.util.List;

import junit.framework.Assert;
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
		String testData =
			"* OK [PARSE] Unterminated mailbox: Undisclosed-Recipient@.MISSING-HOST-NAME.\n"
				+ "* OK [PARSE] Unexpected characters at end of address: :;>\n"
				+ "* SEARCH 7090 8110\n";
		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(testData);

		List list = SearchResultParser.parse(r);

		Assert.assertEquals("7090", list.get(0));
		Assert.assertEquals("8110", list.get(1));

	}

}
