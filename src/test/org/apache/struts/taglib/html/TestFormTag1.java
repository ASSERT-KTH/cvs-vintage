/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestFormTag1.java,v 1.9 2004/07/21 22:34:09 niallp Exp $
 * $Revision: 1.9 $
 * $Date: 2004/07/21 22:34:09 $
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
import org.apache.struts.taglib.SimpleBeanForTesting;

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.html.FormTag</code> class.
 *
 */
public class TestFormTag1 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestFormTag1(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestFormTag1.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestFormTag1.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting("Test Value"), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestFormTag1.jsp");
    }

    /*
     * Testing FormTag.
     */
    public void testFormAction() throws Exception {
        runMyTest("testFormAction", "");
        }
    public void testFormActionEnctype() throws Exception {
        runMyTest("testFormActionEnctype", "");
        }
    public void testFormActionAcceptCharset() throws Exception {
        runMyTest("testFormActionAcceptCharset", "");
        }
    public void testFormActionFocus() throws Exception {
        runMyTest("testFormActionFocus", "");
        }
    public void testFormActionFocusIndexed() throws Exception {
        runMyTest("testFormActionFocusIndexed", "");
        }
    public void testFormActionMethod1() throws Exception {
        runMyTest("testFormActionMethod1", "");
        }
    public void testFormActionMethod2() throws Exception {
        runMyTest("testFormActionMethod2", "");
        }
    public void testFormActionMethod3() throws Exception {
        runMyTest("testFormActionMethod3", "");
        }
    public void testFormActionMethod4() throws Exception {
        runMyTest("testFormActionMethod4", "");
        }
    public void testFormActionMethod5() throws Exception {
        runMyTest("testFormActionMethod5", "");
        }
    public void testFormActionOnreset() throws Exception {
        runMyTest("testFormActionOnreset", "");
        }
    public void testFormActionOnsubmit() throws Exception {
        runMyTest("testFormActionOnsubmit", "");
        }
    public void testFormActionStyle() throws Exception {
        runMyTest("testFormActionStyle", "");
        }
    public void testFormActionStyleClass() throws Exception {
        runMyTest("testFormActionStyleClass", "");
        }
    public void testFormActionStyleId() throws Exception {
        runMyTest("testFormActionStyleId", "");
        }
    public void testFormActionTarget() throws Exception {
        runMyTest("testFormActionTarget", "");
        }






}
