// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.message;

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
