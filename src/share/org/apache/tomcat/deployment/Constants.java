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

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 * @author Danny Coward
 */

public class Constants {

    public static final String ConfigFile = "web.xml";

    public static final String WebApp = "web-app";
    public static final String Servlet = "servlet";    
    public static final String ServletName = "servlet-name";
    public static final String ServletClass = "servlet-class";
    public static String JSP_FILENAME = "jsp-file";
    public static String LOAD_ON_START_UP = "load-on-startup";
     
    public static final String Parameter = "init-param";
    public static String CONTEXT_PARAMETER = "context-param";
    public static final String ParameterName = "param-name";
    public static final String ParameterValue = "param-value";
    public static final String MIMEMapping = "mime-mapping";
    public static final String MIMEMappingExtension = "extension";
    public static final String MIMEMappingType = "mime-type";
    public static final String ServletMapping = "servlet-mapping";
    public static final String URLPattern = "url-pattern";
    public static final String SessionTimeOut = "session-timeout";
    public static final String WelcomeFileList = "welcome-file-list";
    public static final String WelcomeFile = "welcome-file";
    
    public static String DISPLAY_NAME = "display-name";
    public static String DESCRIPTION = "description";
    public static String ICON = "icon";
    public static String LARGE_ICON = "large-icon";
    public static String SMALL_ICON = "small-icon";
    public static String DISTRIBUTABLE = "distributable";
    
    public static String ERROR_PAGE = "error-page";
    public static String ERROR_CODE = "error-code";
    public static String EXCEPTION_TYPE = "exception-type";
    public static String LOCATION = "location";
    
    public static String ENVIRONMENT_ENTRY = "env-entry";
    public static String ENVIRONMENT_NAME = "env-entry-name";
    public static String ENVIRONMENT_VALUE = "env-entry-value";
    public static String ENVIRONMENT_TYPE = "env-entry-type";
    
    
    public static String RESOURCE_REFERENCE = "resource-ref";
    public static String RESOURCE_REFERENCE_NAME = "res-ref-name";
    public static String RESOURCE_TYPE = "res-type";
    public static String RESOURCE_AUTHORIZATION = "res-auth";
    
    public static String SECURITY_ROLE = "security-role";
    public static String ROLE_NAME = "role-name";
    public static String NAME = "name";
    
    public static String SECURITY_CONSTRAINT = "security-constraint";
    public static String WEB_RESOURCE_COLLECTION = "web-resource-collection";
    public static String AUTH_CONSTRAINT = "auth-constraint";
    public static String USERDATA_CONSTRAINT = "user-data-constraint";
    public static String TRANSPORT_GUARANTEE = "transport-guarantee";
    public static String WEB_RESOURCE_NAME = "web-resource-name";
    public static String URL_PATTERN = "url-pattern";
    public static String HTTP_METHOD = "http-method";
    
    public static String SECURITY_ROLE_REFERENCE = "security-role-ref";
    public static String ROLE_LINK = "role-link";
    
    
    public static String EJB_REFERENCE = "ejb-ref";
    public static String EJB_NAME = "ejb-ref-name";
    public static String EJB_TYPE = "ejb-ref-type";
    public static String EJB_HOME = "home";
    public static String EJB_REMOTE = "remote";
    public static String EJB_LINK = "ejb-link";
        
    public static final String SESSION_CONFIG = "session-config";
    
    public static String LOGIN_CONFIG = "login-config";
    public static String AUTH_METHOD = "auth-method";
    public static String REALM_NAME = "realm-name";
    public static String FORM_LOGIN_CONFIG = "form-login-config";
    public static String FORM_LOGIN_PAGE = "form-login-page";
    public static String FORM_ERROR_PAGE = "form-error-page";

    public static final String TAGLIB = "taglib";
    public static final String TAGLIB_URI = "taglib-uri";
    public static final String TAGLIB_LOCATION = "taglib-location";


    public static final String JakartaWebApplication =
        "org.apache.deployment.WebApplicationDescriptorImpl";
}
