package org.columba.core.command;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.columba.core.command");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(DefaultProcessorTest.class));
		suite.addTest(new TestSuite(UndoManagerTest.class));
		//$JUnit-END$
		return suite;
	}
}
