/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import java.util.Date;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DateParserTest extends TestCase {

	/**
	 * Constructor for DateParserTest.
	 * @param arg0
	 */
	public DateParserTest(String arg0) {
		super(arg0);
	}

	public void testParseString() {
		String testData = "datestring";
		
		Date date = DateParser.parseString(testData);
	}

}
