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
package org.jhotdraw.test.util;

// JUnitDoclet begin import
import org.jhotdraw.test.JHDTestCase;
import org.jhotdraw.util.Iconkit;
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
 * TestCase IconkitTest is generated by
 * JUnitDoclet to hold the tests for Iconkit.
 * @see org.jhotdraw.util.Iconkit
 */
// JUnitDoclet end javadoc_class
public class IconkitTest
// JUnitDoclet begin extends_implements
extends JHDTestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private Iconkit iconkit;
	// JUnitDoclet end class

	/**
	 * Constructor IconkitTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public IconkitTest(String name) {
		// JUnitDoclet begin method IconkitTest
		super(name);
		// JUnitDoclet end method IconkitTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public Iconkit createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		return new Iconkit(getDrawingEditor());
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
		iconkit = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		iconkit = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method instance()
	/**
	 * Method testInstance is testing instance
	 * @see org.jhotdraw.util.Iconkit#instance()
	 */
	// JUnitDoclet end javadoc_method instance()
	public void testInstance() throws Exception {
		// JUnitDoclet begin method instance
		// JUnitDoclet end method instance
	}

	// JUnitDoclet begin javadoc_method loadRegisteredImages()
	/**
	 * Method testLoadRegisteredImages is testing loadRegisteredImages
	 * @see org.jhotdraw.util.Iconkit#loadRegisteredImages(java.awt.Component)
	 */
	// JUnitDoclet end javadoc_method loadRegisteredImages()
	public void testLoadRegisteredImages() throws Exception {
		// JUnitDoclet begin method loadRegisteredImages
		// JUnitDoclet end method loadRegisteredImages
	}

	// JUnitDoclet begin javadoc_method registerImage()
	/**
	 * Method testRegisterImage is testing registerImage
	 * @see org.jhotdraw.util.Iconkit#registerImage(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method registerImage()
	public void testRegisterImage() throws Exception {
		// JUnitDoclet begin method registerImage
		// JUnitDoclet end method registerImage
	}

	// JUnitDoclet begin javadoc_method registerAndLoadImage()
	/**
	 * Method testRegisterAndLoadImage is testing registerAndLoadImage
	 * @see org.jhotdraw.util.Iconkit#registerAndLoadImage(java.awt.Component, java.lang.String)
	 */
	// JUnitDoclet end javadoc_method registerAndLoadImage()
	public void testRegisterAndLoadImage() throws Exception {
		// JUnitDoclet begin method registerAndLoadImage
		// JUnitDoclet end method registerAndLoadImage
	}

	// JUnitDoclet begin javadoc_method loadImage()
	/**
	 * Method testLoadImage is testing loadImage
	 * @see org.jhotdraw.util.Iconkit#loadImage(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method loadImage()
	public void testLoadImage() throws Exception {
		// JUnitDoclet begin method loadImage
		// JUnitDoclet end method loadImage
	}

	// JUnitDoclet begin javadoc_method loadImageResource()
	/**
	 * Method testLoadImageResource is testing loadImageResource
	 * @see org.jhotdraw.util.Iconkit#loadImageResource(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method loadImageResource()
	public void testLoadImageResource() throws Exception {
		// JUnitDoclet begin method loadImageResource
		// JUnitDoclet end method loadImageResource
	}

	// JUnitDoclet begin javadoc_method getImage()
	/**
	 * Method testGetImage is testing getImage
	 * @see org.jhotdraw.util.Iconkit#getImage(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method getImage()
	public void testGetImage() throws Exception {
		// JUnitDoclet begin method getImage
		// JUnitDoclet end method getImage
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
