/*
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
import org.apache.jasper.JasperException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.beans.*;

import org.apache.jasper.Constants;

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
    
    public SetPropertyGenerator (Mark start, Mark stop, Hashtable attrs,
				 BeanRepository beanInfo) {
	this.attrs = attrs;
	this.beanInfo = beanInfo;
    }
    
    public void generate (ServletWriter writer, Class phase) 
	throws JasperException {
	    String name     = getAttribute ("name");
	    String property = getAttribute ("property");
	    String param    = getAttribute ("param");
	    String value    = getAttribute ("value");
	    
	    
	    // Check if the bean is in the repository.
	    if (!beanInfo.checkVariable (name)) {
		String m = Constants.getString("jsp.error.setproperty.beanNotFound", new Object[] {name});
		throw new JasperException(m);
	    }
	    
	    // Get the class of the bean.
	    Class cls = null;
	    cls = beanInfo.getBeanType (name);
	    
	    if (cls == null) {
		String m = Constants.getString("jsp.error.setproperty.ClassNotFound", new Object[] {beanInfo.getBeanType(name).toString()});
		throw new JasperException(m);
	    }
	    
	    Method []methods = cls.getMethods ();
	    
	    if (property.equals("*")) {
		
		if (value != null) {
		    String m = Constants.getString("jsp.error.setproperty.invalidSyantx");
		    throw new JasperException(m);
		}
		
		// Set all the properties using name-value pairs in the request.
		writer.println ("JspRuntimeLibrary.introspect(" + name + ", request);");	
		
	    } else {
		
		// Parameter name or value specified.
		// Do the introspection.
		Method method = null;
		Class tp = null;
		try {
		    java.beans.BeanInfo info
			= java.beans.Introspector.getBeanInfo(cls);
		    if (info == null) {
			String m = Constants.getString("jsp.error.setproperty.beanInfoNotFound", new Object[] {name});
			throw new JasperException(m);
		    }
		    if ( info != null ) {
			java.beans.PropertyDescriptor pd[]
			    = info.getPropertyDescriptors();
			for (int i = 0 ; i < pd.length ; i++) {
			    if ( pd[i].getName().equals(property) ) {
				method = pd[i].getWriteMethod();
				tp   = pd[i].getPropertyType();
				break;
			    }
			}
		    }
		} catch (java.beans.IntrospectionException ex) {
		    throw new JasperException (Constants.getString
		    	("jsp.error.beans.introspection.setproperty", 
		    	new Object [] {name}), ex);
		}
		if (method == null) 
		    throw new JasperException (Constants.getString
		    	("jsp.error.beans.nomethod.setproperty", 
		    	new Object [] {name, property}));
		String methodName = method.getName ();
		
		if (value == null) {
		    
		    // Parameter name specified. If not same as property.
		    if (param == null) param = property;
		    
		    // Generate the code.
		    if (tp.isArray()) {
			Class t = tp.getComponentType();

			writer.println ("if(request.getParameterValues(\""+param+"\")!= null" +
					" && !request.getParameterValues(\""+param+"\").equals(\"\")) {");
			if (t.equals(String.class)) {
			    writer.println (name+ "." +methodName+
					    "(request.getParameterValues(\"" +param+ "\"));");
			} else {
			    // Will have to convert explicitly.
			    writer.println ("{");
			    writer.pushIndent ();
			    writer.println (" String _strvalues[] = request.getParameterValues(\"" +param+ "\");");
			    String tdef = getTypeDef (t);
			    writer.println (tdef + "[] _tpvalues = new " +
					    tdef + "[_strvalues.length] ;");
			    writer.println (" for (int i=0; i<_strvalues.length; i++) ");
			    if (t.equals(char.class)) 
				writer.println ("_tpvalues[i]= _strvalues[i].charAt(0);"); 
			    else if (t.equals(Character.class))
				writer.println ("_tpvalues[i] = new Character(_strvalues[i].charAt(0));");
			    else if (t.equals(double.class))
				writer.println ("_tpvalues[i] = Double.valueOf(_strvalues[i]).doubleValue();");
			    else if (t.equals(float.class))
				writer.println ("_tpvalues[i] = Float.valueOf(_strvalues[i]).floatValue();");
			    else
				writer.println ("_tpvalues[i] = " + getMethod(t) +
						"(_strvalues[i]);");
			    writer.println (name+ "." +methodName+ "(_tpvalues);");
			    writer.popIndent ();
			    writer.println ("}");

			}
			writer.println ("}");			

		    } else {
			writer.println ("if(request.getParameter(\""+param+"\") != null&&" +
					"!request.getParameter(\""+param+"\").equals(\"\")){");
			if (tp.equals(char.class))
			    writer.println (name+"."+methodName+"(" +
					    "request.getParameter(\"" + param +
					    "\").charAt(0));"); 
			else if (tp.equals(Character.class))
			     writer.println (name + "." + methodName+
					     "(new Character (" +
					     "request.getParameter (\"" +
					     param + "\").charAt(0)));");
			else if (tp.equals(double.class))
			    writer.println (name + "." + methodName+ "(" +
					    "Double.valueOf(request.getParameter(" +
					    param + ")).doubleValue());");
			else if (tp.equals(float.class))
			    writer.println (name + "." + methodName+ "(" +
					    "Float.valueOf(request.getParameter(" +
					    param + ")).floatValue());");
			else
			    writer.println (name+ "." +methodName+ "(" + 
					    getMethod(tp) + "(" +
					    "request.getParameter (\""+ param + "\")));");
			writer.println ("}");			
		    }
		    
		} else {
		    
		    // Value of the attribute explicitly listed.
		    // Generate the code.
		    
		    if (param != null) {
			String m = Constants.getString("jsp.error.setproperty.paramOrValue");
			throw new JasperException(m);
		    }
		    
		    if (!JspUtil.isExpression (value)) {
			if (tp.isArray()) {
			    String m = Constants.getString("jsp.error.setproperty.arrayVal", new Object[] {property});
			    throw new JasperException(m);
			} else {
			    if (tp.equals(String.class))
			        writer.println (name+"."+methodName+"(" +
						writer.quoteString(value) + ");");
			    else if (tp.equals(char.class))
				writer.println (name+"."+methodName+"((new String (\"" +
						value + "\")).charAt(0));");
			    else if (tp.equals(Character.class))
				writer.println (name+"."+methodName+"(new Character(" +
						"new String(\"" + value + "\").charAt(0)"+
						"));");
			    else if (tp.equals(double.class))
				writer.println (name + "." + methodName + "(" +
						"Double.valueOf(\"" + value +
						"\").doubleValue());");
			    else if (tp.equals(float.class))
				writer.println (name + "." + methodName + "(" +
						"Float.valueOf(\"" + value +
						"\").floatValue());");
			    else
				writer.println (name+"."+methodName+"(" + getMethod(tp)+"(\""
+ value + "\"));");
			}
		    }
		    else {
			// This is an expression.
			// Same treatment for simple and indexed properties.
			writer.println (name + "." + methodName + "(" +
					JspUtil.getExpr(value) + ");");
		    }
		}
	    }
    }
    
    public String getAttribute(String name) {
	return (attrs != null) ? (String) attrs.get(name) : null;
    }

    public String getTypeDef (Class cls)
	throws JasperException {
	    
	    if (cls.equals(int.class) || cls.equals(byte.class) || cls.equals(boolean.class)
		|| cls.equals(short.class) || cls.equals(long.class) || cls.equals(double.class)
		|| cls.equals(float.class))
		return cls.toString();
	    else if (cls.equals(Integer.class)) return "Integer ";
	    else if (cls.equals(Byte.class))    return "Byte ";
	    else if (cls.equals(Boolean.class)) return "Boolean ";
	    else if (cls.equals(Short.class))   return "Short ";
	    else if (cls.equals(Long.class))    return "Long ";
	    else if (cls.equals(Double.class))  return "Double ";
	    else if (cls.equals(Float.class))   return "Float";
	    else throw new JasperException (" Type " + cls.toString() + " unknown.");
	    
    }
    
    public static String getMethod(Class cls) 
	throws JasperException {
	    if (cls.equals(int.class)) return "Integer.parseInt";
	    else if (cls.equals(byte.class)) return "Byte.parseByte";
	    else if (cls.equals(boolean.class)) return "Boolean.getBoolean";
	    else if (cls.equals(long.class)) return "Long.parseLong";
	    else if (cls.equals(short.class)) return "Short.parseShort";
	    else if (cls.equals(Integer.class)) return "Integer.valueOf";
	    else if (cls.equals(Double.class)) return "Double.valueOf";
	    else if (cls.equals(Float.class)) return "Float.valueOf";
	    else if (cls.equals(Byte.class)) return "Byte.valueOf";
	    else if (cls.equals(Boolean.class)) return "Boolean.valueOf";
	    else if (cls.equals(Long.class)) return "Long.valueOf";
	    else if (cls.equals(Short.class)) return "Short.valueOf";
	    else if (cls.equals(String.class)) return "";
	    else throw new JasperException ("setProperty: variable of Class: " + cls + "  can't be set.");
    }
}







