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
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Handler;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.compat.Jdk11Compat;

/**
 *  JDK1.2 specific options. Fix the class loader, etc.
 */
public final class Jdk12Interceptor extends  BaseInterceptor {
    private ContextManager cm;
    private int debug=0;

    public Jdk12Interceptor() {
    }

    public void preServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	fixJDKContextClassLoader(ctx);
    }

    public int preInitCheck( Request req, Handler sw )
	throws TomcatException
    {
        fixJDKContextClassLoader(req.getContext());
	return 0;
    }

    /** Servlet Destroy  notification
     */
    public void preServletDestroy( Context ctx, Handler sw )
	throws TomcatException
    {
	fixJDKContextClassLoader(ctx);
    }

    public void postServletDestroy( Context ctx, Handler sw )
	throws TomcatException
    {
	jdk11Compat.setContextClassLoader(this.getClass().getClassLoader());
    }
    
    public void postServletInit( Context ctx, Handler sw )
	throws TomcatException
    {
	jdk11Compat.setContextClassLoader(this.getClass().getClassLoader());
    }
    
    public int postInitCheck( Request req, Handler sw )
	throws TomcatException
    {
	jdk11Compat.setContextClassLoader(this.getClass().getClassLoader());
	return 0;
    }
    /** Called before service method is invoked. 
     */
    public int preService(Request request, Response response) {
	if( request.getContext() == null ) return 0;
	// fix for 1112
	Request child=request.getChild();
	if( child!=null ) {
	    request=child;
	}
	fixJDKContextClassLoader(request.getContext());
	return 0;
    }

    public int postService(Request request, Response response) {
	Request child=request.getChild();
	if( child==null ) return 0;

	// after include, reset the class loader
	// fix for 1112
	Request chParent=child.getParent();
	if( chParent != null )
	    fixJDKContextClassLoader(chParent.getContext());
	else
	    fixJDKContextClassLoader(request.getContext());
	return 0;
    }

    public int postRequest(Request request, Response response) {
	jdk11Compat.setContextClassLoader(this.getClass().getClassLoader());
	return 0;
    }

    static Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    
    
    // Before we do init() or service(), we need to do some tricks
    // with the class loader - see bug #116.
    // some JDK1.2 code will not work without this fix
    // we save the originalCL because we might be in include
    // and we need to revert to it when we finish
    // that will set a new (JDK)context class loader, and return the old one
    // if we are in JDK1.2
    // XXX move it to interceptor !!!
    final private void fixJDKContextClassLoader( Context ctx ) {
	final ClassLoader cl=ctx.getClassLoader();
	if( cl==null ) {
	    log("ERROR: Jdk12Interceptor: classloader==null");
	    return;
	}
	if( cl == jdk11Compat.getContextClassLoader() )
	    return; // nothing to do - or in include if same context
	
	jdk11Compat.setContextClassLoader(cl);
    }
    
}
