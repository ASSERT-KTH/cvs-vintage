/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/BaseJspListener.java,v 1.8 2004/02/23 02:45:11 billbarker Exp $
 * $Revision: 1.8 $
 * $Date: 2004/02/23 02:45:11 $
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

import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;

/**
 * An abstract base class to make things easy during development.
 *
 * @author Anil K. Vijendran
 */
public class BaseJspListener implements ParseEventListener {
    protected JspReader reader;
    protected ServletWriter writer;
    
    protected BaseJspListener(JspReader reader, ServletWriter writer) {
	this.reader = reader;
	this.writer = writer;
    }

    public void setTemplateInfo(Mark start, Mark stop) {
    }

    public void beginPageProcessing() throws JasperException {
    }
    
    public void endPageProcessing() throws JasperException {
    }
    
    public void handleComment(Mark start, Mark stop) throws JasperException {
	throw new JasperException(Constants.getString("jsp.error.not.impl.comments"));
    }

    public void handleDirective(String directive, Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.directives"));
    }
    
    public void handleDeclaration(Mark start, Mark stop, Hashtable attrs) throws JasperException {
	throw new JasperException(Constants.getString("jsp.error.not.impl.declarations"));
    }
    
    public void handleScriptlet(Mark start, Mark stop, Hashtable attrs) throws JasperException {
	throw new JasperException(Constants.getString("jsp.error.not.impl.scriptlets"));
    }
    
    public void handleExpression(Mark start, Mark stop, Hashtable attrs) throws JasperException {
	throw new JasperException(Constants.getString("jsp.error.not.impl.expressions"));
    }

    public void handleBean(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException
    {
        throw new JasperException(Constants.getString("jsp.error.not.impl.usebean"));
    }
    
    public void handleBeanEnd(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        throw new JasperException(Constants.getString("jsp.error.not.impl.usebean"));
    }

    public void handleGetProperty(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        throw new JasperException(Constants.getString("jsp.error.not.impl.getp"));
    }
    
    public void handleSetProperty(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        throw new JasperException(Constants.getString("jsp.error.not.impl.setp"));
    }
    
    public void handlePlugin(Mark start, Mark stop, Hashtable attrs,
    				Hashtable param, String fallback) 
        throws JasperException 
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.plugin"));
    }
    
    public void handleCharData(Mark start, Mark stop, char[] chars) throws JasperException {
        System.err.print(chars);
    }

    public void handleForward(Mark start, Mark stop, Hashtable attrs, Hashtable param) 
        throws JasperException 
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.forward"));
    }

    public void handleInclude(Mark start, Mark stop, Hashtable attrs, Hashtable param) 
        throws JasperException 
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.include"));
    }

    public void handleTagBegin(Mark start, Mark stop, Hashtable attrs, String prefix, 
			       String shortTagName, TagLibraryInfo tli, 
			       TagInfo ti)
	throws JasperException
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.taglib"));
    }
    
    public void handleTagEnd(Mark start, Mark stop, String prefix, 
			     String shortTagName, Hashtable attrs, 
                             TagLibraryInfo tli, TagInfo ti)
	throws JasperException
    {
	throw new JasperException(Constants.getString("jsp.error.not.impl.taglib"));
    }
    
    public TagLibraries getTagLibraries() {
	return null;
    }
}
