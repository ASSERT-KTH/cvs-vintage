/*
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
 *  See the License for the specific language 
 */

package org.apache.tomcat.facade;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.util.log.Log;
import org.apache.tomcat.util.res.StringManager;

/**
 * Interceptor that loads the "load-on-startup" servlets
 *
 * @author costin@dnt.ro
 */
public class LoadOnStartupInterceptor extends BaseInterceptor {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");
    
    public LoadOnStartupInterceptor() {
    }

    public void contextInit(Context ctx) {
	Hashtable loadableServlets = new Hashtable();
	init(ctx,loadableServlets);
	
	Vector orderedKeys = new Vector();
	Enumeration e=  loadableServlets.keys();
		
	// order keys
	while (e.hasMoreElements()) {
	    Integer key = (Integer)e.nextElement();
	    int slot = -1;
	    for (int i = 0; i < orderedKeys.size(); i++) {
	        if (key.intValue() <
		    ((Integer)(orderedKeys.elementAt(i))).intValue()) {
		    slot = i;
		    break;
		}
	    }
	    if (slot > -1) {
	        orderedKeys.insertElementAt(key, slot);
	    } else {
	        orderedKeys.addElement(key);
	    }
	}

	// loaded ordered servlets

	// Priorities IMO, should start with 0.
	// Only System Servlets should be at 0 and rest of the
	// servlets should be +ve integers.
	// WARNING: Please do not change this without talking to:
	// harishp@eng.sun.com (J2EE impact)

	for (int i = 0; i < orderedKeys.size(); i ++) {
	    Integer key = (Integer)orderedKeys.elementAt(i);
	    Enumeration sOnLevel =  ((Vector)loadableServlets.get( key )).
		elements();
	    while (sOnLevel.hasMoreElements()) {
		String servletName = (String)sOnLevel.nextElement();
		Handler result = ctx.getServletByName(servletName);

		if( ctx.getDebug() > 0 )
		    ctx.log("Loading " + key + " "  + servletName );
		if(result==null)
		    log("Warning: we try to load an undefined servlet " +
			servletName, Log.WARNING);
		else {
		    try {
			// special case for JSP - should be dealed with in
			// ServletHandler !
			if( result instanceof ServletHandler &&
			    ((ServletHandler)result).getServletInfo().
			    getJspFile() != null ) {
			    loadJsp( ctx, result );
			}
			((ServletHandler)result).init();
		    } catch (Throwable ee) {
			// it can be ClassNotFound or other - servlet errors
			// shouldn't stop initialization
			String msg = sm.getString("context.loadServlet.e",
						  servletName);
			log(msg, ee);
		    } 
		}
	    }
	}
    }

    void loadJsp( Context context, Handler result ) throws Exception {
	// A Jsp initialized in web.xml -
        BaseInterceptor ri[];
	String path=((ServletHandler)result).getServletInfo().getJspFile();
	String requestURI = path + "?jsp_precompile=true";

	Request req = cm.createRequest(context, requestURI);
	ri=context.getContainer().
	    getInterceptors(Container.H_preInitCheck);
	for( int i=0; i< ri.length; i++ ) {
	    int status = ri[i].preInitCheck(req, result);
	    if(status != 0) {
		return;
	    }
	}
	ri=context.getContainer().
	    getInterceptors(Container.H_postInitCheck);
	for( int i=0; i< ri.length; i++ ) {
	    int status = ri[i].postInitCheck(req, result);
	    if(status != 0) {
		return;
	    }
	}
    }
    // -------------------- 
    // Old logic from Context - probably something cleaner can replace it.

    void init(Context ctx, Hashtable loadableServlets ) {
	Enumeration enum=ctx.getServletNames();
	while(enum.hasMoreElements()) {
	    String name=(String)enum.nextElement();
	    Handler h=ctx.getServletByName( name );
	    if( ! ( h instanceof ServletHandler ) )
		continue;
	    ServletHandler sw= (ServletHandler)h;
	    if( sw.getServletInfo().getLoadingOnStartUp() ) {
		Integer level=new Integer(sw.getServletInfo().
					  getLoadOnStartUp());
		Vector v;
		if( loadableServlets.get(level) != null ) 
		    v=(Vector)loadableServlets.get(level);
		else
		    v=new Vector();
		
		v.addElement(name);
		loadableServlets.put(level, v);
	    }
	}
    }
    

}
