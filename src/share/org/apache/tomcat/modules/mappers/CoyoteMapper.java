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

package org.apache.tomcat.modules.mappers;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.collections.SimpleHashtable;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.http.mapper.Mapper;
import org.apache.tomcat.util.http.mapper.MappingData;

/**
 *  This class will set up the data structures used by a simple patern matching
 *  algorithm and use it to extract the path components from the request URI.
 *
 *  This particular implementation does the following:
 *  - extract the information that is relevant to matching from the Request
 *   object. The current implementation deals with the Host header and the
 *   request URI.
 *  - Use an external mapper to find the best match.
 *  - Adjust the request paths
 * 
 *  It will maintain a global mapping structure for all prefix mappings,
 *  including contexts. 
 * 
 *  The execution time is proportional with the number of hosts, number of
 *  context, number of mappings and with the length of the request.
 *
 */
public class CoyoteMapper extends  BaseInterceptor  {

    public static final String DEFAULT_HOST = "DEFAULT";
    Mapper map;
    Hashtable hostData = new Hashtable();

    public CoyoteMapper() {
    }

    /* -------------------- Support functions -------------------- */

    /* -------------------- Initialization -------------------- */
    
    /** Set the context manager. To keep it simple we don't support
     *  dynamic add/remove for this interceptor. 
     */
    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	// set-up a per/container note for maps
	map=new Mapper();
	map.setDefaultHostName(DEFAULT_HOST);
    }

    /** Called when a context is added.
     */
    public void addContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	String host = ctx.getHost();
	if(host == null) {
	    host = DEFAULT_HOST;
	} else if(hostData.get(host) == null) {
	    Enumeration vhostAliases=ctx.getHostAliases();
	    Vector valias = new Vector();
	    while(vhostAliases.hasMoreElements()) {
		valias.addElement(vhostAliases.nextElement());
	    }
	    String [] aliasNames = new String[valias.size()];
	    for(int i=0; i < valias.size(); i++) {
		aliasNames[i] = (String)valias.elementAt(i);
	    }
	    map.addHost(host, aliasNames, "");
	    hostData.put(host, valias);
	}
	    
	map.addContext(host, ctx.getPath(), ctx, ctx.getWelcomeFiles(), null);
	// StaticInterceptor doesn't have a Container, so we need to
	// add one.  
	map.addWrapper(host, ctx.getPath(), "/", ctx.getContainer());
    }

    /** Called when a context is removed from a CM - we must ask the mapper to
	remove all the maps related with this context
     */
    public void removeContext( ContextManager cm, Context ctx )
	throws TomcatException
    {
	if(debug>0) log( "Removed from maps ");
	String host = ctx.getHost();
	if(host == null)
	    host = DEFAULT_HOST;
	map.removeContext(host, ctx.getPath());
    }
    

    /**
     * Associate URL pattern  to a set of propreties.
     * 
     * Note that the order of resolution to handle a request is:
     *
     *    exact mapped servlet (eg /catalog)
     *    prefix mapped servlets (eg /foo/bar/*)
     *    extension mapped servlets (eg *jsp)
     *    default servlet
     *
     */
    public void addContainer( Container ct )
	throws TomcatException
    {
	Context ctx=ct.getContext();
	String vhost=ctx.getHost();
	if(vhost == null)
	    vhost = DEFAULT_HOST;
	String path=ct.getPath();
	String ctxP=ctx.getPath();

	// Special containers ( the default is url-mapping ).
	if( ct.isSpecial() ) return;
	if( ct.getNote( "type" ) != null )  return;
	
	if(ct.getRoles() != null || ct.getTransport() != null ) {
	    // it was only a security map, no handler defined
	    return;
	}

	map.addWrapper(vhost, ctxP, path, ct);

    }

    // XXX not implemented - will deal with that after everything else works.
    // Remove context will still work
    public void removeContainer( Container ct )
	throws TomcatException
    {
	Context ctx=ct.getContext();
	String mapping=ct.getPath();
	String ctxP=ctx.getPath();
	String vhost=ctx.getHost();
	if(vhost == null)
	    vhost = DEFAULT_HOST;
	// Special containers ( the default is url-mapping ).
	if( ct.isSpecial() ) return;
	if( ct.getNote( "type" ) != null )  return;
	
	if(ct.getRoles() != null || ct.getTransport() != null ) {
	    // it was only a security map, no handler defined
	    return;
	}
	map.removeWrapper(vhost, ctxP, mapping);
    }


    /* -------------------- Request mapping -------------------- */


    /** First step of request processing is finding the Context.
     */
    public int contextMap( Request req ) {
	MessageBytes pathMB = req.requestURI();
	MessageBytes hostMB = req.serverName();
	if(debug > 9)
	    log("mapping: "+req);
	if(hostMB.isNull()) {
	    // This is a sub-request for the default host.
	    hostMB.setString(DEFAULT_HOST);
	}
	MappingData mdata = new MappingData();
	mdata.wrapperPath = req.servletPath();
	mdata.pathInfo = req.pathInfo();
	try {
	    map.map(hostMB, pathMB, mdata);
	} catch(Exception ex) {
	    log("Error mapping "+pathMB,ex);
	    return 500;
	}
	if(mdata.context != null) {
	    Context ctx=(Context)mdata.context;
	    Container container = (Container)mdata.wrapper;
	    req.setContext(ctx);
	    req.setContainer( container );
	    if(!mdata.redirectPath.isNull()) {
		Response res = req.getResponse();
		if(debug > 0)
		    log("Redirecting '"+req+"' to '"+
			mdata.redirectPath+"'", new Exception());
		res.setHeader("Location", mdata.redirectPath.toString());
		return 302;
	    } 
	    if(container != null)
		req.setHandler( container.getHandler() );
	    if(debug > 9)
		log("mapped: "+req);
	} else {
	    return 404;
	}
	return 0;
    }
    
    // -------------------- Implementation methods --------------------
    

}

 
