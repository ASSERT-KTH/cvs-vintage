/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestErrorsTag2.java,v 1.10 2004/03/14 06:23:40 sraeburn Exp $
 * $Revision: 1.10 $
 * $Date: 2004/03/14 06:23:40 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.struts.taglib.html;

import java.util.Locale;

import javax.servlet.jsp.PageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.struts.Globals;

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.html.ErrorsTag</code> class.
 *
 */
public class TestErrorsTag2 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestErrorsTag2(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestErrorsTag2.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestErrorsTag2.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        request.setAttribute("runTest", whichTest);
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestErrorsTag2.jsp");
    }


    /*
     * Testing ErrorsTag.
     */

    public void testErrorsDefaultBundle0ErrorsProperty() throws Exception {
        runMyTest("testErrorsDefaultBundle0ErrorsProperty", "");
        }
    public void testErrorsDefaultBundle2ErrorsProperty() throws Exception {
        runMyTest("testErrorsDefaultBundle2ErrorsProperty", "");
        }

    public void testErrorsAlternateBundle0ErrorsProperty() throws Exception {
        runMyTest("testErrorsAlternateBundle0ErrorsProperty", "");
        }
    public void testErrorsAlternateBundle2ErrorsProperty() throws Exception {
        runMyTest("testErrorsAlternateBundle2ErrorsProperty", "");
        }

    public void testErrorsDefaultBundle0ErrorsProperty_fr() throws Exception {
        runMyTest("testErrorsDefaultBundle0ErrorsProperty_fr", "fr");
        }
    public void testErrorsDefaultBundle2ErrorsProperty_fr() throws Exception {
        runMyTest("testErrorsDefaultBundle2ErrorsProperty_fr", "fr");
        }

    public void testErrorsAlternateBundle0ErrorsProperty_fr() throws Exception {
        runMyTest("testErrorsAlternateBundle0ErrorsProperty_fr", "fr");
        }
    public void testErrorsAlternateBundle2ErrorsProperty_fr() throws Exception {
        runMyTest("testErrorsAlternateBundle2ErrorsProperty_fr", "fr");
        }



    public void testErrorsDefaultBundle0ErrorsLocaleProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle0ErrorsLocaleProperty", "");
        }
    public void testErrorsDefaultBundle2ErrorsLocaleProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle2ErrorsLocaleProperty", "");
        }

    public void testErrorsAlternateBundle0ErrorsLocaleProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle0ErrorsLocaleProperty", "");
        }
    public void testErrorsAlternateBundle2ErrorsLocaleProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle2ErrorsLocaleProperty", "");
        }

    public void testErrorsDefaultBundle0ErrorsLocaleProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle0ErrorsLocaleProperty_fr", "");
        }
    public void testErrorsDefaultBundle2ErrorsLocaleProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle2ErrorsLocaleProperty_fr", "");
        }

    public void testErrorsAlternateBundle0ErrorsLocaleProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle0ErrorsLocaleProperty_fr", "");
        }
    public void testErrorsAlternateBundle2ErrorsLocaleProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle2ErrorsLocaleProperty_fr", "");
        }







    public void testErrorsDefaultBundle0ErrorsNameProperty() throws Exception {
        runMyTest("testErrorsDefaultBundle0ErrorsNameProperty", "");
        }
    public void testErrorsDefaultBundle2ErrorsNameProperty() throws Exception {
        runMyTest("testErrorsDefaultBundle2ErrorsNameProperty", "");
        }

    public void testErrorsAlternateBundle0ErrorsNameProperty() throws Exception {
        runMyTest("testErrorsAlternateBundle0ErrorsNameProperty", "");
        }
    public void testErrorsAlternateBundle2ErrorsNameProperty() throws Exception {
        runMyTest("testErrorsAlternateBundle2ErrorsNameProperty", "");
        }

    public void testErrorsDefaultBundle0ErrorsNameProperty_fr() throws Exception {
        runMyTest("testErrorsDefaultBundle0ErrorsNameProperty_fr", "fr");
        }
    public void testErrorsDefaultBundle2ErrorsNameProperty_fr() throws Exception {
        runMyTest("testErrorsDefaultBundle2ErrorsNameProperty_fr", "fr");
        }

    public void testErrorsAlternateBundle0ErrorsNameProperty_fr() throws Exception {
        runMyTest("testErrorsAlternateBundle0ErrorsNameProperty_fr", "fr");
        }
    public void testErrorsAlternateBundle2ErrorsNameProperty_fr() throws Exception {
        runMyTest("testErrorsAlternateBundle2ErrorsNameProperty_fr", "fr");
        }



    public void testErrorsDefaultBundle0ErrorsLocaleNameProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle0ErrorsLocaleNameProperty", "");
        }
    public void testErrorsDefaultBundle2ErrorsLocaleNameProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle2ErrorsLocaleNameProperty", "");
        }

    public void testErrorsAlternateBundle0ErrorsLocaleNameProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle0ErrorsLocaleNameProperty", "");
        }
    public void testErrorsAlternateBundle2ErrorsLocaleNameProperty() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle2ErrorsLocaleNameProperty", "");
        }

    public void testErrorsDefaultBundle0ErrorsLocaleNameProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle0ErrorsLocaleNameProperty_fr", "");
        }
    public void testErrorsDefaultBundle2ErrorsLocaleNameProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsDefaultBundle2ErrorsLocaleNameProperty_fr", "");
        }

    public void testErrorsAlternateBundle0ErrorsLocaleNameProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle0ErrorsLocaleNameProperty_fr", "");
        }
    public void testErrorsAlternateBundle2ErrorsLocaleNameProperty_fr() throws Exception {
        pageContext.setAttribute("MY_LOCALE_KEY", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testErrorsAlternateBundle2ErrorsLocaleNameProperty_fr", "");
        }



}
