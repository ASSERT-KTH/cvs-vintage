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
package org.jhotdraw.test.samples.javadraw;

import org.jhotdraw.samples.javadraw.JavaDrawViewer;
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
 * TestCase JavaDrawViewerTest is generated by
 * JUnitDoclet to hold the tests for JavaDrawViewer.
 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer
 */
// JUnitDoclet end javadoc_class
public class JavaDrawViewerTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private JavaDrawViewer javadrawviewer;
	// JUnitDoclet end class

	/**
	 * Constructor JavaDrawViewerTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public JavaDrawViewerTest(String name) {
		// JUnitDoclet begin method JavaDrawViewerTest
		super(name);
		// JUnitDoclet end method JavaDrawViewerTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public JavaDrawViewer createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		return new JavaDrawViewer();
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
		javadrawviewer = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		javadrawviewer = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method init()
	/**
	 * Method testInit is testing init
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#init()
	 */
	// JUnitDoclet end javadoc_method init()
	public void testInit() throws Exception {
		// JUnitDoclet begin method init
		// JUnitDoclet end method init
	}

	// JUnitDoclet begin javadoc_method addViewChangeListener()
	/**
	 * Method testAddViewChangeListener is testing addViewChangeListener
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#addViewChangeListener(org.jhotdraw.framework.ViewChangeListener)
	 */
	// JUnitDoclet end javadoc_method addViewChangeListener()
	public void testAddViewChangeListener() throws Exception {
		// JUnitDoclet begin method addViewChangeListener
		// JUnitDoclet end method addViewChangeListener
	}

	// JUnitDoclet begin javadoc_method removeViewChangeListener()
	/**
	 * Method testRemoveViewChangeListener is testing removeViewChangeListener
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#removeViewChangeListener(org.jhotdraw.framework.ViewChangeListener)
	 */
	// JUnitDoclet end javadoc_method removeViewChangeListener()
	public void testRemoveViewChangeListener() throws Exception {
		// JUnitDoclet begin method removeViewChangeListener
		// JUnitDoclet end method removeViewChangeListener
	}

	// JUnitDoclet begin javadoc_method view()
	/**
	 * Method testView is testing view
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#view()
	 */
	// JUnitDoclet end javadoc_method view()
	public void testView() throws Exception {
		// JUnitDoclet begin method view
		// JUnitDoclet end method view
	}

	// JUnitDoclet begin javadoc_method views()
	/**
	 * Method testViews is testing views
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#views()
	 */
	// JUnitDoclet end javadoc_method views()
	public void testViews() throws Exception {
		// JUnitDoclet begin method views
		// JUnitDoclet end method views
	}

	// JUnitDoclet begin javadoc_method drawing()
	/**
	 * Method testDrawing is testing drawing
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#drawing()
	 */
	// JUnitDoclet end javadoc_method drawing()
	public void testDrawing() throws Exception {
		// JUnitDoclet begin method drawing
		// JUnitDoclet end method drawing
	}

	// JUnitDoclet begin javadoc_method tool()
	/**
	 * Method testTool is testing tool
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#tool()
	 */
	// JUnitDoclet end javadoc_method tool()
	public void testTool() throws Exception {
		// JUnitDoclet begin method tool
		// JUnitDoclet end method tool
	}

	// JUnitDoclet begin javadoc_method setTool()
	/**
	 * Method testSetTool is testing setTool
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#setTool(org.jhotdraw.framework.Tool)
	 */
	// JUnitDoclet end javadoc_method setTool()
	public void testSetTool() throws Exception {
		// JUnitDoclet begin method setTool
		// JUnitDoclet end method setTool
	}

	// JUnitDoclet begin javadoc_method toolDone()
	/**
	 * Method testToolDone is testing toolDone
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#toolDone()
	 */
	// JUnitDoclet end javadoc_method toolDone()
	public void testToolDone() throws Exception {
		// JUnitDoclet begin method toolDone
		// JUnitDoclet end method toolDone
	}

	// JUnitDoclet begin javadoc_method figureSelectionChanged()
	/**
	 * Method testFigureSelectionChanged is testing figureSelectionChanged
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#figureSelectionChanged(org.jhotdraw.framework.DrawingView)
	 */
	// JUnitDoclet end javadoc_method figureSelectionChanged()
	public void testFigureSelectionChanged() throws Exception {
		// JUnitDoclet begin method figureSelectionChanged
		// JUnitDoclet end method figureSelectionChanged
	}

	// JUnitDoclet begin javadoc_method getUndoManager()
	/**
	 * Method testGetUndoManager is testing getUndoManager
	 * @see org.jhotdraw.samples.javadraw.JavaDrawViewer#getUndoManager()
	 */
	// JUnitDoclet end javadoc_method getUndoManager()
	public void testGetUndoManager() throws Exception {
		// JUnitDoclet begin method getUndoManager
		// JUnitDoclet end method getUndoManager
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
