/*
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
 */ 
package org.apache.jasper;

import java.util.*;
import java.io.*;
import java.net.*;

import org.apache.jasper.Options;


/** Another implementation of Options, backed by a Properties file
 *  and with no external dependencies. 
 */
public class JasperOptionsImpl implements Options {
    static final String ieClassId =
	"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";

    // cache
    private Class jspCompilerPlugin = null;

    // special property.
    private Object protectionDomain;

    Properties args;
    
    public JasperOptionsImpl( Properties args ) {
	this.args=args;
    }

    // -------------------- Options implementation --------------------

    public boolean getKeepGenerated() {
	return s2b( args.getProperty("keepgenerated", "true") );
    }

    public String getJavaEncoding() {
	return args.getProperty("javaEncoding", "UTF8");
    }

    public boolean getLargeFile() {
        return s2b( args.getProperty("largefile", "false"));
    }

    public boolean getMappedFile() {
        return s2b( args.getProperty("mappedfile"));
    }
    
    public boolean getSendErrorToClient() {
        return s2b( args.getProperty( "sendErrToClient" ));
    }
 
    public boolean getClassDebugInfo() {
        return s2b( args.getProperty( "classDebugInfo" ));
    }

    public String getIeClassId() {
        return args.getProperty( "ieClassId" , ieClassId);
    }

    public File getScratchDir() {
	if( debug>0 ) log("Options: getScratchDir " );
        return new File( args.getProperty( "scratchdir" ));
    }

    public final Object getProtectionDomain() {
	if( debug>0 ) log("Options: GetPD" );
	return protectionDomain;
    }

    public String getClassPath() {
	if( debug>0 ) log("Options: GetCP "  );
        return args.getProperty( "classpath" );
    }

    public Class getJspCompilerPlugin() {
	if( debug>0 ) log("Options: getJspCompilerPlugin "   );
	if( jspCompilerPlugin!= null ) return jspCompilerPlugin;
	String type=args.getProperty( "jspCompilerPlugin" );
	if( type != null ) {
	    try {
		jspCompilerPlugin=Class.forName(type);
	    } catch(Exception ex ) {
		ex.printStackTrace();
	    }
	}
	return jspCompilerPlugin;
    }

    public String getJspCompilerPath() {
        return args.getProperty( "jspCompilerPath" );
    }

    // -------------------- Setters --------------------

    public void setProtectionDomain( Object pd ) {
	protectionDomain=pd;
    }
    
    // --------------------
    private boolean s2b( String s ) {
	return new Boolean( s ).booleanValue();
    }
        
    // trace for development purpose --------------------    
    private static int debug=0;
    private void log(String s) {
	System.err.println(s);
    }

}


