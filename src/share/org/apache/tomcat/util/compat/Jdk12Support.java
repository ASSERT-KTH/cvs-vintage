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

package org.apache.tomcat.util.compat;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Policy;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.tomcat.util.depend.DependClassLoader;

/**
 *  
 */
public class Jdk12Support extends Jdk11Compat {

    /** Return a class loader. For JDK1.2+ will return a URLClassLoader.
     *  For JDK1.1 will return the util.SimepleClassLoader
     */
    public ClassLoader newClassLoaderInstance( URL urls[],
					       ClassLoader parent )
    {
	return URLClassLoader.newInstance( urls, parent );
    }

    public Object getAccessControlContext() throws Exception {
	return AccessController.getContext();
    }
    
    public Object doPrivileged( Action action, Object accObj ) throws Exception {
        ProtectionDomain domain[]=null;
        if ( accObj instanceof ProtectionDomain ) {
            domain=new ProtectionDomain[1];
            domain[0]=(ProtectionDomain)accObj;
        } else if (accObj instanceof ProtectionDomain[] ) {
            domain=(ProtectionDomain []) accObj;
        }
	AccessControlContext acc=null;
        if( domain==null ) {
            acc=(AccessControlContext)accObj;
        } else {
            acc=new AccessControlContext( domain );
        }
	if( acc==null )
	    throw new Exception("Invalid access control context ");
	Object proxy=action.getProxy();
	if( proxy==null ) {
	    proxy=new PrivilegedProxy(action);
	    action.setProxy( proxy );
	}

	try {
	    return AccessController.
		doPrivileged((PrivilegedExceptionAction)proxy, acc);
	} catch( PrivilegedActionException pe ) {
	    Exception e = pe.getException();
	    throw e;
	}
    }

    public void refreshPolicy() {
	Policy.getPolicy().refresh();
    }
    
    public void setContextClassLoader( ClassLoader cl ) {
	// we can't doPrivileged here - it'll be a major security
	// problem
	Thread.currentThread().setContextClassLoader(cl);
    }

    public ClassLoader getContextClassLoader() {
	return Thread.currentThread().getContextClassLoader();
    }
    
    public ClassLoader getParentLoader( ClassLoader cl ) {
	if( cl instanceof DependClassLoader ) {
	    return ((DependClassLoader)cl).getParentLoader();
	}
	if( cl instanceof SimpleClassLoader ) {
	    return ((SimpleClassLoader)cl).getParentLoader();
	}
	if( cl instanceof URLClassLoader ) {
	    return ((URLClassLoader)cl).getParent();
	}
	return null;
    }
    
    public URL[] getURLs(ClassLoader cl,int depth){
        int c=0;
        do{
            while(cl instanceof DependClassLoader && cl != null )
                cl=((DependClassLoader)cl).getParentLoader();
            if (cl==null) break;
            if (depth==c) {
		if(cl instanceof URLClassLoader)
		    return ((URLClassLoader)cl).getURLs();
		else if(cl instanceof SimpleClassLoader)
		    return ((SimpleClassLoader)cl).getURLs();
		else
		    return null;
	    }
            c++;
            cl=getParentLoader(cl);
        }while((cl!=null) && ( depth >= c ));
        return null;
    }

    public java.util.ResourceBundle getBundle(String name, Locale loc, ClassLoader cl ) {
	if( cl==null )
	    cl=getContextClassLoader();
	if( cl==null )
	    return ResourceBundle.getBundle(name, loc);
	else
	    return ResourceBundle.getBundle(name, loc, cl);
    }

    public Object getX509Certificates( byte x509[] ) throws Exception {
	ByteArrayInputStream bais = new ByteArrayInputStream(x509);
	
	// Fill the first element.
	X509Certificate jsseCerts[] = null;

	CertificateFactory cf =
	    CertificateFactory.getInstance("X.509");
	X509Certificate cert = (X509Certificate)
	    cf.generateCertificate(bais);
	jsseCerts =  new X509Certificate[1];
	jsseCerts[0] = cert;
	return jsseCerts;
    }

    

    // -------------------- Support --------------------
    static class PrivilegedProxy implements PrivilegedExceptionAction
    {
	Action action;
	PrivilegedProxy( Action act ) {
	    action=act;
	}
	public Object run() throws Exception
	{
	    return action.run();
	}
    }

}
