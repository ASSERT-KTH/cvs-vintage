/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/JspFactoryImpl.java,v 1.14 2001/10/20 02:53:39 larryi Exp $
 * $Revision: 1.14 $
 * $Date: 2001/10/20 02:53:39 $
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
package org.apache.jasper.runtime;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.PageContext;

import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.log.*;

/**
 * Implementation of JspFactory from the spec. Helps create
 * PageContext and other animals.  
 *
 * @author Anil K. Vijendran
 */
public class JspFactoryImpl extends JspFactory {
    public static final int DEFAULT_POOL_SIZE=100;
    private SimplePool pool;
    private boolean usePool=true;
    static String lineSeparator;
    static {
	try {
	    lineSeparator =  System.getProperty("line.separator");
	} catch( RuntimeException ex ) {
	    lineSeparator="\r\n";
	}
	// This whole things allows us to set the writer line
	// separator when we init jasper, i.e. in priv. mode -
	// without it we would need a priviledged action.
	JspWriterImpl.lineSeparator=lineSeparator;
    }

    public JspFactoryImpl() {
	pool=new SimplePool( DEFAULT_POOL_SIZE );
	usePool=true;
    }

    public JspFactoryImpl( int size ) {
	if( size==0 ) {
	    pool=null;
	    usePool=false;
	} else {
	    pool=new SimplePool( size );
	}
    }
    

    Log loghelper = Log.getLog("JASPER_LOG", "JspFactoryImpl");
    
    public PageContext getPageContext(Servlet servlet, ServletRequest request,
                                      ServletResponse response, 
				      String errorPageURL, 
                                      boolean needsSession, int bufferSize, 
                                      boolean autoflush) 
    {
        try {
	    PageContext pc;
	    if( usePool ) {
		pc=(PageContext)pool.get();
		if( pc == null ) pc= new PageContextImpl(this);
	    } else {
		pc =  new PageContextImpl(this);
	    }

	    //	    System.out.println("JspFactoryImpl.getPC"  + pc);
	    pc.initialize(servlet, request, response, errorPageURL, 
                          needsSession, bufferSize, autoflush);
	    
            return pc;
        } catch (Throwable ex) {
            /* FIXME: need to do something reasonable here!! */
	    loghelper.log("Exception initializing page context", ex);
            return null;
        }
    }

    public void releasePageContext(PageContext pc) {
	if( pc==null ) return;
	pc.release();
	if( usePool) {
	    pool.put( pc );
	}
    }

    static class SunJspEngineInfo extends JspEngineInfo {
        public String getSpecificationVersion() {
            return "1.1";
        }
    }
    
    static JspEngineInfo info = new SunJspEngineInfo();

    public JspEngineInfo getEngineInfo() {
        return info;
    }
}
