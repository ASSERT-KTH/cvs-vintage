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

	public void testParseString1() {
//		day, month, year, hour, minute
		String testData = "Sun, 07 03 2003 19:20";

		Date date = DateParser.parseString(testData);

		GregorianCalendar c = new GregorianCalendar();
		c.clear();
		// year, month, date, hour, second
		c.set(2003, 2, 7, 19, 20, 0);
		Date testDate = c.getTime();

		Assert.assertTrue( testDate.equals(date) );
	}
	
	public void testParseString2() {
		String testStr = "Thu, 6 Feb 2003 11:05 +0100";
		
		Date date = DateParser.parseString(testStr);
		GregorianCalendar c = new GregorianCalendar();
		c.clear();
		// year, month, date, hour, second
		c.set(2003, 1, 6, 11,5,0);
		Date testDate = c.getTime();
		
		assertTrue(testDate.equals(date));
	}
	
	public void testParseString3() {
		String testStr = "19 Jun 2003 09:46 GMT";
		
		Date date = DateParser.parseString(testStr);
		GregorianCalendar c = new GregorianCalendar();
		c.clear();
		//		year, month, date, hour, second
		c.set(2003, 5, 19, 9, 46, 0);
		Date testDate = c.getTime();
		assertTrue(testDate.equals(date));
	}
	
	public void testParseString4() {
		String testStr = "Thu, 17 Apr 2003 10:06 -0400";
		Date date = DateParser.parseString(testStr);

		GregorianCalendar c = new GregorianCalendar();
		c.clear();
		// year, month, date, hour, second
		c.set(2003, 3, 17, 16, 6, 0);
		Date testDate = c.getTime();

		assertTrue(testDate.equals(date));
	}

}
