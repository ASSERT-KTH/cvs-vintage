/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package test.jboss;

import junit.framework.*;
import test.jboss.minerva.pools.*;

/**
 * Master test Suite for jBoss.  Create new addXXXTests methods for different
 * modules, and then use them to add all the tests you want to run for that
 * module.  Pass this class to the JUnit UI to run all the tests.
 * @version $Revision: 1.1 $
 */
public class JBossTestSuite extends TestSuite {
    public JBossTestSuite() {
        addJBossTests();
    }

    private void addJBossTests() {
        addMinervaTests();
        //addOtherTests
    }

    private void addMinervaTests() {
        addTest(new ObjectPoolTest("testNeedsName"));
        addTest(new ObjectPoolTest("testNeedsFactory"));
        addTest(new ObjectPoolTest("testNeedsInitialize"));
        addTest(new ObjectPoolTest("testMinimumParameters"));
        addTest(new ObjectPoolTest("testParamName"));
        addTest(new ObjectPoolTest("testParamBlocking"));
        addTest(new ObjectPoolTest("testParamShrinking"));
        addTest(new ObjectPoolTest("testParamTimestamp"));
        addTest(new ObjectPoolTest("testParamGC"));
        addTest(new ObjectPoolTest("testParamGCInterval"));
        addTest(new ObjectPoolTest("testParamGCMinIdle"));
        addTest(new ObjectPoolTest("testParamLogWriter"));
        addTest(new ObjectPoolTest("testParamMinSize"));
        addTest(new ObjectPoolTest("testParamMaxSize"));
        addTest(new ObjectPoolTest("testParamShrinkMinIdle"));
        addTest(new ObjectPoolTest("testParamShrinkPercent"));
        addTest(new ObjectPoolTest("testSetsAfterInit"));
        addTest(new ObjectPoolTest("testPoolMax"));
        addTest(new ObjectPoolTest("testShutdown"));
        addTest(new ObjectPoolTest("testLastUpdatesEnabled"));
        addTest(new ObjectPoolTest("testLastUpdatesDisabled"));
        addTest(new ObjectPoolTest("testEvents"));
        addTest(new ObjectPoolTest("testManyGets"));
        addTest(new ObjectPoolTest("testPoolBlocking"));
    }

    public static Test suite() {
        return new JBossTestSuite();
    }
}