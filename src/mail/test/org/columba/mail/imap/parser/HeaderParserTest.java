/*
 * Created on Jul 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.imap.IMAPResponse;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.parser.Rfc822Parser;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HeaderParserTest extends TestCase {

	/**
	 * Constructor for HeaderParserTest.
	 * @param arg0
	 */
	public HeaderParserTest(String arg0) {
		super(arg0);
	}

	//	uw-imapd, cyrus-imapd
	public void testParse() {
		String testData =
			"* 23 FETCH (UID 24 BODY[HEADER.FIELDS (Subject From To Cc Date Size Message-Id In-Reply-To References Content-Type)] {389} \n"
				+ "Message-ID: <dhrxkxm9.yzodjv3japay@frd>\n"
				+ "From: frederikdietz@web.de\n"
				+ "Date: Sat, 05 Jul 03 20:07:03 CEST\n"
				+ "To: schwutte%deswahnsinns.de@deswahnsinns.de\n"
				+ "Subject: test-email\n"
				+ "\n"
				+ ")";

		IMAPResponse[] r = { new IMAPResponse(testData)};

		// first strip from all unnecessary IMAP protocol stuff
		String headerSource = HeaderParser.parse(r);
		ColumbaLogger.log.debug("source=<" + headerSource + ">");

		ColumbaHeader header = new Rfc822Parser().parseHeader(headerSource);

		// now test if all headerfields were parsed correctly
		Assert.assertEquals(
			header.get("Message-ID"),
			"<dhrxkxm9.yzodjv3japay@frd>");
		Assert.assertEquals(header.get("From"), "frederikdietz@web.de");
		Assert.assertEquals(header.get("Date"), "Sat, 05 Jul 03 20:07:03 CEST");
		Assert.assertEquals(
			header.get("To"),
			"schwutte%deswahnsinns.de@deswahnsinns.de");
		Assert.assertEquals(header.get("Subject"), "test-email");
	}

	//	this is basically the same message as above
	// but note that "UID 17" is at the end, instead
	//
	// Communigate Pro Server
	public void testParse2() {
		String testData =
			"* 23 FETCH (BODY[HEADER.FIELDS (Subject From To Cc Date Size Message-Id In-Reply-To References Content-Type)] {389} \n"
				+ "Message-ID: <dhrxkxm9.yzodjv3japay@frd>\n"
				+ "From: frederikdietz@web.de\n"
				+ "Date: Sat, 05 Jul 03 20:07:03 CEST\n"
				+ "To: schwutte%deswahnsinns.de@deswahnsinns.de\n"
				+ "Subject: test-email\n"
				+ " UID 24)";

		IMAPResponse[] r = { new IMAPResponse(testData)};

		// first strip from all unnecessary IMAP protocol stuff
		String headerSource = HeaderParser.parse(r);
		ColumbaLogger.log.debug("source=<" + headerSource + ">");

		ColumbaHeader header = new Rfc822Parser().parseHeader(headerSource);

		// now test if all headerfields were parsed correctly
		Assert.assertEquals(
			header.get("Message-ID"),
			"<dhrxkxm9.yzodjv3japay@frd>");
		Assert.assertEquals(header.get("From"), "frederikdietz@web.de");
		Assert.assertEquals(header.get("Date"), "Sat, 05 Jul 03 20:07:03 CEST");
		Assert.assertEquals(
			header.get("To"),
			"schwutte%deswahnsinns.de@deswahnsinns.de");
		Assert.assertEquals(header.get("Subject"), "test-email");
	}
}
