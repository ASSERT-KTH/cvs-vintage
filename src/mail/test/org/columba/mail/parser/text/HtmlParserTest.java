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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.columba.core.io.StreamUtils;

public class HtmlParserTest extends TestCase {

	public Charset testCharset = Charset.forName("iso-8859-1");
	
    public void testSubstituteURL1() {
        String input = "This page http://columba.sourceforge.net is net!";

        String result = HtmlParser.substituteURL(input);
        assertEquals("This page <A HREF=\"http://columba.sourceforge.net\">http://columba.sourceforge.net</A> is net!", result);
    }

    /**
     * TODO: fix the HtmlParser, which seems to include ")" character
     *  
     */
    public void testSubstituteURL3() {
        String input = "This page \t(http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239) is net!";

        String result = HtmlParser.substituteURL(input);
     
        assertEquals(
                "This page \t(<A HREF=\"http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239\">http://columba.sourceforge.net/phpBB2/viewtopic.php?p=239#239</A>) is net!",
                result);
    }

    public void testSubstituteURL4() {
        String input = "This page http://columba.sourceforge.net. is net!";

        String result = HtmlParser.substituteURL(input);
        assertEquals("This page <A HREF=\"http://columba.sourceforge.net\">http://columba.sourceforge.net</A>. is net!", result);
    }

    public void testSubstituteURL5() {
        String input = "This page http://test.com/$255. is net!";

        String result = HtmlParser.substituteURL(input);
        assertEquals("This page <A HREF=\"http://test.com/$255\">http://test.com/$255</A>. is net!", result);
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
    
    public void testrestoreSpecialCharacters1() {
    	String input = "this &#59; is encoded!";
    	
    	assertEquals("this ; is encoded!", HtmlParser.restoreSpecialCharacters(testCharset, input));
    }

    public void testrestoreSpecialCharacters2() {
    	String input = "this &auml; is encoded!";
    	
    	assertEquals("this \u00e4 is encoded!", HtmlParser.restoreSpecialCharacters(testCharset, input));
    }

    public void testrestoreSpecialCharacters3() {
    	String input = "this is &frac12; encoded &#59; !";
    	
    	assertEquals("this is \u00bd encoded ; !", HtmlParser.restoreSpecialCharacters(testCharset, input));
    }

    public void testrestoreSpecialCharacters4() {
    	String input = "this is&lt;encoded&gt;!";
    	
    	assertEquals("this is<encoded>!", HtmlParser.restoreSpecialCharacters(testCharset, input));
    }

    public void testrestoreSpecialCharacters5() {
    	String input = "&frac12; this is &#160;this is &#59;this is &#59;this is &#59;\nthis is &#59;\nthis is &#59;";
    	
    	assertEquals("\u00bd this is \u00a0this is ;this is ;this is ;\nthis is ;\nthis is ;", HtmlParser.restoreSpecialCharacters(testCharset, input));
    }
    
    public void testsubstitiuteEmail1() {
    	String input = "test@mail.com";
    	
    	assertEquals("<A HREF=\"mailto:" + input +"\">"+ input + "</A>", HtmlParser.substituteEmailAddress(input) );
    }

    public void testsubstitiuteEmail2() {
    	String input = "te+st09@mail.com";
    	
    	assertEquals("<A HREF=\"mailto:" + input +"\">"+ input + "</A>", HtmlParser.substituteEmailAddress(input) );
    }

    public void testsubstitiuteEmail3() {
    	String input = "test09_+@mail.com";
    	
    	assertEquals("<A HREF=\"mailto:" + input +"\">"+ input + "</A>", HtmlParser.substituteEmailAddress(input) );
    }

    public void testsubstitiuteEmail4() {
    	String input = "test09_+@mail.af.two.three.four.five.com";
    	
    	assertEquals("<A HREF=\"mailto:" + input +"\">"+ input + "</A>", HtmlParser.substituteEmailAddress(input) );
    }
}