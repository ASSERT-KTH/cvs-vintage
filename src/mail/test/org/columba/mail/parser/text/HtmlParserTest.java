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

package org.columba.mail.parser.text;

import junit.framework.TestCase;

public class HtmlParserTest extends TestCase {
	
	public void testSubstituteURL1() {
		String input = "This page http://columba.sourceforge.net is net!";
		
		String result = HtmlParser.substituteURL(input);
		assertTrue( result.equals("This page <A HREF=http://columba.sourceforge.net>http://columba.sourceforge.net</A> is net!"));
	}

	public void testSubstituteURL3() {
		String input = "This page \t(http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239) is net!";
		
		String result = HtmlParser.substituteURL(input);
		assertTrue( result.equals("This page \t(<A HREF=http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239>http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239</A>) is net!"));
	}

	public void testSubstituteURL4() {
		String input = "This page http://columba.sourceforge.net. is net!";
		
		String result = HtmlParser.substituteURL(input);
		assertTrue( result.equals("This page <A HREF=http://columba.sourceforge.net>http://columba.sourceforge.net</A>. is net!"));
	}
}
