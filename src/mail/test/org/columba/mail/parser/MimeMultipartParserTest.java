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
public class MimeMultipartParserTest extends TestCase {

	/**
	 * Constructor for MimeMultipartParserTest.
	 * @param arg0
	 */
	public MimeMultipartParserTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		MimeHeader h = new MimeHeader();
		String input = "testdata";

		MimePart mp = new MimeMultipartParser().parse(h, input);
	}

}
