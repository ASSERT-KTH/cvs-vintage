/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/ParseEventListener.java,v 1.8 2004/02/23 02:45:12 billbarker Exp $
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

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;

/**
 * Interface for the JSP code generation backend. At some point should
 * probably try and make this a SAX (XML) listener. 
 *
 * @author Anil K. Vijendran
 */
public interface ParseEventListener {
    void setTemplateInfo(Mark start, Mark stop);
    void beginPageProcessing() throws JasperException;

    void handleComment(Mark start, Mark stop) throws JasperException;
    void handleDirective(String directive, 
			 Mark start, Mark stop, 
			 Hashtable attrs) throws JasperException;
    void handleDeclaration(Mark start, Mark stop, Hashtable attrs) throws JasperException;
    void handleScriptlet(Mark start, Mark stop, Hashtable attrs) throws JasperException;
    void handleExpression(Mark start, Mark stop, Hashtable attrs) throws JasperException;
    void handleBean(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException;
    void handleBeanEnd (Mark start, Mark stop, Hashtable attrs)
	throws JasperException;
    void handleGetProperty(Mark start, Mark stop, Hashtable attrs) throws JasperException;
    void handleSetProperty(Mark start, Mark stop, Hashtable attrs) throws JasperException;
    void handlePlugin(Mark start, Mark stop, Hashtable attrs, Hashtable param, 
    			String fallback) throws JasperException;
    void handleCharData(Mark start, Mark stop, char[] chars) throws JasperException;


    /*
     * Custom tag support
     */
    TagLibraries getTagLibraries();

    /*
     * start: is either the start position at "<" if content type is JSP or empty, or
     *        is the start of the body after the "/>" if content type is tag dependent
     * stop: can be null if the body contained JSP tags... 
     */
    void handleTagBegin(Mark start, Mark stop, Hashtable attrs, String prefix, String shortTagName,
			TagLibraryInfo tli, TagInfo ti) 
	throws JasperException;

    void handleTagEnd(Mark start, Mark stop, String prefix, String shortTagName,
		      Hashtable attrs, TagLibraryInfo tli, TagInfo ti)
	throws JasperException;

    void handleForward(Mark start, Mark stop, Hashtable attrs, Hashtable param)
	throws JasperException;
    void handleInclude(Mark start, Mark stop, Hashtable attrs, Hashtable param)
	throws JasperException;

    void endPageProcessing() throws JasperException;
}

