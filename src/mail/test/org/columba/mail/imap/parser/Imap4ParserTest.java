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

package org.columba.mail.imap.parser;

import junit.framework.TestCase;

import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class Imap4ParserTest extends TestCase {

	/**
	 * Constructor for Imap4ParserTest.
	 * @param arg0
	 */
	public Imap4ParserTest(String arg0) {
		super(arg0);
	}

	public void testParseBodyStructure1() {
		
		Imap4Parser parser = new Imap4Parser();
		
		MimePartTree result = parser.parseBodyStructure("Fehler");
		
		assertNull( result );
	}

	public void testParseBodyStructure2() {
		
		Imap4Parser parser = new Imap4Parser();
		MimePart expected = new MimePart();
		
		MimePartTree result = parser.parseBodyStructure("BODYSTRUCTURE (\"text\" \"plain\" NIL NIL NIL NIL NIL)");
		
		assertEquals( expected, result );
	}


}
