/*
 * Created on 04-11-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.columba.core.xml;

import junit.framework.TestCase;

/**
 * Test case to illustrate that the order of sub elements are significant
 * for the current implementation of equals on XmlElement.
 * 
 * @author Karl Peder Olesen (karlpeder)
 */
public class XmlElementTest2 extends TestCase {

	private XmlElement xml1;
	private XmlElement xml2;
	private XmlElement xml3;
	private XmlElement sub1;
	private XmlElement sub2;
	private XmlElement sub3;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		// define sub elements
		sub1 = new XmlElement("sub1");
		sub1.addAttribute("attr1", "val1");
		sub1.addAttribute("attr2", "val2");
		sub2 = new XmlElement("sub2");
		sub2.addAttribute("attr3", "val3");
		sub2.addAttribute("attr4", "val4");
		sub3 = new XmlElement("sub3");
		sub3.addAttribute("attr5", "val5");
		sub3.addAttribute("attr6", "val6");
		
		// define first xml element
		xml1 = new XmlElement("root");
		xml1.addSubElement((XmlElement) sub1.clone());
		xml1.addSubElement((XmlElement) sub2.clone());
		xml1.addSubElement((XmlElement) sub3.clone());
		
		// define one more element, which should be equal to xml1
		xml2 = new XmlElement("root");
		xml2.addSubElement((XmlElement) sub1.clone());
		xml2.addSubElement((XmlElement) sub2.clone());
		xml2.addSubElement((XmlElement) sub3.clone());
		
		// define a third element, where sub elements are ordered differently
		xml3 = new XmlElement("root");
		xml3.addSubElement((XmlElement) sub3.clone());
		xml3.addSubElement((XmlElement) sub1.clone());
		xml3.addSubElement((XmlElement) sub2.clone());
	}
	
	/*
	 * Test of hashCode()
	 */
	public void testHashCode() {
		// test hash codes of xml1 and xml2
		assertTrue("xml1 and xml2 does not have same hash code",
				xml1.hashCode() == xml2.hashCode());
		
		// test hash codes of xml1 and xml3
		assertTrue("xml1 and xml3 does not have same hash code",
				xml1.hashCode() == xml3.hashCode());
	}
	
	/*
	 * Test of equals()
	 */
	public void testEqualsObject() {
		// test equals btw. xml1 and xml2
		assertTrue("xml1 and xml2 are not equal", xml1.equals(xml2));
		
		// test equals btw. xml1 and xml3
		assertTrue("xml1 and xml3 are not equal", xml1.equals(xml3));		
	}
}
