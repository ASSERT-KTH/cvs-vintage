/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestImageTag8.java,v 1.6 2004/01/13 12:48:54 husted Exp $
 * $Revision: 1.6 $
 * $Date: 2004/01/13 12:48:54 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
 * <code>org.apache.struts.taglib.html.ImageTag</code> class.
 *
 */
public class TestImageTag8 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestImageTag8(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestImageTag8.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestImageTag8.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting("Test Value"), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestImageTag8.jsp");
    }

    /*
     * Testing ImageTag.
     */

//--------Testing attributes using page------

    public void testImageSrcKeyOnmousedown() throws Exception {
        runMyTest("testImageSrcKeyOnmousedown", "");
    }

    public void testImageSrcKeyOnmousemove() throws Exception {
        runMyTest("testImageSrcKeyOnmousemove", "");
    }

    public void testImageSrcKeyOnmouseout() throws Exception {
        runMyTest("testImageSrcKeyOnmouseout", "");
    }

    public void testImageSrcKeyOnmouseover() throws Exception {
        runMyTest("testImageSrcKeyOnmouseover", "");
    }

    public void testImageSrcKeyOnmouseup() throws Exception {
        runMyTest("testImageSrcKeyOnmouseup", "");
    }

    public void testImageSrcKeyProperty() throws Exception {
        runMyTest("testImageSrcKeyProperty", "");
    }

    public void testImageSrcKeyStyle() throws Exception {
        runMyTest("testImageSrcKeyStyle", "");
    }

    public void testImageSrcKeyStyleClass() throws Exception {
        runMyTest("testImageSrcKeyStyleClass", "");
    }

    public void testImageSrcKeyStyleId() throws Exception {
        runMyTest("testImageSrcKeyStyleId", "");
    }

    public void testImageSrcKeyTabindex() throws Exception {
        runMyTest("testImageSrcKeyTabindex", "");
    }

    public void testImageSrcKeyTitle() throws Exception {
        runMyTest("testImageSrcKeyTitle", "");
    }

    public void testImageSrcKeyTitleKeyDefaultBundle() throws Exception {
        runMyTest("testImageSrcKeyTitleKeyDefaultBundle", "");
    }

    public void testImageSrcKeyTitleKeyAlternateBundle() throws Exception {
        runMyTest("testImageSrcKeyTitleKeyAlternateBundle", "");
    }

    public void testImageSrcKeyTitleKeyDefaultBundle_fr() throws Exception {
        runMyTest("testImageSrcKeyTitleKeyDefaultBundle_fr", "fr");
    }

    public void testImageSrcKeyTitleKeyAlternateBundle_fr() throws Exception {
        runMyTest("testImageSrcKeyTitleKeyAlternateBundle_fr", "fr");
    }

    public void testImageSrcKeyIndexedArray() throws Exception {
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        pageContext.setAttribute("lst", lst, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedArray", "");
        }

    public void testImageSrcKeyIndexedArrayProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        ArrayList lst = new ArrayList();
        lst.add("Test Message");
        sbft.setList(lst);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedArrayProperty", "");
        }

    public void testImageSrcKeyIndexedMap() throws Exception {
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        pageContext.setAttribute("lst", map, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedMap", "");
        }

    public void testImageSrcKeyIndexedMapProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        HashMap map = new HashMap();
        map.put("tst1", "Test Message");
        sbft.setMap(map);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedMapProperty", "");
        }

    public void testImageSrcKeyIndexedEnumeration() throws Exception {
        StringTokenizer st = new StringTokenizer("Test Message");
        pageContext.setAttribute("lst", st, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedEnumeration", "");
        }

    public void testImageSrcKeyIndexedEnumerationProperty() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting();
        StringTokenizer st = new StringTokenizer("Test Message");
        sbft.setEnumeration(st);
        pageContext.setAttribute("lst", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testImageSrcKeyIndexedEnumerationProperty", "");
        }





}
