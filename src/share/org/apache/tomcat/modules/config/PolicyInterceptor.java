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

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.PropertyPermission;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.io.FileUtil;
import org.apache.tomcat.util.log.Log;

/**
 * Set policy-based access to tomcat.
 * Must be hooked before class loader setter.
 * The context will have a single protection domain, pointing to the doc root.
 *  That will include all classes loaded that belong to the context
 * ( jsps, WEB-INF/classes, WEB-INF/lib/
 *
 * @author  Glenn Nielsen 
 * @author costin@dnt.ro
 */
public class PolicyInterceptor extends PolicyLoader { //  BaseInterceptor {
    // PolicyLoader is used to load PolicyInterceptor
    String securityManagerClass="java.lang.SecurityManager";
    String policyFile=null;
    
    public PolicyInterceptor() {
    }

    public void setSecurityManagerClass(String cls) {
	securityManagerClass=cls;
    }

    public void setPolicyFile( String pf) {
	policyFile=pf;
    }

    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	// Just override parent
    }

    /** Set the security manager, so that policy will be used
     */
    public void engineInit(ContextManager cm) throws TomcatException {
	initSecurityManager( cm );
    }
    
    public void initSecurityManager(ContextManager cm) throws TomcatException {
	if( System.getSecurityManager() != null ) return;
	try {
	    if( null == System.getProperty("java.security.policy")) {
		log( "Setting java.security.policy. This may fail on some VMs, please"
		     + " set it as a system property before starting tomcat");
		File f=null;
		if( policyFile==null ) {
		    policyFile="conf/tomcat.policy";
		} 
		    
		if( FileUtil.isAbsolute(policyFile)) 
		    f=new File(policyFile);
		else
		    f=new File(cm.getHome() + File.separator +
			       policyFile);
		try {
		    policyFile=f.getCanonicalPath();
		} catch(IOException ex ) {}

		if( debug > 0 )
		    log("Setting policy file to " + policyFile +
			" tomcat.home= " + System.getProperty( "tomcat.home") );

		System.setProperty("java.security.policy",  policyFile);
		
	    }
	    
	    Class c=Class.forName(securityManagerClass);
	    Object o=c.newInstance();
	    Policy.getPolicy().refresh();
	    
	    System.setSecurityManager((SecurityManager)o);
	    log("SANDBOX mode enabled");
	    if( ! "java.lang.SecurityManager".equals(securityManagerClass) )
		log( "Security Manager=" + securityManagerClass);
	} catch( ClassNotFoundException ex ) {
	    log("SecurityManager Class not found: " +
			       securityManagerClass, Log.ERROR);
	} catch( Exception ex ) {
	    ex.printStackTrace();
            log("SecurityManager Class could not be loaded: " +
			       securityManagerClass, Log.ERROR);
	}
    }

    
    /** Add a default set of permissions to the context
     */
    protected void addDefaultPermissions( Context context,String base,
					  Permissions p )
    {
	if( context.isTrusted() ) {
	    if( debug > 0 ) log( "All permissions for " + context );
	    AllPermission aP=new AllPermission();
	    p.add( aP );
	    return;
	}

	// Add default read "-" FilePermission for docBase, classes, lib
	FilePermission fp = new FilePermission(base + File.separator + "-",
					       "read");
	p.add(fp);

	// Add default write "-" FilePermission for docBase 
	fp = new FilePermission(base + File.separator + "-", "write");
	p.add(fp);

        // Add read permission for the directory itself, needed to use
        // exists() on the directory
        fp = new FilePermission(base,"read");
        p.add(fp);

	fp = new FilePermission(context.getWorkDir() + File.separator + "-",
				"read");
	p.add(fp);
	fp = new FilePermission(context.getWorkDir() + File.separator + "-",
				"write");
	p.add(fp);

        // Add read permission for the work directory itself, needed to use
        // exists() on the directory
        fp = new FilePermission(context.getWorkDir().toString(),"read");
        p.add(fp);

	// Read on the common and apps dir
	fp = new FilePermission(cm.getInstallDir() + File.separator +
				"lib" + File.separator + "common" +
				File.separator + "-",
				"read");
	p.add(fp);
	fp = new FilePermission(cm.getInstallDir() + File.separator +
				"lib" + File.separator + "apps" +
				File.separator + "-",
				"read");
	p.add(fp);
	
	RuntimePermission rp = new RuntimePermission("getClassLoader");
	p.add( rp );
	
	// JspFactory.getPageContext() runs in JSP Context and needs the below
	// permission during the init of a servlet generated from a JSP.
	PropertyPermission pp = new PropertyPermission("line.separator","read");
	p.add(pp);
	pp = new PropertyPermission("file.separator", "read");
	p.add(pp);
	pp = new PropertyPermission("path.separator", "read");
	p.add(pp);

	if( debug > 0 || context.getDebug() > 0 )
	    context.log( "permissions " + p );
	    
    }
    
    public void contextInit( Context context)
	throws TomcatException
    {
	ContextManager cm = context.getContextManager();
	String base = context.getAbsolutePath();
	    
	try {	
	    File dir = new File(base);
	    URL url = new URL("file:" + dir.getAbsolutePath());
	    CodeSource cs = new CodeSource(url,null);
	    
	    /* We'll construct permissions for Jasper. 
	       Tomcat uses normal policy and URLClassLoader.

	       We may add fancy config later, if needed
	     */
	    Permissions p = new Permissions();
	    
	    addDefaultPermissions( context, dir.getAbsolutePath(), p);
	
	    /** Add whatever permissions are specified in the policy file
	     */
	    Policy.getPolicy().refresh();
	    PermissionCollection pFileP=Policy.getPolicy().getPermissions(cs);
	    if( pFileP!= null ) {
		Enumeration enum=pFileP.elements();
		while(enum.hasMoreElements()) {
		    p.add((Permission)enum.nextElement());
		}
	    }

	    // This is used only for Jasper ! Should be replaced by
	    // a standard URLClassLoader.
	    ProtectionDomain pd = new ProtectionDomain(cs,p);
	    // 	    context.setProtectionDomain(pd);

	    context.setAttribute( Context.ATTRIB_PROTECTION_DOMAIN,
				  pd);

	    // new permissions - added context manager and file to whatever was
	    // specified by default
	    //	    context.setPermissions( p );

	} catch(Exception ex) {
	    log("Security init for Context " + base + " failed", ex);
	}

    }
}
