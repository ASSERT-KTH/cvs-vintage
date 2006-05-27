package org.columba.mail.parser;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

public class NormalizeRecipientListParserTest extends TestCase {

	/*
	 * Test method for 'org.columba.mail.parser.NormalizeRecipientListParser.normalizeRCPTVector(List<String>)'
	 */
	public void testNormalizeRCPTVector() {

		List<String> list = new Vector<String>();
		list.add("Frederik Dietz <fdietz@gmx.de>");
		list.add("fdietz@gmx.de");
		list.add("<fdietz@gmx.de>");
		
		List<String> result = new NormalizeRecipientListParser().normalizeRCPTVector(list);
		assertEquals(result.get(0),"<fdietz@gmx.de>");
		assertEquals(result.get(1),"<fdietz@gmx.de>");
		assertEquals(result.get(2),"<fdietz@gmx.de>");
	}

}
