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
import java.util.Hashtable;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.depend.DependManager;
import org.apache.tomcat.util.depend.Dependency;
import org.apache.tomcat.util.io.FileUtil;

/**
 * Will manage a repository of .war files, expanding them automatically
 * and eventually re-deploying them.
 *
 * Based on the original AutoSetup.
 * 
 * @author cmanolache@yahoo.com
 */
public class AutoDeploy extends BaseInterceptor {
    // Afer DefaultCMSettup, before any other interceptor that needs contexts
    Hashtable hosts=new Hashtable();

    String src="webapps";
    String dest="webapps";
    boolean redeploy=false;

    File webappS;
    File webappD;
	

    // map destination dir ( used in Ctx docBase ) -> File ( war source)
    Hashtable expanded=new Hashtable();
    
    public AutoDeploy() {
    }

    //-------------------- Config --------------------
    
    /**
     *  Directory where war files are deployed
     *  Defaults to TOMCAT_HOME/webapps.
     */
    public void setSource( String d ) {
	src=d;
    }

    /**
     *  Directory where war files are deployed
     *  Defaults to TOMCAT_HOME/webapps.
     */
    public void setTarget( String d ) {
	dest=d;
    }

    /**
     * "Flat" directory support - no virtual host support.
     *  XXX Not implemented - only true.
     */
    public void setFlat( boolean b ) {
    }

    /**
     *  Re-deploy the context if the war file is modified.
     */
    public void setRedeploy( boolean b ) {
	redeploy=b;
    }
    
    //-------------------- Implementation --------------------
    
    /**
     *  Find all wars and expand them. 
     *  Do this as early as possible - we don't need anything from the engine.
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	//	checkHooks(cm, ctx, module);
	if( this != module ) return;
	// For all contexts in <server.xml > or loaded by differen means,
	// check if the docBase ends with .war - and expand it if so,
	// after that replace the docBase with the dir. See bug 427.
	/* XXX not ready yet.
	   // XXX Should be done on addContext hook too
	Enumeration loadedCtx=cm.getContexts();
	while( loadedCtx.hasMoreElements() ) {
	    Context ctx=(Context)loadedCtx.nextElement();
	    String docBase=ctx.getDocBase();
	    if( docBase.endsWith( ".war" ) ) {
		expandWar( ctx, docBase);
	    }
	}
	*/
	
	// expand all the wars from srcDir ( webapps/*.war ).
	String home=cm.getHome();

	if( src.startsWith( "/" ) ) 
	    webappS=new File(src);
	else
	    webappS=new File(home + "/" + src);

	if( dest.startsWith( "/" ) ) 
	    webappD=new File(dest);
	else
	    webappD=new File(home + "/" + dest);
	
	if (! webappD.exists() || ! webappD.isDirectory() ||
	    ! webappS.exists() || ! webappS.isDirectory()) {
	    log("Source or destination missing ");
	    return ; // nothing to set up
	}
	
	String[] list = webappS.list();

	for (int i = 0; i < list.length; i++) {
	    String name = list[i];
	    File f=new File( webappS, name );
	    if( name.endsWith(".war") ) {
		expandWar( webappS, webappD, name );
	    }
	}
    }

    /** Auto-expand wars
     */
    private void expandWar( File srcD, File destD, String name ) {
	String fname=name.substring(0, name.length()-4);

	File appDir=new File( destD, fname);
	File srcF=new File( srcD, name );
	expanded.put( appDir.getAbsolutePath(),
		      new DeployInfo( srcD, destD, srcF, appDir, name ) );
	if( redeploy ) {
	    // if appDir is older than the war, and re-deploy enabled -
	    if( appDir.exists() &&
		appDir.lastModified() < srcF.lastModified() ) {
		log( "WAR file is newer, removing old dir " + srcF + " " +name );
		FileUtil.clearDir( appDir );
	    }
	}
	
	if( ! appDir.exists() ) {
	    // no check if war file is "newer" than directory 
	    // To update you need to "remove" the context first!!!
	    appDir.mkdirs();
	    // Expand war file
	    log( "Expanding " + srcF );
	    try {
		FileUtil.expand(srcF.getAbsolutePath(), 
				appDir.getAbsolutePath() );

	    } catch( IOException ex) {
		log("expanding webapp " + name, ex);
		// do what ?
	    }
	}
    }

    public void addContext( ContextManager cm, Context ctx )
	throws TomcatException 
    {
	// this may be called on a "full" reload ( stop/start ctx )
	if( redeploy ) {
	    String ctxBase=ctx.getAbsolutePath();
	    DeployInfo dInfo=(DeployInfo)expanded.get( ctxBase );
	    if( dInfo == null || ! dInfo.srcF.exists() )
		return;
	    if( dInfo.appDir.exists() &&
		dInfo.appDir.lastModified() < dInfo.srcF.lastModified() ) {
		log( "WAR file is newer, removing old dir " + dInfo.srcF
		     + " " + dInfo.name );
		FileUtil.clearDir( dInfo.appDir );
		
		dInfo.appDir.mkdirs();
		// Expand war file
		log( "Expanding " + dInfo.srcF );
		try {
		    FileUtil.expand(dInfo.srcF.getAbsolutePath(), 
				    dInfo.appDir.getAbsolutePath() );
		    
		} catch( IOException ex) {
		    log("expanding webapp " + dInfo.name, ex);
		    // do what ?
		}
	    }
	    
	}
    }

    public void contextInit( Context context)
	throws TomcatException
    {
	if( redeploy ) {
	    String ctxBase=context.getAbsolutePath();
	    DeployInfo dInfo=(DeployInfo)expanded.get( ctxBase );
	    if( dInfo == null || ! dInfo.srcF.exists() )
		return;

	    File warFile=dInfo.srcF;
	    DependManager dm=(DependManager)context.getContainer().
		getNote("DependManager");
	    if( dm!=null ) {
		log( "Adding dependency " + context + " -> " +  warFile );
		Dependency dep=new Dependency();
		dep.setTarget("web.xml");
		dep.setOrigin( warFile );
		dep.setLastModified( warFile.lastModified() );
		dm.addDependency( dep );
		context.getContainer().setNote( "autoDeploy.war", dInfo);

	    } else {
		log( "No reloading for " + context + " -> " +  warFile );
	    }
	}
    }
    
    public void reload( Request req, Context context) throws TomcatException {
	log("Reloading " + redeploy );
	if( redeploy ) {
	    DeployInfo dI=(DeployInfo)context.getContainer().getNote( "autoDeploy.war" );
	    if( dI==null ) return;
	    log( "Re-deploying " + dI.srcF );
	    
	    // First remove the old directory
	    log( "Removing " + dI.appDir );
	    FileUtil.clearDir( dI.appDir );
	
	// now expand again.
	    expandWar( dI.srcD, dI.destD, dI.name );
	}
    }

    static class DeployInfo {
	File srcD, destD, srcF, appDir;
	String name;
	
	DeployInfo(File srcD, File destD, File srcF, File appDir, String name)
	{
	    this.srcD=srcD;
	    this.srcF=srcF;
	    this.destD=destD;
	    this.appDir=appDir;
	    this.name=name;
	    
	}

    }
}

