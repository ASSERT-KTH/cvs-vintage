/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.columba.core.logging.ColumbaLogger;

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
		String messageSource = "header\n\nbody";

		String[] dividedMessage =
			MessageParserUtils.divideMessage(messageSource);

		ColumbaLogger.log.debug("header:<" + dividedMessage[0] + ">");
		ColumbaLogger.log.debug("body:<" + dividedMessage[1] + ">");

		Assert.assertTrue(dividedMessage[0].equals("header\n"));
		Assert.assertTrue(dividedMessage[1].equals("body"));
	}

}
