/*
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.tomcat.context;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.request.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.servlet.http.*;

import org.apache.tomcat.logging.*;

/**
 * Check ContextManager and set defaults for non-set properties
 *
 * @author costin@dnt.ro
 */
public class DefaultCMSetter extends BaseInterceptor {

    public DefaultCMSetter() {
    }

    public void engineInit(ContextManager cm) throws TomcatException {
	File homeF=new File( cm.getHome());
	
	// Setup loggers - they may have relative paths.
	Enumeration enum=Logger.getLoggerNames();
	while( enum.hasMoreElements() ) {
	    String loggerN=(String)enum.nextElement();
	    Logger l=Logger.getLogger( loggerN );
	    String path=l.getPath();
	    if( path!=null ) {
		File f=new File( path );
		if( ! f.isAbsolute() ) {
		    // Make it relative to home !
		    File wd=new File(homeF , path );
		    l.setPath( wd.getAbsolutePath() );
		}
		// create the files, ready to log.
	    } 
	    l.open();
	}

	// check if we have the right tomcat.home directory
	// Tomcat.home is needed in several places - it keeps .dtd and default
	// config files, etc - if it's not set probably something is wrong.
	//
	// It defaults to the home attribute - this is the common case anyway.
	//
	// XXX document all uses of tomcat.home
	// ( as a rule: cm.getHome() is used to access all files, but if a file is not
	// found, cm.getTomcatHome() is used)
	// ( getHome returns the "instance" of tomcat, it may have it's own log, work, webapps,
	//   getTomcatHome is where tomcat is installed )
	//  Note: home defaults to tomcat.home if none is set...
	File f=new File( cm.getInstallDir() + "/conf/web.xml");
	if( ! f.exists() ) {
	    throw new TomcatException( "Wrong tomcat home " +
	                               cm.getInstallDir());
	}
	// update the workdir
	String workDir=cm.getWorkDir();
	f=new File( workDir );
	if( ! f.isAbsolute() ) {
	    // Make it relative to home !
	    File wd=new File(homeF , workDir );
	    cm.setWorkDir( wd.getAbsolutePath() );
	}

    }

    public void contextInit( Context ctx)
	throws TomcatException
    {
	setEngineHeader( ctx );

	if( ctx.getWorkDir() == null)
	    setWorkDir(ctx);

	if (! ctx.getWorkDir().exists()) {
	    //log  System.out.println("Creating work dir " + ctx.getWorkDir() );
	    ctx.getWorkDir().mkdirs();
	}
	ctx.setAttribute(Constants.ATTRIB_WORKDIR1, ctx.getWorkDir());
	ctx.setAttribute(Constants.ATTRIB_WORKDIR , ctx.getWorkDir());

	// Set default session manager if none set
	ServletWrapper authWrapper=new ServletWrapper();
	authWrapper.setContext( ctx );
	authWrapper.setServletName( "tomcat.authServlet");
	String login_type=ctx.getAuthMethod();
	if( "BASIC".equals( login_type )) {
	    authWrapper.setServletClass( "org.apache.tomcat.servlets.BasicLoginServlet" );
	    ctx.addServlet( authWrapper );
	} else if( "FORM".equals( login_type )) {
	    authWrapper.setServletClass( "org.apache.tomcat.servlets.BasicLoginServlet" );
	    //authWrapper.setServletClass( "org.apache.tomcat.servlets.FormLoginServlet" );
	    ctx.addServlet( authWrapper );
	} else {
	    authWrapper.setServletClass( "org.apache.tomcat.servlets.BasicLoginServlet" );
	    ctx.addServlet( authWrapper );
	    //	    ctx.log("Unknown auth method " + login_type );
	}
	
	ServletWrapper errorWrapper=new ServletWrapper();
	errorWrapper.setContext( ctx );
	errorWrapper.setServletClass( "org.apache.tomcat.servlets.DefaultErrorPage" );
	errorWrapper.setServletName( "tomcat.errorPage");
	ctx.addServlet( errorWrapper );

	// Validation for error  servlet
 	try {
	    ServletWrapper errorWrapper1=ctx.getServletByName( "tomcat.errorPage");
	    errorWrapper1.initServlet();
	} catch( Exception ex ) {
	    System.out.println("Error loading default servlet ");
            ex.printStackTrace();
	    // XXX remove this context from CM
	    throw new TomcatException( "Error loading default error servlet ", ex );
	}
    }

    // -------------------- implementation
    /** Encoded ContextManager.getWorkDir() + host + port + path
     */
    private void setWorkDir(Context ctx ) {
	ContextManager cm=ctx.getContextManager();

	StringBuffer sb=new StringBuffer();
	sb.append(cm.getWorkDir());
	sb.append(File.separator);
	String host=ctx.getHost();
	if( host==null ) 
	    sb.append(cm.getHostName() );
	else
	    sb.append( host );
	sb.append("_").append(cm.getPort());
	sb.append(URLEncoder.encode( ctx.getPath() ));
	
	ctx.setWorkDir( new File(sb.toString()));
    }
    
    private void setEngineHeader(Context ctx) {
        String engineHeader=ctx.getEngineHeader();

	if( engineHeader==null) {
	    /*
	     * Whoever modifies this needs to check this modification is
	     * ok with the code in com.jsp.runtime.ServletEngine or talk
	     * to akv before you check it in. 
	     */
	    // Default value for engine header
	    // no longer use core.properties - the configuration comes from
	    // server.xml or web.xml - no more properties.
	    StringBuffer sb=new StringBuffer();
	    sb.append(Constants.TOMCAT_NAME).append("/").append(Constants.TOMCAT_VERSION);
	    sb.append(" (").append(Constants.JSP_NAME).append(" ").append(Constants.JSP_VERSION);
	    sb.append("; ").append(Constants.SERVLET_NAME).append(" ");
	    sb.append(Constants.SERVLET_MAJOR).append(".").append(Constants.SERVLET_MINOR);
	    sb.append( "; Java " );
	    sb.append(System.getProperty("java.version")).append("; ");
	    sb.append(System.getProperty("os.name") + " ");
	    sb.append(System.getProperty("os.version") + " ");
	    sb.append(System.getProperty("os.arch") + "; java.vendor=");
	    sb.append(System.getProperty("java.vendor")).append(")");
	    engineHeader=sb.toString();
	}
	ctx.setEngineHeader( engineHeader );
    }

}
