/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestLinkTag6.java,v 1.5 2004/01/10 21:03:34 dgraham Exp $
 * $Revision: 1.5 $
 * $Date: 2004/01/10 21:03:34 $
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
 *    any, must include the following acknowlegement:
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
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import java.util.Locale;

import javax.servlet.jsp.PageContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.struts.Globals;
import org.apache.struts.taglib.SimpleBeanForTesting;

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.html.FrameTag</code> class.
 *
 */
public class TestLinkTag6 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestLinkTag6(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestLinkTag6.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestLinkTag6.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        pageContext.setAttribute(Constants.BEAN_KEY, new SimpleBeanForTesting("Test Value"), PageContext.REQUEST_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestLinkTag6.jsp");
    }

    /*
     * Testing FrameTag.
     */

//--------Testing attributes using forward------
    public void testLinkHrefOnblur() throws Exception {
        runMyTest("testLinkHrefOnblur", "");
    }

    public void testLinkHrefOnclick() throws Exception {
        runMyTest("testLinkHrefOnclick", "");
    }

    public void testLinkHrefOndblclick() throws Exception {
        runMyTest("testLinkHrefOndblclick", "");
    }

    public void testLinkHrefOnfocus() throws Exception {
        runMyTest("testLinkHrefOnfocus", "");
    }

    public void testLinkHrefOnkeydown() throws Exception {
        runMyTest("testLinkHrefOnkeydown", "");
    }

    public void testLinkHrefOnkeypress() throws Exception {
        runMyTest("testLinkHrefOnkeypress", "");
    }

    public void testLinkHrefOnkeyup() throws Exception {
        runMyTest("testLinkHrefOnkeyup", "");
    }

    public void testLinkHrefOnmousedown() throws Exception {
        runMyTest("testLinkHrefOnmousedown", "");
    }

    public void testLinkHrefOnmousemove() throws Exception {
        runMyTest("testLinkHrefOnmousemove", "");
    }

    public void testLinkHrefOnmouseout() throws Exception {
        runMyTest("testLinkHrefOnmouseout", "");
    }

    public void testLinkHrefOnmouseover() throws Exception {
        runMyTest("testLinkHrefOnmouseover", "");
    }

    public void testLinkHrefOnmouseup() throws Exception {
        runMyTest("testLinkHrefOnmouseup", "");
    }

    public void testLinkHrefParamIdParamNameNoScope() throws Exception {
                pageContext.setAttribute("paramName", "paramValue", PageContext.REQUEST_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameNoScope", "");
    }

    public void testLinkHrefParamIdParamNameParamPropertyNoScope() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting("paramPropertyValue");
                pageContext.setAttribute("testingParamProperty", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameParamPropertyNoScope", "");
    }

    public void testLinkHrefParamIdParamNameApplicationScope() throws Exception {
                pageContext.setAttribute("paramName", "paramValue", PageContext.APPLICATION_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameApplicationScope", "");
    }

    public void testLinkHrefParamIdParamNameParamPropertyApplicationScope() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting("paramPropertyValue");
                pageContext.setAttribute("testingParamProperty", sbft, PageContext.APPLICATION_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameParamPropertyApplicationScope", "");
    }

    public void testLinkHrefParamIdParamNameSessionScope() throws Exception {
                pageContext.setAttribute("paramName", "paramValue", PageContext.SESSION_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameSessionScope", "");
    }

    public void testLinkHrefParamIdParamNameParamPropertySessionScope() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting("paramPropertyValue");
                pageContext.setAttribute("testingParamProperty", sbft, PageContext.SESSION_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameParamPropertySessionScope", "");
    }

    public void testLinkHrefParamIdParamNameRequestScope() throws Exception {
                pageContext.setAttribute("paramName", "paramValue", PageContext.REQUEST_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameRequestScope", "");
    }

    public void testLinkHrefParamIdParamNameParamPropertyRequestScope() throws Exception {
        SimpleBeanForTesting sbft = new SimpleBeanForTesting("paramPropertyValue");
                pageContext.setAttribute("testingParamProperty", sbft, PageContext.REQUEST_SCOPE);
        runMyTest("testLinkHrefParamIdParamNameParamPropertyRequestScope", "");
    }


    public void testLinkHrefStyle() throws Exception {
        runMyTest("testLinkHrefStyle", "");
    }

    public void testLinkHrefStyleClass() throws Exception {
        runMyTest("testLinkHrefStyleClass", "");
    }

    public void testLinkHrefStyleId() throws Exception {
        runMyTest("testLinkHrefStyleId", "");
    }

    public void testLinkHrefTabIndex() throws Exception {
        runMyTest("testLinkHrefTabIndex", "");
    }

    public void testLinkHrefTarget() throws Exception {
        runMyTest("testLinkHrefTarget", "");
    }







    public void testLinkHrefTitle() throws Exception {
        runMyTest("testLinkHrefTitle", "");
    }

    public void testLinkHrefTitleKey() throws Exception {
        runMyTest("testLinkHrefTitleKey", "");
    }

    public void testLinkHrefTransaction() throws Exception {
        pageContext.setAttribute(Globals.TRANSACTION_TOKEN_KEY, "Some_Token_Here", PageContext.SESSION_SCOPE);
        runMyTest("testLinkHrefTransaction", "");
    }





}
