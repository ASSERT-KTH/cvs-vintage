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

package org.apache.tomcat.modules.loggers;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.hooks.Hooks;

/** Log all hook events during tomcat execution.
 *  Use debug>0 to log addContainer ( since this generates a lot of
 *  output )
 */
public class LogEvents extends BaseInterceptor {
    boolean enabled=false;
    
    public LogEvents() {
    }

    public void setEnabled( boolean b ) {
	enabled=b;
    }
    
    public int registerHooks( Hooks hooks, ContextManager cm, Context ctx ) {
	if( enabled || cm.getDebug() > 5 ) {
	    enabled=true;
	    log( "Adding LogEvents, cm.debug=" + cm.getDebug() + " "
		 + enabled);
	    hooks.addModule( this );
	}
	return OK;
    }
    
    // -------------------- Request notifications --------------------
    public int requestMap(Request request ) {
	log( "requestMap " + request);
	return 0;
    }

    public int contextMap( Request request ) {
	log( "contextMap " + request);
	return 0;
    }

    public int preService(Request request, Response response) {
	log( "preService " + request);
	return 0;
    }

    public int authenticate(Request request, Response response) {
	log( "authenticate " + request);
	return DECLINED;
    }

    public int authorize(Request request, Response response,
			 String reqRoles[])
    {
	StringBuffer sb=new StringBuffer();
	appendSA( sb, reqRoles, " ");
	log( "authorize " + request + " " + sb.toString() );
	return DECLINED;
    }

    public int beforeBody( Request request, Response response ) {
	log( "beforeBody " + request);
	return 0;
    }

    public int beforeCommit( Request request, Response response) {
	log( "beforeCommit " + request);
	return 0;
    }


    public int afterBody( Request request, Response response) {
	log( "afterBody " + request);
	return 0;
    }

    public int postRequest( Request request, Response response) {
	log( "postRequest " + request);
	return 0;
    }

    public int handleError( Request request, Response response, Throwable t) {
	log( "handleError " + request +  " " + t);
	return 0;
    }

    public int postService(Request request, Response response) {
	log( "postService " + request);
	return 0;
    }

    public int newSessionRequest( Request req, Response res ) {
	log( "newSessionRequest " + req );
	return 0;
    }
    
    // -------------------- Context notifications --------------------
    public void contextInit(Context ctx) throws TomcatException {
	log( "contextInit " + ctx);
    }

    public void contextShutdown(Context ctx) throws TomcatException {
	log( "contextShutdown " + ctx);
    }

    /** Notify when a new servlet is added
     */
    public void addServlet( Context ctx, Handler sw) throws TomcatException {
	log( "addServlet " + ctx + " " + sw );
    }
    
    /** Notify when a servlet is removed from context
     */
    public void removeServlet( Context ctx, Handler sw) throws TomcatException {
	log( "removeServlet " + ctx + " " + sw);
    }

    public void addMapping( Context ctx, String path, Handler servlet)
	throws TomcatException
    {
	log( "addMapping " + ctx + " " + path + "->" + servlet);
    }


    public void removeMapping( Context ctx, String path )
	throws TomcatException
    {
	log( "removeMapping " + ctx + " " + path);
    }

    private void appendSA( StringBuffer sb, String s[], String sep) {
	for( int i=0; i<s.length; i++ ) {
	    sb.append( sep ).append( s[i] );
	}
    }
    
    /** 
     */
    public void addSecurityConstraint( Context ctx, String path[],
				       String methods[], String transport,
				       String roles[] )
	throws TomcatException
    {
	StringBuffer sb=new StringBuffer();
	sb.append("addSecurityConstraint " + ctx + " " );
	if( methods!=null ) {
	    sb.append("Methods: ");
	    appendSA( sb, methods, " " );
	}
	if( path!=null) {
	    sb.append(" Paths: ");
	    appendSA( sb, path, " " );
	}
	if( roles!=null) {
	    sb.append(" Roles: ");
	    appendSA( sb, roles, " " );
	}
	sb.append(" Transport " + transport );
	log(sb.toString());
    }

    public void addInterceptor( ContextManager cm, Context ctx,
				BaseInterceptor i )
	throws TomcatException
    {
	if( ! enabled ) return;
	if( ctx==null)
	    log( "addInterceptor " + i );
	else {
	    log( "addInterceptor " + ctx + " " + i);
	}
    }
    
    /** Called when the ContextManger is started
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	log( "engineInit ");
    }

    /** Called before the ContextManager is stoped.
     *  You need to stop any threads and remove any resources.
     */
    public void engineShutdown(ContextManager cm) throws TomcatException {
	log( "engineShutdown ");
    }


    /** Called when a context is added to a CM
     */
    public void addContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	log( "addContext " + ctx );
    }

    public void addContainer( Container ct )
	throws TomcatException
    {
	if( debug > 0 )
	    log( "addContainer " + ct.getContext() + " " + ct );
    }

    public void engineState( ContextManager cm , int state )
	throws TomcatException
    {
	log( "engineState " + state );
    }

    public void engineStart( ContextManager cm )
	throws TomcatException
    {
	log( "engineStart " );
    }

    /** Called when a context is removed from a CM
     */
    public void removeContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	log( "removeContext" + ctx);
    }

    /** Servlet Init  notification
     */
    public void preServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	log( "preServletInit " + ctx + " " + sw);
    }

    
    public void postServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	log( "postServletInit " + ctx + " " + sw);
    }

    /** Servlet Destroy  notification
     */
    public void preServletDestroy( Context ctx, Handler sw )
	throws TomcatException
    {
	log( "preServletDestroy " + ctx + " " + sw);
    }

    
    public void postServletDestroy( Context ctx, Handler sw )
	throws TomcatException
    {
	log( "postServletDestroy " + ctx +  " " + sw);
    }

}
