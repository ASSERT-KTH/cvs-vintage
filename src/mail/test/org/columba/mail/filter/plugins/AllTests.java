// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.filter.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.columba.mail.folder.IMAPTstFactory;
import org.columba.mail.folder.MHFolderFactory;
import org.columba.mail.folder.MailboxTstFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author fdietz
 *
 */
public class AllTests {

    private static String[] list = { "DateFilterTest",
            "ColorFilterTest", "HeaderfieldFilterTest",
            "FlagsFilterTest", "BodyFilterTest",
            "PriorityFilterTest","AccountFilterTest",
			"SizeFilterTest"};

    /**
     * Add all testcases to the passed testsuite, using a the folder type as
     * created in the factory.
     * 
     * @param suite
     *            test suite
     * @param factory
     *            factory which creates the folder instances
     */
    private static void setup(TestSuite suite, MailboxTstFactory factory) {
        try {
            for (int j = 0; j < list.length; j++) {
                Class clazz = Class.forName("org.columba.mail.filter.plugins."
                        + list[j]);

                Method[] methods = clazz.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().startsWith("test")) {

                        suite.addTest((TestCase) clazz.getConstructor(
                                new Class[] { MailboxTstFactory.class,
                                        String.class}).newInstance(
                                new Object[] { factory, methods[i].getName()}));
                    }
                }
            }
        } catch (SecurityException e) {

            e.printStackTrace();
        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        } catch (InstantiationException e) {

            e.printStackTrace();
        } catch (IllegalAccessException e) {

            e.printStackTrace();
        } catch (InvocationTargetException e) {

            e.printStackTrace();
        } catch (NoSuchMethodException e) {

            e.printStackTrace();
        }
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.columba.mail.folder");

        setup(suite, new MHFolderFactory());
        setup(suite, new IMAPTstFactory());

        return suite;
    }
/*
public static Test suite() {
        TestSuite suite = new TestSuite(
            "Test for org.columba.mail.filter.plugins");
        //$JUnit-BEGIN$
        suite.addTestSuite(DateFilterTest.class);
        suite.addTestSuite(ColorFilterTest.class);
        suite.addTestSuite(HeaderfieldFilterTest.class);
        suite.addTestSuite(FlagsFilterTest.class);
        suite.addTestSuite(BodyFilterTest.class);
        suite.addTestSuite(PriorityFilterTest.class);
        suite.addTestSuite(AccountFilterTest.class);
        suite.addTestSuite(SizeFilterTest.class);
        //$JUnit-END$
        return suite;
    }
    */
}