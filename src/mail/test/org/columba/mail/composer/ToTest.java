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

import org.columba.ristretto.message.Header;


/**
 * Testcases for generation of To: headerfield
 *
 * @author fdietz
 */
public class ToTest extends TestCase {
    /**
 * Check if Reply-To: headerfield is used as default
 *
 */
    public void testReplyTo() {
        String s = "donald@mail.com";
        Header header = new Header();
        header.set("Reply-To", s);
        header.set("From", "donald.duck@mail.com");

        String result = MessageBuilderHelper.createTo(header);

        assertEquals(s, result);
    }

    /**
 * Check if method is falling back to From: headerfield, if Reply-To:
 * headerfield is not available
 *
 */
    public void testReplyTo2() {
        String s = "donald.duck@mail.com";
        Header header = new Header();
        header.set("From", "donald.duck@mail.com");

        String result = MessageBuilderHelper.createTo(header);

        assertEquals(s, result);
    }

    /**
 * Test it Reply-To: or From: headerfield and
 * all To: and Cc: headerfields are concatenated correctly
 *
 */
    public void testReplyToAll() {
        String s = "donald@mail.com";
        Header header = new Header();
        header.set("Reply-To", s);
        header.set("From", "donald.duck@mail.com");
        header.set("To", "dagobert.duck@mail.com");
        header.set("Cc", "tick@mail.com, trick@mail.com, daisy@mail.com");

        String result = MessageBuilderHelper.createToAll(header);

        String shouldbe = "donald@mail.com, dagobert.duck@mail.com, tick@mail.com, trick@mail.com, daisy@mail.com";

        assertEquals(shouldbe, result);
    }

    /**
 *
 * Check if method is falling back to X-BeenThere:, to Reply-To or From: headerfield,
 * if not available
 *
 */
    public void testReplyToMailinglist() {
        // TODO (@author fdietz):: implement test
    }
}
