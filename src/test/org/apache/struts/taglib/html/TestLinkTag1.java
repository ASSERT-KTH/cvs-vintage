/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestLinkTag1.java,v 1.7 2004/03/14 06:23:40 sraeburn Exp $
 * $Revision: 1.7 $
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
 * <code>org.apache.struts.taglib.html.LinkTag</code> class.
 *
 */
public class TestLinkTag1 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestLinkTag1(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestLinkTag1.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestLinkTag1.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestLinkTag1.jsp");
    }

    /*
     * Testing LinkTag.
     */

//--------Testing attributes using forward------

    public void testLinkForward() throws Exception {
        runMyTest("testLinkForward", "");
    }

    public void testLinkForwardAccesskey() throws Exception {
        runMyTest("testLinkForwardAccesskey", "");
    }

    public void testLinkForwardAnchor() throws Exception {
        runMyTest("testLinkForwardAnchor", "");
    }

    public void testLinkForwardIndexedArray() throws Exception {
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        pageContext.setAttribute("lst", lst, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedArray", "");
        }

    public void testLinkForwardIndexedArrayProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        sbft.setList(lst);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedArrayProperty", "");
        }

    public void testLinkForwardIndexedMap() throws Exception {
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        pageContext.setAttribute("lst", map, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedMap", "");
        }

    public void testLinkForwardIndexedMapProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        sbft.setMap(map);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedMapProperty", "");
        }

    public void testLinkForwardIndexedEnumeration() throws Exception {
        StringTokenizer st = new StringTokenizer("Test Message");
        pageContext.setAttribute("lst", st, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedEnumeration", "");
        }

    public void testLinkForwardIndexedEnumerationProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        StringTokenizer st = new StringTokenizer("Test Message");
        sbft.setEnumeration(st);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedEnumerationProperty", "");
        }


    public void testLinkForwardIndexedAlternateIdArray() throws Exception {
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        pageContext.setAttribute("lst", lst, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdArray", "");
        }

    public void testLinkForwardIndexedAlternateIdArrayProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        sbft.setList(lst);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdArrayProperty", "");
        }

    public void testLinkForwardIndexedAlternateIdMap() throws Exception {
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        pageContext.setAttribute("lst", map, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdMap", "");
        }

    public void testLinkForwardIndexedAlternateIdMapProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        sbft.setMap(map);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdMapProperty", "");
        }

    public void testLinkForwardIndexedAlternateIdEnumeration() throws Exception {
        StringTokenizer st = new StringTokenizer("Test Message");
        pageContext.setAttribute("lst", st, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdEnumeration", "");
        }

    public void testLinkForwardIndexedAlternateIdEnumerationProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        StringTokenizer st = new StringTokenizer("Test Message");
        sbft.setEnumeration(st);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkForwardIndexedAlternateIdEnumerationProperty", "");
        }

    public void testLinkForwardLinkName() throws Exception {
       runMyTest("testLinkForwardLinkName", "");
    }

    public void testLinkForwardNameNoScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                pageContext.setAttribute("paramMap", map, PageContext.REQUEST_SCOPE);
       runMyTest("testLinkForwardNameNoScope", "");
    }

    public void testLinkForwardNamePropertyNoScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                SimpleBeanForTesting sbft = new SimpleBeanForTesting(map);
                pageContext.setAttribute("paramPropertyMap", sbft, PageContext.REQUEST_SCOPE);
       runMyTest("testLinkForwardNamePropertyNoScope", "");
    }

    public void testLinkForwardNameApplicationScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                pageContext.setAttribute("paramMap", map, PageContext.APPLICATION_SCOPE);
       runMyTest("testLinkForwardNameApplicationScope", "");
    }

    public void testLinkForwardNamePropertyApplicationScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                SimpleBeanForTesting sbft = new SimpleBeanForTesting(map);
                pageContext.setAttribute("paramPropertyMap", sbft, PageContext.APPLICATION_SCOPE);
       runMyTest("testLinkForwardNamePropertyApplicationScope", "");
    }

    public void testLinkForwardNameSessionScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                pageContext.setAttribute("paramMap", map, PageContext.SESSION_SCOPE);
       runMyTest("testLinkForwardNameSessionScope", "");
    }

    public void testLinkForwardNamePropertySessionScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                SimpleBeanForTesting sbft = new SimpleBeanForTesting(map);
                pageContext.setAttribute("paramPropertyMap", sbft, PageContext.SESSION_SCOPE);
       runMyTest("testLinkForwardNamePropertySessionScope", "");
    }

    public void testLinkForwardNameRequestScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                pageContext.setAttribute("paramMap", map, PageContext.REQUEST_SCOPE);
       runMyTest("testLinkForwardNameRequestScope", "");
    }

    public void testLinkForwardNamePropertyRequestScope() throws Exception {
                HashMap map = new HashMap();
                map.put("param1","value1");
                map.put("param2","value2");
                map.put("param3","value3");
                map.put("param4","value4");
                SimpleBeanForTesting sbft = new SimpleBeanForTesting(map);
                pageContext.setAttribute("paramPropertyMap", sbft, PageContext.REQUEST_SCOPE);
       runMyTest("testLinkForwardNamePropertyRequestScope", "");
    }

}
