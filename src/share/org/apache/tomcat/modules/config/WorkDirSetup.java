/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.modules.config;

import java.io.File;
import java.net.URLEncoder;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.io.FileUtil;

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
    private int attributeInfo;

    /** Workdir - a place where the servlets are allowed to write
     */
    public static final String ATTRIB_WORKDIR="javax.servlet.context.tempdir";
    // old: org.apache.tomcat.workdir
    
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

    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	attributeInfo=cm.getNoteId(ContextManager.REQUEST_NOTE,
				   "req.attribute");
    }

    
    public void addContext(ContextManager cm, Context ctx) {
	// not explicitely configured
	if( ctx.getWorkDir() == null)
	    initWorkDir(ctx);

	// #3581 - remove old dir _before_ creating the new one 
	if ( cleanWorkDir ) {
	    FileUtil.clearDir(ctx.getWorkDir() );
	}

	// Make sure the workdir exists 
	if (! ctx.getWorkDir().exists()) {
	    log("Creating work dir " + ctx.getWorkDir());
	    ctx.getWorkDir().mkdirs();
	}

    }

    public void contextShutdown( Context ctx ) {
	if ( cleanWorkDir ) {
	    FileUtil.clearDir(ctx.getWorkDir());
	}
    }

    public final Object getInfo( Context ctx, Request req,
				 int info, String k )
    {
	if( req!=null )
	    return null;
	if( info== attributeInfo ) {
	    // request for a context attribute, handled by tomcat
	    if (k.equals(ATTRIB_WORKDIR)) {
		return ctx.getWorkDir();
	    }
	}
	return null;
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
	sb.append( "TOMCAT_WORKDIR" ).append( File.separator );

        String path=ctx.getPath();
        if( path.startsWith("/")) path=path.substring(1);
        if( "".equals(path) ) path="ROOT";
        sb.append( URLEncoder.encode( path ).replace('%', '_') );
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
	    sb.append( host.replace(':', '_') );
	

	if( oldStyle ) {
	    sb.append(URLEncoder.encode( ctx.getPath() ).replace('%', '_'));
	    workDirF=new File(sb.toString());
	} else {
	    File hostD=new File( sb.toString());
	    hostD.mkdirs();

	    String path=ctx.getPath();
	    if( path.startsWith("/")) path=path.substring(1);
            if( "".equals(path) ) path="ROOT";
	    workDirF=new File( hostD, URLEncoder.encode( path ).replace('%', '_') );
	}
	ctx.setWorkDir( workDirF );
    }

	
}
