/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/DumbParseEventListener.java,v 1.6 2004/02/23 06:22:36 billbarker Exp $
 * $Revision: 1.6 $
 * $Date: 2004/02/23 06:22:36 $
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;

import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;

import org.apache.jasper.JasperException;


/**
 * Throwaway class that can be used for debugging during development
 * etc. This probably should go away soon. 
 *
 * @author Anil K. Vijendran
 */
public class DumbParseEventListener extends BaseJspListener {
    
    public DumbParseEventListener(JspReader reader, ServletWriter writer) {
	super(reader, writer);
    }

    public void handleComment(Mark start, Mark stop) 
        throws JasperException 
    {
	System.err.println("\nComment: ");
	System.err.print("\t");
	System.err.println(reader.getChars(start, stop));
    }

    public void handleDirective(String directive, Mark start, Mark stop, Hashtable attrs) 
        throws JasperException 
    {
	System.err.println("\nDirective: "+directive);
	System.err.println("\t"+attrs);
    }
    
    public void handleDeclaration(Mark start, Mark stop, Hashtable attrs) 
        throws JasperException 
    {
	System.err.println("\nDeclaration: ");
	System.err.println(reader.getChars(start, stop));
    }
    
    public void handleScriptlet(Mark start, Mark stop, Hashtable attrs) 
        throws JasperException 
    {
	System.err.println("\nScriptlet: ");
	System.err.println(reader.getChars(start, stop));
    }
    
    public void handleExpression(Mark start, Mark stop, Hashtable attrs) 
        throws JasperException 
    {
	System.err.println("\nExpression: ");
	System.err.println(reader.getChars(start, stop));
    }

    public void handleBean(Mark start, Mark stop, Hashtable attrs)
        throws JasperException 
    {
	System.err.println("\nBean: ");
	System.err.println("\t"+attrs);
    }

    public void handleBeanEnd (Mark start, Mark stop, Hashtable attrs)
	throws JasperException 
    {
        
	System.err.println("\nBean: ");
	System.err.println("\t"+attrs);
    }


    public void handleGetProperty(Mark start, Mark stop, Hashtable attrs)	
        throws JasperException 
    {
	System.err.println("\nGetProperty: ");
	System.err.println("\t"+attrs);
    }
    
    public void handleSetProperty(Mark start, Mark stop, Hashtable attrs)
        throws JasperException 
    {
	System.err.println("\nSetProperty: ");
	System.err.println("\t"+attrs);
    }
    
    public void handlePlugin(Mark start, Mark stop, Hashtable attrs)
        throws JasperException 
    {
	System.err.println("\nPlugin: ");
	System.err.println("\t"+attrs);
    }
    
    public void handleCharData(char[] chars)
        throws JasperException 
    {
	System.err.print(chars);
    }
    
    public void handleForward(Mark start, Mark stop, Hashtable attrs)
        throws JasperException 
    {
	System.err.println("\n Forward: ");
	System.err.println("\t"+attrs);
    }

    public void handleInclude(Mark start, Mark stop, Hashtable attrs)
        throws JasperException 
    {
	System.err.println("\n Include: ");
	System.err.println("\t"+attrs);
    }

    public TagLibraries getTagLibraries() {
	return null;
    }

    public void handleTagBegin(Mark start, Hashtable attrs, String prefix, 
			       String shortTagName, TagLibraryInfo tli, 
			       TagInfo ti)
	throws JasperException
    {
	System.err.println("\nUser-defined Tag Start "+prefix+":"+shortTagName+" --> " );
	System.err.println("\tAttrs"+attrs);
    }
    
    public void handleTagEnd(Mark start, Mark stop, String prefix, 
			     String shortTagName, Hashtable attrs, 
                             TagLibraryInfo tli, TagInfo ti)
	throws JasperException
    {
	System.err.println("\nUser-defined Tag End "+prefix+":"+shortTagName+" --> ");
	System.err.println("\tBody "+reader.getChars(start, stop));
    }

}
