/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagGeneratorBase.java,v 1.2 1999/10/20 11:22:55 akv Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/20 11:22:55 $
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

import java.util.Stack;
import java.util.Hashtable;

import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Common stuff for use with TagBegin and TagEndGenerators. 
 *
 * @author Anil K. Vijendran
 */
abstract class TagGeneratorBase extends GeneratorBase {
    static private Stack tagHandlerStack = new Stack();

    static private Hashtable tagVarNumbers = new Hashtable();

    static protected void tagBegin(String tagHandlerInstanceName) {
	tagHandlerStack.push(tagHandlerInstanceName);
    }

    static protected String tagEnd() {
	return (String) tagHandlerStack.pop();
    }

    static protected String topTag() {
	if (tagHandlerStack.empty())
	    return "null";
	return (String) tagHandlerStack.peek();
    }
	    
    static protected String getTagVarName(String prefix, String shortTagName) {
	synchronized (tagVarNumbers) {
	    String tag = prefix+":"+shortTagName;
	    String varName = prefix+"_"+shortTagName+"_";
	    if (tagVarNumbers.get(tag) != null) {
		Integer i = (Integer) tagVarNumbers.get(tag);
		varName = varName + i.intValue();
		tagVarNumbers.put(tag, new Integer(i.intValue()+1));
		return varName;
	    } else {
		tagVarNumbers.put(tag, new Integer(1));
		return varName+"0";
	    }
	}
    }

    protected static void declareVariables(ServletWriter writer, VariableInfo[] vi, 
                                           boolean declare, boolean update, int scope) 
    {
        if (vi != null)
            for(int i = 0; i < vi.length; i++)
                if (vi[i].getScope() == scope) {
                    if (vi[i].getDeclare() == true && declare == true)
                        writer.println(vi[i].getClassName()+" "+vi[i].getVarName()+" = null;");
                    if (update == true)
                        writer.println(vi[i].getVarName()+" = ("+
                                       vi[i].getClassName()+") pageContext.getAttribute("
                                       +writer.quoteString(vi[i].getVarName())+");");
                }
    }
}
