/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/bean/TestMessageTag.java,v 1.9 2004/01/13 12:48:53 husted Exp $
 * $Revision: 1.9 $
 * $Date: 2004/01/13 12:48:53 $
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
package org.apache.struts.taglib.bean;

import java.util.Locale;

import javax.servlet.jsp.PageContext;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebResponse;
import org.apache.struts.Globals;
import org.apache.struts.taglib.SimpleBeanForTesting;
import org.apache.commons.lang.StringUtils;

/**
  * These tests attempt to cover every single possible configuration of the
  * org.apache.struts.taglib.bean.MessageTag
  *
  * I've tried to describe what I'm testing as best as possible by the method names.
  * To see how I'm testing, refer to the jsp file that these tests forward to.
  *
  * All of these tests depend on a value being correctly written on the repose, then
  * checked here in endXXX method.
  *
  */
public class TestMessageTag extends JspTestCase {

    protected final static String TEST_KEY = "BeanKey";
    protected final static String TEST_VAL = "Testing Message";

    public TestMessageTag(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestMessageTag.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestMessageTag.class);
    }

    private void runMyTest(String whichTest, Locale locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, locale, PageContext.SESSION_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/bean/TestMessageTag.jsp");
    }

        private void formatAndTest(String compare, String output) {
                //fix for introduced carriage return / line feeds
                output = StringUtils.replace(output,"\r","");
                output = StringUtils.replace(output,"\n","");
                output = output.trim();
                //System.out.println("Testing [" + compare + "] == [" + output + "]");
            assertEquals(compare, output);
        }

    /*
     * ===========================================================
     * Testing MessageTag (these comments serve as a divider of
     *                     functionality being tested)
     *
     * Section: NoArg
     * Locale:  (default)
     * ===========================================================
     */


    public void testMessageTagNoArgKeyNoScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyNoScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeyApplicationScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyApplicationScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeySessionScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeySessionScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeySessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeyRequestScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyRequestScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }


    public void testMessageTagNoArgKeyNoScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyNoScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeyApplicationScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyApplicationScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeySessionScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeySessionScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeySessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgKeyRequestScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgKeyRequestScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgKeyRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }



    public void testMessageTagNoArgNameNoScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameNoScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameApplicationScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameApplicationScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameSessionScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameSessionScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameSessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameRequestScopeDefaultBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameRequestScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }


    public void testMessageTagNoArgNameNoScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameNoScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameApplicationScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameApplicationScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameSessionScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameSessionScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameSessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNameRequestScopeAlternateBundle() throws Exception {
     runMyTest("testMessageTagNoArgNameRequestScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNameRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }




    public void testMessageTagNoArgNamePropertyNoScopeDefaultBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyNoScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertyApplicationScopeDefaultBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message"), PageContext.APPLICATION_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyApplicationScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertySessionScopeDefaultBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message"), PageContext.SESSION_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertySessionScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertySessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertyRequestScopeDefaultBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyRequestScopeDefaultBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }


    public void testMessageTagNoArgNamePropertyNoScopeAlternateBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyNoScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertyApplicationScopeAlternateBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message"), PageContext.APPLICATION_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyApplicationScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertySessionScopeAlternateBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message"), PageContext.SESSION_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertySessionScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertySessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }

    public void testMessageTagNoArgNamePropertyRequestScopeAlternateBundle() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTagNoArgNamePropertyRequestScopeAlternateBundle", new Locale("",""));
        }
        public void endMessageTagNoArgNamePropertyRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL, response.getText());
        }








}
