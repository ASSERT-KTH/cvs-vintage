/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestButtonTag2.java,v 1.11 2004/03/14 06:23:40 sraeburn Exp $
 * $Revision: 1.11 $
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.jsp.PageContext;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.struts.Globals;
import org.apache.struts.taglib.SimpleBeanForTesting;

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.html.ButtonTag</code> class.
 *
 */
public class TestButtonTag2 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestButtonTag2(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestButtonTag2.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestButtonTag2.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestButtonTag2.jsp");
    }

    /*
     * Testing ButtonTag.
     */

    public void testButtonPropertyStyle() throws Exception {
        runMyTest("testButtonPropertyStyle", "");
        }

    public void testButtonPropertyStyleClass() throws Exception {
        runMyTest("testButtonPropertyStyleClass", "");
        }

    public void testButtonPropertyStyleId() throws Exception {
        runMyTest("testButtonPropertyStyleId", "");
        }

    public void testButtonPropertyTabindex() throws Exception {
        runMyTest("testButtonPropertyTabindex", "");
        }

    public void testButtonPropertyTitle() throws Exception {
        runMyTest("testButtonPropertyTitle", "");
        }

    public void testButtonPropertyTitleKey() throws Exception {
        runMyTest("testButtonPropertyTitleKey", "");
        }

    public void testButtonPropertyTitleKey_fr() throws Exception {
        runMyTest("testButtonPropertyTitleKey_fr", "fr");
        }

    public void testButtonPropertyValue() throws Exception {
        runMyTest("testButtonPropertyValue", "");
        }

    public void testButtonPropertyBodyContent() throws Exception {
        runMyTest("testButtonPropertyBodyContent", "");
        }

    public void testButtonPropertyBodyContentMessageKey() throws Exception {
        runMyTest("testButtonPropertyBodyContentMessageKey", "");
        }

    public void testButtonPropertyBodyContentMessageKey_fr() throws Exception {
        runMyTest("testButtonPropertyBodyContentMessageKey_fr", "fr");
        }

    public void testButtonPropertyIndexedArray() throws Exception {
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        pageContext.setAttribute("lst", lst, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedArray", "");
        }

    public void testButtonPropertyIndexedArrayProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        sbft.setList(lst);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedArrayProperty", "");
        }

    public void testButtonPropertyIndexedMap() throws Exception {
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        pageContext.setAttribute("lst", map, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedMap", "");
        }

    public void testButtonPropertyIndexedMapProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        sbft.setMap(map);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedMapProperty", "");
        }

    public void testButtonPropertyIndexedEnumeration() throws Exception {
        StringTokenizer st = new StringTokenizer("Test Message");
        pageContext.setAttribute("lst", st, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedEnumeration", "");
        }

    public void testButtonPropertyIndexedEnumerationProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        StringTokenizer st = new StringTokenizer("Test Message");
        sbft.setEnumeration(st);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testButtonPropertyIndexedEnumerationProperty", "");
        }



}
