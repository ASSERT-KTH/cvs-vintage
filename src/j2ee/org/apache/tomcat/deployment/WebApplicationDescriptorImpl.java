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
import java.util.Vector;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 * @author Anil K. Vijendran [akv@eng.sun.com] -- added support for taglib in web.xml
 */

public class WebApplicationDescriptorImpl
extends WebDescriptorImpl
implements WebApplicationDescriptor {
    private Vector webComponentDescriptors = new Vector();
    private int sessionTimeOut = 30;
    private Vector mimeMappings = new Vector();
    private Vector welcomeFiles = new Vector();
    private Vector errorURIs = new Vector();
    private Vector contextParameters = new Vector();
    private boolean isDistributable = false;
    private Vector ejbReferences = new Vector();
    private Vector resourceReferences = new Vector();
    private Vector securityRoles = new Vector();
    private Vector securityConstraints = new Vector();
    private Vector tldConfigs = new Vector();
    
    // XXX
    // commented out in anticipation they'll eventually be supported 
/*
    private Vector localizedContentDescriptors = new Vector();
*/
    private LoginConfiguration loginConfiguration;
    private Vector environmentEntries;
    
    public Enumeration getWebComponentDescriptors() {
        return webComponentDescriptors.elements();
    }

    public void addWebComponentDescriptor(
        WebComponentDescriptor webDeploymentDescriptor) {
        webComponentDescriptors.addElement(webDeploymentDescriptor);
    }

    public int getSessionTimeout() {
        return sessionTimeOut;
    }

    public void setSessionTimeout(int sessionTimeOut) {
        this.sessionTimeOut = sessionTimeOut;
    }

    public Enumeration getMimeMappings() {
        return mimeMappings.elements();
    }

    public void addMimeMapping(MimeMapping mimeMapping) {
        mimeMappings.addElement(mimeMapping);
    }

    public Enumeration getWelcomeFiles() {
        return welcomeFiles.elements();
    }

    public void addWelcomeFile(String welcomeFile) {
        welcomeFiles.addElement(welcomeFile);
    }

    public Enumeration getErrorPageDescriptors() {
        return errorURIs.elements();
    }

    public void addErrorPageDescriptor(ErrorPageDescriptor errorURI) {
        errorURIs.addElement(errorURI);
    }

    public Enumeration getContextParameters() {
        return contextParameters.elements();
    }

    public void addContextParameter(ContextParameter contextParameter) {
        contextParameters.addElement(contextParameter);
    }

    public boolean isDistributable() {
        return isDistributable;
    }

    public void setDistributable(boolean isDistributable) {
        this.isDistributable = isDistributable;
    }

    public Enumeration getEjbReferences() {
        return ejbReferences.elements();
    }

    public void addEjbReference(EjbReference ejbReference) {
        ejbReferences.addElement(ejbReference);
    }

    public Enumeration getResourceReferences() {
        return resourceReferences.elements();
    }

    public void addResourceReference(ResourceReference resourceReference) {
        resourceReferences.addElement(resourceReference);
    }

    public Enumeration getSecurityRoles() {
        return securityRoles.elements();
    }

    public void addSecurityRole(SecurityRole securityRole) {
        securityRoles.addElement(securityRole);
    }

    public Enumeration getSecurityConstraints() {
      return securityConstraints.elements();
    }

    public void addSecurityConstraint(
        SecurityConstraint securityConstraint) {
        securityConstraints.addElement(securityConstraint);
    }
    
    // XXX
    // commented out in anticipation they'll eventually be supported 
/*
    public Enumeration getLocalizedContentDescriptors() {
	return localizedContentDescriptors.elements();
    }
    
    public void addLocalizedContentDescriptor(
        LocalizedContentDescriptor localizedContentDescriptor) {
	this.localizedContentDescriptors.addElement(
	    localizedContentDescriptor);
    }
*/
    
    public LoginConfiguration getLoginConfiguration() {
	return this.loginConfiguration;
    }
    
    public void setLoginConfiguration(LoginConfiguration loginConfiguration) {
	this.loginConfiguration = loginConfiguration;
    }
    
    private Vector getEnvironmentEntryVector() {
	if (this.environmentEntries == null) {
	    this.environmentEntries = new Vector();
	}
	return this.environmentEntries;
    }
    
    public Enumeration getEnvironmentEntries() {
	return this.getEnvironmentEntryVector().elements();
    }
    public void addEnvironmentEntry(EnvironmentEntry environmentEntry) {
	this.getEnvironmentEntryVector().addElement(environmentEntry);
    }

    public Enumeration getTagLibConfigs() {
        return tldConfigs.elements();
    }
    
    public void addTagLibConfig(TagLibConfig tldConfig) {
        tldConfigs.addElement(tldConfig);
    }
    
    public String toString() {
	String s = "Web App Descriptor ";
	s = s + super.toString();
	//s = s + " \n webComponentDescriptors" + webComponentDescriptors;
	s = s + " \n sessionTimeOut" + sessionTimeOut;
	//s = s + " \n mimeMappings" + mimeMappings;
	s = s + " \n welcomeFiles" + welcomeFiles;
	s = s + " \n errorURIs" + errorURIs;
	s = s + " \n contextParameters" + contextParameters;
	s = s + " \n isDistributable" + isDistributable;
	s = s + " \n ejbReferences" + ejbReferences;
	s = s + " \n resourceReferences" + resourceReferences;
	s = s + " \n securityRoles" + securityRoles;
	s = s + " \n securityConstraints" + securityConstraints;

      // XXX
      // commented out in anticipation they'll eventually be supported 
/*
	s = s + " \n localizedContentDescriptors" + localizedContentDescriptors;
*/
	s = s + " \n loginConfiguration " + loginConfiguration;
	s = s + " \n environmentEntries " + environmentEntries;
        s = s + " \n tldConfigs " + tldConfigs;

	return s;
    }
}
