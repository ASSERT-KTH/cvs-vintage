/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/bean/TestMessageTag2_fr.java,v 1.8 2004/01/10 21:03:34 dgraham Exp $
 * $Revision: 1.8 $
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
public class TestMessageTag2_fr extends JspTestCase {

    protected final static String TEST_KEY = "BeanKey";
    protected final static String TEST_VAL_FR = "Message D'Essai 1 2";

    public TestMessageTag2_fr(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestMessageTag2_fr.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestMessageTag2_fr.class);
    }

    private void runMyTest(String whichTest, Locale locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, locale, PageContext.SESSION_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/bean/TestMessageTag2.jsp");
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
     * Section: 2 Args
     * Locale:  (default)
     * ===========================================================
     */


    public void testMessageTag2ArgKeyNoScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyNoScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeyApplicationScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyApplicationScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeySessionScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeySessionScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeySessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeyRequestScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyRequestScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }


    public void testMessageTag2ArgKeyNoScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyNoScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeyApplicationScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyApplicationScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeySessionScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeySessionScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeySessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgKeyRequestScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgKeyRequestScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgKeyRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }



    public void testMessageTag2ArgNameNoScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameNoScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameApplicationScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameApplicationScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameSessionScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameSessionScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameSessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameRequestScopeDefaultBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameRequestScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }


    public void testMessageTag2ArgNameNoScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameNoScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameApplicationScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameApplicationScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameSessionScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameSessionScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameSessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNameRequestScopeAlternateBundle_fr() throws Exception {
     runMyTest("testMessageTag2ArgNameRequestScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNameRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }




    public void testMessageTag2ArgNamePropertyNoScopeDefaultBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message.2"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyNoScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyNoScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertyApplicationScopeDefaultBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message.2"), PageContext.APPLICATION_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyApplicationScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyApplicationScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertySessionScopeDefaultBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message.2"), PageContext.SESSION_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertySessionScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertySessionScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertyRequestScopeDefaultBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("default.bundle.message.2"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyRequestScopeDefaultBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyRequestScopeDefaultBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }


    public void testMessageTag2ArgNamePropertyNoScopeAlternateBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message.2"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyNoScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyNoScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertyApplicationScopeAlternateBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message.2"), PageContext.APPLICATION_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyApplicationScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyApplicationScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertySessionScopeAlternateBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message.2"), PageContext.SESSION_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertySessionScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertySessionScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }

    public void testMessageTag2ArgNamePropertyRequestScopeAlternateBundle_fr() throws Exception {
        pageContext.setAttribute("key", new SimpleBeanForTesting("alternate.bundle.message.2"), PageContext.REQUEST_SCOPE);
     runMyTest("testMessageTag2ArgNamePropertyRequestScopeAlternateBundle", new Locale("fr","fr"));
        }
        public void endMessageTag2ArgNamePropertyRequestScopeAlternateBundle(WebResponse response){
                formatAndTest(TEST_VAL_FR, response.getText());
        }


}
