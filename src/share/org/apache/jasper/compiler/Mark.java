/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/Mark.java,v 1.1 1999/10/09 00:20:37 duncan Exp $
 * $Revision: 1.1 $
 * $Date: 1999/10/09 00:20:37 $
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

/**
 * Mark represents a point in the JSP input. 
 *
 * @author Anil K. Vijendran
 */
public final class Mark {
    int cursor, line, col;
    int fileid;
    private JspReader reader;
    
    Mark(JspReader reader, int fileid) {
	this.reader = reader;
	this.cursor = this.line = this.col = 0;
	this.fileid = fileid;
    }
	
    Mark(Mark other) {
	this.reader = other.reader;
	this.fileid = other.fileid;
	this.cursor = other.cursor;
	this.line = other.line;
	this.col = other.col;
    }
	    
    public String toString() {
	return getFile()+"("+line+","+col+")";
    }

    public String getFile() {
        return reader.getFile(fileid);
    }
    
    public String toShortString() {
        return "("+line+","+col+")";
    }

    public boolean equals(Object other) {
	if (other instanceof Mark) {
	    Mark m = (Mark) other;
	    return this.reader == m.reader && this.fileid == m.fileid 
		&& this.cursor == m.cursor && this.line == m.line 
		&& this.col == m.col;
	} 
	return false;
    }
}

