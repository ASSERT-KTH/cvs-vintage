/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestMultiboxTag4.java,v 1.6 2004/03/14 06:23:41 sraeburn Exp $
 * $Revision: 1.6 $
 * $Date: 2004/03/14 06:23:41 $
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
 * <code>org.apache.struts.taglib.html.MultiboxTag</code> class.
 *  NOTE - These tests were separated into 4 files each because of the
 *         size of the jsp. (not playing well with Tomcat 3.3
 *
 *  These tests are numbered as such:
 *
 *  1 thru 4 test a single checkbox
 *  TestMultiboxTag1 - These test validate true (a value was in the array) on our form.
 *  TestMultiboxTag2 - Same as 1, but using BodyContent instead of value attribute
 *
 *  TestMultiboxTag3 - These test validate true (a value was in the array) on our form.
 *  TestMultiboxTag4 - Same as 3, but using BodyContent instead of value attribute
 *
 *  5 thru 8 test multiple checkboxes
 *  TestMultiboxTag5 - These test validate true (a value was in the array) on our form.
 *  TestMultiboxTag6 - Same as 5, but using BodyContent instead of value attribute
 *
 *  TestMultiboxTag7 - These test validate true (a value was in the array) on our form.
 *  TestMultiboxTag8 - Same as 7, but using BodyContent instead of value attribute
 *
 *
 */
public class TestMultiboxTag4 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestMultiboxTag4(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestMultiboxTag4.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestMultiboxTag4.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY,
                        new Locale(locale, locale),
                        PageContext.SESSION_SCOPE);

                String[] s = new String[7];
                for(int i = 1; i < 7; i++){
                        s[i] = "value" + i;
                }
                SimpleBeanForTesting sbft = new SimpleBeanForTesting(s);

        pageContext.setAttribute(Constants.BEAN_KEY, sbft, PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestMultiboxTag4.jsp");
    }

    /*
     * Testing MultiboxTag.
     */
    public void testMultiboxBodyPropertyFalse() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalse", "");
        }
    public void testMultiboxBodyPropertyFalseAccesskey() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAccesskey", "");
        }
    public void testMultiboxBodyPropertyFalseAlt() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAlt", "");
        }
    public void testMultiboxBodyPropertyFalseAltKey1() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAltKey1", "");
        }
    public void testMultiboxBodyPropertyFalseAltKey2() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAltKey2", "");
        }
    public void testMultiboxBodyPropertyFalseAltKey_fr1() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAltKey1_fr", "fr");
        }
    public void testMultiboxBodyPropertyFalseAltKey_fr2() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseAltKey2_fr", "fr");
        }
    public void testMultiboxBodyPropertyFalseDisabled_False() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseDisabled_True", "");
        }
    public void testMultiboxBodyPropertyFalseDisabled_False1() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseDisabled_False1", "");
        }
    public void testMultiboxBodyPropertyFalseDisabled_False2() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseDisabled_False2", "");
        }
    public void testMultiboxBodyPropertyFalseOnblur() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnblur", "");
        }

    public void testMultiboxBodyPropertyFalseOnchange() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnchange", "");
        }

    public void testMultiboxBodyPropertyFalseOnclick() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnclick", "");
        }

    public void testMultiboxBodyPropertyFalseOndblclick() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOndblclick", "");
        }

    public void testMultiboxBodyPropertyFalseOnfocus() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnfocus", "");
        }

    public void testMultiboxBodyPropertyFalseOnkeydown() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnkeydown", "");
        }

    public void testMultiboxBodyPropertyFalseOnkeypress() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnkeypress", "");
        }

    public void testMultiboxBodyPropertyFalseOnkeyup() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnkeyup", "");
        }

    public void testMultiboxBodyPropertyFalseOnmousedown() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnmousedown", "");
        }

    public void testMultiboxBodyPropertyFalseOnmousemove() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnmousemove", "");
        }

    public void testMultiboxBodyPropertyFalseOnmouseout() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnmouseout", "");
        }

    public void testMultiboxBodyPropertyFalseOnmouseover() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnmouseover", "");
        }

    public void testMultiboxBodyPropertyFalseOnmouseup() throws Exception {
        runMyTest("testMultiboxBodyPropertyFalseOnmouseup", "");
        }

}
