/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/html/TestButtonTag1.java,v 1.6 2003/12/26 22:10:32 germuska Exp $
 * $Revision: 1.6 $
 * $Date: 2003/12/26 22:10:32 $
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

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.html.ButtonTag</code> class.
 *
 * @author James Mitchell
 */
public class TestButtonTag1 extends JspTestCase {

    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestButtonTag1(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestButtonTag1.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestButtonTag1.class);
    }

    private void runMyTest(String whichTest, String locale) throws Exception {
        pageContext.setAttribute(Globals.LOCALE_KEY, new Locale(locale, locale), PageContext.SESSION_SCOPE);
        request.setAttribute("runTest", whichTest);
        pageContext.forward("/test/org/apache/struts/taglib/html/TestButtonTag1.jsp");    }

    /*
     * Testing ButtonTag.
     */
    public void testButtonProperty() throws Exception {
        runMyTest("testButtonProperty", "");
        }
    public void testButtonPropertyAccesskey() throws Exception {
        runMyTest("testButtonPropertyAccesskey", "");
        }
    public void testButtonPropertyAlt() throws Exception {
        runMyTest("testButtonPropertyAlt", "");
        }
    public void testButtonPropertyAltKey1() throws Exception {
        runMyTest("testButtonPropertyAltKey1", "");
        }
    public void testButtonPropertyAltKey2() throws Exception {
        runMyTest("testButtonPropertyAltKey2", "");
        }
    public void testButtonPropertyAltKey_fr1() throws Exception {
        runMyTest("testButtonPropertyAltKey1_fr", "fr");
        }
    public void testButtonPropertyAltKey_fr2() throws Exception {
        runMyTest("testButtonPropertyAltKey2_fr", "fr");
        }
    public void testButtonPropertyDisabled_True() throws Exception {
        runMyTest("testButtonPropertyDisabled_True", "");
        }
    public void testButtonPropertyDisabled_False1() throws Exception {
        runMyTest("testButtonPropertyDisabled_False1", "");
        }
    public void testButtonPropertyDisabled_False2() throws Exception {
        runMyTest("testButtonPropertyDisabled_False2", "");
        }
    public void testButtonPropertyOnblur() throws Exception {
        runMyTest("testButtonPropertyOnblur", "");
        }

    public void testButtonPropertyOnchange() throws Exception {
        runMyTest("testButtonPropertyOnchange", "");
        }

    public void testButtonPropertyOnclick() throws Exception {
        runMyTest("testButtonPropertyOnclick", "");
        }

    public void testButtonPropertyOndblclick() throws Exception {
        runMyTest("testButtonPropertyOndblclick", "");
        }

    public void testButtonPropertyOnfocus() throws Exception {
        runMyTest("testButtonPropertyOnfocus", "");
        }

    public void testButtonPropertyOnkeydown() throws Exception {
        runMyTest("testButtonPropertyOnkeydown", "");
        }

    public void testButtonPropertyOnkeypress() throws Exception {
        runMyTest("testButtonPropertyOnkeypress", "");
        }

    public void testButtonPropertyOnkeyup() throws Exception {
        runMyTest("testButtonPropertyOnkeyup", "");
        }

    public void testButtonPropertyOnmousedown() throws Exception {
        runMyTest("testButtonPropertyOnmousedown", "");
        }

    public void testButtonPropertyOnmousemove() throws Exception {
        runMyTest("testButtonPropertyOnmousemove", "");
        }

    public void testButtonPropertyOnmouseout() throws Exception {
        runMyTest("testButtonPropertyOnmouseout", "");
        }

    public void testButtonPropertyOnmouseover() throws Exception {
        runMyTest("testButtonPropertyOnmouseover", "");
        }

    public void testButtonPropertyOnmouseup() throws Exception {
        runMyTest("testButtonPropertyOnmouseup", "");
        }

}
