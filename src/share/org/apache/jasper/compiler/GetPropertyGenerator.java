/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/GetPropertyGenerator.java,v 1.8 2004/02/23 02:45:12 billbarker Exp $
 * $Revision: 1.8 $
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
import org.apache.jasper.runtime.JspRuntimeLibrary;

/**
 * Generator for <jsp:getProperty.../>
 *
 * @author Mandar Raje
 */
public class GetPropertyGenerator 
    extends GeneratorBase 
    implements ServiceMethodPhase 
{
    Hashtable attrs;
    BeanRepository beanInfo;
    
    public GetPropertyGenerator (Mark start, Mark stop, Hashtable attrs,
				 BeanRepository beanInfo) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException	{
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");

	    // Should ideally throw exception here if the bean
	    // is not present in the pageContext.
	    
	    if (beanInfo.checkVariable(name)) {
		// Bean is defined using useBean.
                // introspect at compile time
                Class cls = beanInfo.getBeanType(name);
		String clsName = cls.getName();
                java.lang.reflect.Method meth = JspRuntimeLibrary.getReadMethod(cls, property);
                
                String methodName = meth.getName();
		writer.println("out.print(" +
			       Constants.JSP_RUNTIME_PACKAGE +
			       ".JspRuntimeLibrary.toString(" +
			       "(((" + clsName + ")pageContext.findAttribute(" +
                               "\"" + name + "\"))." + methodName + "())));");
	    } else {
                // Get the class name and then introspect at runtime.
		writer.println("out.print(" +
			       Constants.JSP_RUNTIME_PACKAGE + ".JspRuntimeLibrary.toString(" +
			       Constants.JSP_RUNTIME_PACKAGE + ".JspRuntimeLibrary." +
			       "handleGetProperty(pageContext.findAttribute(" +
			       "\"" + name + "\"), \"" + property + "\")));"
	        );
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }
}

