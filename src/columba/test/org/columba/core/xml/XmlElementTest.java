/*
 * Created on 2003-okt-30
 */
package org.columba.core.xml;

import junit.framework.TestCase;

/**
 * Tests for the <code>XmlElement</code> class.
 * 
 * @author Erik Mattsson
 */
public class XmlElementTest extends TestCase {

	/*
	 * Test for boolean equals(Object)
	 */
	public void testEquals() {
		XmlElement xml1 = new XmlElement();
		XmlElement xml2 = new XmlElement();
		xml1.setName("ONE");
		xml2.setName("ONE");
		xml1.setData("DATA");
		xml2.setData("DATA");
		xml1.addAttribute("name","value");
		xml2.addAttribute("name","value");
		
		XmlElement child1 = new XmlElement("child1");
		XmlElement sibling1 = new XmlElement("sibling1");
		XmlElement child2 = new XmlElement("child2");
		XmlElement sibling2 = new XmlElement("sibling2");

		child1.addElement((XmlElement) child2.clone());
		child1.addElement((XmlElement) sibling2.clone());

		xml1.addElement((XmlElement) child1.clone());
		xml1.addElement((XmlElement) sibling1.clone());
		xml2.addElement((XmlElement) child1.clone());
		xml2.addElement((XmlElement) sibling1.clone());
		
		assertTrue("The XML elements are not equal", xml1.equals(xml2) );
		assertTrue("The XML elements are not equal", xml2.equals(xml1) );
		assertTrue("The XML elements are not equal", xml1.equals(xml1) );
		assertTrue("The XML elements are not equal", xml2.equals(xml2) );
	}
	/*
	 * Test for boolean equals(Object)
	 */
	public void testEquals2() {
		XmlElement xml1 = new XmlElement();
		XmlElement xml2 = new XmlElement();
		xml1.setName("ONE");
		xml2.setName("ONE");
		xml1.setData("DATA");
		xml2.setData("DATA");
		xml1.addAttribute("name","value");
		assertTrue("The XML elements are equal", !xml1.equals(xml2) );
		assertTrue("The XML elements are equal", !xml2.equals(xml1) );	}
	
	/*
	 * Test for boolean not equals(Object)
	 */
	public void testNotEqualsObject() {
		XmlElement xml1 = new XmlElement();
		XmlElement xml2 = new XmlElement();
		xml1.setName("ONE");
		xml2.setName("ONE");
		xml1.addElement(new XmlElement("child1"));
		assertTrue("The XML elements are equal", ! xml1.equals(xml2) );
		assertTrue("The XML elements are equal", ! xml2.equals(xml1) );
	}
	
	/*
	 * Test for hashCode()
	 */
	public void testHashcode() {
		XmlElement xml1 = new XmlElement();
		XmlElement xml2 = new XmlElement();
		xml1.setName("ONE");
		xml2.setName("ONE");
		xml1.addElement(new XmlElement("child1"));
		xml2.addElement(new XmlElement("child1"));
		assertEquals("The hashcode are not equal", xml2.hashCode(), xml1.hashCode() );
	}
}
