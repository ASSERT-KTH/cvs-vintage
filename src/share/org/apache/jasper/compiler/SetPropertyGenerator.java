/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/SetPropertyGenerator.java,v 1.9 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.9 $
 * $Date: 2004/02/23 02:45:12 $
 *
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
 * Generator for <jsp:setProperty .../>
 *
 * @author Mandar Raje
 */
public class SetPropertyGenerator
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    Hashtable attrs;
    BeanRepository beanInfo;
    Mark start;
    
    public SetPropertyGenerator (Mark start, Mark stop, Hashtable attrs,
				 BeanRepository beanInfo) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
	this.start = start;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException {
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");
	    String param    = getAttribute ("param");
	    String value    = getAttribute ("value");
	    
	    if (property.equals("*")) {
		
		if (value != null) {
		    String m = Constants.getString("jsp.error.setproperty.invalidSyantx");
		    throw new CompileException(start, m);
		}
		
		// Set all the properties using name-value pairs in the request.
		writer.println(Constants.JSP_RUNTIME_PACKAGE +
			       ".JspRuntimeLibrary.introspect(pageContext.findAttribute(" +
			       "\"" + name + "\"), request);");		
		
	    } else {
		
		if (value == null) {
		    
		    // Parameter name specified. If not same as property.
		    if (param == null) param = property;
		    
		    writer.println(Constants.JSP_RUNTIME_PACKAGE +
				   ".JspRuntimeLibrary.introspecthelper(pageContext." +
				   "findAttribute(\"" + name + "\"), \"" + property +
				   "\", request.getParameter(\"" + param + "\"), " +
				   "request, \"" + param + "\", false);");
		} else {
		    
		    // value is a constant.
		    if (!JspUtil.isExpression (value)) {
			writer.println(Constants.JSP_RUNTIME_PACKAGE +
				       ".JspRuntimeLibrary.introspecthelper(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\",\"" + JspUtil.escapeQueryString(value) +
				       "\",null,null, false);");
		    } else {
			
			// This requires some careful handling.
			// int, boolean, ... are not Object(s).
			writer.println(Constants.JSP_RUNTIME_PACKAGE +
				       ".JspRuntimeLibrary.handleSetProperty(pageContext." +
				       "findAttribute(\"" + name + "\"), \"" + property +
				       "\"," + JspUtil.getExpr(value) + ");");
		    }
		}
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }
}







