/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestTextareaTag2.java,v 1.1 2004/09/23 00:37:23 niallp Exp $
 * $Revision: 1.1 $
 * $Date: 2004/09/23 00:37:23 $
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * <code>org.apache.struts.taglib.html.TextareaTag</code> class.
 *
 */
public class TestTextareaTag2 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestTextareaTag2(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestTextareaTag2.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestTextareaTag2.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting("Test Value"), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestTextareaTag2.jsp");
    }

    /*
     * Testing TextareaTag.
     */

    public void testTextareaPropertyReadonly() throws Exception {
        runMyTest("testTextareaPropertyReadonly", "");
    }
    public void testTextareaPropertyRows() throws Exception {
        runMyTest("testTextareaPropertyRows", "");
    }
    public void testTextareaPropertyStyle() throws Exception {
        runMyTest("testTextareaPropertyStyle", "");
    }
    public void testTextareaPropertyStyleClass() throws Exception {
        runMyTest("testTextareaPropertyStyleClass", "");
    }
    public void testTextareaPropertyStyleId() throws Exception {
        runMyTest("testTextareaPropertyStyleId", "");
    }
    public void testTextareaPropertyTitle() throws Exception {
        runMyTest("testTextareaPropertyTitle", "");
    }
    public void testTextareaPropertyTitleKey() throws Exception {
        runMyTest("testTextareaPropertyTitleKey", "");
    }
    public void testTextareaPropertyTitleKey_fr() throws Exception {
        runMyTest("testTextareaPropertyTitleKey_fr", "fr");
    }
    public void testTextareaPropertyValue() throws Exception {
        runMyTest("testTextareaPropertyValue", "");
    }
    public void testTextareaPropertyIndexedArray() throws Exception {
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        pageContext.setAttribute("lst", lst, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedArray", "");
    }
    public void testTextareaPropertyIndexedArrayProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        sbft.setList(lst);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedArrayProperty", "");
    }
    public void testTextareaPropertyIndexedMap() throws Exception {
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        pageContext.setAttribute("lst", map, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedMap", "");
    }
    public void testTextareaPropertyIndexedMapProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        sbft.setMap(map);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedMapProperty", "");
    }
    public void testTextareaPropertyIndexedEnumeration() throws Exception {
        StringTokenizer st = new StringTokenizer("Test Message");
        pageContext.setAttribute("lst", st, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedEnumeration", "");
    }
    public void testTextareaPropertyIndexedEnumerationProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        StringTokenizer st = new StringTokenizer("Test Message");
        sbft.setEnumeration(st);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testTextareaPropertyIndexedEnumerationProperty", "");
    }

}
