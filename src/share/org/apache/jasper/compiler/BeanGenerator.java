/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/BeanGenerator.java,v 1.11 2004/02/23 02:45:11 billbarker Exp $
 * $Revision: 1.11 $
 * $Date: 2004/02/23 02:45:11 $
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

/**
 * Generate code for useBean.
 *
 * @author Mandar Raje
 */
public class BeanGenerator extends GeneratorBase implements ServiceMethodPhase, 
    ClassDeclarationPhase {
  
	Hashtable attrs;
	BeanRepository beanInfo;
	boolean genSession;
	boolean beanRT = false;
	Mark start;
  
    public BeanGenerator (Mark start, Hashtable attrs, BeanRepository beanInfo,
			  boolean genSession) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
	this.genSession = genSession;
	this.start = start;
    }
	
    public void generate (ServletWriter writer, Class phase)
	throws JasperException {

	    if (ClassDeclarationPhase.class.equals (phase)) 
		checkSyntax (writer, phase);
	    else if (ServiceMethodPhase.class.equals (phase))
		generateMethod (writer, phase);
    }
  
    public void checkSyntax (ServletWriter writer, Class phase) 
	throws JasperException		{
	    String  name       = getAttribute ("id");
	    String  varname    = name;
	    String  serfile    = name;
	    String  scope      = getAttribute ("scope");
	    String  clsname    = getAttribute ("class");
	    String  type       = getAttribute ("type");
	    String beanName    = getAttribute ("beanName");
	    
	    // Check for mandatory attributes:
	    if ( name == null ) {
                String m = Constants.getString("jsp.error.usebean.missing.attribute");
		throw new CompileException(start, m);
	    }
	    
	    if (clsname == null && type == null) {
                String m = Constants.getString("jsp.error.usebean.missing.type",
					       new Object[] {name});
		throw new CompileException (start, m);
	    }

	    if (beanInfo.checkVariable(name) == true) {
                String m = Constants.getString("jsp.error.usebean.duplicate",
					       new Object[] {name});
                throw new CompileException (start, m);
	    }
            
	    if (scope != null && scope.equalsIgnoreCase ("session")) {
		if (genSession != true) {
                    String m = Constants.getString("jsp.error.usebean.prohibited.as.session",
						   new Object[] {name});
                    throw new CompileException (start, m);
                }
	    }

	    if (clsname != null && beanName != null) {
		String m = Constants.getString("jsp.error.usebean.not.both",
					       new Object[] {name});
		throw new CompileException (start, m);
	    }	     
	    
	    if (clsname == null) clsname = type;
	    if (scope == null || scope.equals("page")) {
		beanInfo.addPageBean(name, clsname);
	    } else if (scope.equals("request")) {
		beanInfo.addRequestBean(name, clsname);
	    } else if (scope.equals("session")) {
		beanInfo.addSessionBean(name,clsname);
	    } else if (scope.equals("application")) {
		beanInfo.addApplicationBean(name,clsname);
	    } else {
                String m = Constants.getString("jsp.error.usebean.invalid.scope",
					       new Object[] {name, scope});
	        throw new CompileException (start, m);
            }
    }
  
    public void generateMethod (ServletWriter writer, Class phase) 
	throws JasperException {
	    String  name       = getAttribute ("id");
	    String  varname    = name;
	    String  serfile    = name;
	    String  scope      = getAttribute ("scope");
	    String  clsname    = getAttribute ("class");
	    String  type       = getAttribute ("type");
	    String  beanName   = getAttribute ("beanName");
	    
	    if (type == null) type = clsname;

	    // See if beanName is a request-time expression.
	    if (beanName != null && JspUtil.isExpression (beanName)) {
		beanName = JspUtil.getExpr (beanName);
		beanRT = true;
	    }
	    
	    if (scope == null || scope.equals ("page")) {

		// declare the variable.
		declareBean (writer, type, varname);
		
		// synchronized inspection.
		lock (writer, "pageContext");

		// Generate code to locate the bean.
		locateBean (writer, type, varname, name,
			    "PageContext.PAGE_SCOPE");

		// create the bean if it doesn't exists.
		createBean (writer, varname, clsname, beanName, name, type,
			    "PageContext.PAGE_SCOPE");

		// unlock
		unlock (writer);
		
		// Initialize the bean if the body is present.
		generateInit (writer, varname);
		
	    } else  if (scope.equals ("request")) {
		
		// declare the variable.
		declareBean (writer, type, varname);
		
		// synchronized inspection.
		lock (writer, "request");
		    
		// Generate code to locate the bean.
		locateBean (writer, type, varname, name,
			    "PageContext.REQUEST_SCOPE");
		
		// create the bean if it doesn't exists.
		createBean (writer, varname, clsname, beanName, name, type,
			    "PageContext.REQUEST_SCOPE");

		// unlock.
		unlock (writer);
	       
		// Initialize the bean if the body is present.
		generateInit (writer, varname);
		
	    } else if (scope.equals ("session")) {
		
		// declare the variable.
		declareBean (writer, type, varname);
		
		// synchronized inspection.
		lock (writer, "session");
		  
		// Generate code to locate the bean.
		locateBean (writer, type, varname, name,
			    "PageContext.SESSION_SCOPE");
		
		// create the bean if it doesn't exists.
		createBean (writer, varname, clsname, beanName, name, type,
			    "PageContext.SESSION_SCOPE");

		// unlock.
		unlock (writer);
		
		// Initialize the bean.
		generateInit (writer, varname);
		
	    } else if (scope.equals ("application")) {

		// declare the variable.
		declareBean (writer, type, varname);
		
		// synchronized inspection
		lock (writer, "application");
		
		// Generate code to locate the bean.
		locateBean (writer, type, varname, name,
			    "PageContext.APPLICATION_SCOPE");
		
		// create the bean if it doesn't exist.
		createBean (writer, varname, clsname, beanName, name, type,
			    "PageContext.APPLICATION_SCOPE");

		// unlock.
		unlock (writer);
		
		// Initialize the bean.
		generateInit (writer, varname);
	    }     
    }

    private void lock (ServletWriter writer, String scope) {
	
	writer.println(" synchronized (" + scope + ") {");
	writer.pushIndent();
    }

    private void unlock (ServletWriter writer) {

	writer.popIndent();
	writer.println(" } ");
    }
	
    private void generateInit (ServletWriter writer, String name) {
	    
	    writer.println ("if(_jspx_special" + name + " == true) {");
    }

    private void declareBean (ServletWriter writer, String type, String varname) {

	writer.println (type + " " + varname + " = null;");
	
	// Variable _jspx_special used for initialization.
	writer.println ("boolean _jspx_special" + varname + "  = false;");
	
    }
	
    private void locateBean (ServletWriter writer,
			     String type,
			     String varname,
			     String name,
			     String scope) {
	
	writer.println (varname + "= (" + type + ")");
	writer.println ("pageContext.getAttribute(" +
			writer.quoteString(name) + "," + scope + ");");
    }
		
    private void createBean(ServletWriter writer, String varname, String clsname,
			    String beanName, String name, String type, String scope) {
	

	// Create the bean only if classname is specified.
	// Otherwise bean must be located in the scope.
	if (clsname != null || beanName != null) {
	    writer.println ("if ( "+varname+" == null ) {");
	    
	    // Code to create the bean:
	    writer.pushIndent ();
	    
	    // Set the boolean var. so that bean can be initialized.
	    writer.println ("_jspx_special" + name + " = true;");
	    
	    generateBeanCreate (writer, varname, clsname, beanName, type);

	    writer.println("pageContext.setAttribute(" +
			   writer.quoteString(name) + ", " + varname + ", " +
			   scope + ");");
	    
	    writer.popIndent ();
	    writer.println ("}");
	}
	else {
	    
	    // clsname not specified -- object must be found inside the scope.
	    writer.println ("if (" + varname + "  == null) ");
	    writer.println (" throw new java.lang.InstantiationException (\"bean " +
			    varname + "  not found within scope \"); ");
	}
    }
	
    protected void generateBeanCreate (ServletWriter writer,
				       String varname,
				       String clsname,
				       String beanName,
				       String type) {

	String convert = (clsname == null) ? type : clsname;
	if (beanName != null) clsname = beanName;
	writer.println ("try {");
	writer.pushIndent ();
	if (beanRT == false)
	    writer.println(varname+" = ("+ convert + 
			   ") java.beans.Beans.instantiate(this.getClass().getClassLoader(), "+
			   writer.quoteString(clsname) +");");
	else
	    writer.println(varname+" = ("+ convert + 
			   ") java.beans.Beans.instantiate(this.getClass().getClassLoader(), "+
			   clsname +");");
	writer.popIndent ();
	writer.println ("} catch (Exception exc) {");
	writer.pushIndent ();
	writer.println (" throw new ServletException (\" Cannot create bean of class \""  +
			"+\"" + clsname + "\", exc);"); 
	writer.popIndent ();
	writer.println ("}");
	
    }
	
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }
	
}
