/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestCheckboxTag1.java,v 1.12 2004/09/24 01:17:57 niallp Exp $
 * $Revision: 1.12 $
 * $Date: 2004/09/24 01:17:57 $
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
 * <code>org.apache.struts.taglib.html.CheckboxTag</code> class.
 *  NOTE - These tests were separated into 4 files each because of the
 *         size of the jsp. (not playing well with Tomcat 3.3
 *
 *  These tests are numbered as such:
 *
 *  TestCheckboxTag(1 and 2) - These test using a boolean property
 *                             set to true on our form.
 *
 *  TestCheckboxTag(3 and 4) - These test using a boolean property
 *                             set to false on our form.
 *
 */
public class TestCheckboxTag1 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestCheckboxTag1(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestCheckboxTag1.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestCheckboxTag1.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting(true), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestCheckboxTag1.jsp");
    }

    /*
     * Testing CheckboxTag.
     */
    public void testCheckboxPropertybooleanTrue() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrue", "");
        }
    public void testCheckboxPropertybooleanTrueAccesskey() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAccesskey", "");
        }
    public void testCheckboxPropertybooleanTrueAlt() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAlt", "");
        }
    public void testCheckboxPropertybooleanTrueAltKey1() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAltKey1", "");
        }
    public void testCheckboxPropertybooleanTrueAltKey2() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAltKey2", "");
        }
    public void testCheckboxPropertybooleanTrueAltKey3() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAltKey3", "");
    }
    public void testCheckboxPropertybooleanTrueAltKey_fr1() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAltKey1_fr", "fr");
        }
    public void testCheckboxPropertybooleanTrueAltKey_fr2() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueAltKey2_fr", "fr");
        }
    public void testCheckboxPropertybooleanTrueDisabled_True() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueDisabled_True", "");
        }
    public void testCheckboxPropertybooleanTrueDisabled_False1() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueDisabled_False1", "");
        }
    public void testCheckboxPropertybooleanTrueDisabled_False2() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueDisabled_False2", "");
        }
    public void testCheckboxPropertybooleanTrueOnblur() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnblur", "");
        }

    public void testCheckboxPropertybooleanTrueOnchange() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnchange", "");
        }

    public void testCheckboxPropertybooleanTrueOnclick() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnclick", "");
        }

    public void testCheckboxPropertybooleanTrueOndblclick() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOndblclick", "");
        }

    public void testCheckboxPropertybooleanTrueOnfocus() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnfocus", "");
        }

    public void testCheckboxPropertybooleanTrueOnkeydown() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnkeydown", "");
        }

    public void testCheckboxPropertybooleanTrueOnkeypress() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnkeypress", "");
        }

    public void testCheckboxPropertybooleanTrueOnkeyup() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnkeyup", "");
        }

    public void testCheckboxPropertybooleanTrueOnmousedown() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnmousedown", "");
        }

    public void testCheckboxPropertybooleanTrueOnmousemove() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnmousemove", "");
        }

    public void testCheckboxPropertybooleanTrueOnmouseout() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnmouseout", "");
        }

    public void testCheckboxPropertybooleanTrueOnmouseover() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnmouseover", "");
        }

    public void testCheckboxPropertybooleanTrueOnmouseup() throws Exception {
        runMyTest("testCheckboxPropertybooleanTrueOnmouseup", "");
        }

}
