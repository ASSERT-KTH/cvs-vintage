/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestImgTag7.java,v 1.7 2004/03/14 06:23:41 sraeburn Exp $
 * $Revision: 1.7 $
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
 * <code>org.apache.struts.taglib.html.ImgTag</code> class.
 *
 */
public class TestImgTag7 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestImgTag7(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestImgTag7.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestImgTag7.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting("Test Value"), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestImgTag7.jsp");
    }

    /*
     * Testing ImgTag.
     */

//--------Testing attributes using page------

    public void testImgSrcKeyAlign1() throws Exception {
        runMyTest("testImgSrcKeyAlign1", "");
    }

    public void testImgSrcKeyAlign2() throws Exception {
        runMyTest("testImgSrcKeyAlign2", "");
    }

    public void testImgSrcKeyAlign3() throws Exception {
        runMyTest("testImgSrcKeyAlign3", "");
    }

    public void testImgSrcKeyAlign4() throws Exception {
        runMyTest("testImgSrcKeyAlign4", "");
    }

    public void testImgSrcKeyAlign5() throws Exception {
        runMyTest("testImgSrcKeyAlign5", "");
    }

    public void testImgSrcKeyAlign6() throws Exception {
        runMyTest("testImgSrcKeyAlign6", "");
    }

    public void testImgSrcKeyAlign7() throws Exception {
        runMyTest("testImgSrcKeyAlign7", "");
    }

    public void testImgSrcKeyAlign8() throws Exception {
        runMyTest("testImgSrcKeyAlign8", "");
    }

    public void testImgSrcKeyAlign9() throws Exception {
        runMyTest("testImgSrcKeyAlign9", "");
    }

    public void testImgSrcKeyAlign10() throws Exception {
        runMyTest("testImgSrcKeyAlign10", "");
    }

    public void testImgSrcKeyAlt() throws Exception {
        runMyTest("testImgSrcKeyAlt", "");
    }

    public void testImgSrcKeyAltKeyDefaultBundle() throws Exception {
        runMyTest("testImgSrcKeyAltKeyDefaultBundle", "");
    }

    public void testImgSrcKeyAltKeyAlternateBundle() throws Exception {
        runMyTest("testImgSrcKeyAltKeyAlternateBundle", "");
    }

    public void testImgSrcKeyAltKeyDefaultBundle_fr() throws Exception {
        runMyTest("testImgSrcKeyAltKeyDefaultBundle_fr", "fr");
    }

    public void testImgSrcKeyAltKeyAlternateBundle_fr() throws Exception {
        runMyTest("testImgSrcKeyAltKeyAlternateBundle_fr", "fr");
    }

    public void testImgSrcKeyBorder() throws Exception {
        runMyTest("testImgSrcKeyBorder", "");
    }

    public void testImgSrcKeyHeight1() throws Exception {
        runMyTest("testImgSrcKeyHeight1", "");
    }

    public void testImgSrcKeyHeight2() throws Exception {
        runMyTest("testImgSrcKeyHeight2", "");
    }

    public void testImgSrcKeyHspace() throws Exception {
        runMyTest("testImgSrcKeyHspace", "");
    }

    public void testImgSrcKeyImageName() throws Exception {
        runMyTest("testImgSrcKeyImageName", "");
    }

    public void testImgSrcKeyImageIsmap() throws Exception {
        runMyTest("testImgSrcKeyImageIsmap", "");
    }

    public void testImgSrcKeyLocale() throws Exception {
        pageContext.setAttribute("secret locale", new Locale("fr", "fr"), PageContext.SESSION_SCOPE);
        runMyTest("testImgSrcKeyLocale", "");
    }

}
