/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/runtime/JspFactoryImpl.java,v 1.17 2004/02/23 06:26:32 billbarker Exp $
 * $Revision: 1.17 $
 * $Date: 2004/02/23 06:26:32 $
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

package org.apache.jasper.runtime;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.log.Log;

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
