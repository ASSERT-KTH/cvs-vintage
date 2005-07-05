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
package org.columba.mail.gui.composer.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.columba.core.io.StreamUtils;


public class QuoteFilterInputStreamTest extends TestCase {
    public void testOneLiner() throws IOException {
        String line = "This is a test";
        InputStream in = new ByteArrayInputStream(line.getBytes());

        StringBuffer result = StreamUtils.readCharacterStream(new QuoteFilterInputStream(
                    in));
        assertTrue(result.toString().equals(line.replaceAll("(?m)^(.*)$", "> $1")));
    }

    public void testMultiLiner1() throws IOException {
        String line = "This is a test\nForget the rest\n\n";
        InputStream in = new ByteArrayInputStream(line.getBytes());

        StringBuffer result = StreamUtils.readCharacterStream(new QuoteFilterInputStream(
                    in));
        assertTrue(result.toString().equals(line.replaceAll("(?m)^(.*)$", "> $1")));
    }

    public void testMultiLiner2() throws IOException {
        String line = "This is a test\nForget the rest\n\n\n";
        InputStream in = new ByteArrayInputStream(line.getBytes());

        StringBuffer result = StreamUtils.readCharacterStream(new QuoteFilterInputStream(
                    in));
        assertTrue(result.toString().equals(line.replaceAll("(?m)^(.*)$", "> $1")));
    }

    public void testMultiLiner3() throws IOException {
        String line = "\nThis is a test\nForget the rest\n\n\n";
        InputStream in = new ByteArrayInputStream(line.getBytes());

        StringBuffer result = StreamUtils.readCharacterStream(new QuoteFilterInputStream(
                    in));
        assertTrue(result.toString().equals(line.replaceAll("(?m)^(.*)$", "> $1")));
    }
}
