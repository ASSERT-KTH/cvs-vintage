/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/taglib/logic/TestLessEqualTag.java,v 1.6 2004/03/14 06:23:43 sraeburn Exp $
 * $Revision: 1.6 $
 * $Date: 2004/03/14 06:23:43 $
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
package org.apache.struts.taglib.logic;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.JspTestCase;
import org.apache.cactus.WebRequest;
import org.apache.struts.util.LabelValueBean;

/**
 * Suite of unit tests for the
 * <code>org.apache.struts.taglib.logic.LessEqualTag</code> class.
 *
 */
public class TestLessEqualTag extends JspTestCase {
	
    protected final static String COOKIE_KEY = "org.apache.struts.taglib.logic.COOKIE_KEY";
    protected final static String HEADER_KEY = "org.apache.struts.taglib.logic.HEADER_KEY";
    protected final static String PARAMETER_KEY = "org.apache.struts.taglib.logic.PARAMETER_KEY";
    protected final static String GREATER_VAL = "5";
    protected final static String LESSER_VAL = "5";


    /**
     * Defines the testcase name for JUnit.
     *
     * @param theName the testcase's name.
     */
    public TestLessEqualTag(String theName) {
        super(theName);
    }

    /**
     * Start the tests.
     *
     * @param theArgs the arguments. Not used
     */
    public static void main(String[] theArgs) {
        junit.awtui.TestRunner.main(new String[] {TestLessEqualTag.class.getName()});
    }

    /**
     * @return a test suite (<code>TestSuite</code>) that includes all methods
     *         starting with "test"
     */
    public static Test suite() {
        // All methods starting with "test" will be executed in the test suite.
        return new TestSuite(TestLessEqualTag.class);
    }

    //----- Test initApplication() method --------------------------------------
	
    /**
     * Create cookie for testCookiePresent method test.
    */
    public void beginCookieLessEqual(WebRequest testRequest) {
       testRequest.addCookie(COOKIE_KEY, GREATER_VAL);
    }

    /**
     * Create header for testHeaderLessEqual method test.
    */
    public void beginHeaderLessEqual(WebRequest testRequest) {
       testRequest.addHeader(HEADER_KEY, GREATER_VAL);
    }

    /**
     * Create header for testParameterLessEqual method test.
    */
    public void beginParameterLessEqual(WebRequest testRequest) {
       testRequest.addParameter(PARAMETER_KEY, GREATER_VAL);
    }

    /**
     * Verify the value stored in a cookie using <code>LessEqualTag</code>.
    */
    public void testCookieLessEqual() throws ServletException,  JspException {
        LessEqualTag ge = new LessEqualTag();
        ge.setPageContext(pageContext);
        ge.setCookie(COOKIE_KEY);
        ge.setValue(LESSER_VAL);

        assertTrue(
        	"Cookie Value (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }
    
    /**
     * Verify the value stored in header using <code>LessEqualTag</code>.
    */
    public void testHeaderLessEqual() throws ServletException,  JspException {
        LessEqualTag ge = new LessEqualTag();
        ge.setPageContext(pageContext);
        ge.setHeader(HEADER_KEY);
        ge.setValue(LESSER_VAL);

        assertTrue(
        	"Header Value (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }

    /**
     * Verify the value stored in parameter using <code>LessEqualTag</code>.
    */
    public void testParameterLessEqual() throws ServletException,  JspException {
        LessEqualTag ge = new LessEqualTag();
        ge.setPageContext(pageContext);
        ge.setParameter(PARAMETER_KEY);
        ge.setValue(LESSER_VAL);

        assertTrue(
        	"Parameter Value (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }
    

    /**
     * Testing <code>LessEqualTag</code> using name attribute in
     * the application scope.
    */
    public void testApplicationScopeNameLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testApplicationScopeNameLessEqual";
		Integer itgr = new Integer(GREATER_VAL);
		
		pageContext.setAttribute(testKey, itgr, PageContext.APPLICATION_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("application");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Application scope value from name (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }

    /**
     * Testing <code>LessEqualTag</code> using name attribute in
     * the session scope.
    */
    public void testSessionScopeNameLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testSessionScopeNameLessEqual";
		Integer itgr = new Integer(GREATER_VAL);
		
		pageContext.setAttribute(testKey, itgr, PageContext.SESSION_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("session");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Session scope value from name (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }

    /**
     * Testing <code>LessEqualTag</code> using name attribute in
     * the request scope.
    */
    public void testRequestScopeNameLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testRequestScopeNameLessEqual";
		Integer itgr = new Integer(GREATER_VAL);
		
		pageContext.setAttribute(testKey, itgr, PageContext.REQUEST_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("request");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Request scope value from name (" + GREATER_VAL + ") is less than or equal to value (" + LESSER_VAL + ")", 
        	ge.condition());
    }




    /**
     * Testing <code>LessEqualTag</code> using name and property attribute in
     * the application scope.
    */
    public void testApplicationScopePropertyLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testApplicationScopePropertyLessEqual";
		LabelValueBean lvb = new LabelValueBean("The Key", GREATER_VAL);
		
		pageContext.setAttribute(testKey, lvb, PageContext.APPLICATION_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("application");
		ge.setProperty("value");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Value (" + LESSER_VAL + ") is less than or equal to value (" + GREATER_VAL + ")",
        	ge.condition());
    }

    /**
     * Testing <code>LessEqualTag</code> using name and property attribute in
     * the session scope.
    */
    public void testSessionScopePropertyLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testSessionScopePropertyLessEqual";
		LabelValueBean lvb = new LabelValueBean("The Key", GREATER_VAL);
		
		pageContext.setAttribute(testKey, lvb, PageContext.SESSION_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("session");
		ge.setProperty("value");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Value (" + LESSER_VAL + ") is less than or equal to value (" + GREATER_VAL + ")",
        	ge.condition());
    }
    
    /**
     * Testing <code>LessEqualTag</code> using name and property attribute in
     * the request scope.
    */
    public void testRequestScopePropertyLessEqual() 
    	throws ServletException,  JspException {
    
        LessEqualTag ge = new LessEqualTag();

		String testKey = "testRequestScopePropertyLessEqual";
		LabelValueBean lvb = new LabelValueBean("The Key", GREATER_VAL);
		
		pageContext.setAttribute(testKey, lvb, PageContext.REQUEST_SCOPE);
		ge.setPageContext(pageContext);
		ge.setName(testKey);
		ge.setScope("request");
		ge.setProperty("value");
		ge.setValue(LESSER_VAL);

        assertTrue(
        	"Value (" + LESSER_VAL + ") is less than or equal to value (" + GREATER_VAL + ")",
        	ge.condition());
    }
    
    
    
}
