/*
 * $Id: AllTest.java,v 1.1 2001/09/29 16:26:02 mumbly Exp $
 * Copyright 2001 Truis Corporation. All rights reserved.
 */

package org.tigris.scarab.om;

//import com.truis.iq.util.CISTestCase;
import org.tigris.scarab.test.BaseTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * AllTest    (Copyright 2001 Truis Corporation)
 *
 * <p> This class performs unit tests on com.truis.business.busobj.All </p>
 *
 * <p> Explanation about the tested class and its responsibilities </p>
 *
 * <p> Relations:
 *     All extends com.truis.business.model.AllModel <br>
 *     All implements com.truis.business.busobj.PersistentObject </p>
 *
 * @author Tim McNerney tmcnerney@truis.com - Truis Corporation
 * @date $Date: 2001/09/29 16:26:02 $
 * @version $Revision: 1.1 $
 *
 * @see com.truis.business.busobj.All
 * @see some.other.package
 */

public class AllTest extends BaseTestCase {
    /**
     * @param name    Name of Object
     */
    public AllTest(String name) {
        super(name);
		System.out.println("\n\nIn all test const\n");
    }

    public AllTest() {
        super("AllTest");
    }

    public static Test suite() {
		System.out.println("\n\nReturning the test suite\n");
        TestSuite suite = new TestSuite();
        suite.addTest(AttributeTest.suite());
        suite.addTest(AttributeOptionTest.suite());
        suite.addTest(IssueTest.suite());
        suite.addTest(QueryTest.suite());
        return suite;
    }

    /**
     * Main method needed to make a self runnable class
     *
     * @param args This is required for main method
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(AllTest.class));
    }
}
