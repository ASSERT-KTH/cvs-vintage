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

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.compat.Jdk11Compat;

/**
 * Load the PolicyInterceptor if JDK1.2 is detected and if "sandbox"
 * property of ContextManager is set.
 *
 * This simplifies the configuration of tomcat - we don't need to
 * use special configs for jdk1.1/jdk1.2+ ( the code can auto-detect that ).
 * We use a simple ContextManager property ( that can be set from command
 * line, or via sandbox="true" ).
 *
 * This class acts as a proxy for the PolicyInterceptor.
 */
public class PolicyLoader extends BaseInterceptor {
    String securityManagerClass="java.lang.SecurityManager";
    String policyFile=null;
    boolean sandbox=false;
    
    public PolicyLoader() {
    }

    public void setSecurityManagerClass(String cls) {
	securityManagerClass=cls;
    }

    public String getSecurityManagerClass() {
	return securityManagerClass;
    }

    public String getPolicyFile() {
	return policyFile;
    }

    public void setPolicyFile(String pf) {
	policyFile=pf;
    }

    /** Enable/disable the module, independent of command line
	options
    */
    public void setSandbox( boolean b ) {
	this.sandbox=b;
    }
    
    static Jdk11Compat jdk11Compat=Jdk11Compat.getJdkCompat();
    
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( this != module ) return;

	if( ! jdk11Compat.isJava2() )
	    return;

	if( debug > 0 )
	    log("Checking for security manager " + cm.getProperty( "sandbox" ));
	// find if PolicyInterceptor has already been loaded
	if( sandbox ||
	    System.getSecurityManager() != null ||
	    cm.getProperty("sandbox") != null )
	    {
	    log("Loading sandbox ");
	    try {
		Class c=Class.
             forName( "org.apache.tomcat.modules.config.PolicyInterceptor" );
		// trick to configure PolicyInterceptor.
		PolicyLoader policyModule=(PolicyLoader)c.newInstance();
		policyModule.setSecurityManagerClass( securityManagerClass);
		policyModule.setPolicyFile( policyFile );
		policyModule.setDebug( debug );
		cm.addInterceptor( policyModule );

		// we could also remove PolicyLoader, since it's no longer
		// needed
	    } catch( Exception ex ) {
		ex.printStackTrace();
	    }
	}
	// load the PolicyInterceptor
	
    }
}
