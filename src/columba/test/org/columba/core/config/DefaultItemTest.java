/*
 * Created on 2003-okt-31
 */
package org.columba.core.config;

import junit.framework.TestCase;

import org.columba.core.xml.XmlElement;


/**
 * Test cases for the <code>DefaultItem</code> class.
 *
 * @author redsolo
 */
public class DefaultItemTest extends TestCase {
    /*
 * Test for int hashCode().
 */
    public void testHashCode() {
        DefaultItem item = new DefaultItem(new XmlElement());
        item.set("boolean", false);
        item.set("badboolean", true);
        item.set("key", "value");

        DefaultItem item2 = new DefaultItem(new XmlElement());
        item2.set("boolean", false);
        item2.set("badboolean", true);
        item2.set("key", "value");
        assertTrue("The hashcodes are not the same",
            item.hashCode() == item2.hashCode());
        assertTrue("The hashcodes are the same for different items.",
            item.hashCode() != new DefaultItem(new XmlElement()).hashCode());
    }

    /*
 * Test for boolean equals(Object)
 */
    public void testEqualsObject() {
        DefaultItem item = new DefaultItem(new XmlElement());
        item.set("boolean", false);
        item.set("badboolean", true);
        item.set("key", "value");

        DefaultItem item2 = new DefaultItem(new XmlElement());
        item2.set("boolean", false);
        item2.set("badboolean", true);
        item2.set("key", "value");
        assertTrue("The items are not equal", item.equals(item2));
        assertTrue("The items are not equal", item2.equals(item));
        assertTrue("The items are not equal", item.equals(item));
        assertTrue("The items are not equal", item2.equals(item2));
        assertNotSame("The objects are the same", item, item2);
        assertTrue("The items are equal",
            !item.equals(new DefaultItem(new XmlElement())));

        assertFalse("The item is equal to an empty item",
            item.equals(new DefaultItem(null)));
        assertFalse("The item is equal to a null object", item.equals(null));
        assertTrue("The items are not equal",
            item.equals(new DefaultItem((XmlElement) item.getRoot().clone())));
    }

    /*
 * Test for clone()
 */
    public void testClone() {
        DefaultItem item1 = new DefaultItem(new XmlElement("EL"));
        DefaultItem item2 = (DefaultItem) item1.clone();
        assertEquals("The parent and the cloned object are not equal", item1,
            item2);
        assertNotSame("The parent and the cloned object are the same", item1,
            item2);
        assertNotSame("The parent and the cloned Xml Elements objects are the same object.",
            item1.getRoot(), item2.getRoot());
        assertEquals("The parent and the cloned Xml Elements objects are not equal.",
            item1.getRoot(), item2.getRoot());
        assertEquals("The parent and the cloned object did not return the same hashcodes",
            item1.hashCode(), item2.hashCode());

        XmlElement xml = new XmlElement();
        xml.setName("a NAME");
        xml.addAttribute("key", "values");
        xml.addAttribute("key2", "other values");
        xml.addSubElement("child");
        xml.addSubElement(new XmlElement("child2"));

        item1 = new DefaultItem(xml);
        item2 = (DefaultItem) item1.clone();
        assertEquals("The parent and the cloned object are not equal", item1,
            item2);
        assertNotSame("The parent and the cloned object are the same", item1,
            item2);
        assertSame("The getRoot() method did not return the same object put in",
            xml, item1.getRoot());
        assertNotSame("The parent and the cloned Xml Elements objects are the same object.",
            item1.getRoot(), item2.getRoot());
        assertEquals("The parent and the cloned Xml Elements objects are not equal.",
            item1.getRoot(), item2.getRoot());
        assertNotSame("The parent and the cloned Xml Elements objects are the same object.",
            xml, item2.getRoot());
        assertEquals("The parent and the cloned object did not return the same hashcodes",
            item1.hashCode(), item2.hashCode());
    }
    
    public void testSet() {
    	XmlElement root = new XmlElement("root");
    	DefaultItem item = new DefaultItem(root);
    	item.set("sub/path", "test", "value");
    	
    	assertTrue( root.getElement("sub/path")!= null ); 
    	assertEquals( item.get("sub/path","test"), "value");
    }
}
