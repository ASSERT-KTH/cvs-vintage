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
package org.jhotdraw.test.figures;

import org.jhotdraw.figures.ImageFigure;
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
 * TestCase ImageFigureTest is generated by
 * JUnitDoclet to hold the tests for ImageFigure.
 * @see org.jhotdraw.figures.ImageFigure
 */
// JUnitDoclet end javadoc_class
public class ImageFigureTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private ImageFigure imagefigure;
	// JUnitDoclet end class

	/**
	 * Constructor ImageFigureTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public ImageFigureTest(String name) {
		// JUnitDoclet begin method ImageFigureTest
		super(name);
		// JUnitDoclet end method ImageFigureTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public ImageFigure createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		return new ImageFigure();
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
		imagefigure = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		imagefigure = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method basicDisplayBox()
	/**
	 * Method testBasicDisplayBox is testing basicDisplayBox
	 * @see org.jhotdraw.figures.ImageFigure#basicDisplayBox(java.awt.Point, java.awt.Point)
	 */
	// JUnitDoclet end javadoc_method basicDisplayBox()
	public void testBasicDisplayBox() throws Exception {
		// JUnitDoclet begin method basicDisplayBox
		// JUnitDoclet end method basicDisplayBox
	}

	// JUnitDoclet begin javadoc_method handles()
	/**
	 * Method testHandles is testing handles
	 * @see org.jhotdraw.figures.ImageFigure#handles()
	 */
	// JUnitDoclet end javadoc_method handles()
	public void testHandles() throws Exception {
		// JUnitDoclet begin method handles
		// JUnitDoclet end method handles
	}

	// JUnitDoclet begin javadoc_method displayBox()
	/**
	 * Method testDisplayBox is testing displayBox
	 * @see org.jhotdraw.figures.ImageFigure#displayBox()
	 */
	// JUnitDoclet end javadoc_method displayBox()
	public void testDisplayBox() throws Exception {
		// JUnitDoclet begin method displayBox
		// JUnitDoclet end method displayBox
	}

	// JUnitDoclet begin javadoc_method draw()
	/**
	 * Method testDraw is testing draw
	 * @see org.jhotdraw.figures.ImageFigure#draw(java.awt.Graphics)
	 */
	// JUnitDoclet end javadoc_method draw()
	public void testDraw() throws Exception {
		// JUnitDoclet begin method draw
		// JUnitDoclet end method draw
	}

	// JUnitDoclet begin javadoc_method imageUpdate()
	/**
	 * Method testImageUpdate is testing imageUpdate
	 * @see org.jhotdraw.figures.ImageFigure#imageUpdate(java.awt.Image, int, int, int, int, int)
	 */
	// JUnitDoclet end javadoc_method imageUpdate()
	public void testImageUpdate() throws Exception {
		// JUnitDoclet begin method imageUpdate
		// JUnitDoclet end method imageUpdate
	}

	// JUnitDoclet begin javadoc_method release()
	/**
	 * Method testRelease is testing release
	 * @see org.jhotdraw.figures.ImageFigure#release()
	 */
	// JUnitDoclet end javadoc_method release()
	public void testRelease() throws Exception {
		// JUnitDoclet begin method release
		// JUnitDoclet end method release
	}

	// JUnitDoclet begin javadoc_method write()
	/**
	 * Method testWrite is testing write
	 * @see org.jhotdraw.figures.ImageFigure#write(org.jhotdraw.util.StorableOutput)
	 */
	// JUnitDoclet end javadoc_method write()
	public void testWrite() throws Exception {
		// JUnitDoclet begin method write
		// JUnitDoclet end method write
	}

	// JUnitDoclet begin javadoc_method read()
	/**
	 * Method testRead is testing read
	 * @see org.jhotdraw.figures.ImageFigure#read(org.jhotdraw.util.StorableInput)
	 */
	// JUnitDoclet end javadoc_method read()
	public void testRead() throws Exception {
		// JUnitDoclet begin method read
		// JUnitDoclet end method read
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
