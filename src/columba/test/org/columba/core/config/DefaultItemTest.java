/*
 * Created on 2003-okt-31
 */
package org.columba.core.config;

import org.columba.core.xml.XmlElement;

import junit.framework.TestCase;

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
		DefaultItem item = new DefaultItem( new XmlElement() );
		item.set("boolean", false );
		item.set("badboolean", true );
		item.set("key", "value");
		DefaultItem item2 = new DefaultItem( new XmlElement() );
		item2.set("boolean", false );
		item2.set("badboolean", true );
		item2.set("key", "value");
		assertTrue( "The hashcodes are not the same", item.hashCode() == item2.hashCode() );
		assertTrue( "The hashcodes are the same for different items.", item.hashCode() != new DefaultItem(new XmlElement()).hashCode());
	}

	/*
	 * Test for boolean equals(Object)
	 */
	public void testEqualsObject() {
		DefaultItem item = new DefaultItem( new XmlElement() );
		item.set("boolean", false );
		item.set("badboolean", true );
		item.set("key", "value");
		DefaultItem item2 = new DefaultItem( new XmlElement() );
		item2.set("boolean", false );
		item2.set("badboolean", true );
		item2.set("key", "value");
		assertTrue("The items are not equal", item.equals(item2));
		assertTrue("The items are not equal", item2.equals(item));
		assertTrue("The items are not equal", item.equals(item));
		assertTrue("The items are not equal", item2.equals(item2));
		assertNotSame("The objects are the same", item, item2);
		assertTrue("The items are equal", ! item.equals(new DefaultItem(new XmlElement())));

		assertFalse("The item is equal to an empty item", item.equals(new DefaultItem(null)));
		assertFalse("The item is equal to a null object", item.equals(null));
		assertTrue("The items are not equal", item.equals(new DefaultItem((XmlElement)item.getRoot().clone()) ));
	}
}
