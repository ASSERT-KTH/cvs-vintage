/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagBeginGenerator.java,v 1.5 1999/10/21 07:57:22 akv Exp $
 * $Revision: 1.5 $
 * $Date: 1999/10/21 07:57:22 $
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
import java.lang.reflect.Method;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyTag;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspEngineContext;
import org.apache.jasper.Constants;

import org.apache.jasper.compiler.ServletWriter;

/**
 * Custom tag support.
 *
 * @author Anil K. Vijendran
 */
public class TagBeginGenerator 
    extends TagGeneratorBase 
    implements ServiceMethodPhase
{
    String prefix;
    String shortTagName;
    Hashtable attrs;
    TagLibraryInfoImpl tli;
    TagInfo ti;
    TagAttributeInfo[] attributes;
    String baseVarName, thVarName;
    TagCache tc;
    TagData tagData;

    
    public TagBeginGenerator(String prefix, String shortTagName, Hashtable attrs,
			     TagLibraryInfoImpl tli, TagInfo ti) 
        throws JasperException
    {
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.attrs = attrs;
	this.tli = tli;
	this.ti = ti;
	this.attributes = ti.getAttributes();
	this.baseVarName = getTagVarName(prefix, shortTagName);
	this.thVarName = "_jspx_th_"+baseVarName;
    }

    public void init(JspEngineContext ctxt) throws JasperException {
        validate();
        tc = tli.getTagCache(shortTagName);
        if (tc == null) {
            tc = new TagCache(shortTagName);

            ClassLoader cl = ctxt.getClassLoader();
            Class clz = null;
            try {
                clz = cl.loadClass(ti.getTagClassName());
            } catch (Exception ex) {
                throw new JasperException(Constants.getString("jsp.error.unable.loadclass", 
                                                              new Object[] { ti.getTagClassName(),
                                                                             ex.getMessage()
                                                              }
                                                              ));
            }
            tc.setTagHandlerClass(clz);
            tli.putTagCache(shortTagName, tc);
        }
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

        tagData = new TagData(attrs);
        if (!ti.isValid(tagData))
            throw new JasperException(Constants.getString("jsp.error.invalid_attributes"));
    }

    private final void generateSetters(ServletWriter writer, String parent) 
        throws JasperException
    {
        writer.println(thVarName+".setPageContext(pageContext);");
        writer.println(thVarName+".setParent("+parent+");");

        if (attributes.length != 0)
	    for(int i = 0; i < attributes.length; i++) {
                String attrValue = (String) attrs.get(attributes[i].getName());
                if (attrValue != null) {
                    if (attributes[i].canBeRequestTime()) {
                        if (JspUtil.isExpression(attrValue))
                            attrValue = JspUtil.getExpr(attrValue);
                        else 
                            attrValue = writer.quoteString(attrValue);
                    } else
                        attrValue = writer.quoteString(attrValue);
                    
                    String attrName = attributes[i].getName();
                    Method m = tc.getSetterMethod(attrName);
                    if (m == null)
                        throw 
                            new JasperException(Constants.getString("jsp.error.unable.to_find_method",
                                                                    new Object[] { attrName }));
                
                    writer.println(thVarName+"."+m.getName()+"("+attrValue+");");
                }
            }
    }
    
    public void generateServiceMethodStatements(ServletWriter writer) 
        throws JasperException 
    {
        TagVariableData top = topTag();
        String parent = top == null ? null : top.tagHandlerInstanceName;

        String evalVar = "_jspx_eval_"+baseVarName;
	tagBegin(new TagVariableData(thVarName, evalVar));

        writer.println("/* ----  "+prefix+":"+shortTagName+" ---- */");

        writer.println(ti.getTagClassName()+" "+thVarName+" = new "+ti.getTagClassName()+"();");

        generateSetters(writer, parent);
        
        VariableInfo[] vi = ti.getVariableInfo(tagData);

        // Just declare AT_BEGIN here... 
        declareVariables(writer, vi, true, false, VariableInfo.AT_BEGIN);

	writer.println("try {");
	writer.pushIndent();



        writer.println("int "+evalVar+" = "
                       +thVarName+".doStartTag();");
        
        boolean implementsBodyTag = BodyTag.class.isAssignableFrom(tc.getTagHandlerClass());
        
        // Need to update AT_BEGIN variables here
        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);
        
        // FIXME: I'm not too sure if this is the right approach. I don't like 
        //        throwing English language strings into the generated servlet. 
        //        Perhaps, I'll just define an inner classes as necessary for these 
        //        types of exceptions? -akv

        if (implementsBodyTag) {
            writer.println("if ("+evalVar+" == Tag.EVAL_BODY_INCLUDE)");
            writer.pushIndent();
            writer.println("throw new JspError(\"Since tag handler "+tc.getTagHandlerClass()+
                           " implements BodyTag, it can't return Tag.EVAL_BODY_INCLUDE\");");
            writer.popIndent();
        } else {
            writer.println("if ("+evalVar+" == BodyTag.EVAL_BODY_TAG)");
            writer.pushIndent();
            writer.println("throw new JspError(\"Since tag handler "+tc.getTagHandlerClass()+
                           " does not implement BodyTag, it can't return BodyTag.EVAL_BODY_TAG\");");
            writer.popIndent();
        }

        writer.println("if ("+evalVar+" != Tag.SKIP_BODY) {");
	writer.pushIndent();

	writer.println("try {");
	writer.pushIndent();
	

	if (implementsBodyTag) {
	    writer.println("if ("+evalVar+" != Tag.EVAL_BODY_INCLUDE) {");
	    writer.pushIndent();

	    writer.println("out = pageContext.pushBody();");
	    writer.println(thVarName+".setBodyContent((BodyContent) out);");

	    writer.popIndent();
	    writer.println("}");
	    
	    writer.println(thVarName+".doInitBody();");
	}
        
	writer.println("do {");
	writer.pushIndent();
        // Need to declare and update NESTED variables here
        declareVariables(writer, vi, true, true, VariableInfo.NESTED);
        // Need to update AT_BEGIN variables here
        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);
    }

    public void generate(ServletWriter writer, Class phase) 
        throws JasperException 
    {
        generateServiceMethodStatements(writer);
    }    
}
