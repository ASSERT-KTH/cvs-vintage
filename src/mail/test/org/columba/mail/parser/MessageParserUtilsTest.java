/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageParserUtilsTest extends TestCase {

	/**
	 * Constructor for MessageParserUtilsTest.
	 * @param arg0
	 */
	public MessageParserUtilsTest(String arg0) {
		super(arg0);
	}

	public void testDivideMessage() {
		String messageSource = "testdata";
		
		String[] dividedMessage = MessageParserUtils.divideMessage(messageSource);
	}

}
