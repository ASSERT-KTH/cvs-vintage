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
package org.jhotdraw.test.contrib;

import org.jhotdraw.contrib.SVGStorageFormat;
import junit.framework.TestCase;
// JUnitDoclet begin import
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
 * TestCase SVGStorageFormatTest is generated by
 * JUnitDoclet to hold the tests for SVGStorageFormat.
 * @see org.jhotdraw.contrib.SVGStorageFormat
 */
// JUnitDoclet end javadoc_class
public class SVGStorageFormatTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private SVGStorageFormat svgstorageformat;
	// JUnitDoclet end class

	/**
	 * Constructor SVGStorageFormatTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public SVGStorageFormatTest(String name) {
		// JUnitDoclet begin method SVGStorageFormatTest
		super(name);
		// JUnitDoclet end method SVGStorageFormatTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public SVGStorageFormat createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		return new SVGStorageFormat();
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
		svgstorageformat = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		svgstorageformat = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method createFileDescription()
	/**
	 * Method testCreateFileDescription is testing createFileDescription
	 * @see org.jhotdraw.contrib.SVGStorageFormat#createFileDescription()
	 */
	// JUnitDoclet end javadoc_method createFileDescription()
	public void testCreateFileDescription() throws Exception {
		// JUnitDoclet begin method createFileDescription
		// JUnitDoclet end method createFileDescription
	}

	// JUnitDoclet begin javadoc_method store()
	/**
	 * Method testStore is testing store
	 * @see org.jhotdraw.contrib.SVGStorageFormat#store(java.lang.String, org.jhotdraw.framework.Drawing)
	 */
	// JUnitDoclet end javadoc_method store()
	public void testStore() throws Exception {
		// JUnitDoclet begin method store
		// JUnitDoclet end method store
	}

	// JUnitDoclet begin javadoc_method restore()
	/**
	 * Method testRestore is testing restore
	 * @see org.jhotdraw.contrib.SVGStorageFormat#restore(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method restore()
	public void testRestore() throws Exception {
		// JUnitDoclet begin method restore
		// JUnitDoclet end method restore
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
