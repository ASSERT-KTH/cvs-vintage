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

package org.apache.tomcat.modules.aaa;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.ServerSession;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.buf.Base64;

/**
 *  Extract user/password credentials from a request.
 *  This module is specialized in detecting BASIC and FORM authentication, and
 *  will set 2 notes in the request: "credentials.user" and
 *  "credentials.password".
 *
 *  A "Realm" module may use the 2 notes in authenticating the user. 
 * 
 *  This module must will act on the "authenticate" callback - the action
 *  will happen _only_ for requests requiring authentication, not for
 *  every request.
 *
 *  It must be configured before the Realm module.
 */
public class CredentialsInterceptor extends BaseInterceptor
{
    int userNote;
    int passwordNote;

    /** The module will set a note with this name on the request for
	the extracted user, if Basic or Form authentication is used
    */
    public static final String USER_NOTE="credentials.user";
    /** The module will set a note with this name on the request for
	the extracted password, if Basic or Form authentication is used
    */
    public static final String PASSWORD_NOTE="credentials.password";
    
    public CredentialsInterceptor() {
    }

    public void engineInit( ContextManager cm )
	throws TomcatException
    {
	userNote=cm.getNoteId( ContextManager.REQUEST_NOTE, USER_NOTE);
	passwordNote=cm.getNoteId( ContextManager.REQUEST_NOTE, PASSWORD_NOTE);
    }

    /** Extract the credentails from req
     */
    public int authenticate( Request req , Response res ) {
	Context ctx=req.getContext();
	String login_type=ctx.getAuthMethod();
	if( "BASIC".equals( login_type )) {
	    basicCredentials( req );
	}
	if( "FORM".equals( login_type )) {
	    formCredentials( req );
	}
	return DECLINED;
    }
	
    
    /** Extract userName and password from a request using basic
     *  authentication.
     */
    private void basicCredentials( Request req )
    {
	String authorization = req.getHeader("Authorization");
	
	if (authorization == null )
	    return; // no credentials
	if( ! authorization.startsWith("Basic ")) {
	    log( "Wrong syntax for basic authentication " + req + " " +
		 authorization);
	    return; // wrong syntax
	}
	
	authorization = authorization.substring(6).trim();
	String unencoded=Base64.base64Decode( authorization );
	
	int colon = unencoded.indexOf(':');
	if (colon < 0) {
	    log( "Wrong syntax for basic authentication " + req + " " +
		 authorization);
	    return;
	}
	
	req.setNote( userNote, unencoded.substring(0, colon));
	req.setNote( passwordNote , unencoded.substring(colon + 1));
    }


    private void formCredentials( Request req  ) {
	ServerSession session=(ServerSession)req.getSession( false );

	if( session == null )
	    return; // not authenticated

	// XXX The attributes are set on the first access.
	// It is possible for a servlet to set the attributes and
	// bypass the security checking - but that's ok, since
	// everything happens inside a web application and all servlets
	// are in the same domain.
	String username=(String)session.getAttribute("j_username");
	String password=(String)session.getAttribute("j_password");

	if( username!=null && password!=null) {
	    req.setNote( userNote , username );
	    req.setNote( passwordNote, password);
	}
    }
}

