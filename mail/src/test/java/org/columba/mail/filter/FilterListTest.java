/*
 * Created on 2003-okt-30
 */
package org.columba.mail.filter;

import junit.framework.TestCase;

import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterList;
import org.columba.core.xml.XmlElement;

/**
 * Tests for the <code>FilterList</code> class.
 * 
 * @author redsolo
 */
public class FilterListTest extends TestCase {
	/**
	 * Test to add filters to the list. The method should be able to handle
	 * nulls as well.
	 */
	public void testAdd() {
		FilterList filterList = new FilterList(new XmlElement());
		filterList.add(createNamedFilter("ONE"));
		assertEquals("Wrong number of filters in the list.", 1, filterList
				.count());
		filterList.add(null);
		assertEquals("Wrong number of filters in the list.", 1, filterList
				.count());
		filterList.add(createNamedFilter("ONE"));
		assertEquals("Wrong number of filters in the list.", 2, filterList
				.count());
	}

	/**
	 * Test to remove filters from the list.
	 */
	public void testRemoveFilter() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterTwo = createNamedFilter("TWO");
		filterList.add(createNamedFilter("ONE"));
		filterList.add(filterTwo);
		filterList.add(createNamedFilter("THREE"));
		assertEquals("Wrong number of filters in the list.", 3, filterList
				.count());

		filterList.remove(filterTwo);
		assertEquals("Wrong number of filters in the list.", 2, filterList
				.count());
		filterList.remove(null);
		assertEquals("Wrong number of filters in the list.", 2, filterList
				.count());
	}

	/**
	 * Test the count() method.
	 *  
	 */
	public void testCount() {
		FilterList filterList = new FilterList(new XmlElement());
		assertEquals("Expected an empty filter list", 0, filterList.count());
		filterList.add(FilterList.createDefaultFilter());
		assertEquals("Expected a filter list with one filter", 1, filterList
				.count());
		filterList.remove(0);
		assertEquals("Expected an empty filter list", 0, filterList.count());
	}

	/**
	 * Test for Filter#get(int)
	 */
	public void testGetint() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterTwo, filterList.get(1));
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(2));
	}

	/**
	 * Test for insert(Filter, int) method.
	 */
	public void testInsert() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterTwo, filterList.get(1));

		Filter filterFour = createNamedFilter("FOUR");
		filterList.insert(filterFour, 1);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterFour, filterList.get(1));
		assertEquals("The get(int) method returned the wrong filter.",
				filterTwo, filterList.get(2));
	}

	/**
	 * Test to move up a filter in the list.
	 */
	public void testMoveUp() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(2));
		filterList.moveUp(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(1));
		filterList.moveUp(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(1));
		filterList.moveUp(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(0));
	}

	/**
	 * Test to move down a filter in the list.
	 */
	public void testMoveDown() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(0));
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(2));
		filterList.moveDown(filterOne);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(1));
		filterList.moveDown(filterOne);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(2));
		assertEquals("The get(int) method returned the wrong filter.",
				filterThree, filterList.get(1));
		filterList.moveDown(filterOne);
		assertEquals("The get(int) method returned the wrong filter.",
				filterOne, filterList.get(2));
	}

	/**
	 * Test for indexOf() method.
	 */
	public void testIndexOf() {
		FilterList filterList = new FilterList(new XmlElement());
		Filter filterOne = createNamedFilter("ONE");
		Filter filterTwo = createNamedFilter("TWO");
		Filter filterThree = createNamedFilter("THREE");
		filterList.add(filterOne);
		filterList.add(filterTwo);
		filterList.add(filterThree);

		assertEquals("The indexof() method did not return the right index", -1,
				filterList.indexOf(null));
		assertEquals("The indexof() method did not return the right index", 0,
				filterList.indexOf(filterOne));
		assertEquals("The indexof() method did not return the right index", 1,
				filterList.indexOf(filterTwo));
		assertEquals("The indexof() method did not return the right index", 2,
				filterList.indexOf(filterThree));
		assertEquals("The indexof() method did not return the right index", -1,
				filterList.indexOf(createNamedFilter("NONE")));
	}

	/**
	 * Returns an empty filter with a specified name.
	 * 
	 * @param name
	 *            the name of the filter.
	 * @return a <code>Filter</code> with the specified name.
	 */
	private Filter createNamedFilter(String name) {
		Filter filter = FilterList.createDefaultFilter();
		filter.setName(name);

		return filter;
	}

	/**
	 * Asserts that the filters are the same.
	 * 
	 * @param msg
	 *            the message to output if the assertion fails.
	 * @param expected
	 *            the expected filter.
	 * @param actual
	 *            the actual filter.
	 */
	private void assertEquals(String msg, Filter expected, Filter actual) {
		assertEquals(msg, expected.getName(), actual.getName());
	}
}