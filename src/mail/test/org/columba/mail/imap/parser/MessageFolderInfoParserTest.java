/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.TestCase;

import org.columba.mail.folder.MessageFolderInfo;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageFolderInfoParserTest extends TestCase {

	/**
	 * Constructor for MessageFolderInfoParserTest.
	 * @param arg0
	 */
	public MessageFolderInfoParserTest(String arg0) {
		super(arg0);
	}

	public void testParseMessageFolderInfo() {

		MessageFolderInfo messageFolderInfo =
			MessageFolderInfoParser.parseMessageFolderInfo("testdata");

	}

}
