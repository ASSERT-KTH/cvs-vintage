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
 * [Additional notices, if required by prior licensing conditions]
 *
 */ 


package org.apache.tomcat.deployment;

import java.util.Enumeration;

    /** An object exhibiting this interface is the root object of all the deployment
    * information describing a web application.
    * @author Danny Coward
     */

public interface WebApplicationDescriptor extends WebDescriptor {


	/* Return the set of jsp or servlet descriptors. */
    public Enumeration getWebComponentDescriptors();
    public void addWebComponentDescriptor(WebComponentDescriptor servletDescriptor);
	/** Return the session timeout. */
    public int getSessionTimeout();
    public void setSessionTimeout(int sessionTimeout);
    /** Return the extension to type mappings for this web application. */
    public Enumeration getMimeMappings();
    public void addMimeMapping(MimeMapping mimeMapping);
	/** Return a list of the welcome files in order that they be shown. */
    public Enumeration getWelcomeFiles();
    public void addWelcomeFile(String fileUri);
	/** Return the error page redirects for this application. */
    public Enumeration getErrorPageDescriptors();
    public void addErrorPageDescriptor(ErrorPageDescriptor errorPageDescriptor);
	/** Return the environment properties that this web application will need. */
    public Enumeration getContextParameters();
    public void addContextParameter(ContextParameter environmentProperty);
    
    public boolean isDistributable();
    public void setDistributable(boolean isDistributable);
	/** return the list of references to EJBs that this wen application has. */
    public Enumeration getEjbReferences();
    public void addEjbReference(EjbReference ejbReference);
	/** Return the list of references to databases that this web application will use. */
    public Enumeration getResourceReferences();
    public void addResourceReference(ResourceReference resourceReference);
    
    public Enumeration getEnvironmentEntries();
    public void addEnvironmentEntry(EnvironmentEntry environmentEntry);
	/** Return the list of security roles for this web application. */
    public Enumeration getSecurityRoles();
    public void addSecurityRole(SecurityRole securityRole);
	/** Return the security constraints on this web application. */
    public Enumeration getSecurityConstraints();
    public void addSecurityConstraint(SecurityConstraint securityConstraint);
    
    
    public LoginConfiguration getLoginConfiguration();
    public void setLoginConfiguration(LoginConfiguration loginConfiguration);

    /** Get all the TLD location/uri mappings. */
    public Enumeration getTagLibConfigs();
    public void addTagLibConfig(TagLibConfig tldConfig);
    
    // XXX
    // commented out in anticipation they'll eventually be supported
/*
    public Enumeration getLocalizedContentDescriptors();
    public void addLocalizedContentDescriptor(LocalizedContentDescriptor localizedContentDescriptor);
*/
}

