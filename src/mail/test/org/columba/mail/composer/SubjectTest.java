// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.composer;

import junit.framework.TestCase;

/**
 * Test cases for generating subject lines, when replying and/or forwarding
 * messages
 * <p>
 * 
 * TODO: Re:[columba-devel]test-subject 
 * 
 * @author fdietz
 */
public class SubjectTest extends TestCase {

	/**
	 * Check if "Re: " is correctly prepended
	 *  
	 */
	public void testReply() {
		String s = "Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Re: Subject", result);
	}

	/**
	 * Check if "Fwd: " is correctly prepended
	 *  
	 */
	public void testForward() {
		String s = "Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Fwd: Subject", result);

	}

	/**
	 * Check if "Re: " is only prepended if not already found in string
	 *  
	 */
	public void testReply2() {
		String s = "Re: Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Re: Subject", result);
	}

	/**
	 * Check if "Fwd: " is only prepended if not already found in string
	 *  
	 */
	public void testForward2() {
		String s = "Fwd: Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Fwd: Subject", result);

	}

	/**
	 * Check if "Re:" is only prepended if not already found in string
	 * <p>
	 * Note, the missing space
	 */
	public void testReply3() {
		String s = "Re:Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Re:Subject", result);
	}

	/**
	 * Check if "Fwd:" is only prepended if not already found in string
	 * <p>
	 * Note, the missing space
	 *  
	 */
	public void testForward3() {
		String s = "Fwd:Subject";

		String result = MessageBuilderHelper.createForwardSubject(s);

		assertEquals("Fwd:Subject", result);

	}
	
	/**
	 * Test if string is matched correctly.
	 *
	 */
	public void testAlreadyInString() {
		String s = "Test: Hallo";
		
		boolean result = MessageBuilderHelper.isAlreadyReply(s, "Test:");
	
		assertTrue(result);
	}
}
