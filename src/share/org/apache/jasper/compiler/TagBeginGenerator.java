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

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;

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
    TagLibraryInfo tli;
    TagInfo ti;
    TagAttributeInfo[] attributes;
    String baseVarName, thVarName;
    TagCache tc;
    TagData tagData;
    Mark start;
    TagLibraries libraries;


    public TagBeginGenerator(Mark start, String prefix, String shortTagName, Hashtable attrs,
			     TagLibraryInfo tli, TagInfo ti, TagLibraries libraries,
                             Stack tagHandlerStack, Hashtable tagVarNumbers)
        throws JasperException
    {
        setTagHandlerStack(tagHandlerStack);
        setTagVarNumbers(tagVarNumbers);
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.attrs = attrs;
	this.tli = tli;
	this.ti = ti;
	this.attributes = ti.getAttributes();
	this.baseVarName = getTagVarName(prefix, shortTagName);
	this.thVarName = "_jspx_th_"+baseVarName;
	this.start = start;
	this.libraries = libraries;
    }

    public void init(JspCompilationContext ctxt) throws JasperException {
        validate();
        tc = libraries.getTagCache(prefix, shortTagName);
        if (tc == null) {
            tc = new TagCache(shortTagName);

            ClassLoader cl = ctxt.getClassLoader();
            Class clz = null;
            try {
                clz = cl.loadClass(ti.getTagClassName());
            } catch (Exception ex) {
                throw new CompileException(start,
					   Constants.getString("jsp.error.unable.loadclass",
                                                              new Object[] { ti.getTagClassName(),
                                                                             ex.getMessage()
                                                              }
                                                              ));
            }
            tc.setTagHandlerClass(clz);
            libraries.putTagCache(prefix, shortTagName, tc);
        }
    }

    void validate() throws JasperException {

        // Sigh! I wish I didn't have to clone here.
        Hashtable attribs = (Hashtable) attrs.clone();

        // First make sure all required attributes are indeed present.
        for(int i = 0; i < attributes.length; i++)
            if (attributes[i].isRequired() && attribs.get(attributes[i].getName()) == null)
                throw new CompileException(start,
					   Constants.getString("jsp.error.missing_attribute",
                                                              new Object[] {
                                                                  attributes[i].getName(),
                                                                  shortTagName
                                                              }
                                                              ));
        // Now make sure there are no invalid attributes...
        Enumeration e = attribs.keys();
        while (e.hasMoreElements()) {
            String attr = (String) e.nextElement();
            boolean found = false;
            for(int i = 0; i < attributes.length; i++)
                if (attr.equals(attributes[i].getName())) {
                    found = true;
                    if (attributes[i].canBeRequestTime() &&
			JspUtil.isExpression((String)attribs.get(attr)))
                        attribs.put(attr, TagData.REQUEST_TIME_VALUE);
		}

            if (!found)
                throw new CompileException(start,
					   Constants.getString("jsp.error.bad_attribute",
                                                              new Object[] {
                                                                  attr
                                                              }
                                                              ));
        }
        
        tagData = new TagData(attribs);
        if (!ti.isValid(tagData))
            throw new CompileException(start,
				       Constants.getString("jsp.error.invalid_attributes"));
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
		    String attrName = attributes[i].getName();
		    Method m = tc.getSetterMethod(attrName);
		    if (m == null)
			throw new CompileException
			    (start, Constants.getString
			     ("jsp.error.unable.to_find_method",
			      new Object[] { attrName }));
                    Class c[] = m.getParameterTypes();
                    // assert(c.length > 0)

                    if (attributes[i].canBeRequestTime()) {
			if (JspUtil.isExpression(attrValue))
			    attrValue = JspUtil.getExpr(attrValue);
			else
                            attrValue = convertString(c[0], attrValue, writer, attrName);
		    } else
			attrValue = convertString(c[0], attrValue, writer, attrName);
		    writer.println(thVarName+"."+m.getName()+"("+attrValue+");");
                }
            }
    }

    public String convertString(Class c, String s, ServletWriter writer, String attrName)
        throws JasperException 
    {
        if (c == String.class) {
            return writer.quoteString(s);
        } else if (c == boolean.class) {
            return Boolean.valueOf(s).toString();
        } else if (c == Boolean.class) {
            return "new Boolean(" + Boolean.valueOf(s).toString() + ")";
        } else if (c == byte.class) {
            return "((byte)" + Byte.valueOf(s).toString() + ")";
        } else if (c == Byte.class) {
            return "new Byte((byte)" + Byte.valueOf(s).toString() + ")";
        } else if (c == char.class) {
            // non-normative, because a normative method would fail to compile
            if (s.length() > 1) {
                char ch = s.charAt(0);
                // this trick avoids escaping issues
                return "((char) " + (int) ch + ")";
            } else {
                throw new NumberFormatException(Constants.getString(
                            "jsp.error.bad_string_char",
                            new Object[0]));
            }
        } else if (c == Character.class) {
            // non-normative, because a normative method would fail to compile
            if (s.length() > 1) {
                char ch = s.charAt(0);
                // this trick avoids escaping issues
                return "new Character((char) " + (int) ch + ")";
            } else {
                throw new NumberFormatException(Constants.getString(
                            "jsp.error.bad_string_Character",
                            new Object[0]));
            }
        } else if (c == double.class) {
            return Double.valueOf(s).toString();
        } else if (c == Double.class) {
            return "new Double(" + Double.valueOf(s).toString() + ")";
        } else if (c == float.class) {
            return Float.valueOf(s).toString() + "f";
        } else if (c == Float.class) {
            return "new Float(" + Float.valueOf(s).toString() + "f)";
        } else if (c == int.class) {
            return Integer.valueOf(s).toString();
        } else if (c == Integer.class) {
            return "new Integer(" + Integer.valueOf(s).toString() + ")";
        } else if (c == long.class) {
            return Long.valueOf(s).toString() + "l";
        } else if (c == Long.class) {
            return "new Long(" + Long.valueOf(s).toString() + "l)";
        } else {
             throw new CompileException
                    (start, Constants.getString
                     ("jsp.error.unable.to_convert_string",
                      new Object[] { c.getName(), attrName }));
        }
    }   
    
    public void generateServiceMethodStatements(ServletWriter writer)
        throws JasperException
    {
        TagVariableData top = topTag();
        String parent = top == null ? null : top.tagHandlerInstanceName;

        String evalVar = "_jspx_eval_"+baseVarName;
        TagVariableData tvd = new TagVariableData(thVarName, evalVar);
        String exceptionCheckName = tvd.tagExceptionCheckName;
	tagBegin(tvd);

        writer.println("/* ----  "+prefix+":"+shortTagName+" ---- */");
        
	writer.println(ti.getTagClassName() + " " + thVarName + " = null;");
        // set the exception check variable to false by default.
        // it will be set to true if an exception is caught.
        writer.println("boolean " + exceptionCheckName + " = false;");
        
	VariableInfo[] vi = ti.getVariableInfo(tagData);
	
        // Just declare AT_BEGIN here...
        declareVariables(writer, vi, true, false, VariableInfo.AT_BEGIN);
	
        // this first try is for tag cleanup
        writer.println("try {");
        writer.pushIndent();
        writer.println("try {");
        writer.pushIndent();
        String poolName = TagPoolGenerator.getPoolVariableName(tli, ti, attrs);
        writer.println("if (" + poolName + " != null) {");
        writer.pushIndent();
        writer.println(thVarName + " = (" + ti.getTagClassName() + ") " + poolName + ".getHandler();");
        writer.popIndent();
        writer.println("}");
        writer.println("if (" + thVarName + " == null) {");
        writer.pushIndent();
        writer.println(thVarName + " = new " + ti.getTagClassName() + "();");
        writer.popIndent();
        writer.println("}");

        generateSetters(writer, parent);

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
            writer.println("if ("+evalVar+" == javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)");
            writer.pushIndent();
            writer.println("throw new JspTagException(\"Since tag handler "+tc.getTagHandlerClass()+
                           " implements BodyTag, it can't return Tag.EVAL_BODY_INCLUDE\");");
            writer.popIndent();
        } else {
            writer.println("if ("+evalVar+" == javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_TAG)");
            writer.pushIndent();
            writer.println("throw new JspTagException(\"Since tag handler "+tc.getTagHandlerClass()+
                           " does not implement BodyTag, it can't return BodyTag.EVAL_BODY_TAG\");");
            writer.popIndent();
        }

        writer.println("if ("+evalVar+" != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {");
	writer.pushIndent();

	if (implementsBodyTag) {
            writer.println("try {");
            writer.pushIndent();

	    writer.println("if ("+evalVar+" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE) {");
	    writer.pushIndent();

	    writer.println("out = pageContext.pushBody();");
	    writer.println(thVarName+".setBodyContent((javax.servlet.jsp.tagext.BodyContent) out);");

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
