/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MailUrlParserTest extends TestCase {

	/**
	 * Constructor for MailUrlParserTest.
	 * @param arg0
	 */
	public MailUrlParserTest(String arg0) {
		super(arg0);
	}
	
	/**
	 * Tests a good String
	 */
	public void testParser1() {
		String testData = "mailto:fdietz@users.sourceforge.net";
		MailUrlParser p = new MailUrlParser(testData);
		String adr = (String) p.get("mailto:");
		Assert.assertTrue(adr.equals("fdietz@users.sourceforge.net"));
	}
	/**
	 * Test a defekt mailto Striing
	 */
	public void testParser2() {
		String testStr = "malto:waffel@users.sourceforge.net";
		MailUrlParser p = new MailUrlParser(testStr);
		String adr = (String) p.get("mailto:");
		assertNull(adr);
	}
	/**
	 * Test a defekt p.get call
	 * @author waffel
	 */
	public void testParse3() {
		String testStr = "mailto:waffel@users.sourceforge.net";
		MailUrlParser p = new MailUrlParser(testStr);
		String adr = (String) p.get("malto:");
		assertNull(adr);
	}
	/**
	 * Test a defekt mail-string
	 * @author waffel
	 */
	public void testParse4() {
		String testStr = "mailto:waffel.users.sourceforge.net";
		MailUrlParser p = new MailUrlParser(testStr);
		String adr = (String) p.get("mailto:");
		assertTrue(adr.equals("waffel.users.sourceforge.net"));
	}
	/**
	 * Test a defekt delimiter
	 * @author waffel
	 */
	public void testParse5() {
		String testStr = "mailto;waffel@users.sourceforge.net";
		MailUrlParser p = new MailUrlParser(testStr);
		String adr = (String) p.get("mailto:");
		assertNull(adr);
	}
}
