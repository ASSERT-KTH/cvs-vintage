/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/JspUtil.java,v 1.6 1999/12/24 04:40:04 rubys Exp $
 * $Revision: 1.6 $
 * $Date: 1999/12/24 04:40:04 $
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

import java.net.URL;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;


import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;

/** 
 * This class has all the utility method(s).
 * Ideally should move all the bean containers here.
 *
 * @author Mandar Raje.
 * @author Rajiv Mordani.
 */
public class JspUtil {

    private static final String OPEN_EXPR  = "<%=";
    private static final String CLOSE_EXPR = "%>";
    private static final String OPEN_EXPR_2 = "<jsp:expression>";
    private static final String CLOSE_EXPR_2 = "</jsp:expression>";

    public static char[] removeQuotes(char []chars) {
	CharArrayWriter caw = new CharArrayWriter();
	for (int i = 0; i < chars.length; i++) {
	    if (chars[i] == '%' && chars[i+1] == '\\' &&
		chars[i+2] == '\\' && chars[i+3] == '>') {
		caw.write('%');
		caw.write('>');
		i = i + 3;
	    }
	    else caw.write(chars[i]);
	}
	return caw.toCharArray();
    }

    // Checks if the token is a runtime expression.
    public static boolean isExpression (String token) {
	
	if (token.startsWith(OPEN_EXPR) && token.endsWith(CLOSE_EXPR))
	    return true;

	if (token.startsWith(OPEN_EXPR_2) && token.endsWith(CLOSE_EXPR_2))
	    return true;

	return false;
    }

    // Returns the "expression" part -- takin <%= and %> out.
    public static String getExpr (String expression) {
	String returnString;
	int length = expression.length();
	
	if (expression.startsWith(OPEN_EXPR) && expression.endsWith(CLOSE_EXPR))
	    returnString = expression.substring (OPEN_EXPR.length(), length - CLOSE_EXPR.length());

	else if (expression.startsWith(OPEN_EXPR_2) && expression.endsWith(CLOSE_EXPR_2))
	    returnString = expression.substring (OPEN_EXPR_2.length(), length - CLOSE_EXPR_2.length());
	
	else
	    returnString = "";

	return returnString;
    }

    // Parses the XML document contained in the InputStream.
    public static XmlDocument parseXMLDoc(InputStream in, URL dtdURL, 
    					  String dtdId) throws JasperException 
    {
	XmlDocument tld;
	XmlDocumentBuilder builder = new XmlDocumentBuilder();
	
        com.sun.xml.parser.ValidatingParser 
            parser = new com.sun.xml.parser.ValidatingParser();

        /***
         * These lines make sure that we have an internal catalog entry for 
         * the taglib.dtdfile; this is so that jasper can run standalone 
         * without running out to the net to pick up the taglib.dtd file.
         */
        Resolver resolver = new Resolver();
        resolver.registerCatalogEntry(dtdId, 
                                      dtdURL.toString());
        
        try {
            parser.setEntityResolver(resolver);
            builder.setParser(parser);
            builder.setDisableNamespaces(false);
            parser.parse(new InputSource(in));
        } catch (SAXException sx) {
            throw new JasperException(Constants.getString(
	    	"jsp.error.parse.error.in.TLD", new Object[] {
							sx.getMessage()
		    				     }));
        } catch (IOException io) {
            throw new JasperException(Constants.getString(
	    		"jsp.error.unable.to.open.TLD", new Object[] {
							    io.getMessage() }));
        }
        
        tld = builder.getDocument();
	return tld;
    }

    public static void checkAttributes (String typeOfTag, Hashtable attrs,
    					ValidAttribute[] validAttributes)
					throws JasperException
    {
	boolean valid = true;
	Hashtable temp = (Hashtable)attrs.clone ();

	/**
	 * First check to see if all the mandatory attributes are present.
	 * If so only then proceed to see if the other attributes are valid
	 * for the particular tag.
	 */
	String missingAttribute = null;

	for (int i = 0; i < validAttributes.length; i++) {
	        
	    if (validAttributes[i].mandatory) {
	        if (temp.get (validAttributes[i].name) != null) {
	            temp.remove (validAttributes[i].name);
		    valid = true;
		} else {
		    valid = false;
		    missingAttribute = validAttributes[i].name;
		    break;
		}
	    }
	}

	/**
	 * If mandatory attribute is missing then the exception is thrown.
	 */
	if (!valid)
	    throw new JasperException(Constants.getString(
			"jsp.error.mandatory.attribute", 
                                 new Object[] { typeOfTag, missingAttribute}));

	/**
	 * Check to see if there are any more attributes for the specified
	 * tag.
	 */
	if (temp.size() == 0)
	    return;

	/**
	 * Now check to see if the rest of the attributes are valid too.
	 */
   	Enumeration enum = temp.keys ();
	String attribute = null;

	while (enum.hasMoreElements ()) {
	    valid = false;
	    attribute = (String) enum.nextElement ();
	    for (int i = 0; i < validAttributes.length; i++) {
	        if (attribute.equals(validAttributes[i].name)) {
		    valid = true;
		    break;
		}
	    }
	    if (!valid)
	        throw new JasperException(Constants.getString(
			"jsp.error.invalid.attribute", 
                                 new Object[] { typeOfTag, attribute }));
	}
    }
    
    public static String escapeQueryString(String unescString) {
	if ( unescString == null )
	    return null;
	
	String escString    = "";
	String shellSpChars = "&;`'\"|*?~<>^()[]{}$\\\n";
	
	for(int index=0; index<unescString.length(); index++) {
	    char nextChar = unescString.charAt(index);
	    
	    if( shellSpChars.indexOf(nextChar) != -1 )
		escString += "\\";
	    
	    escString += nextChar;
	}
	return escString;
    }
    

    public static class ValidAttribute {
   	String name;
	boolean mandatory;

	public ValidAttribute (String name, boolean mandatory) {
	    this.name = name;
	    this.mandatory = mandatory;
	}

	public ValidAttribute (String name) {
	    this (name, false);
	}
    }
}
