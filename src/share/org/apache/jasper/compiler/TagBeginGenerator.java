/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagBeginGenerator.java,v 1.2 1999/10/14 04:07:10 akv Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/14 04:07:10 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
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
package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;

import org.apache.jasper.JasperException;
import org.apache.jasper.Constants;

import org.apache.jasper.compiler.ServletWriter;

/**
 * Custom tag support. Needs updating for JSP 1.1 PR2
 *
 * @author Anil K. Vijendran
 */
public class TagBeginGenerator 
    extends TagGeneratorBase 
    implements ServiceMethodPhase, ClassDeclarationPhase
{
    String prefix;
    String shortTagName;
    Hashtable attrs;
    TagLibraryInfo tli;
    TagInfo ti;
    TagAttributeInfo[] attributes;
    String baseVarName, arrVarName, tdVarName, thVarName;

    public TagBeginGenerator(String prefix, String shortTagName, Hashtable attrs,
			     TagLibraryInfo tli, TagInfo ti) 
        throws JasperException
    {
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.attrs = attrs;
	//        this.body = body;
	this.tli = tli;
	this.ti = ti;
	this.attributes = ti.getAttributes();
	this.baseVarName = getTagDataVarName(prefix, shortTagName);
	this.arrVarName = "_jspx_tdarr_"+baseVarName;
	this.tdVarName = "_jspx_td_"+baseVarName;
	this.thVarName = "_jspx_th_"+baseVarName;
        validate();
    }

    void validate() throws JasperException {

        // First make sure all required attributes are indeed present. 
        for(int i = 0; i < attributes.length; i++)
            if (attributes[i].isRequired() && attrs.get(attributes[i].getName()) == null)
                throw new JasperException(Constants.getString("jsp.error.missing_attribute",
                                                              new Object[] {
                                                                  attributes[i].getName(),
                                                                  shortTagName
                                                              }
                                                              ));
        // Now make sure there are no invalid attributes... 
        Enumeration e = attrs.keys();
        while (e.hasMoreElements()) {
            String attr = (String) e.nextElement();
            boolean found = false;
            for(int i = 0; i < attributes.length; i++)
                if (attr.equals(attributes[i].getName()))
                    found = true;
            if (!found)
                throw new JasperException(Constants.getString("jsp.error.bad_attribute",
                                                              new Object[] {
                                                                  attr
                                                              }
                                                              ));
        }
    }

    public void generateStaticDeclarations(ServletWriter writer) {
	if (attributes.length != 0) {
	    writer.println("private static final Object[][] "+arrVarName+"  = {");
	    writer.pushIndent();
	    for(int i = 0; i < attributes.length; i++) {
		String name = writer.quoteString(attributes[i].getName());
		String value;
		String attrValue = (String) attrs.get(attributes[i].getName());
		if (attributes[i].canBeRequestTime())
		    value = "TagData.REQUEST_TIME_VALUE";
		value = attrValue != null ? writer.quoteString(attrValue) : "\"null\"";
		writer.indent(); writer.print("{"+name+", "+value+"}");
		if (i < attributes.length - 1)
		    writer.print(",\n");
		else
		    writer.println();
	    }
	    writer.popIndent();
	    writer.println("};");
	    writer.println("private static TagData "+tdVarName+" = new TagData("+arrVarName+");");
	}
    }

    public void generateServiceMethodStatements(ServletWriter writer) {
	String parent = topTag();
	tagBegin(thVarName);

        writer.println("/* ----  "+prefix+":"+shortTagName+" ---- */");

        String qPrefix = writer.quoteString(prefix);
        String qTagName = writer.quoteString(shortTagName);
        writer.println("Tag "+thVarName+" = new "+ti.getTagClassName()+"("+qPrefix+", "+
		       qTagName+");");

	if (attributes.length != 0)
	    for(int i = 0; i < attributes.length; i++)
		if (attributes[i].canBeRequestTime()) {
		    String attrValue = (String) attrs.get(attributes[i].getName());
		    if (JspUtil.isExpression(attrValue)) {
			String attrName = writer.quoteString(attributes[i].getName());
			writer.println(tdVarName+".setAttribute("+attrName+", "
				       +JspUtil.getExpr(attrValue)+");");
		    }
		}

        if (attributes.length == 0)
            writer.println(thVarName+".initialize("+parent+", null, pageContext);");
        else
            writer.println(thVarName+".initialize("+parent+", "+tdVarName+", pageContext);");

        VariableInfo[] vi = ti.getVariableInfo(new TagData(attrs));

        // Just declare AT_BEGIN here... 
        declareVariables(writer, vi, true, false, VariableInfo.AT_BEGIN);

	writer.println("try {");
	writer.pushIndent();

        String evalVar = "_jspx_eval_"+baseVarName;

        writer.println("boolean "+evalVar+" = "
                       +thVarName+".doStartTag() == Tag.EVAL_BODY;");

        // Need to update AT_BEGIN variables here
        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);

        writer.println("if ("+evalVar+" == true) {");
	writer.pushIndent();

	writer.println("try {");
	writer.pushIndent();
	
	writer.println("out = pageContext.pushBody();");
	writer.println(thVarName+".setBodyOut((BodyContent) out);");
	writer.println("do {");
	writer.pushIndent();
        // Need to declare and update NESTED variables here
        declareVariables(writer, vi, true, true, VariableInfo.NESTED);
        // Need to update AT_BEGIN variables here
        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);

	writer.println(thVarName+".doBeforeBody();");

    }

    public void generate(ServletWriter writer, Class phase) {
	if (phase.equals(ClassDeclarationPhase.class))
	    generateStaticDeclarations(writer);
	else if (phase.equals(ServiceMethodPhase.class))
	    generateServiceMethodStatements(writer);
    }    
}
