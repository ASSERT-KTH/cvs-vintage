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

import org.columba.mail.imap.parser.*;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite =
			new TestSuite("Test for org.columba.modules.mail.parser");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(Imap4ParserTest.class));
		//$JUnit-END$
		return suite;
	}
}
