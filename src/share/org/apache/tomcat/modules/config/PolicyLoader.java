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
