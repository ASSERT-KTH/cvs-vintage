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
import org.jhotdraw.util.VersionManagement;
import junit.framework.TestCase;
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
 * TestCase VersionManagementTest is generated by
 * JUnitDoclet to hold the tests for VersionManagement.
 * @see org.jhotdraw.util.VersionManagement
 */
// JUnitDoclet end javadoc_class
public class VersionManagementTest
// JUnitDoclet begin extends_implements
extends TestCase
// JUnitDoclet end extends_implements
{
	// JUnitDoclet begin class
	// instance variables, helper methods, ... put them in this marker
	private VersionManagement versionmanagement;
	// JUnitDoclet end class

	/**
	 * Constructor VersionManagementTest is
	 * basically calling the inherited constructor to
	 * initiate the TestCase for use by the Framework.
	 */
	public VersionManagementTest(String name) {
		// JUnitDoclet begin method VersionManagementTest
		super(name);
		// JUnitDoclet end method VersionManagementTest
	}

	/**
	 * Factory method for instances of the class to be tested.
	 */
	public VersionManagement createInstance() throws Exception {
		// JUnitDoclet begin method testcase.createInstance
		return new VersionManagement();
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
		versionmanagement = createInstance();
		// JUnitDoclet end method testcase.setUp
	}

	/**
	 * Method tearDown is overwriting the framework method to
	 * clean up after each single test of this TestCase.
	 * It's called from the JUnit framework only.
	 */
	protected void tearDown() throws Exception {
		// JUnitDoclet begin method testcase.tearDown
		versionmanagement = null;
		super.tearDown();
		// JUnitDoclet end method testcase.tearDown
	}

	// JUnitDoclet begin javadoc_method getJHotDrawVersion()
	/**
	 * Method testGetJHotDrawVersion is testing getJHotDrawVersion
	 * @see org.jhotdraw.util.VersionManagement#getJHotDrawVersion()
	 */
	// JUnitDoclet end javadoc_method getJHotDrawVersion()
	public void testGetJHotDrawVersion() throws Exception {
		// JUnitDoclet begin method getJHotDrawVersion
		// JUnitDoclet end method getJHotDrawVersion
	}

	// JUnitDoclet begin javadoc_method getPackageVersion()
	/**
	 * Method testGetPackageVersion is testing getPackageVersion
	 * @see org.jhotdraw.util.VersionManagement#getPackageVersion(java.lang.Package)
	 */
	// JUnitDoclet end javadoc_method getPackageVersion()
	public void testGetPackageVersion() throws Exception {
		// JUnitDoclet begin method getPackageVersion
		// JUnitDoclet end method getPackageVersion
	}

	// JUnitDoclet begin javadoc_method isCompatibleVersion()
	/**
	 * Method testIsCompatibleVersion is testing isCompatibleVersion
	 * @see org.jhotdraw.util.VersionManagement#isCompatibleVersion(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method isCompatibleVersion()
	public void testIsCompatibleVersion() throws Exception {
		// JUnitDoclet begin method isCompatibleVersion
		// JUnitDoclet end method isCompatibleVersion
	}

	// JUnitDoclet begin javadoc_method readVersionFromFile()
	/**
	 * Method testReadVersionFromFile is testing readVersionFromFile
	 * @see org.jhotdraw.util.VersionManagement#readVersionFromFile(java.lang.String, java.lang.String)
	 */
	// JUnitDoclet end javadoc_method readVersionFromFile()
	public void testReadVersionFromFile() throws Exception {
		// JUnitDoclet begin method readVersionFromFile
		// JUnitDoclet end method readVersionFromFile
	}

	// JUnitDoclet begin javadoc_method normalizePackageName()
	/**
	 * Method testNormalizePackageName is testing normalizePackageName
	 * @see org.jhotdraw.util.VersionManagement#normalizePackageName(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method normalizePackageName()
	public void testNormalizePackageName() throws Exception {
		// JUnitDoclet begin method normalizePackageName
		// JUnitDoclet end method normalizePackageName
	}

	// JUnitDoclet begin javadoc_method extractVersionInfo()
	/**
	 * Method testExtractVersionInfo is testing extractVersionInfo
	 * @see org.jhotdraw.util.VersionManagement#extractVersionInfo(java.lang.String)
	 */
	// JUnitDoclet end javadoc_method extractVersionInfo()
	public void testExtractVersionInfo() throws Exception {
		// JUnitDoclet begin method extractVersionInfo
		// JUnitDoclet end method extractVersionInfo
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
