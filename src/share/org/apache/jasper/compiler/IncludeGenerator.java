/*
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

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;






/**
 * Generator for <jsp:include.../>
 *
 *
 * @author Anil K. Vijendran
 * @author Mandar Raje
 */
public class IncludeGenerator 
    extends GeneratorBase
    implements ServiceMethodPhase 
{
    String page;
    boolean isExpression = false;
    Hashtable params;
    
    public IncludeGenerator(Mark start,  Hashtable attrs, Hashtable param )
    throws JasperException    {
        page = (String) attrs.get("page");
        if (page == null){
    	    throw new CompileException(start,
    				       Constants.getString("jsp.error.include.tag"));
        }
        String flush = (String) attrs.get("flush");
        if (flush == null){
            throw new CompileException(start,
				       Constants.getString("jsp.error.include.noflush"));
        }
        if (!flush.equals("true")){
            throw new CompileException(start,
				       Constants.getString("jsp.error.include.badflush"));
	    }
    	if (attrs.size() != 2){
		    throw new CompileException(start,
			       Constants.getString("jsp.error.include.tag"));
        }

	    this.params = param;
	    isExpression = JspUtil.isExpression (page);
    }
    
    public void generate(ServletWriter writer, Class phase) {
	boolean initial = true;
	String sep = "?";
	writer.println("{");
	writer.pushIndent();
	writer.println("String _jspx_qStr = \"\";");
	
	if (params.size() > 0) {
	    Enumeration en = params.keys();
	    while (en.hasMoreElements()) {
		String key = (String) en.nextElement();
		String []value = (String []) params.get(key);
		if (initial == true) {
		    sep = "?";
		    initial = false;
		} else sep = "&";
		
		if (value.length == 1 && JspUtil.isExpression(value[0])) {
		    writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
				   key + "=\" + (" + JspUtil.getExpr(value[0]) + ");");
		} else {
		    if (value.length == 1) {
			writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
				       key + "=\" + \"" + value[0] + "\";");
		    } else {
			for (int i = 0; i < value.length; i++) {
			    if (!JspUtil.isExpression(value[i]))
				writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
					       key + "=\" + \"" + value[i] + "\";");
			    else
				writer.println("_jspx_qStr = _jspx_qStr + \"" + sep +
					       key + "=\" + (" + JspUtil.getExpr(value[i])+ " );");
			    if (sep.equals("?")) sep = "&";
			    
			}
		    }
		}
	    }
	}
	if (!isExpression) 
	    writer.println("pageContext.include(" +
			   writer.quoteString(page) + " + _jspx_qStr);");
	else
	    writer.println ("pageContext.include(" + 
			    JspUtil.getExpr(page) + " + _jspx_qStr);");

	writer.popIndent();
	writer.println("}");
    }
}
