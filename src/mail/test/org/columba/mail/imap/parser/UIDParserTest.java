/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.columba.core.util.ListTools;
import org.columba.mail.imap.IMAPResponse;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UIDParserTest extends TestCase {

	/**
	 * Constructor for UIDParserTest.
	 * @param arg0
	 */
	public UIDParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		String s =
			"* 1 FETCH (UID 0)\r\n"
				+ "* 2 FETCH(UID 1)\r\n"
				+ "*3 FETCH(UID 2)\r\n"
				+ "*4 FETCH(UID 3)\r\n"
				+ "*5 FETCH(UID 4)\r\n"
				+ "A1 OK FETCH completed\r\n";

		IMAPResponse[] r = ParserTestUtil.fillIMAPResponse(s);
		List list = UIDParser.parse(r);

		// create testData
		List testList = new Vector();
		String[] stringUids = { "0", "1", "2", "3", "4" };
		testList.addAll(Arrays.asList(stringUids));

		ListTools.substract(testList, list);
		Assert.assertTrue(testList.size() == 0);
	}

}
