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

import org.apache.tomcat.util.XMLParser;
import org.apache.tomcat.util.XMLTree;
import org.apache.tomcat.core.*;
import java.io.*;
import java.util.*;

/**
 * I am a class that translates an input steram containting an
 * XML document into an implementation object for the interface
 * WebApplicationDescriptor using the factory supplied.
 */

/**
 * @author Danny Coward
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 */

public class WebApplicationReader {
    private WebDescriptorFactory factory = null;
    private WebApplicationDescriptor webApplicationDescriptor = null;
    private XMLTree config = null;
    private static final boolean DefaultXMLValidation = true;

    public WebApplicationDescriptor getDescriptor(
        InputStream inputStream, WebDescriptorFactory factory)
    throws Exception {
        return this.getDescriptor(inputStream, factory,
	    DefaultXMLValidation);
    }
		       
    public WebApplicationDescriptor getDescriptor(
        InputStream inputStream, WebDescriptorFactory factory,
	boolean validate)
    throws Exception {
        if (inputStream == null) {
	    String msg = "null inputStream";

	    throw new NullPointerException(msg);
	}

	if (factory == null) {
	    String msg = "null factory";

	    throw new NullPointerException(msg);
	} else {
	    this.factory = factory;
	}

	try {
	    config = (new XMLParser()).process(inputStream, validate);
	} catch (Exception e) {
	    String msg = "can't read config: " + e.getMessage();

	    throw new IllegalStateException(msg);
	}

	try {
	    webApplicationDescriptor =
	        (WebApplicationDescriptor)factory.createDescriptor(
		    WebApplicationDescriptor.class);

	    if (config.getName().equals(Constants.WebApp)) {
	        processIcon();
		processDisplayName();
		processDescription();
		processDistributable();
		processContextParameters();
		processServlets();
		processServletMappings();
		processSessionTimeOut();
		processMIMEMappings();
		processWelcomeFiles();
		processErrorPages();
		processResourceReferences();
		processSecurityConstraints();
		processLoginConfig();
		processSecurityRoles();
		processEnvironmentEntries();    
		processEjbReferences();
                processTagLibConfigs();
	    } else {
	        String msg = "parsing error";

		throw new IllegalStateException(msg);
	    }

            // FIXME: Anil, remove this when you are done - akv
            // System.err.println("Printing the web application descriptor: ");
            // System.err.println(webApplicationDescriptor.toString());
            
	    return webApplicationDescriptor;
	} catch (Throwable t) {
	    String msg = "parsing error: " + t.getMessage();

	    throw new Exception(msg);
	}
    }

    private void processIcon() {
        XMLTree iconTree = this.config.getFirstElement(Constants.ICON);

	if (iconTree != null) {
	    // there is icon information

	    XMLTree smallIconTree =
	        iconTree.getFirstElement(Constants.SMALL_ICON);

	    if (smallIconTree != null) {
	        this.webApplicationDescriptor.setSmallIconUri(
		    smallIconTree.getValue());
	    }
	    
	    XMLTree largeIconTree =
	        iconTree.getFirstElement(Constants.LARGE_ICON);

	    if (largeIconTree != null) {
	        webApplicationDescriptor.setLargeIconUri(
		    largeIconTree.getValue());
	    }
	}
    }

    private void processDisplayName() {
        XMLTree displayNameTree =
	    this.config.getFirstElement(Constants.DISPLAY_NAME);

	if (displayNameTree != null) {
	    this.webApplicationDescriptor.setName(
	        displayNameTree.getValue());
	}
    }

    private void processDescription() {
        XMLTree descriptionTree =
	    this.config.getFirstElement(Constants.DESCRIPTION);

	if (descriptionTree != null) {
	    this.webApplicationDescriptor.setDescription(
	        descriptionTree.getValue());
	}
    }

    private void processDistributable() {
        this.webApplicationDescriptor.setDistributable(
	    config.getFirstElement(Constants.DISTRIBUTABLE) != null);
    }
    
    private void processContextParameters() {
	 Enumeration enum = this.config.elements(Constants.CONTEXT_PARAMETER);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    ContextParameter param = (ContextParameter) this.factory.createDescriptor(ContextParameter.class);
	    if (next.getFirstElement(Constants.DESCRIPTION) != null) {
		param.setDescription(next.getFirstElement(Constants.DESCRIPTION).getValue());
	    }
	    param.setName(next.getFirstElement(Constants.ParameterName).getValue());
	    param.setValue(next.getFirstElement(Constants.ParameterValue).getValue());
	    this.webApplicationDescriptor.addContextParameter(param);
	 }
    }


    private void processTagLibConfigs() {
        Enumeration enum = this.config.elements(Constants.TAGLIB);
        while (enum.hasMoreElements()) {
            XMLTree next = (XMLTree) enum.nextElement();
            TagLibConfig config 
                = (TagLibConfig) this.factory.createDescriptor(TagLibConfig.class);
            config.setTagLibURI(next.getFirstElement(Constants.TAGLIB_URI).getValue());
            config.setTagLibLocation(next.getFirstElement(Constants.TAGLIB_LOCATION).getValue());
            this.webApplicationDescriptor.addTagLibConfig(config);
        }
    }
    
     private void processErrorPages() {
	 Enumeration enum = this.config.elements(Constants.ERROR_PAGE);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    ErrorPageDescriptor epd = (ErrorPageDescriptor) this.factory.createDescriptor(ErrorPageDescriptor.class);
	    if ( next.getFirstElement(Constants.ERROR_CODE) != null) {
		Integer errorCode = new Integer(next.getFirstElement(Constants.ERROR_CODE).getValue());
		epd.setErrorCode(errorCode.intValue());
	    } else if (next.getFirstElement(Constants.EXCEPTION_TYPE) != null) {
		epd.setExceptionType(next.getFirstElement(Constants.EXCEPTION_TYPE).getValue());
	    } else {
		// problem - both nodes were missing - maybe warn ??
	    }
	    epd.setLocation(next.getFirstElement(Constants.LOCATION).getValue());
	    this.webApplicationDescriptor.addErrorPageDescriptor(epd);
	 }
    }
    
    private void processResourceReferences() {
	 Enumeration enum = this.config.elements(Constants.RESOURCE_REFERENCE);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    ResourceReference rrd = (ResourceReference) this.factory.createDescriptor(ResourceReference.class);
	    rrd.setName(next.getFirstElement(Constants.RESOURCE_REFERENCE_NAME).getValue());
	    if (next.getFirstElement(Constants.DESCRIPTION) != null) {
		rrd.setDescription(next.getFirstElement(Constants.DESCRIPTION).getValue());
	    }
	    rrd.setType(next.getFirstElement(Constants.RESOURCE_TYPE).getValue());
	    rrd.setAuthorization(next.getFirstElement(Constants.RESOURCE_AUTHORIZATION).getValue());
	    this.webApplicationDescriptor.addResourceReference(rrd);
	 }
    }
    
    private void processSecurityConstraints() {
	for (Enumeration e = this.config.elements(Constants.SECURITY_CONSTRAINT); e.hasMoreElements();) {
	    XMLTree scTree = (XMLTree) e.nextElement();
	    SecurityConstraint sc = (SecurityConstraint) this.factory.createDescriptor(SecurityConstraint.class);
	    for (Enumeration ee = scTree.elements(Constants.WEB_RESOURCE_COLLECTION); ee.hasMoreElements();) {
		XMLTree wrcTree = (XMLTree) ee.nextElement();
		WebResourceCollection wrc = (WebResourceCollection) this.factory.createDescriptor(WebResourceCollection.class);
		wrc.setName(wrcTree.getFirstElement(Constants.WEB_RESOURCE_NAME).getValue());
		if (wrcTree.getFirstElement(Constants.DESCRIPTION) != null) {
		    wrc.setDescription(wrcTree.getFirstElement(Constants.DESCRIPTION).getValue());
		}
		for (Enumeration eee = wrcTree.elements(Constants.URL_PATTERN); eee.hasMoreElements();) {
		    XMLTree upTree = (XMLTree) eee.nextElement();
		    wrc.addUrlPattern(upTree.getValue());
		}
		for (Enumeration eee = wrcTree.elements(Constants.HTTP_METHOD); eee.hasMoreElements();) {
		    XMLTree hmTree = (XMLTree) eee.nextElement();
		    wrc.addHttpMethod(hmTree.getValue());
		}
		sc.addWebResourceCollection(wrc);
	    }
	    XMLTree acTree = scTree.getFirstElement(Constants.AUTH_CONSTRAINT);
	    if (acTree != null) {
		AuthorizationConstraint ac = (AuthorizationConstraint) this.factory.createDescriptor(AuthorizationConstraint.class);
		if (acTree.getFirstElement(Constants.DESCRIPTION) != null) {
		    ac.setDescription(acTree.getFirstElement(Constants.DESCRIPTION).getValue());
		    for (Enumeration eeee = acTree.elements(Constants.ROLE_NAME); eeee.hasMoreElements();) {
			XMLTree roleNameTree = (XMLTree) eeee.nextElement();
			SecurityRole sr = (SecurityRole) this.factory.createDescriptor(SecurityRole.class);
			sr.setName(roleNameTree.getValue());
			ac.addSecurityRole(sr);
		    }
		    
		}
		sc.setAuthorizationConstraint(ac);
	    }
	    
	    XMLTree udcTree = scTree.getFirstElement(Constants.USERDATA_CONSTRAINT);
	    if (udcTree != null) {
		UserDataConstraint udc = (UserDataConstraint) this.factory.createDescriptor(UserDataConstraint.class);
		if (udcTree.getFirstElement(Constants.DESCRIPTION) != null) {
		    udc.setDescription(udcTree.getFirstElement(Constants.DESCRIPTION).getValue());
		}
		udc.setTransportGuarantee(udcTree.getFirstElement(Constants.TRANSPORT_GUARANTEE).getValue());
		sc.setUserDataConstraint(udc);
	    }
	    this.webApplicationDescriptor.addSecurityConstraint(sc);
	}
    }
    
    private void processLoginConfig() {
	XMLTree flTree = this.config.getFirstElement(Constants.LOGIN_CONFIG);
	if (flTree != null) {
	    LoginConfiguration lc = (LoginConfiguration) this.factory.createDescriptor(LoginConfiguration.class);
	    if (flTree.getFirstElement(Constants.AUTH_METHOD) != null) {
		lc.setAuthenticationMethod(flTree.getFirstElement(Constants.AUTH_METHOD).getValue());
	    }
	    if (flTree.getFirstElement(Constants.REALM_NAME) != null) {
		lc.setRealmName(flTree.getFirstElement(Constants.REALM_NAME).getValue());
	    }
	    if (flTree.getFirstElement(Constants.FORM_LOGIN_CONFIG) != null) {
		XMLTree formTree = flTree.getFirstElement(Constants.FORM_LOGIN_CONFIG);
		lc.setFormLoginPage(formTree.getFirstElement(Constants.FORM_LOGIN_PAGE).getValue());
		lc.setFormErrorPage(formTree.getFirstElement(Constants.FORM_ERROR_PAGE).getValue());
	    }
	    this.webApplicationDescriptor.setLoginConfiguration(lc);
	}
	
    }
    
    private void processSecurityRoles() {
	Enumeration enum = this.config.elements(Constants.SECURITY_ROLE);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    SecurityRole sr = (SecurityRole) this.factory.createDescriptor(SecurityRole.class);
	    if ( next.getFirstElement(Constants.DESCRIPTION) != null) {
		sr.setDescription(next.getFirstElement(Constants.DESCRIPTION).getValue());
	    }
	    sr.setName(next.getFirstElement(Constants.ROLE_NAME).getValue());
	    this.webApplicationDescriptor.addSecurityRole(sr);
	}
    }
    
    private void processEnvironmentEntries() {
	Enumeration enum = this.config.elements(Constants.ENVIRONMENT_ENTRY);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    EnvironmentEntry ee = (EnvironmentEntry) this.factory.createDescriptor(EnvironmentEntry.class);
	    if ( next.getFirstElement(Constants.DESCRIPTION) != null) {
		ee.setDescription(next.getFirstElement(Constants.DESCRIPTION).getValue());
	    }
	    ee.setName(next.getFirstElement(Constants.ENVIRONMENT_NAME).getValue());
	    if (next.getFirstElement(Constants.ENVIRONMENT_VALUE) != null) {
		ee.setValue(next.getFirstElement(Constants.ENVIRONMENT_VALUE).getValue());
	    }
	    ee.setType(next.getFirstElement(Constants.ENVIRONMENT_TYPE).getValue());
	    this.webApplicationDescriptor.addEnvironmentEntry(ee);
	}
    }
    

    
    private void processEjbReferences() {
	Enumeration enum = this.config.elements(Constants.EJB_REFERENCE);
	 while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    EjbReference ejbr = (EjbReference) this.factory.createDescriptor(EjbReference.class);
	    if (next.getFirstElement(Constants.DESCRIPTION) != null) {
		ejbr.setName(next.getFirstElement(Constants.DESCRIPTION).getValue());
	    }
	    
	    ejbr.setName(next.getFirstElement(Constants.EJB_NAME).getValue());
	    ejbr.setType(next.getFirstElement(Constants.EJB_TYPE).getValue());
	    ejbr.setHomeClassName(next.getFirstElement(Constants.EJB_HOME).getValue());
	    ejbr.setRemoteClassName(next.getFirstElement(Constants.EJB_REMOTE).getValue());
	    if (next.getFirstElement(Constants.EJB_LINK) != null) {
		ejbr.setLinkName(next.getFirstElement(Constants.EJB_LINK).getValue());
	    }
	    this.webApplicationDescriptor.addEjbReference(ejbr);
	}
	
    }
    
    
					 
    private void processMIMEMappings() {
        Enumeration enum = parseMIMEMappings(
	    this.config.elements(Constants.MIMEMapping)).elements();

	try {
	    while (enum.hasMoreElements()) {
	        this.webApplicationDescriptor.addMimeMapping(
		    (MimeMapping)enum.nextElement());
	    }
	} catch (Exception e) {
	    System.out.println("parser error: MIMEMappings");
	}
    }

    private Vector parseMIMEMappings(Enumeration mimeMaps) {
        Vector mimeMappings = new Vector();

	while (mimeMaps.hasMoreElements()) {
	    XMLTree x = (XMLTree)mimeMaps.nextElement();
	    String extension = null;
	    String mimeType = null;
	    Enumeration enum =
	        x.getElements(Constants.MIMEMappingExtension).elements();

	    while (enum.hasMoreElements()) {
		extension =
		    (String)((XMLTree)enum.nextElement()).getValue().trim();
	    }

	    enum = x.getElements(Constants.MIMEMappingType).elements();

	    while (enum.hasMoreElements()) {
		mimeType =
		    (String)((XMLTree)enum.nextElement()).getValue().trim();
	    }

	    if (extension != null &&
	        extension.length() > 0 &&
		mimeType != null &&
		mimeType.length() > 0) {
	        try {
		    MimeMapping mimeMapping =
		        (MimeMapping)this.factory.createDescriptor(
			    MimeMapping.class);
		    mimeMapping.setExtension(extension);
		    mimeMapping.setMimeType(mimeType);
		    mimeMappings.addElement(mimeMapping);
		} catch (Exception e) {
		}
	    }
	}

	return mimeMappings;
    }

    private void processServlets() {
	Enumeration enum = this.config.elements(Constants.Servlet);
	while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    
	    WebComponentDescriptor descriptor = null;
	    
	    
	    
	    // first we have to work out whether it s a servlet of jsp
	    if (next.getFirstElement(Constants.ServletClass) != null) {
		// its a servlet
		descriptor = (ServletDescriptor) this.factory.createDescriptor(ServletDescriptor.class);
		((ServletDescriptor) descriptor).setClassName(next.getFirstElement(Constants.ServletClass).getValue());
	    } else {
		descriptor = (JspDescriptor) this.factory.createDescriptor(JspDescriptor.class);
		((JspDescriptor) descriptor).setJspFileName(next.getFirstElement(Constants.JSP_FILENAME).getValue());
	    }

	    if (next.getFirstElement(Constants.LOAD_ON_START_UP) != null) {
//		Integer loadOnStartUp = new Integer(0);
		Integer loadOnStartUp = new Integer(Integer.MAX_VALUE);

		try {
			loadOnStartUp = new Integer(next.getFirstElement(Constants.LOAD_ON_START_UP).getValue());
		} catch (NumberFormatException nfe) {
		}

		descriptor.setLoadOnStartUp(loadOnStartUp.intValue());
	    }
	    // init params
	    Enumeration initParamTrees = next.getElements(Constants.Parameter).elements();
	    while(initParamTrees.hasMoreElements()) {
		XMLTree paramTree = (XMLTree)initParamTrees.nextElement();
                String parameterName = null;
                String parameterValue = null;
	
		InitializationParameter ip =
		    (InitializationParameter)this.factory.createDescriptor(
		        InitializationParameter.class);
		
		ip.setName(paramTree.getFirstElement(Constants.ParameterName).getValue());
		ip.setValue(paramTree.getFirstElement(Constants.ParameterValue).getValue());
		if ( paramTree.getFirstElement(Constants.DESCRIPTION) != null ) {
		    ip.setDescription(paramTree.getFirstElement(Constants.DESCRIPTION).getValue());
		}
		descriptor.addInitializationParameter(ip);
	    }
	    
	    Enumeration roleRefsTree = next.getElements(Constants.SECURITY_ROLE_REFERENCE).elements();
	    while(roleRefsTree.hasMoreElements()) {
		XMLTree roleRefTree = (XMLTree)roleRefsTree.nextElement();
                
	
		SecurityRoleReference srr =
		    (SecurityRoleReference)this.factory.createDescriptor(
		        SecurityRoleReference.class);
		if ( roleRefTree.getFirstElement(Constants.DESCRIPTION) != null ) {
		    srr.setDescription(roleRefTree.getFirstElement(Constants.DESCRIPTION).getValue());
		}
		
		SecurityRole sr =
		    (SecurityRole)this.factory.createDescriptor(
		        SecurityRole.class);
		sr.setName(roleRefTree.getFirstElement(Constants.ROLE_LINK).getValue());
		
		srr.setSecurityRoleLink(   sr  );
		srr.setRolename(roleRefTree.getFirstElement(Constants.ROLE_NAME).getValue());
		if ( roleRefTree.getFirstElement(Constants.DESCRIPTION) != null ) {
		    srr.setDescription(roleRefTree.getFirstElement(Constants.DESCRIPTION).getValue());
		}
		descriptor.addSecurityRoleReference(srr);
	    }
	    
	    XMLTree iconTree = next.getFirstElement(Constants.ICON);
	    if (iconTree != null) {
		XMLTree smallIconTree =
		    iconTree.getFirstElement(Constants.SMALL_ICON);
		if (smallIconTree != null) {
		    descriptor.setSmallIconUri(
			smallIconTree.getValue());
		}
		XMLTree largeIconTree =
		    iconTree.getFirstElement(Constants.LARGE_ICON);
		if (largeIconTree != null) {
		    descriptor.setLargeIconUri(
			largeIconTree.getValue());
		}
	    }
	    
	    XMLTree displayNameTree = next.getFirstElement(Constants.DISPLAY_NAME);
	    if (displayNameTree != null) {
		descriptor.setName(displayNameTree.getValue());
	    }
   

   
	    XMLTree descriptionTree = next.getFirstElement(Constants.DESCRIPTION);
	    if (descriptionTree != null) {
		descriptor.setDescription(descriptionTree.getValue());
	    }
		
	    descriptor.setCanonicalName(next.getFirstElement(Constants.ServletName).getValue());

	    this.webApplicationDescriptor.addWebComponentDescriptor(descriptor);
	}
    }
    
    private void processServletMappings() {
	Enumeration enum = this.config.elements(Constants.ServletMapping);
	while(enum.hasMoreElements()) {
	    XMLTree next = (XMLTree) enum.nextElement();
	    WebComponentDescriptor descriptor = this.getWebComponentDescriptorByName(next.getFirstElement(Constants.ServletName).getValue());
	    descriptor.addUrlPattern(next.getFirstElement(Constants.URLPattern).getValue());
	}
    }
    
    private WebComponentDescriptor getWebComponentDescriptorByName(String name) {
	for (Enumeration e = this.webApplicationDescriptor.getWebComponentDescriptors(); e.hasMoreElements();) {
	    WebComponentDescriptor next = (WebComponentDescriptor) e.nextElement();
	    if (next.getCanonicalName().equals(name)) {
		return next;
	    }
	}
	throw new RuntimeException("There is no web component by the name of " + name + " here.");
    }

    
    
    private Vector parseServlets(Enumeration servlets,
        Enumeration servletMaps) {
        Vector webComponentDescriptors = new Vector();

        while (servlets.hasMoreElements()) {
            XMLTree x = (XMLTree)servlets.nextElement();
            String name = null;
            String clazz = null;
            Enumeration e =
                x.getElements(Constants.ServletName).elements();

            while (e.hasMoreElements()) {
                name =
                    (String)((XMLTree)e.nextElement()).getValue().trim();
            }    

            e = x.getElements(Constants.ServletClass).elements();

            while (e.hasMoreElements()) {
                clazz =
                    (String)((XMLTree)e.nextElement()).getValue().trim();
            }    

	    ServletDescriptorImpl servletDescriptor =
	        new ServletDescriptorImpl(name, clazz);

            e = x.getElements(Constants.Parameter).elements();

            while (e.hasMoreElements()) {
                XMLTree x1 = (XMLTree)e.nextElement();
                String parameterName = null;
                String parameterValue = null;
                Enumeration e1 =
                    x1.getElements(Constants.ParameterName).elements();
 
                while (e1.hasMoreElements()) {
                    parameterName =
                        (String)((XMLTree)e1.nextElement()).getValue().trim();
                }
                 
                e1 = x1.getElements(Constants.ParameterValue).elements();
 
                while (e1.hasMoreElements()) {
                    parameterValue =
                        (String)((XMLTree)e1.nextElement()).getValue().trim();
                }
		InitializationParameter ip =
		    (InitializationParameter)this.factory.createDescriptor(
		        InitializationParameter.class);
		ip.setName(parameterName);
		ip.setValue(parameterValue);
		servletDescriptor.addInitializationParameter(ip);
            }

	    webComponentDescriptors.addElement(servletDescriptor);
        }

	while (servletMaps.hasMoreElements()) {
	    XMLTree x = (XMLTree)servletMaps.nextElement();
	    String servletName = null;
	    String map = null;
	    Enumeration e =
	        x.getElements(Constants.ServletName).elements();

	    while (e.hasMoreElements()) {
		servletName =
                    (String)((XMLTree)e.nextElement()).getValue().trim();
	    }

	    e = x.getElements(Constants.URLPattern).elements();

	    while (e.hasMoreElements()) {
		map = (String)((XMLTree)e.nextElement()).getValue().trim();
	    }

	    if (servletName != null &&
	        servletName.length() > 0 &&
		map != null &&
		map.length() > 0) {
		WebComponentDescriptor webComponentDescriptor = null;
		Enumeration enum = webComponentDescriptors.elements();

		while (enum.hasMoreElements()) {
		    WebComponentDescriptor wcd =
		        (WebComponentDescriptor)enum.nextElement();

		    if (wcd.getName().equals(servletName)) {
		        webComponentDescriptor = wcd;

			break;
		    }
		}

		if (webComponentDescriptor == null) {
		    webComponentDescriptor =
		        new ServletDescriptorImpl(servletName);
		    webComponentDescriptors.addElement(
			webComponentDescriptor);
		}

		webComponentDescriptor.addUrlPattern(map);
	    }
	}

	return webComponentDescriptors;
    }

    private void processWelcomeFiles() {
	XMLTree wflTree = this.config.getFirstElement(Constants.WelcomeFileList);
	if (wflTree != null) {
	    Enumeration enum = wflTree.elements(Constants.WelcomeFile);
	    while(enum.hasMoreElements()) {
		XMLTree next = (XMLTree) enum.nextElement();
		this.webApplicationDescriptor.addWelcomeFile(next.getValue());
	    }
	}
    }

    private Vector parseWelcomeFiles(Enumeration welcomeFiles) {
        Vector welcomeFilesV = new Vector();

        while (welcomeFiles.hasMoreElements()) {
	    XMLTree x = (XMLTree)welcomeFiles.nextElement();

            welcomeFilesV.addElement((String)x.getValue().trim());
	}

	return welcomeFilesV;
    }

    private void processSessionTimeOut() {
	if (this.config.getFirstElement(Constants.SESSION_CONFIG) != null) {
    
	    this.webApplicationDescriptor.setSessionTimeout(
		getSessionTimeOut(this.config.getFirstElement(
		    Constants.SESSION_CONFIG).elements(
			Constants.SessionTimeOut)));
	}
    }

    private int getSessionTimeOut(Enumeration sessionTimeOuts) {
        Integer sessionTimeOut = new Integer(-1);

	while (sessionTimeOuts.hasMoreElements()) {
	    XMLTree x = (XMLTree)sessionTimeOuts.nextElement();

	    try {
	        sessionTimeOut = new Integer(x.getValue().trim());
	    } catch (Exception e) {
	    }
	}
	
	return sessionTimeOut.intValue();
    }

    // -------------------- Context setup--------------------
    public void processDefaultWebApp(Context ctx) throws Exception {
	Class webApplicationDescriptor = this.getClass();
	InputStream is =
	    webApplicationDescriptor.getResourceAsStream("web.xml");
	processWebApp(ctx, is);
    }

    public void processWebApp(Context ctx, InputStream is) throws Exception {
	WebApplicationDescriptor webDescriptor =getDescriptor(is,
							      new WebDescriptorFactoryImpl(),
							      ctx.isWARValidated());
	
	ctx.setDescription( webDescriptor.getDescription());
	ctx.setDistributable( webDescriptor.isDistributable());
	
	Enumeration contextParameters=webDescriptor.getContextParameters();
	while (contextParameters.hasMoreElements()) {
	    ContextParameter contextParameter =
		(ContextParameter)contextParameters.nextElement();
	    ctx.setInitParameter(contextParameter.getName(),
				 contextParameter.getValue());
	}
	ctx.setSessionTimeOut( webDescriptor.getSessionTimeout());
	
	processServlets(ctx, webDescriptor.getWebComponentDescriptors());
	processMimeMaps(ctx, webDescriptor.getMimeMappings());
	processWelcomeFiles(ctx, webDescriptor.getWelcomeFiles());
	processErrorPages(ctx, webDescriptor.getErrorPageDescriptors());
    }

    private void processServlets(Context ctx, Enumeration servlets) throws Exception {
        // XXX
        // oh my ... this has suddenly turned rather ugly
        // perhaps the reader should do this normalization work

        while (servlets.hasMoreElements()) {
	    WebComponentDescriptor webComponentDescriptor =
	        (WebComponentDescriptor)servlets.nextElement();
	    String name = webComponentDescriptor.getCanonicalName();
	    String description = webComponentDescriptor.getDescription();
	    String resourceName = null;
	    boolean removeResource = false;

	    if (webComponentDescriptor instanceof ServletDescriptor) {
		resourceName =
		    ((ServletDescriptor)webComponentDescriptor).getClassName();
		
		if ( ctx.getServletByName(name) != null) {
// 		    String msg = sm.getString("context.dd.dropServlet",
// 					      name + "(" + resourceName + ")" );
		    
// 		    System.out.println(msg);
		    
		    removeResource = true;
		    ctx.removeServletByName(name);
		}

		ServletWrapper sw=new ServletWrapper();
		sw.setContext( ctx );
		sw.setServletName( name );
		sw.setServletClass( resourceName );
		ctx.addServlet(sw);
	    } else if (webComponentDescriptor instanceof JspDescriptor) {
		resourceName =
		    ((JspDescriptor)webComponentDescriptor).getJspFileName();

		if (! resourceName.startsWith("/")) {
		    resourceName = "/" + resourceName;
		}

		
		if (containsJSP(ctx, resourceName)) {
// 		    String msg = sm.getString("context.dd.dropServlet",
// 					      resourceName);

// 		    System.out.println(msg);
		    
		    removeResource = true;
		    Enumeration enum = ctx.getServletNames();
		    while (enum.hasMoreElements()) {
			String key = (String)enum.nextElement();
			ServletWrapper sw = ctx.getServletByName(key);
			if(resourceName.equals( (sw).getPath())) {
			    ctx.removeServletByName( sw.getServletName() );
			}
		    }
		}

		ServletWrapper wrapper = new ServletWrapper();
		wrapper.setContext(ctx);
		wrapper.setServletName(name);
		wrapper.setServletDescription(description);
		wrapper.setPath(resourceName);

		ctx.addServlet(wrapper);
	    }


	    // XXX ugly, but outside of context - the whole thing will be
	    // rewriten, so don't worry
	    
	    int loadOnStartUp = webComponentDescriptor.getLoadOnStartUp();

            if (loadOnStartUp > Integer.MIN_VALUE) {
		ServletWrapper swrap=ctx.getServletByName(name);
		swrap.setLoadOnStartUp( loadOnStartUp );
	    }

	    Enumeration enum =
	        webComponentDescriptor.getInitializationParameters();

	    String cName=webComponentDescriptor.getCanonicalName();
	    ServletWrapper sw=ctx.getServletByName( cName );

	    while (enum.hasMoreElements()) {
	        InitializationParameter initializationParameter =
		    (InitializationParameter)enum.nextElement();

		if (sw != null) {
		    sw.addInitParam(initializationParameter.getName(),
				    initializationParameter.getValue());
		}
	    }


	    enum = webComponentDescriptor.getUrlPatterns();

	    while (enum.hasMoreElements()) {
	        String mapping = (String)enum.nextElement();

		if (! mapping.startsWith("*.") &&
		    ! mapping.startsWith("/")) {
		    mapping = "/" + mapping;
		}

		if (! containsServlet(ctx, mapping) &&
		    ! containsJSP(ctx, mapping)) {

                    ctx.addServletMapping( mapping, name);
		} else {
// 		    String msg = sm.getString("context.dd.ignoreMapping",
// 		        mapping);

// 		    System.out.println(msg);
		}
	    }
	}
    }

    private void processMimeMaps(Context ctx, Enumeration mimeMaps) {
        while (mimeMaps.hasMoreElements()) {
	    MimeMapping mimeMapping = (MimeMapping)mimeMaps.nextElement();

	    ctx.addContentType(	mimeMapping.getExtension(),
				mimeMapping.getMimeType());
	}
    }

    private void processWelcomeFiles(Context ctx, Enumeration welcomeFiles ) {
        if ( welcomeFiles.hasMoreElements()) {
            ctx.removeWelcomeFiles();
        }

	while (welcomeFiles.hasMoreElements()) {
	    ctx.addWelcomeFile((String)welcomeFiles.nextElement());
	}
    }

    private void processErrorPages(Context ctx, Enumeration errorPages) {
        while (errorPages.hasMoreElements()) {
	    ErrorPageDescriptor errorPageDescriptor =
	        (ErrorPageDescriptor)errorPages.nextElement();
	    String key = null;

	    if (errorPageDescriptor.getErrorCode() > -1) {
	        key = String.valueOf(errorPageDescriptor.getErrorCode());
	    } else {
	        key = errorPageDescriptor.getExceptionType();
	    }

	    ctx.addErrorPage(key, errorPageDescriptor.getLocation());
	}
    }

    private boolean containsJSP( Context ctx, String path) {
	Enumeration enum = ctx.getServletNames();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = ctx.getServletByName(key);

	    if( path.equals( (sw).getPath()))
		return true;
	}
	return false;
    }

    
    /** True if we have a servlet with className.
     */
    public boolean containsServlet(Context ctx, String className) {
	Enumeration enum = ctx.getServletNames();

	while (enum.hasMoreElements()) {
	    String key = (String)enum.nextElement();
	    ServletWrapper sw = ctx.getServletByName(key);
            if (className.equals(sw.getServletClass()))
	        return true;
	}
	return false;
    }

}
