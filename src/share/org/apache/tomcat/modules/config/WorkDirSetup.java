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

package org.apache.tomcat.modules.config;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles work dir setup/removal.
 *
 * @author cmanolache@yahoo.com
 */
public class WorkDirSetup extends BaseInterceptor {
    /** Default work dir, relative to home
     */
    public static final String DEFAULT_WORK_DIR="work";
    
    boolean cleanWorkDir=false;
    String workdirBase=null;
    boolean useWebInf=false;
    boolean oldStyle=false;
    
    public WorkDirSetup() {
    }

    // -------------------- Properties --------------------
    
    /**
     * Auto-remove the work dir when tomcat is (grecefully) stoped
     * and when tomcat starts.
     * 
     * IMHO this shouldn't be used - if true, we'll loose
     * all jsp compiled files. The workdir is the only directory
     *  where the servlet is allowed to write anyway ( if policy
     * is used ).
     */
    public void setCleanWorkDir( boolean b ) {
	cleanWorkDir=b;
    }

    /** Allow the user to customize the base directory for
	workdirs ( /var/tomcat/workdir for example )
    */
    public void setWorkDirBase( String s ) {
	this.workdirBase=s;
    }

    public void setUseWebInf( boolean useWebInf ) {
	this.useWebInf = useWebInf;
    }

    public void setOldStyle( boolean b ) {
	this.oldStyle=b;
    }
    
    // -------------------- Callbacks --------------------
    
    /** Adjust context manager paths
     */
    public void engineInit( ContextManager cm )
    	throws TomcatException
    {
	initWorkDir(cm);
    }

    private void initWorkDir(ContextManager cm) {
	String workDir=cm.getWorkDir();
	if( workDir==null ) {
	    workDir= DEFAULT_WORK_DIR;
	}
	
	if( ! FileUtil.isAbsolute( workDir )) {
	    workDir=FileUtil.
		getCanonicalPath(cm.getHome() + File.separator+
				 workDir);
	}
	cm.setWorkDir( workDir );
    }

    public void addContext(ContextManager cm, Context ctx) {
	// not explicitely configured
	if( ctx.getWorkDir() == null)
	    initWorkDir(ctx);

	// Make sure the workdir exists 
	if (! ctx.getWorkDir().exists()) {
	    log("Creating work dir " + ctx.getWorkDir());
	    ctx.getWorkDir().mkdirs();
	}

	// 
	if ( cleanWorkDir ) {
	    clearDir(ctx.getWorkDir() );
	}
    }

    public void contextShutdown( Context ctx ) {
	if ( cleanWorkDir ) {
	    clearDir(ctx.getWorkDir());
	}
    }

    // -------------------- Implementation --------------------

    /** Encoded ContextManager.getWorkDir() + host + port + path
     */
    private void initWorkDir(Context ctx ) {
	if( useWebInf ) {
	    initWebInfWorkDir( ctx );
	} else {
	    initStandaloneWorkDir( ctx );
	}
    }

    private void initWebInfWorkDir( Context ctx ) {
	String absPath=ctx.getAbsolutePath();
	StringBuffer sb=new StringBuffer();
	sb.append( absPath ).append( File.separator );
	sb.append( "WEB-INF" ).append( File.separator );
	sb.append( "TOMCAT_WORKDIR" );
	File workDirF= new File( sb.toString() );
	workDirF.mkdirs();

	ctx.setWorkDir( workDirF );
    }
    
    private void initStandaloneWorkDir( Context ctx ) {
	ContextManager cm=ctx.getContextManager();
	String base=workdirBase;
	// getWorkDir will be deprecated!
	if( base==null )
	    base=cm.getWorkDir();

	File workDirF=null;

	StringBuffer sb=new StringBuffer();
	sb.append(cm.getWorkDir());
	sb.append(File.separator);
	String host=ctx.getHost();
	if( host==null ) 
	    sb.append( "DEFAULT" );
	else
	    sb.append( host );
	

	if( oldStyle ) {
	    sb.append(URLEncoder.encode( ctx.getPath() ));
	    workDirF=new File(sb.toString());
	} else {
	    File hostD=new File( sb.toString());
	    hostD.mkdirs();

	    String path=ctx.getPath();
	    if( path.startsWith("/")) path=path.substring(1);
	    workDirF=new File( hostD, URLEncoder.encode( path ));
	}
	ctx.setWorkDir( workDirF );
    }

    private void clearDir(File dir) {
        String[] files = dir.list();

        if (files != null) {
	    for (int i = 0; i < files.length; i++) {
	        File f = new File(dir, files[i]);

	        if (f.isDirectory()) {
		    clearDir(f);
	        }

	        try {
	            f.delete();
	        } catch (Exception e) {
	        }
	    }

	    try {
	        dir.delete();
	    } catch (Exception e) {
	    }
        }
    }


	
}
