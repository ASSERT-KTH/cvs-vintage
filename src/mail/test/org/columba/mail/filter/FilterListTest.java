/*
 * Created on 2003-okt-30
 */
package org.columba.mail.filter;

import org.columba.core.xml.XmlElement;

import junit.framework.TestCase;

/**
 * Tests for the <code>FilterList</code> class. 
 * 
 * @author redsolo
 */
public class FilterListTest extends TestCase {

	/**
	 * Test to add filters to the list.
	 * The method should be able to handle nulls as well.
	 */
	public void testAdd() {
		FilterList filterList = new FilterList( new XmlElement() );
		filterList.add(createNamedFilter("ONE"));
		assertEquals("Wrong number of filters in the list.", 1, filterList.count());
		filterList.add(null);
		assertEquals("Wrong number of filters in the list.", 1, filterList.count());
		filterList.add(createNamedFilter("ONE"));
		assertEquals("Wrong number of filters in the list.", 2, filterList.count());
	}

	/**
	 * Test to remove filters from the list. 
	 */
	public void testRemoveFilter() {
		FilterList filterList = new FilterList( new XmlElement() );
		Filter filterTwo = createNamedFilter("TWO");
		filterList.add(createNamedFilter("ONE"));
		filterList.add(filterTwo);
		filterList.add(createNamedFilter("THREE"));		
		assertEquals("Wrong number of filters in the list.", 3, filterList.count());
		
		filterList.remove(filterTwo);
		assertEquals("Wrong number of filters in the list.", 2, filterList.count());
		filterList.remove(null);
		assertEquals("Wrong number of filters in the list.", 2, filterList.count());
	}
	
	/**
	 * Test the count() method.
	 *
	 */
	public void testCount() {
		FilterList filterList = new FilterList( new XmlElement() );
		assertEquals("Expected an empty filter list", 0, filterList.count() );
		filterList.add( FilterList.createEmptyFilter() );
		assertEquals("Expected a filter list with one filter", 1, filterList.count() );
		filterList.remove(0);
		assertEquals("Expected an empty filter list", 0, filterList.count() );
	}

	/**
	 * Test for Filter#get(int)
	 */
	public void testGetint() {
		FilterList filterList = new FilterList( new XmlElement() );
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);
		assertEquals("The get(int) method returned the wrong filter.", filterOne.getName(), filterList.get(0).getName());
		assertEquals("The get(int) method returned the wrong filter.", filterTwo.getName(), filterList.get(1).getName());
		assertEquals("The get(int) method returned the wrong filter.", filterThree.getName(), filterList.get(2).getName());
	}

	/**
	 * Returns an empty filter with a specified name.
	 * @param name the name of the filter.
	 * @return a <code>Filter</code> with the specified name.
	 */
	private Filter createNamedFilter(String name) {
		Filter filter = FilterList.createEmptyFilter();
		filter.setName(name);
		return filter;
	}
}