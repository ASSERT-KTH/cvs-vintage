/*
 * @(#)Test.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	� by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */
package org.jhotdraw.test.standard;

import java.awt.Point;
import java.util.List;

// JUnitDoclet begin import
import org.jhotdraw.figures.RectangleFigure;
import org.jhotdraw.standard.HandleEnumerator;
import org.jhotdraw.standard.NullHandle;
import org.jhotdraw.standard.RelativeLocator;
import org.jhotdraw.test.JHDTestCase;
import org.jhotdraw.util.CollectionsFactory;
// JUnitDoclet end import

/*
 * Generated by JUnitDoclet, a tool provided by
 * ObjectFab GmbH under LGPL.
 * Please see www.junitdoclet.org, www.gnu.org
 * and www.objectfab.de for informations about
 * the tool, the licence and the authors.
 */

// JUnitDoclet begin javadoc_class
/**
 * TestCase HandleEnumeratorTest is generated by
 * JUnitDoclet to hold the tests for HandleEnumerator.
 * @see org.jhotdraw.standard.HandleEnumerator
 */
// JUnitDoclet end javadoc_class
public class HandleEnumeratorTest
// JUnitDoclet begin extends_implements
extends JHDTestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private HandleEnumerator handleenumerator;
	// JUnitDoclet end class

	/**
	 * Constructor HandleEnumeratorTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public HandleEnumeratorTest(String name) {
		// JUnitDoclet begin method HandleEnumeratorTest
		super(name);
		// JUnitDoclet end method HandleEnumeratorTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public HandleEnumerator createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		RectangleFigure figure = new RectangleFigure(new Point(15, 15), new Point(20, 20));
		List l = CollectionsFactory.current().createList();
		l.add(new NullHandle(figure, RelativeLocator.north()));
		l.add(new NullHandle(figure, RelativeLocator.north()));
		//return (HandleEnumerator)figure.handles();
		return new HandleEnumerator(l);
		// JUnitDoclet end method testcase.createInstance
	}

	/**
	 * Method setUp is overwriting the framework method to
	 * prepare an instance of this TestCase for a single test.
	 * It's called from the JUnit framework only.
	 */
	protected void setUp() throws Exception {
		// JUnitDoclet begin method testcase.setUp
		super.setUp();
		handleenumerator = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		handleenumerator = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method hasNextHandle()
	/**
	 * Method testHasNextHandle is testing hasNextHandle
	 * @see org.jhotdraw.standard.HandleEnumerator#hasNextHandle()
	 */
	// JUnitDoclet end javadoc_method hasNextHandle()
	public void testHasNextHandle() throws Exception {
		// JUnitDoclet begin method hasNextHandle
		// JUnitDoclet end method hasNextHandle
	}

	// JUnitDoclet begin javadoc_method nextHandle()
	/**
	 * Method testNextHandle is testing nextHandle
	 * @see org.jhotdraw.standard.HandleEnumerator#nextHandle()
	 */
	// JUnitDoclet end javadoc_method nextHandle()
	public void testNextHandle() throws Exception {
		// JUnitDoclet begin method nextHandle
		// JUnitDoclet end method nextHandle
	}

	// JUnitDoclet begin javadoc_method toList()
	/**
	 * Method testToList is testing toList
	 * @see org.jhotdraw.standard.HandleEnumerator#toList()
	 */
	// JUnitDoclet end javadoc_method toList()
	public void testToList() throws Exception {
		// JUnitDoclet begin method toList
		// JUnitDoclet end method toList
	}

	// JUnitDoclet begin javadoc_method reset()
	/**
	 * Method testReset is testing reset
	 * @see org.jhotdraw.standard.HandleEnumerator#reset()
	 */
	// JUnitDoclet end javadoc_method reset()
	public void testReset() throws Exception {
		// JUnitDoclet begin method reset
		// JUnitDoclet end method reset
	}

	// JUnitDoclet begin javadoc_method getEmptyEnumeration()
	/**
	 * Method testGetEmptyEnumeration is testing getEmptyEnumeration
	 * @see org.jhotdraw.standard.HandleEnumerator#getEmptyEnumeration()
	 */
	// JUnitDoclet end javadoc_method getEmptyEnumeration()
	public void testGetEmptyEnumeration() throws Exception {
		// JUnitDoclet begin method getEmptyEnumeration
		// JUnitDoclet end method getEmptyEnumeration
	}

	// JUnitDoclet begin javadoc_method testVault
	/**
	 * JUnitDoclet moves marker to this method, if there is not match
	 * for them in the regenerated code and if the marker is not empty.
	 * This way, no test gets lost when regenerating after renaming.
	 * <b>Method testVault is supposed to be empty.</b>
	 */
	// JUnitDoclet end javadoc_method testVault
	public void testVault() throws Exception {
		// JUnitDoclet begin method testcase.testVault
		// JUnitDoclet end method testcase.testVault
	}

}
