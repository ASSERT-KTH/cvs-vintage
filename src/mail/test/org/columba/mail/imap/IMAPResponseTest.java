/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IMAPResponseTest extends TestCase {

	/**
	 * Constructor for IMAPResponseTest.
	 * @param arg0
	 */
	public IMAPResponseTest(String arg0) {
		super(arg0);
	}

	public void testParse3() throws Exception {
		String testData = "A1 OK FETCH completed";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}


	public void testParse6() throws Exception {
		String testData = "* BYE IMAP4rev1 Server logging out";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}

	public void testParse8() throws Exception {
		String testData = "* OK [UNSEEN 12] Message 12 is first unseen";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}

	public void testParse9() throws Exception {
		String testData = "* OK [UIDVALIDITY 3857529045] UIDs valid";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}
	public void testParse10() throws Exception {
		String testData =
			"* OK [PERMANENTFLAGS (\\Deleted \\Seen \\*)] Limited";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}
	public void testParse11() throws Exception {
		String testData = "A142 OK [READ-WRITE] SELECT completed";
		IMAPResponse r = new IMAPResponse(testData);
		r.parse(testData);
	}

}
