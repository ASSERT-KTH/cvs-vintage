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

package org.apache.jasper;

import java.io.File;
import java.util.Properties;


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


