/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LSubParserTest extends TestCase {

	/**
	 * Constructor for LSubParserTest.
	 * @param arg0
	 */
	public LSubParserTest(String arg0) {
		super(arg0);
	}

	public void testParseLsub() {
		List list = LSubParser.parseLsub("testData");
	}

}
