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


package org.apache.tomcat.request;

import org.apache.tomcat.core.*;
import org.apache.tomcat.core.Constants;
import org.apache.tomcat.util.*;
import javax.servlet.http.*;
import java.util.*;

/** Parse request URI and find ContextPath, ServletPath, PathInfo and QueryString
 *  This interceptor is an adapter between tomcat and a parsing/matching engine
 *  ( the mapper ). Use normal OO programming to reuse and extend - you can
 *  either start a completely new mapping interceptor or use parts of this
 *  one, and you can use the simple mapper or a better one.
 *
 *  This class contains mostly support functions and implements a "bridge"
 *  pattern. A different bridge can be used - but try to keep the same
 *  semantics ( i.e. allow run-time changes )
 *
 *  This particular implementation does the following:
 *  - avoid mapping if someone already did that ( web server )
 *  - extract the information that is relevant to matching from the Request object.
 *    The current implementation deals with the Host header and the request URI.
 *    If you want more complex rules - you'll need to either extend this interceptor
 *    and use more of info from request ( IP, etc) OR create a more specialized mapper.
 *  - Represent the information in the format understood by mapper and call the
 *    right mapping methods in order to respect the servlet specs.
 *
 *  The mapper we use is a generic matching alghoritm - we feed it with CharChunks (
 *   in order to avoid String garbage ). This is a generic problem - probably
 *   xalan have similar needs, so optimizations will be generally usefull.
 *
 *  This also prepare us to deal with Adapters that are more recylcing-friendly, and
 *  work with char[] instead of Strings.
 *
 *  A very interesting experiment would be to use a RE package instead of our simple
 *  mapper - of course req-exp mapping is not supported by servlets, but using a real
 *  RE engine even for the simple mapping rules defined in the API may provide benefits.
 *  Again - it's the interceptor responsability to feed the right information and regexps
 *  to  RE engine, in order to implement the spec as is, without extensions.
 *  
 *  Another interesting experiment is re-using the matching code in XPath/XSL - again
 *  it's a much wider set of rules, but that also mean they are forced to be very
 *  aggressive in optimizations and we can reuse more code.
 */
public class SimpleMapper1 extends  BaseInterceptor  {
    int debug=0;
    ContextManager cm;
    Mappings map;
    int ctExtMapNote=-1;
    boolean mapCacheEnabled=false;
    
    // Cache the most recent mappings
    // Disabled by default ( since we haven't implemented
    // capacity and remove ). 
    SimpleHashtable mapCache=new SimpleHashtable();
    // By using TreeMap instead of SimpleMap you go from 143 to 161 RPS
    // ( at least on my machine )
    // Interesting - even if SimpleHashtable is faster than Hashtable
    // most of the time, the average is very close for both - it seems
    // that while the synchronization in Hashtable is locking, GC have
    // a chance to work, while in SimpleHashtable case GC creates big
    // peeks. That will go away with more reuse, so we should use SH.

    // An alternative to explore after everything works is to use specialized
    // mappers ( extending this one for example ) using 1.2 collections
    // TreeMap mapCache;
    int capacity;
    int currentL;

    public SimpleMapper1() {
	map=new Mappings();
	mapCache=new SimpleHashtable();
	//	mapCache=new TreeMap();
    }

    /* -------------------- Support functions -------------------- */
    public void setDebug( int level ) {
	if(level!=0) log("SM: SimpleMapper - set debug " + level);
	debug=level;
    }

    void log( String msg ) {
	if( cm==null) 
	    System.out.println("SimpleMapper: " + msg );
	else
	    cm.log( msg );
    }

    public void setMapCache( boolean v ) {
	mapCacheEnabled = v;
    }

    /* -------------------- Initialization -------------------- */
    
    /** In normal operation - it will do nothing but set cm.
	If you add the interceptor at run-time ( is it usefull ??)
	this will also set it up with the current config from other components.
	( the feature was never tested ).
    */
    public void setContextManager( ContextManager cm ) {
	this.cm=cm;
	// set-up a per/container note - will be used to keep private
	// data for this object.
	try {
	    ctExtMapNote = cm.getNoteId( ContextManager.CONTAINER_NOTE, "Extension maps");
	} catch( TomcatException ex ) {
	    ex.printStackTrace();
	    throw new RuntimeException( "Invalid state ");
	}
	// now "simulate" all the callbacks that we missed by
	// beeing late
	
	// Add all context that are set in CM
	Enumeration enum=cm.getContextNames();
	while( enum.hasMoreElements() ) {
	    String name=(String) enum.nextElement();
	    try {
		Context ctx=cm.getContext( name );
		addContext( cm, ctx );
	    } catch (TomcatException ex ) {
		ex.printStackTrace();
	    }
	}
    }

    /** Called when a context is added - it may have all the mappings already
	in, so we need to add them too. Most of the time ( i.e. in normal
	operation ) this is a no-op, but we want to support run-time
	changes in interceptors and contexts.
     */
    public void addContext( ContextManager cm, Context ctx ) throws TomcatException
    {
	if(debug>0) log( "Adding to maps " + ctx);
	// find all the mappings that are declared in context,
	// and register them.
	
	// this is called when the interceptor is added and
	// we have pre-set mappings - not very common 
	// or tested. Normal operation is to set up tomcat
	// and the interceptors and then add the contexts.

	map.prefixMappedServlets.put( ctx.getPath(), ctx.getContainer());
	
	Enumeration ctE=ctx.getContainers();
	while( ctE.hasMoreElements() ) {
	    // the internal method to add this (existing)
	    // mapping.
	    addContainer( (Container)ctE.nextElement() );
	}
    }

    /** Called when a context is removed from a CM - we must ask the mapper to
	remove all the maps related with this context
     */
    public void removeContext( ContextManager cm, Context ctx ) throws TomcatException
    {
	if(debug>0) log( "Removed from maps ");

	// Remove all mappings associated with this context ( including the default
	// servlet associated with the context container
	
	// XXX specific to internal representation !!!
	String ctxP=ctx.getPath();
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
	String path=ct.getPath();
	String ctxP=ctx.getPath();

	// 	if( ct.getHandler() != null )
	// 	    mtable=contextPaths;

	if(debug>0) log("SM: Add mapping/container " + path + " " + ctx.getDebug() + " " + ctxP + " " +
			    ct.getHandler() + " " + ct.getRoles());

	switch( ct.getMapType() ) {
	case Container.PREFIX_MAP:
	    // cut /* !
	    map.prefixMappedServlets.put( ctxP + path.substring( 0, path.length()-2 ), ct);
	    if( debug>0 ) log("SM: Adding Prefix Map " + ctxP + path + " -> " + ct + " " );
	    break;
	case Container.EXTENSION_MAP:
	    // Add it per/defaultContainer - as spec require ( it may also be
	    // possible to support type maps per/Container, i.e. /foo/*.jsp -
	    // but that would require changes in the spec.
	    Context mapCtx=ct.getContext();
	    Container defC=mapCtx.getContainer();
	    
	    SimpleHashtable eM=(SimpleHashtable) defC.getNote( ctExtMapNote );
	    if( eM==null ) {
		eM=new SimpleHashtable();
		defC.setNote( ctExtMapNote, eM );
	    }
	    // add it to the Container local maps
	    eM.put( path.substring( 1 ), ct );
	    if(debug>0) log( "Add Extension Map " + ctxP + "/" + path + " " + ct + " " );
	    break;
	case Container.PATH_MAP:
	    map.prefixMappedServlets.put( ctxP + path, ct);
	    if( debug>0 ) log("SM: Adding Exact Map " + ctxP + path + " -> " + ct + " " );
	    break;
	}
    }

    public void removeContainer( Container ct )
	throws TomcatException
    {
	Context ctx=ct.getContext();
	String mapping=ct.getPath();
	String ctxP=ctx.getPath();
        mapping = mapping.trim();
	if(debug>0) log( "Remove mapping " + mapping );
    }


    /* -------------------- Request mapping -------------------- */


    /** First step of request porcessing is finding the Context.
     *  Advanced mappers will do only one parsing.
     */
    public int contextMap( Request req ) {
	String path = req.getRequestURI();
	if( path==null) throw new RuntimeException("ASSERT: null path in request URI");
	if( path.indexOf("?") >=0 ) throw new RuntimeException("ASSERT: ? in requestURI");
	
	// strip session URL rewrite part which interferes processing
	// XXX works only if ;jsessionid= is path param for the last component
	// of the path! 
	String sig=";jsessionid=";
	int foundAt=-1;
	if ((foundAt=path.indexOf(sig))!=-1){
	    path=path.substring(0, foundAt);  
	}

	try {

	Context ctx = null;
	Mappings mapC=null;
	Container container = null;
	boolean cached=false;
	
	if( mapCacheEnabled ) {
	    container=(Container)getCachedResult( path );
	    if( container != null ) {
		cached=true;
		if(debug>0) log( "CM: cache hit " + path);
	    }
	}
	
	if( ! cached )
	    container=(Container)map.getLongestPrefixMatch(  path );
	
	if( container == null ) throw new RuntimeException( "Assertion failed - container==null");
	if( container.getHandler() == null ) throw new RuntimeException( "Assertion failed - container.handler==null");
	
	if(debug>0) cm.log("SM: Prefix match " + path + " -> " + container.getPath() + " " +
			   container.getHandler()  + " " + container.getRoles());

	// Once - adjust for prefix and context path
	// If cached - we don't need to do it again ( since it is the final Container,
	// either prefix or extension )
	fixRequestPaths( path, req, container );
	

	// if it's default container - try extension match
	if ( ! cached && container.getMapType() == Container.DEFAULT_MAP ) {
	    Container extC = matchExtension( req );
	
	    if( extC != null ) {
		// change the handler
		if( extC.getHandler() != null ) {
		    fixRequestPaths( path, req, extC );
		    container=extC;
		}
		if( debug > 0 ) log("SM: Found extension mapping " + extC.getHandler());
		// change security roles
	    }
	}
	
	if( mapCacheEnabled && ! cached ) {
	    addCachedResult( path, container );
	}
	    
	if(debug>0) log("SM: After mapping " + req + " " + req.getWrapper());
	
	} catch(Exception ex ) {
	    ex.printStackTrace();
	    return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
	return OK;
    }
    
    /** 
     */
    public int requestMap(Request req) {
	// No op. All mapping is done in the first step - it's better because the
	// alghoritm is more efficient. The only case where those 2 are not called togheter
	// is in getContext( "path" ). 
	// 
	// We can split it again later if that creates problems - but right now it's important
	// to have a clear alghoritm.
	// Note that requestMap is _allways_ called after contextMap ( it was asserted in
	//  all implementations).
	
	return OK;
    }

    void mergeSecurityInfo( ) {
    	// Merge the security info into the container
	//
	// XXX merging is a very usefull optimization, but we should do it
	// at init time, it's very expensive because we need to be sure it has the same
	// pattern !!
	// 	if(debug>0) log("SM: Merging security constraint " + scontainer + " into " + container );
	// 	container.setRoles( scontainer.getRoles());
	// 	container.setTransport( scontainer.getTransport());

	// until merging is implemented, we'll just create a new container with the combined
	// properties. This code needs optimizations ( i.e. alghoritm + data, not OptimizeIt!)
// 	Container ct=container.getClone();
//  	ct.setRoles( scontainer.getRoles());
// 	ct.setTransport( scontainer.getTransport());
// 	req.setContainer( ct );
// 	if(debug>0) log("SM: Set security constraings " + req + " " + container );
    }

    
    /** Will match an extension - note that Servlet API use special rules
     *  for mapping extension, different from what is used in existing web servers.
     *  That makes this code very easy ( only need to deal with the last component
     *  of the name ), but it's hard to integrate and you have no way to use pathInfo.
     */
    public Container matchExtension( Request req ) {
	Context ctx=req.getContext();
	String ctxP=ctx.getPath();
	String path = req.getPathInfo(); // we haven't matched any prefix,
	// we check path Info
	String extension=StringUtil.getExtension( path );

	if(debug>0) cm.log("SM: Extension match " + ctxP +  " " + path + " " + extension );
	if( extension == null ) return null;

	// Find extension maps for the context
	SimpleHashtable extM=(SimpleHashtable)ctx.getContainer().getNote( ctExtMapNote );
	if( extM==null ) return null;
	
	// Find the container associated with that extension
	Container container= (Container)extM.get(extension);

	if (container == null)
	    return null;

	// This container doesn't change the mappings - it only 
	// has "other" properties ( in the current code security
	// constraints 
	if( container.getHandler() == null) return container;

	return container; 
    }

    void fixRequestPaths( String path, Request req, Container container ) {
	// Set servlet path and path info
	// Found a match !
	// Adjust paths based on the match 
	String s=container.getPath();
	String ctxP=container.getContext().getPath();
	int sLen=s.length();
	int pathLen=path.length();
	int ctxPLen=ctxP.length();
	String pathI=null;
	
	switch( container.getMapType()) {
	case  Container.PREFIX_MAP: 
	    s=s.substring( 0, sLen -2 );
	    pathI= path.substring( ctxPLen + sLen - 2, pathLen);
	    break;
	case Container.DEFAULT_MAP:
	    s="/";
	    pathI= path.substring( ctxPLen ) ;
	    break;
	case Container.PATH_MAP:
	    pathI= path.substring( ctxPLen + sLen, pathLen);
	    break; // keep the path
	case Container.EXTENSION_MAP:
	    /*  adjust paths */
	    s= path.substring( ctxPLen );
	    pathI=null;

	}
	req.setServletPath( s );
	
	if( ! "".equals(pathI) ) 
	    req.setPathInfo(pathI);
	Context ctx=container.getContext();
	req.setContext(ctx);
	req.setWrapper( container.getHandler() );
	req.setContainer( container );
    }
    
    /** Cache for request results - exploit the fact that few
     *  request are more "popular" than other.
     *  Disable it if you want to benchmark the mapper !!!
     */
    Object getCachedResult(String path) {
	// XXX make sure we don't keep too many requests in memory
	return mapCache.get( path );
    }

    void addCachedResult(String path, Object o) {
	// XXX make sure we don't keep too many requests in memory
	mapCache.put( path, o );
    }
    
}

/** Mapping alghoritm.
    XXX finish factoring out the creation of the map ( right now direct field access is
    used, since the code was just cut out from SimpleMapper).
    XXX make sure the code is useable as a general path mapper - or at least a bridge
    can be created between SimpleMapper and a patern matcher like the one in XPath
 */
class Mappings {
    SimpleHashtable prefixMappedServlets;
    
    Mappings() {
	prefixMappedServlets=new SimpleHashtable();
    }
    
    // -------------------- Implementation --------------------

    /** Match a prefix rule - /foo/bar/index.html/abc
     */
    public Object getLongestPrefixMatch( String path ) {
	Container container = null;
        String s = path;
	boolean exact=true;
	
	// ??/baz/== /baz ==/baz/* 
	//if( s.endsWith( "/" ))
	//  s=removeLast(s);
	
	while (s.length() > 0) {
	    //if(debug>8) context.log( "Prefix: " + s  );
	    container = (Container)prefixMappedServlets.get(s);
	    
	    if (container == null) {
		s=StringUtil.removeLast( s );
		exact=false;
	    }  else {
		if( container.getMapType() == Container.PATH_MAP &&
		    ! exact ) {
		    // we matched a path_map, but we have path_info,
		    // so this is not a good map
		} else {
		    break;
		}
		    
	    }
	}
	return container;
    }

}

/* -------------------- Caching results -------------------- */
class StringUtil {

    public static String removeLast( String s) {
	int i = s.lastIndexOf("/");
	
	if (i > 0) {
	    s = s.substring(0, i);
	} else if (i == 0 && ! s.equals("/")) {
	    s = "/";
	} else {
	    s = "";
	}
	return s;
    }

    public static String getFirst( String path ) {
	if (path.startsWith("/")) 
	    path = path.substring(1);
	
	int i = path.indexOf("/");
	if (i > -1) {
	    path = path.substring(0, i);
	}

	return  "/" + path;
    }
    
    public static String getExtension( String path ) {
        int i = path.lastIndexOf(".");
	int j = path.lastIndexOf("/");

	if ((i > 0) && (i > j))
	    return path.substring(i);
	else
	    return null;
    }

}
