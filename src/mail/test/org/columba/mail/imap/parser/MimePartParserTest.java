/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.imap.IMAPResponse;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MimePartParserTest extends TestCase {

	/**
	 * Constructor for MimePartParserTest.
	 * @param arg0
	 */
	public MimePartParserTest(String arg0) {
		super(arg0);
	}

	//	one-liners don't work correctly right now
	// parser has to be fixed

	// uw-imapd, cyrus-imapd
	public void testParse() {
		String testData =
			"* 3 FETCH (UID 17 BODY[1] {454} \n"
				+ "The Columba-bugs@lists.sourceforge.net mailing list has 1 request(s)\n"
				+ "waiting for your consideration at:\n"
				+ "https://lists.sourceforge.net/lists/admindb/columba-bugs\n"
				+ "\n"
				+ "Please attend to this at your earliest convenience.  This notice of\n"
				+ "pending requests, if any, will be sent out daily.\n"
				+ "\n"
				+ ")";

		IMAPResponse[] r = { new IMAPResponse(testData)};

		String shouldbe =
			"The Columba-bugs@lists.sourceforge.net mailing list has 1 request(s)\n"
				+ "waiting for your consideration at:\n"
				+ "https://lists.sourceforge.net/lists/admindb/columba-bugs\n"
				+ "\n"
				+ "Please attend to this at your earliest convenience.  This notice of\n"
				+ "pending requests, if any, will be sent out daily.";
		String messageSource = MimePartParser.parse(r);

		ColumbaLogger.log.debug("source=<" + messageSource+">");
		ColumbaLogger.log.debug("shouldbe=<" + shouldbe+">");

		Assert.assertEquals(shouldbe, messageSource);
	}

	// this is basically the same message as above
	// but note that "UID 17" is at the end, instead
	//
	// Communigate Pro Server
	
	public void testParse2() {
		String testData =
			"* 3 FETCH (BODY[1] {454} \n"
				+ "The Columba-bugs@lists.sourceforge.net mailing list has 1 request(s)\n"
				+ "waiting for your consideration at:\n"
				+ "https://lists.sourceforge.net/lists/admindb/columba-bugs\n"
				+ "\n"
				+ "Please attend to this at your earliest convenience.  This notice of\n"
				+ "pending requests, if any, will be sent out daily.\n"
				+ " UID 17)";

		IMAPResponse[] r = { new IMAPResponse(testData)};
		String shouldbe =
			"The Columba-bugs@lists.sourceforge.net mailing list has 1 request(s)\n"
				+ "waiting for your consideration at:\n"
				+ "https://lists.sourceforge.net/lists/admindb/columba-bugs\n"
				+ "\n"
				+ "Please attend to this at your earliest convenience.  This notice of\n"
				+ "pending requests, if any, will be sent out daily."
				+ "\n";

		String messageSource = MimePartParser.parse(r);
		ColumbaLogger.log.debug("source=<" + messageSource+">");
		ColumbaLogger.log.debug("shouldbe=<" + shouldbe+">");
		
		Assert.assertEquals(shouldbe, messageSource);
	}
	

}
