/*
 * Created on Jul 3, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.parser;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MessageSetTest extends TestCase {

	/**
	 * Constructor for MessageSetTest.
	 * @param arg0
	 */
	public MessageSetTest(String arg0) {
		super(arg0);
	}

	public void testParse() {
		Object[] uids = {
			"0","1","2","3","7","10"
		};
				
		MessageSet set = new MessageSet(uids);
		
		// should be = "0:3,7,10"
		String stringRepresentation = set.getString();
	}

}
