/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/ForwardGenerator.java,v 1.6 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/23 06:22:36 $
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;

/**
 * Generator for <jsp:forward>
 *
 * @author Anil K. Vijendran
 */
public class ForwardGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    String page;
    boolean isExpression = false;
    Hashtable params;
    
    public ForwardGenerator(Mark start, Hashtable attrs, Hashtable param)
	throws JasperException {
	    if (attrs.size() != 1)
		throw new JasperException(Constants.getString("jsp.error.invalid.forward"));
	    
	    page = (String) attrs.get("page");
	    if (page == null)
		throw new CompileException(start,
					   Constants.getString("jsp.error.invalid.forward"));
	    
	    this.params = param;
	    isExpression = JspUtil.isExpression (page);
    }
    
    public void generate(ServletWriter writer, Class phase) {
	boolean initial = true;
	String sep = "?";	
        writer.println("if (true) {");
        writer.pushIndent();
        writer.println("out.clear();");
	writer.println("String _jspx_qfStr = \"\";");
	
	if (params.size() > 0) {
	    Enumeration en = params.keys();
	    while (en.hasMoreElements()) {
		String key = (String) en.nextElement();
		String []value = (String []) params.get(key);
		if (initial == true) {
		    sep = "?";
		    initial = false;
		} else sep = "&";

		// Bug 1705 - need "("
		if (value.length == 1 && JspUtil.isExpression(value[0]))
		    writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
				   key + "=\" + (" + JspUtil.getExpr(value[0]) + ");");
		else {
		    if (value.length == 1)
			writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
				       key + "=\" + \"" + value[0] + "\";");			
		    else {
			for (int i = 0; i < value.length; i++) {
			    if (!JspUtil.isExpression(value[i]))
				writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
					       key + "=\" + \"" + value[i] + "\";");
			    else
				writer.println("_jspx_qfStr = _jspx_qfStr + \"" + sep +
					       key + "=\" + (" + JspUtil.getExpr(value[i])+ ");");
			    if (sep.equals("?")) sep = "&";			    
			}
		    }
		}
	    }
	}
	if (!isExpression)
            writer.println("pageContext.forward(" +
			   writer.quoteString(page) + " +  _jspx_qfStr);");
	else
            writer.println("pageContext.forward(" +
			   JspUtil.getExpr (page) +  " +  _jspx_qfStr);");
	
        writer.println("return;");
        writer.popIndent();
        writer.println("}");
    }
}
