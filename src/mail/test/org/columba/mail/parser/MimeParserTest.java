/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.TestCase;

import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MimeParserTest extends TestCase {

	/**
	 * Constructor for MimeParserTest.
	 * @param arg0
	 */
	public MimeParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		String messageSource = "testdata";

		MimePart mp = new MimeParser().parse(messageSource);
	}

	public void testParseHeader() {
		String header = "testdata";

		MimeHeader mh = new MimeParser().parseHeader(header);
	}

}
