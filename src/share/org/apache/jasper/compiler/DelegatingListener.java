/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/DelegatingListener.java,v 1.8 2004/02/23 02:45:12 billbarker Exp $
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
 * Simple util class.... see usage in Parser.Parser(). Not intended for anything
 * other than that purpose.... 
 *
 * @author Anil K. Vijendran
 */
final class DelegatingListener implements ParseEventListener {
    ParseEventListener delegate;
    Parser.Action action;
    Mark tmplStart, tmplStop;
    
    DelegatingListener(ParseEventListener delegate, Parser.Action action) {
        this.delegate = delegate;
        this.action = action;
    }

    void doAction(Mark start, Mark stop) throws JasperException {
        action.execute(start, stop);
    }

    public void setTemplateInfo(Mark start, Mark stop) {
	this.tmplStart = start;
	this.tmplStop = stop;
    }

    public void beginPageProcessing() throws JasperException {
        delegate.beginPageProcessing();
    }
    
    public void endPageProcessing() throws JasperException {
        delegate.endPageProcessing();
    }
    
    public void handleComment(Mark start, Mark stop) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleComment(start, stop);
    }

    public void handleDirective(String directive, Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleDirective(directive, start, stop, attrs);
    }
    
    public void handleDeclaration(Mark start, Mark stop, Hashtable attrs) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleDeclaration(start, stop, attrs);
    }
    
    public void handleScriptlet(Mark start, Mark stop, Hashtable attrs) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleScriptlet(start, stop, attrs);
    }
    
    public void handleExpression(Mark start, Mark stop, Hashtable attrs) throws JasperException {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleExpression(start, stop, attrs);
    }

    public void handleBean(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleBean(start, stop, attrs);
    }
    
    public void handleBeanEnd(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleBeanEnd(start, stop, attrs);
    }

    public void handleGetProperty(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleGetProperty(start, stop, attrs);
    }
    
    public void handleSetProperty(Mark start, Mark stop, Hashtable attrs) 
	throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleSetProperty(start, stop, attrs);
    }
    
    public void handlePlugin(Mark start, Mark stop, Hashtable attrs,
    				Hashtable param, String fallback) 
        throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handlePlugin(start, stop, attrs, param, fallback);
    }
    
    public void handleCharData(Mark start, Mark stop, char[] chars) throws JasperException {
        delegate.handleCharData(start, stop, chars);
    }

    public void handleForward(Mark start, Mark stop, Hashtable attrs, Hashtable param) 
        throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleForward(start, stop, attrs, param);
    }

    public void handleInclude(Mark start, Mark stop, Hashtable attrs, Hashtable param) 
        throws JasperException 
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleInclude(start, stop, attrs, param);
    }

    public void handleTagBegin(Mark start, Mark stop, Hashtable attrs, String prefix, 
			       String shortTagName, TagLibraryInfo tli, 
			       TagInfo ti)
	throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleTagBegin(start, stop, attrs, prefix, shortTagName, tli, ti);
    }
    
    public void handleTagEnd(Mark start, Mark stop, String prefix, 
			     String shortTagName, Hashtable attrs, 
                             TagLibraryInfo tli, TagInfo ti)
	throws JasperException
    {
        doAction(this.tmplStart, this.tmplStop);
        delegate.handleTagEnd(start, stop, prefix, shortTagName, attrs, tli, ti);
    }
    
    public TagLibraries getTagLibraries() {
        return delegate.getTagLibraries();
    }
}

