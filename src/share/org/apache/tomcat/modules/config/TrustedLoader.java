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
package org.apache.tomcat.modules.config;

import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.modules.server.*;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.hooks.*;
import org.apache.tomcat.util.IntrospectionUtils;
import org.xml.sax.*;

/**
 * Special configuration for trusted applications.
 * Need to be loaded _after_ LoaderInterceptor.
 *
 * @author Costin Manolache
 */
public class TrustedLoader extends BaseInterceptor {

    public TrustedLoader() {
    }

    // -------------------- Properties --------------------
    
    // -------------------- Hooks --------------------

    public void contextInit(  Context ctx )
	throws TomcatException
    {
	if( ! ctx.isTrusted() ) return;

	// PathSetter is the first module in the chain, we shuld have
	// a valid path by now 
	String dir=ctx.getAbsolutePath();

	File f=new File(dir);
	File modules=new File( f, "WEB-INF" + File.separator +
			       "interceptors.xml" );
	if( modules.exists() ) {
	    ctx.log( "Loading modules from webapp " + modules );
	} else {
	    if( debug > 0 )
		ctx.log( "Can't find " + modules );
	    return;
	}

	cm.setNote( "trustedLoader.currentContext", ctx );
	
	XmlMapper xh=new XmlMapper();
	xh.setClassLoader( ctx.getClassLoader());
	xh.setDebug( debug );

	ServerXmlReader.setTagRules( xh );
	// first, load <module> definitions
	ServerXmlReader.loadConfigFile(xh,modules,cm);
	
	ServerXmlReader.setPropertiesRules( cm, xh );
	ServerXmlReader.addTagRules( cm, xh );
	// no backward compat rules. Use Module ( taskdef :-) and the tag

	// then load the actual config 
	ServerXmlReader.loadConfigFile(xh,modules,cm);

	cm.setNote( "trustedLoader.currentContext", null );
    }

}

