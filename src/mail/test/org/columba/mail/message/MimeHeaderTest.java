//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.mail.message;

import org.columba.mail.message.MimeHeader;

import junit.framework.TestCase;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MimeHeaderTest extends TestCase {

	/**
	 * Constructor for MimeHeaderTest.
	 * @param arg0
	 */
	public MimeHeaderTest(String arg0) {
		super(arg0);
	}

	/*
	 * Test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		MimeHeader test = new MimeHeader();		
		test.contentType = "text";
		test.contentSubtype = "html";
		test.putContentParameter("charset", "iso-8859-1");
		test.putContentParameter("boundary","--aslkdjfowieurlskj�afl=");
		test.contentDisposition = "inline";
		test.putDispositionParameter("Filename","test.test");
		test.contentID = "asdlkfja�sldkfj";
		test.size = new Integer( 123 );
		
		assertTrue( test.equals(test) );
		
		
	}

}
