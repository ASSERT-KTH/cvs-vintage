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
package org.columba.mail.gui.util;

import junit.framework.TestCase;

import org.columba.ristretto.message.Address;

/**
 */
public class AddressListRendererTest extends TestCase {

    /**
     * Test to send an empty Address array to the list.
     * The method should not throw an <code>IndexOutOfBoundsException</code> if
     * an empty array is sent in.
     * @author redsolo
     */
    public void testRenderWithEmptyArray() {
        Address[] addresses = new Address[0];
        AddressListRenderer.renderToHTMLWithLinks(addresses);
    }

    /**
     * Test the rendering with only one address.
     */
    public void testRenderWithSingleItem() {
        Address[] addresses = new Address[] {new Address("email@internet.org")};
        String actual = AddressListRenderer.renderToHTMLWithLinks(addresses).toString();
        String expected = "<a href=\"mailto:email@internet.org\">email@internet.org</a>";
        assertEquals("address wasnt rendered correctly", expected.toLowerCase(), actual.toLowerCase());
    }

    /**
     * Test the rendering with multiple addresses.
     */
    public void testRenderWithMultipleItems() {
        Address[] addresses = new Address[] {new Address("email@internet.org"), new Address("ftp@internet.org"), new Address("web@internet.org")};
        String actual = AddressListRenderer.renderToHTMLWithLinks(addresses).toString();
        String expected = "<a href=\"mailto:email@internet.org\">email@internet.org</a>, "
                + "<a href=\"mailto:ftp@internet.org\">ftp@internet.org</a>, "
                + "<a href=\"mailto:web@internet.org\">web@internet.org</a>";
        assertEquals("addresses wasnt rendered correctly", expected.toLowerCase(), actual.toLowerCase());
    }

    /**
     * Test the rendering with multiple addresses with display names.
     */
    public void testRenderWithDisplayName() {
        Address[] addresses = new Address[] {new Address("Emil", "email@internet.org"), new Address("Alfred", "ftp@internet.org")};
        String actual = AddressListRenderer.renderToHTMLWithLinks(addresses).toString();
        String expected = "<a href=\"mailto:email@internet.org\">Emil</a>, "
            + "<a href=\"mailto:ftp@internet.org\">Alfred</a>";
        assertEquals("address wasnt rendered correctly", expected.toLowerCase(), actual.toLowerCase());
    }
}
