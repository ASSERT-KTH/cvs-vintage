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
import java.io.IOException;
import java.util.Enumeration;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.res.StringManager;

/**
 * Set ( and guess ) the paths to absolute ( and canonical ) directories.
 * This module must be added first ( before even ServerXmlReader ).
 * 
 * If tomcat is embeded _and_ you are sure that all paths you set
 * are OK - you may not need this ( but better to be safe and add it ).
 *
 * You don't have to insert this in server.xml - it's better to add it
 * manually, to be sure it is first.
 *
 * Will set: tomcat.home, CM.home, CM.installDir, CM.workDir, Ctx.absolutePath
 * 
 * ( based on DefaultCMSetter )
 *
 * @author Costin Manolache
 */
public final class PathSetter extends BaseInterceptor {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    /** Default work dir, relative to home
     */
    public static final String DEFAULT_WORK_DIR="work";
    
    public PathSetter() {
    }

    /** Adjust context manager paths. This happens before anything
     * 	else. 
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( this != module ) return;

	// Adjust paths in CM
	String home=cm.getHome();
	if( home==null ) {
	    // try system property
	    home=System.getProperty(ContextManager.TOMCAT_HOME);
	}

	// if "home" is not set, guess "install" and use as "home"
	if( home==null ) {
	    home=IntrospectionUtils.guessInstall(
				ContextManager.TOMCAT_INSTALL,
				ContextManager.TOMCAT_HOME,
				"tomcat_core.jar",
				"org/apache/tomcat/core/Request.class");
	}

	if (home != null) {
	    // Make it absolute
	    home=FileUtil.getCanonicalPath( home );
	    cm.setHome( home );
	}
	
	String installDir=cm.getInstallDir();
	// if "install" is not set, guess "install" if not already guessed
	if ( installDir==null ) {
	    installDir=IntrospectionUtils.guessInstall(
				ContextManager.TOMCAT_INSTALL,
				ContextManager.TOMCAT_HOME,
				"tomcat_core.jar",
				"org/apache/tomcat/core/Request.class");
	}
	if( installDir!= null ) {
	    installDir=FileUtil.getCanonicalPath( installDir );
	    cm.setInstallDir( installDir );
	}

	// if only one is set home==installDir

	if( home!=null && installDir == null ) {
	    cm.setInstallDir( home );
	    installDir=home;
	}

	if( home==null && installDir != null ) {
	    cm.setHome( installDir );
	    home=installDir;
	}

	// if neither home or install is set,
	// and no system property, try "."
	if( home==null && installDir==null ) {
	    home=FileUtil.getCanonicalPath( "." );
	    installDir=home;

	    cm.setHome( home );
	    cm.setInstallDir( home );
	}

	System.getProperties().put(ContextManager.TOMCAT_HOME, cm.getHome());
	System.getProperties().put(ContextManager.TOMCAT_INSTALL, cm.getInstallDir());
    }

    /** After server.xml is read - make sure the workDir is absolute,
     *  and all global loggers are set to absolute paths and open.
     */
    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	// Adjust work dir
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

	if( debug>0 ||
	    ! cm.getHome().equals( cm.getInstallDir() ) ) {
	    log( "install=" + cm.getInstallDir());
	}
	log( "home=" +  cm.getHome());
	if( debug>0)
	    log( " work=" + workDir);
    }

    public void engineState( ContextManager cm , int state )
	throws TomcatException
    {
	if( state!=ContextManager.STATE_CONFIG ) return;
	Enumeration ctxsE= cm.getContexts();
	while( ctxsE.hasMoreElements() ) {
	    // Set the paths - we do this in advanced, at this stage we should be
	    // ready to do so.
	    Context context=(Context)ctxsE.nextElement();
	    addContext( cm, context);
	}
    }
    
    /** Adjust paths for a context - make the base and all loggers
     *  point to canonical paths.
     */
    public void addContext( ContextManager cm, Context ctx)
	throws TomcatException
    {
	// adjust context paths and loggers

	String docBase=ctx.getDocBase();
	String absPath=ctx.getAbsolutePath();
	if( absPath==null ) {
	    if (FileUtil.isAbsolute( docBase ) )
		absPath=docBase;
	    else
		absPath = cm.getHome() + File.separator + docBase;
	    try {
		absPath = new File(absPath).getCanonicalPath();
	    } catch (IOException npe) {
	    }
	    ctx.setAbsolutePath( absPath );
	}
	if( debug > 0 ) {
	    String h=ctx.getHost();
	    log( "addContext: " + ((h==null) ? "":h) + ":" +
		 ctx.getPath() + " " + docBase + " " + absPath + " " +
		 cm.getHome());
	}
	
    }
}

