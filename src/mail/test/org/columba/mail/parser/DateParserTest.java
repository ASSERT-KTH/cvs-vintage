/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.columba.core.logging.ColumbaLogger;

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
//		day, month, year, hour, minute
		String testData = "Sun, 07 03 2003 19:20";

		Date date = DateParser.parseString(testData);
		ColumbaLogger.log.debug("date="+date.toString());
		
		GregorianCalendar c = new GregorianCalendar();
		// day, month, year, hour, minute
		c.set(2003, 7, 3, 19, 20);
		Date testDate = c.getTime();
		ColumbaLogger.log.debug("testDate="+testDate.toString());
		
		Assert.assertTrue( testDate.equals(date) );

	}

}
