/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test.jboss;

import java.lang.reflect.*;
import junit.framework.*;
import test.jboss.minerva.pools.*;
import test.jboss.minerva.factories.*;

/**
 * Master test Suite for JBoss.  Create new addXXXTests methods for different
 * modules, and then use them to add all the tests you want to run for that
 * module.  Pass this class to the JUnit UI to run all the tests.
 * @version $Revision: 1.5 $
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
        addTestsFromMethods(ObjectPoolTest.class);
        addTestsFromMethods(JDBCFactoryTest.class);
    }

    /**
     * Loads as test all the methods of the specified class that have a name
     * like "testSomething" and no arguments.  The class must be a descendent
     * of Test (or more commonly, TestCase).
     */
    private void addTestsFromMethods(Class cls) {
        if(!Test.class.isAssignableFrom(cls))
            throw new IllegalArgumentException("Not a test case!");
        try {
            Constructor con = cls.getConstructor(new Class[]{String.class});
            Method list[] = cls.getMethods();
            for(int i=0; i<list.length; i++) {
                String name = list[i].getName();
                if(!name.startsWith("test") || name.length() < 5 || !Character.isUpperCase(name.charAt(4)))
                    continue;
                if(list[i].getParameterTypes().length != 0)
                    continue;
                addTest((Test)con.newInstance(new Object[]{name}));
            }
        } catch(Exception e) {
            Logger.exception(e);
        }
    }

    public static Test suite() {
        return new JBossTestSuite();
    }
}