/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/security/Attic/SecurityInterceptor.java,v 1.1 2000/02/11 00:22:33 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/02/11 00:22:33 $
 *
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


package org.apache.tomcat.security;


import java.security.Principal;
import java.util.Enumeration;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.InterceptorException;
import org.apache.tomcat.core.ServiceInterceptor;
import org.apache.tomcat.deployment.AuthorizationConstraint;
import org.apache.tomcat.deployment.LoginConfiguration;
import org.apache.tomcat.deployment.SecurityConstraint;
import org.apache.tomcat.deployment.SecurityRole;
import org.apache.tomcat.deployment.SecurityRoleReference;
import org.apache.tomcat.deployment.ServletDescriptor;
import org.apache.tomcat.deployment.UserDataConstraint;
import org.apache.tomcat.deployment.WebApplicationDescriptor;
import org.apache.tomcat.deployment.WebResourceCollection;


/**
 * Implementation of <code>org.apache.tomcat.core.ServiceInterceptor</code>
 * that implements the security functionality described in Section 11 of the
 * Java Servlet Specification Version 2.2.
 * <p>
 * XXX - Unimplemented Features:
 * <ul>
 * <li>Role mapping for per-servlet <code>&lt;security-role-ref&gt;</code>
 *     elements in the deployment descriptor.
 * <li>HTTP Digest Authentication support.
 * <li>HTTPS Client Authentication support.
 * <li>Form Based Authentication support.
 * </ul>
 *
 * @author Craig R. McClanahan
 */


public final class SecurityInterceptor implements ServiceInterceptor {


    // --------------------------------------------------------- Public Methods


    /**
     * Interceptor method called immediately before the service() method
     * is called.
     *
     * @param context Context within which we are operating
     * @param servlet Servlet whose <code>service()</code> method we will
     *  ultimately call
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     *
     * @exception InterceptorException if we do not want the service()
     *  method to actually be called
     */
    public void preInvoke(Context context, Servlet servlet,
			  HttpServletRequest req, HttpServletResponse res)
	throws InterceptorException {

	// Acquire the WebApplicationDescriptor for this Context
	WebApplicationDescriptor descriptor = null;
	// XXX - descriptor = context.getWebApplicationDescriptor();
	if (descriptor == null)
	    return;

	// Is this request URI subject to a security constraint?
	SecurityConstraint constraint = findConstraint(req, descriptor);
	if (constraint == null)
	    return;

	// Enforce any user data constraint for this security constraint
	userData(req, res, constraint.getUserDataConstraint());

	// Authenticate based upon the specified login configuration
	authenticate(context, req, res, descriptor.getLoginConfiguration());

	// Perform access control based on the specified role(s)
	accessControl(context, req, res,
		      constraint.getAuthorizationConstraint());

    }


    /**
     * Interceptor method called immediately after the service() method
     * is called.
     *
     * @param context Context within which we are operating
     * @param servlet Servlet whose <code>service()</code> method we will
     *  ultimately call
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     *
     * @exception InterceptorException if a problem we wish to report occurs
     */
    public void postInvoke(Context context, Servlet servlet,
			   HttpServletRequest req, HttpServletResponse res)
	throws InterceptorException {

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Perform access control based on the specified authorization constraint.
     *
     * @param context Context within which we are operating
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     * @param auth Authorization constraint we are enforcing
     *
     * @exception InterceptorException if we have already created the response
     *  and the underlying service() method should not be called
     */
    private void accessControl(Context context, HttpServletRequest req,
			       HttpServletResponse res,
			       AuthorizationConstraint auth)
	throws InterceptorException {

	Principal principal = req.getUserPrincipal();
	if (principal == null) {
	    ;	// XXX - Send "misconfigured" error and
	    ;	// throw InterceptorException
	}
	RealmConnector realm = null;
	// XXX - realm = context.getRealmConnector();

	// Check each role included in this constraint
	Enumeration roles = auth.getSecurityRoles();
	while (roles.hasMoreElements()) {
	    SecurityRole role = (SecurityRole) roles.nextElement();
	    if (realm.hasRole(principal, role.getName()))
		return;
	}

	;	// XXX - Send "forbidden" error and
	;	// throw InterceptorException

    }


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.
     *
     * @param context Context within which we are operating
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     * @param login LoginConfiguration describing how authentication
     *  should be performed
     *
     * @exception InterceptorException if we have already created the response
     *  and the underlying service() method should not be called
     */
    private void authenticate(Context context, HttpServletRequest req,
			      HttpServletResponse res,
			      LoginConfiguration config)
	throws InterceptorException {

	if (config == null) {
	    ;	// XXX - Send "misconfiguration" error response, and
	    ;	// throw InterceptorException
	}
	String method = config.getAuthenticationMethod();
	if (method == null)
	    method = "BASIC";	// XXX - Is this default correct?

	if (method.equals("BASIC"))
	    authenticateBasic(context, req, res, config);
	/*
	else if (method.equals("DIGEST"))
	    authenticateDigest(context, req, res, config);
	else if (method.equals("FORM"))
	    authenticateForm(context, req, res, config);
	else if (method.equals("CLIENT-CERT"))
	    authenticateClientCert(context, req, res, config);
	*/
	else {
	    ;	// XXX - Send "misconfiguration error response, and
	    ;	// throw InterceptorException
	}

    }


    /**
     * Authenticate the user making this request, using HTTP BASIC
     * authentication (see RFC 2617).
     *
     * @param context Context within which we are operating
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     * @param login LoginConfiguration describing how authentication
     *  should be performed
     *
     * @exception InterceptorException if we have already created the response
     *  and the underlying service() method should not be called
     */
    private void authenticateBasic(Context context, HttpServletRequest req,
				   HttpServletResponse res,
				   LoginConfiguration config)
	throws InterceptorException {

	// Validate any credentials already included with this request
	String authorization = req.getHeader("Authorization");
	if (authorization != null) {
	    RealmConnector realm = null;
	    //	XXX - realm = context.getRealmConnector();
	    Principal principal = findPrincipalBasic(authorization, realm);
	    if (principal != null) {
		;	// XXX - req.setUserPrincipal()
		;	// XXX - req.setRemoteUser()
		return;
	    }
	}

	// Send an Unauthorized response and an appropriate challenge
	String realm = config.getRealmName();
	if (realm == null)
	    realm = req.getServerName() + ":" + req.getServerPort();
	res.setHeader("WWW-Authenticate", "Basic \"" + realm + "\"");
	res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	;	// XXX - Throw InterceptorException, but wrapping what?
    }


    /**
     * Return the SecurityConstraint configured to guard the request URI for
     * this request, or <code>null</code> if there is no such constraint.
     *
     * XXX - For efficiency, the patterns being protected should really
     * be unpacked into a collection that can be accessed more quickly.
     * This will require an instance of SecurityInterceptor per context, and
     * the addition of lifecycle start/stop methods.  (This will also eliminate
     * the need to pass the Context argument on each call).
     *
     * @param req Servlet request we are processing
     * @param descriptor WebApplicationDescriptor within which we are operating
     */
    private SecurityConstraint findConstraint(HttpServletRequest req,
					WebApplicationDescriptor descriptor) {

	if (descriptor == null)
	    return (null);
	Enumeration constraints = descriptor.getSecurityConstraints();
	if (constraints == null)
	    return (null);

	// Check each defined security constraint
	while (constraints.hasMoreElements()) {
	    SecurityConstraint constraint =
		(SecurityConstraint) constraints.nextElement();
	    Enumeration collections = constraint.getWebResourceCollections();
	    while (collections.hasMoreElements()) {
		WebResourceCollection collection =
		    (WebResourceCollection) collections.nextElement();
		if (matchCollection(req, collection))
		    return (constraint);
	    }
	}

	// No applicable security constraint was found
	return (null);

    }


    /**
     * Return the Principal, from the specified RealmConnector, authenticated
     * by the specified credentials.  If there is no such Principal, return
     * <code>null</code>.
     *
     * @param authorization Authorization credentials from the request
     * @param realm RealmConnector associated with this Context
     */
    private Principal findPrincipalBasic(String authorization,
					 RealmConnector realm) {

	// Validate the authorization credentials format
	if (authorization == null)
	    return (null);
	if (!authorization.startsWith("Basic "))
	    return (null);
	authorization = authorization.substring(6).trim();

	// Decode and parse the authorization credentials
	String unencoded = authorization;	// XXX - Base64 Decoder needed!
	int colon = unencoded.indexOf(':');
	if (colon < 0)
	    return (null);
	String username = unencoded.substring(0, colon);
	String password = unencoded.substring(colon + 1);

	// Validate these credentials in our associated realm
	return (realm.authenticate(username, password));

    }


    /**
     * Do the characteristics of this request match the protection patterns
     * of the specified web resource collection?  Matching is done based on
     * both the URL pattern and HTTP method (if any) restrictions.
     *
     * @param req Servlet request we are processing
     * @param collection WebResourceCollection to test against
     */
    private boolean matchCollection(HttpServletRequest req,
				    WebResourceCollection collection) {

	// Test against the HTTP methods
	String method = req.getMethod();
	int n = 0;
	boolean match = false;
	Enumeration methods = collection.getHttpMethods();
	while (methods.hasMoreElements()) {
	    n++;
	    if (method.equals((String) methods.nextElement())) {
		match = true;
		break;
	    }
	}
	if ((!match) && (n > 0))
	    return (false);

	// Test against the URL patterns
	String path = req.getServletPath();
	if (path == null)
	    path = "";
	if (req.getPathInfo() != null)
	    path += req.getPathInfo();
	Enumeration patterns = collection.getUrlPatterns();
	while (patterns.hasMoreElements()) {
	    String pattern = (String) patterns.nextElement();
	    if (matchPattern(path, pattern))
		return (true);
	}

	return (false);

    }


    /**
     * Does the specified request path match the specified URL pattern?
     *
     * XXX - Shouldn't this be a shared utility method someplace?  I could
     * not find one -- the logic in Container.java seems specific to adding
     * mappings to the internal collections.
     *
     * @param path Context-relative request path to be checked
     *  (must start with '/')
     * @param pattern URL pattern to be compared against
     */
    private boolean matchPattern(String path, String pattern) {

	// Normalize the argument strings
	if ((path == null) || (path.length() == 0))
	    path = "/";
	if ((pattern == null) || (pattern.length() == 0))
	    pattern = "/";

	// Check for exact match
	if (path.equals(pattern))
	    return (true);

	// Check for universal mapping
	if (pattern.equals("/"))
	    return (true);

	// Check for path prefix matching
	if (pattern.startsWith("/") && pattern.endsWith("/*")) {
	    pattern = pattern.substring(0, pattern.length() - 2);
	    if (pattern.length() == 0)
		return (true);	// "/*" is the same as "/"
	    if (path.endsWith("/"))
		path = path.substring(0, path.length() - 1);
	    while (true) {
		if (pattern.equals(path))
		    return (true);
		int slash = path.lastIndexOf('/');
		if (slash <= 0)
		    break;
		path = path.substring(0, slash);
	    }
	    return (false);
	}

	// Check for suffix matching
	else if (pattern.startsWith("*.")) {
	    int slash = path.lastIndexOf('/');
	    int period = path.lastIndexOf('.');
	    if ((slash >= 0) && (period > slash) &&
		path.endsWith(pattern.substring(1))) {
		return (true);
	    }
	}

	return (false);

    }


    /**
     * Enforce any user data constraint required by the security constraint
     * guarding this request URI.
     *
     * @param req Servlet request we are processing
     * @param res Servlet response we are creating
     * @param user UserDataConstraint we are enforcing
     *
     * @exception InterceptorException if the constraint was violated, we have
     *  already created the response, and service() should not be called
     */
    private void userData(HttpServletRequest req, HttpServletResponse res,
			  UserDataConstraint user)
	throws InterceptorException {

	if (user == null)
	    return;
	String guarantee = user.getTransportGuarantee();
	if (guarantee == null)
	    return;
	if (guarantee.equals(UserDataConstraint.NONE_TRANSPORT))
	    return;
	if (!req.isSecure()) {
	    ;	// XXX - Create error response, throw
	    ;	// InterceptorException
	}

    }


}
