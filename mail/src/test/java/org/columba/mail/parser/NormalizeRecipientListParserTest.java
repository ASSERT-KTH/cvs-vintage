package org.columba.mail.parser;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

public class NormalizeRecipientListParserTest extends TestCase {


	
	/*
	 * test with null list
	 */
	public void testNormalizeRCPTVectorNull() {
		
		try {
			new NormalizeRecipientListParser().normalizeRCPTVector(null);
			fail();
		} catch (IllegalArgumentException e) {
		}
	}

	
	/*
	 * test with empty list
	 */
	public void testNormalizeRCPTVectorEmptyList() {

		List<String> list = new Vector<String>();
		
		List<String> result = new NormalizeRecipientListParser().normalizeRCPTVector(list);
		assertEquals(0, result.size());
	}

	
	/*
	 * Test with all kinds of input data
	 */
	public void testNormalizeRCPTVector() {

		List<String> list = new Vector<String>();
		list.add("Firstname Lastname <mail@mail.org>");
		list.add("<mail@mail.org>");
		list.add("mail@mail.org");
		list.add("\"Lastname, Firstname\" <mail@mail.org>");
		
		List<String> result = new NormalizeRecipientListParser().normalizeRCPTVector(list);
		assertEquals(result.get(0),"<mail@mail.org>");
		assertEquals(result.get(1),"<mail@mail.org>");
		assertEquals(result.get(2),"<mail@mail.org>");
		assertEquals(result.get(3),"<mail@mail.org>");
	}

}
