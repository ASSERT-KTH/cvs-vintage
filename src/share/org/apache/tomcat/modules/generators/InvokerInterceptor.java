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

package org.apache.tomcat.modules.generators;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Container;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.TomcatException;

/**
 *
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Costin Manolache
 */
public class InvokerInterceptor extends BaseInterceptor {

    String prefix="/servlet/";
    int prefixLen=prefix.length();

    public void setPrefix(String prefix) {
        if( prefix != null ) {
            this.prefix = prefix;
            prefixLen=this.prefix.length();
        }
    }

    public int requestMap(Request req) {
	// If we have an explicit mapper - return
	Container ct=req.getContainer();

	// 	log( "Ct: " + ct.getHandler() + " " +
	// 	     ct.getPath() + " " + ct.getMapType());
	
	if(  req.getHandler()!=null &&
	     ct!=null &&
	     ct.getMapType() != Container.DEFAULT_MAP )
	    return 0;
	
	// default servlet / container
	
	// if doesn't starts with /servlet - return
	String pathInfo = req.pathInfo().toString();
	String servletPath=req.servletPath().toString();
	
	// Now we need to fix path info and servlet path
	if( servletPath == null ||
	    ! servletPath.startsWith( prefix ))
	    return 0;

	Context ctx=req.getContext();
	// Set the wrapper, and add a new mapping - next time
	// we'll not have to do that ( the simple mapper is
	// supposed to be faster )
	
	String servletName = null;
	String newPathInfo = null;
	
	if( debug>0 )
	    log( "Original ServletPath=" +servletPath +
		 " PathInfo=" + pathInfo);

	int secondSlash=servletPath.indexOf("/", prefixLen );
	if ( secondSlash > -1) {
	    servletName = servletPath.substring(prefixLen, secondSlash );
	    newPathInfo = servletPath.substring( secondSlash );
	} else {
	    servletName = servletPath.substring( prefixLen );
	}
	
	String newServletPath = prefix + servletName;

	if( debug > 0)
	    log( "After pathfix SN=" + servletName +
		 " SP=" + newServletPath +
		 " PI=" + newPathInfo);
	
 	req.servletPath().setString(newServletPath);
	req.pathInfo().setString(newPathInfo);
	
	Handler wrapper = ctx.getServletByName(servletName);
	if (wrapper != null) {
	    req.setHandler( wrapper );
	    return 0;
	}
	    
	// Dynamic add for the wrapper
	
	// even if the server doesn't supports dynamic mappings,
	// we'll avoid the interceptor for include() and
	// it's a much cleaner way to construct the servlet and
	// make sure all interceptors are up to date.
	try {
	    ctx.addServletMapping( newServletPath + "/*" ,
				   servletName );
	    // The facade should create the servlet name
	    
	    Handler sw=ctx.getServletByName( servletName );
	    // 	    sw.setContext(ctx);
	    // 	    sw.setServletName(servletName);
	    //	    ctx.addServlet( sw );
	    //	    sw.setServletClass( servletName );
	    //sw.setOrigin( Handler.ORIGIN_INVOKER );
	    wrapper=sw;

	    if( debug > 0)
		log( "Added mapping " + wrapper +
		     " path=" + newServletPath + "/*" );
	} catch( TomcatException ex ) {
	    loghelper.log("dynamically adding wrapper for " + servletName, ex);
	    return 404;
	}

	req.setHandler( wrapper );
	return 0;
    }
    

}
