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
package org.columba.mail.parser.text;

import junit.framework.TestCase;

public class HtmlParserTest extends TestCase {

    public void testSubstituteURL1() {
        String input = "This page http://columba.sourceforge.net is net!";

        String result = HtmlParser.substituteURL(input);
        assertTrue(result
                .equals("This page <A HREF=\"http://columba.sourceforge.net\">http://columba.sourceforge.net</A> is net!"));
    }

    /**
     * TODO: fix the HtmlParser, which seems to include ")" character
     *  
     */
    public void testSubstituteURL3() {
        String input = "This page \t(http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239) is net!";

        String result = HtmlParser.substituteURL(input);
     
        // TODO: fix testcase
        /*
        assertEquals(
                "This page \t(<A HREF=\"http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239\">http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239</A>) is net!",
                result);
                */
    }

    public void testSubstituteURL4() {
        String input = "This page http://columba.sourceforge.net. is net!";

        String result = HtmlParser.substituteURL(input);
        assertTrue(result
                .equals("This page <A HREF=\"http://columba.sourceforge.net\">http://columba.sourceforge.net</A>. is net!"));
    }

    public void testRemoveComments1() {
        String input = "<html><body><p><!- this is a text without comments -></p></body></html>";
        String result = HtmlParser.removeComments(input);
        assertTrue(result
                .equals("<html><body><p><!- this is a text without comments -></p></body></html>"));
    }

    public void testRemoveComments2() {
        String input = "<html><body><p><!-- this is a comment -->And some text</p></body></html>";
        String result = HtmlParser.removeComments(input);
        assertTrue(result
                .equals("<html><body><p>And some text</p></body></html>"));
    }

    public void testRemoveComments3() {
        String input = "<html><body><p><!-- this is a comment \n"
                + "\t\twhich is spread over \n"
                + "   multiple lines-->And some text</p> \n\n"
                + "<h1>A header </h><!-- a little comment --><p>"
                + "<i>The end</i></p></body></html>";
        String result = HtmlParser.removeComments(input);
        assertTrue(result.equals("<html><body><p>And some text</p> \n\n"
                + "<h1>A header </h><p>" + "<i>The end</i></p></body></html>"));
    }
}