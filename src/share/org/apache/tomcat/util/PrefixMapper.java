/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/util/PrefixMapper.java,v 1.12 2004/02/25 07:47:50 billbarker Exp $
 * $Revision: 1.12 $
 * $Date: 2004/02/25 07:47:50 $
 *
 *   
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

package org.apache.tomcat.util;

import java.util.Enumeration;

import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.collections.SimpleHashtable;
import org.apache.tomcat.util.io.FileUtil;

/** Prefix and exact mapping alghoritm.
 *XXX finish factoring out the creation of the map ( right now direct field access is
 *  used, since the code was just cut out from SimpleMapper).
 *  XXX make sure the code is useable as a general path mapper - or at least a bridge
 *  can be created between SimpleMapper and a patern matcher like the one in XPath
 *
 * @author costin@costin.dnt.ro
 */
public class PrefixMapper {
    // host -> PrefixMapper for virtual hosts
    // hosts are stored in lower case ( the "common" case )
    SimpleHashtable vhostMaps=new SimpleHashtable();


    SimpleHashtable prefixMappedServlets;
    SimpleHashtable exactMappedServlets;

        // Cache the most recent mappings
    // Disabled by default ( since we haven't implemented
    // capacity and remove ). 
    SimpleHashtable mapCache;
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
    boolean mapCacheEnabled=false;

    
    public PrefixMapper() {
	prefixMappedServlets=new SimpleHashtable();
	exactMappedServlets=new SimpleHashtable();
	mapCache=new SimpleHashtable();
    }

    public void setMapCache( boolean v ) {
	mapCacheEnabled=v;
    }

    /** Remove all mappings matching path
     */
    public void removeAllMappings( String host, String path ) {
	PrefixMapper vmap=this;
	if( host!=null ) {
	    host=host.toLowerCase();
	    vmap=(PrefixMapper)vhostMaps.get(host);
	}
	
	// remove all paths starting with path
	Enumeration en=vmap.prefixMappedServlets.keys();
	while( en.hasMoreElements() ) {
	    String s=(String)en.nextElement();
	    if( s.startsWith( path ))
		vmap.prefixMappedServlets.remove( s );
	}
	
	en=vmap.exactMappedServlets.keys();
	while( en.hasMoreElements() ) {
	    String s=(String)en.nextElement();
	    if( s.startsWith( path ))
		vmap.exactMappedServlets.remove( s );
	}
	// reset the cache
	mapCache=new SimpleHashtable();
	
    }

    /**
     */
    void addMapping( String path, Object target ) {
	prefixMappedServlets.put( path, target);
    }

    /**
     */
    void addExactMapping( String path, Object target ) {
	exactMappedServlets.put( path, target);
    }
    
    /**
     */
    public void addMapping( String host, String path, Object target ) {
	if( host == null )
	    prefixMappedServlets.put( path, target);
	else {
	    host=host.toLowerCase();
	    PrefixMapper vmap=(PrefixMapper)vhostMaps.get( host );
	    if( vmap == null ) {
		vmap=new PrefixMapper();
		vhostMaps.put( host, vmap );
		vmap.setMapCache( mapCacheEnabled );
	    }
	    vmap.addMapping( path, target );
	}
    }

    /**
     */
    public void addExactMapping( String host, String path, Object target ) {
	if( host==null )
	    exactMappedServlets.put( path, target);
	else {
	    host=host.toLowerCase();
	    PrefixMapper vmap=(PrefixMapper)vhostMaps.get( host );
	    if( vmap == null ) {
		vmap=new PrefixMapper();
		vhostMaps.put( host, vmap );
	    }
	    vmap.addExactMapping( path, target );
	}
    }
    
    
    // -------------------- Implementation --------------------

    /** Match a prefix rule - /foo/bar/index.html/abc
     */
    public Object getLongestPrefixMatch( MessageBytes hostMB,
					 MessageBytes pathMB )
    {
	// XXX fixme
	String host=hostMB.toString();
	String path=pathMB.toString();
	Object container = null;
        String s = path;

	PrefixMapper myMap=null;
	if( host!=null ) {
	    myMap=(PrefixMapper)vhostMaps.get( host );
	    if( myMap==null ) {
		myMap=(PrefixMapper)vhostMaps.get( host.toLowerCase() );
	    }
	}
	
	if( myMap==null ) myMap = this; // default server

	
	container=myMap.exactMappedServlets.get( path );
	if( container != null ) return container; // and we're done!

	/** Cache for request results - exploit the fact that few
	 *  request are more "popular" than other.
	 *  Disable it if you want to benchmark the mapper !!!
	 */
	if( myMap.mapCacheEnabled ) {
	    container=myMap.mapCache.get(path);
	    if( container!=null ) return container;
	}
		
	while (s.length() >= 0) {
	    //if(debug>8) context.log( "Prefix: " + s  );
	    container = myMap.prefixMappedServlets.get(s);
	    
	    if (container == null) {
		// if empty string didn't map, time to give up
		if ( s.length() == 0 )
                    break;
		s=FileUtil.removeLast( s );
	    }  else {
		if( myMap.mapCacheEnabled ) {
		    // XXX implement LRU or another replacement alghoritm
		    myMap.mapCache.put( path, container );
		}
		return container;
	    }
	}
	return container;
    }

}

 
