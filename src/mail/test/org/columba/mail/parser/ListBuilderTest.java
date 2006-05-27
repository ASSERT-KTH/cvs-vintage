package org.columba.mail.parser;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.columba.addressbook.facade.IHeaderItem;

public class ListBuilderTest extends TestCase {

	/*
	 * Test method for 'org.columba.mail.parser.ListBuilder.createFlatList(List<String>)'
	 */
	public void testCreateFlatList() {
		List<String> l = new Vector<String>();
		
		List<String> result = ListBuilder.createFlatList(l);
		
		
	}

	/*
	 * Test method for 'org.columba.mail.parser.ListBuilder.createStringListFromItemList(List<IHeaderItem>)'
	 */
	public void testCreateStringListFromItemList() {

		List<IHeaderItem> l = new Vector<IHeaderItem>();
		
		List<String> result = ListBuilder.createStringListFromItemList(l);
		
	}

}
