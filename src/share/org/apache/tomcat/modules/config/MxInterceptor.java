/* $Id: MxInterceptor.java,v 1.2 2002/07/30 17:27:25 costin Exp $
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
package org.apache.tomcat.modules.config;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.io.FileUtil;
import java.io.*;
import java.util.*;

import org.apache.tomcat.util.mx.*;

/**
 *
 * @author Costin Manolache
 */
public class MxInterceptor  extends BaseInterceptor { 

    // -------------------- Tomcat callbacks --------------------

    private void createMBean( String domain, Object proxy, String name ) {
        try {
            DynamicMBeanProxy mbean=new DynamicMBeanProxy();
            mbean.setReal( proxy );
            if( name!=null ) {
                mbean.setName( name );
            }

            mbean.registerMBean( domain );
        } catch( Throwable t ) {
            log( "Error creating mbean ", t );
        }
    }

    public void addContext( ContextManager cm,
                            Context ctx )
	throws TomcatException
    {
        String host=ctx.getHost();
        if( host==null ) host="DEFAULT";
        
        createMBean( "webapps", ctx, host + ctx.getPath() );
    }
    
    public void addInterceptor( ContextManager cm,
				Context ctx,
				BaseInterceptor bi )
	throws TomcatException
    {
        if( bi==this ) {
            // Adding myself and on-time things
            createMBean( "tomcat3", cm, "Tomcat3Container" );
        }
        createMBean( "tomcat3", bi, null);
    }

    public void initRequest( ContextManager cm, Request req, Response resp )
    {
        createMBean( "tomcat3.requests", req, null);
    }
}
