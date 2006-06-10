/*
 * Created on 2003-11-02
 */
package org.columba.mail.config;

import junit.framework.TestCase;

import org.columba.core.xml.XmlElement;


/**
 * Test cases for the <code>AccountItem</code> class.
 *
 * @author karlpeder
 */
public class AccountItemTest extends TestCase {
    /*
 * Test for int hashCode().
 */
    public void testHashCode() {
        // first account item
        XmlElement xml = new XmlElement("account");
        xml.addAttribute("name", "my account");
        xml.addAttribute("uid", "0");

        XmlElement child = xml.addSubElement("identity");
        child.addAttribute("name", "John Doe");
        child.addAttribute("attach_signature", "false");
        child = xml.addSubElement("popserver");
        child.addAttribute("port", "25");
        child.addAttribute("login_method", "USER");
        child = xml.addSubElement("specialfolders");
        child.addAttribute("inbox", "101");
        child.addAttribute("sent", "104");

        AccountItem item = new AccountItem(xml);

        // second account item
        XmlElement xml2 = new XmlElement("account");
        xml2.addAttribute("uid", "0");
        xml2.addAttribute("name", "my account");

        XmlElement child2 = xml2.addSubElement("identity");
        child2.addAttribute("attach_signature", "false");
        child2.addAttribute("name", "John Doe");
        child2 = xml2.addSubElement("popserver");
        child2.addAttribute("login_method", "USER");
        child2.addAttribute("port", "25");
        child2 = xml2.addSubElement("specialfolders");
        child2.addAttribute("sent", "104");
        child2.addAttribute("inbox", "101");

        AccountItem item2 = new AccountItem(xml2);

        // third item, a bit different from the first
        XmlElement xml3 = new XmlElement("account");
        xml3.addAttribute("name", "my account");
        xml3.addAttribute("uid", "0");

        XmlElement child3 = xml3.addSubElement("identity");
        child3.addAttribute("name", "Kalle Kamel");
        child3.addAttribute("attach_signature", "false");
        child3 = xml3.addSubElement("popserver");
        child3.addAttribute("port", "25");
        child3.addAttribute("login_method", "USER");
        child3 = xml3.addSubElement("specialfolders");
        child3.addAttribute("inbox", "101");
        child3.addAttribute("sent", "104");

        AccountItem item3 = new AccountItem(xml3);

        // should have the same hashcodes...
        assertTrue("The hashcodes of item and item2 are not the same",
            item.hashCode() == item2.hashCode());

        // expect a different hashcode from a newly created item...
        assertFalse("The hashcodes of item and a new object are the same",
            item.hashCode() == (new AccountItem(new XmlElement())).hashCode());

        // expect a different hashcode for item and item3
        assertFalse("The hashcodes of item and item3 are the same",
            item.hashCode() == item3.hashCode());
    }

    /*
 * Test for boolean equals(Object)
 */
    public void testEqualsObject() {
        // first account item
        XmlElement xml = new XmlElement("account");
        xml.addAttribute("name", "my account");
        xml.addAttribute("uid", "0");

        XmlElement child = xml.addSubElement("identity");
        child.addAttribute("name", "John Doe");
        child.addAttribute("attach_signature", "false");
        child = xml.addSubElement("popserver");
        child.addAttribute("port", "25");
        child.addAttribute("login_method", "USER");
        child = xml.addSubElement("specialfolders");
        child.addAttribute("inbox", "101");
        child.addAttribute("sent", "104");

        AccountItem item = new AccountItem(xml);

        // second account item
        XmlElement xml2 = new XmlElement("account");
        xml2.addAttribute("uid", "0");
        xml2.addAttribute("name", "my account");

        XmlElement child2 = xml2.addSubElement("identity");
        child2.addAttribute("attach_signature", "false");
        child2.addAttribute("name", "John Doe");
        child2 = xml2.addSubElement("popserver");
        child2.addAttribute("login_method", "USER");
        child2.addAttribute("port", "25");
        child2 = xml2.addSubElement("specialfolders");
        child2.addAttribute("sent", "104");
        child2.addAttribute("inbox", "101");

        AccountItem item2 = new AccountItem(xml2);

        // third item, a bit different from the first
        XmlElement xml3 = new XmlElement("account");
        xml3.addAttribute("name", "my account");
        xml3.addAttribute("uid", "0");

        XmlElement child3 = xml3.addSubElement("identity");
        child3.addAttribute("name", "Kalle Kamel");
        child3.addAttribute("attach_signature", "false");
        child3 = xml3.addSubElement("popserver");
        child3.addAttribute("port", "25");
        child3.addAttribute("login_method", "USER");
        child3 = xml3.addSubElement("specialfolders");
        child3.addAttribute("inbox", "101");
        child3.addAttribute("sent", "104");

        AccountItem item3 = new AccountItem(xml3);

        // test self equality...
        assertTrue("Self equality failed for item", item.equals(item));
        assertTrue("Self equality failed for item2", item2.equals(item2));

        // item and item2 should be equal...
        assertTrue("item and item2 are not equal", item.equals(item2));
        assertTrue("item2 and item are not equal", item2.equals(item));

        // item and item2 should be two different objects
        assertNotSame("item and item2 refers to the same object", item, item2);

        // item should not be equal to a newly created item or null
        assertFalse("item is equal to a newly created AccountItem",
            item.equals(new AccountItem(new XmlElement())));
        assertFalse("item is equal to null", item.equals(null));

        // item and item3 should not be equal
        assertFalse("item and item3 are equal", item.equals(item3));
    }
}
