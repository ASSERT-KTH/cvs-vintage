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
package org.apache.tomcat.modules.session;

import java.io.*;
import java.util.Random;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.compat.*;
import org.apache.tomcat.util.threads.*;
import org.apache.tomcat.core.*;
import java.util.*;
import org.apache.tomcat.util.collections.SimplePool;
import org.apache.tomcat.util.log.*;
import org.apache.tomcat.util.buf.*;
import java.security.*;


/**
 * This module handles session expiration ( independent of the
 * session storage and reloading ). 
 *
 * For scalability it uses a single thread per module ( you can
 * use per/context interceptors instead of global to change that )
 * It is derived from SimpleSessionStrore, refactored to keep the
 * code clear.
 *
 * @author costin@eng.sun.com
 * @author hans@gefionsoftware.com
 * @author pfrieden@dChain.com
 * @author Shai Fultheim [shai@brm.com]
 */
public final class SessionExpirer  extends BaseInterceptor {
    int manager_note;

    int checkInterval = 60;
    Expirer expirer=null;
    
    public SessionExpirer() {
    }

    // -------------------- Configuration properties --------------------

    /**
     * Set the check interval (in seconds) for this Manager.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval( int secs ) {
	checkInterval=secs;
    }

    public Expirer getExpirer() {
	return expirer;
    }

    // -------------------- Tomcat request events --------------------

    // XXX use contextInit/shutdown for local modules
    
    public void engineStart( ContextManager cm ) throws TomcatException {
	expirer=new Expirer();
	expirer.setCheckInterval( checkInterval );
	expirer.setExpireCallback( new SessionExpireCallback(this, debug) );
	expirer.start();
    }

    public void engineStop( ContextManager cm ) throws TomcatException {
	expirer.stop();
    }

    public int sessionState( Request req, ServerSession session, int state ) {
	TimeStamp ts=session.getTimeStamp();

	if( state==ServerSession.STATE_NEW ) {
	    if( debug > 0 ) log("Registering new session for expiry checks");
	    ts.setNew(true);
	    ts.setValid(true);
	    
	    ts.setCreationTime(System.currentTimeMillis());
	    Context ctx=session.getContext();
	    ts.setMaxInactiveInterval( ctx.getSessionTimeOut() * 60000 );

	    session.getTimeStamp().setParent( session );

	    expirer.addManagedObject( ts );
	}  else if( state==ServerSession.STATE_EXPIRED ) {
	    if( debug > 0 ) log("Removing expired session from expiry checks"); 
	    expirer.removeManagedObject( ts );
	}
	return state;
    }

    // -------------------- Internal methods --------------------

    // Handle expire events
    static class SessionExpireCallback implements Expirer.ExpireCallback {
	SessionExpirer se;
	int debug;
	
	SessionExpireCallback( SessionExpirer se, int debug ) {
	    this.se=se;
	    this.debug=debug;
	}
	
	public void expired(TimeStamp o ) {
	    ServerSession sses=(ServerSession)o.getParent();
	    if( debug > 0  ) {
		se.log( "Session expired " + sses);
	    }
	    sses.setState( ServerSession.STATE_EXPIRED );
	    // After expiring it, we clean up.
	    if( debug > 0 ) se.log( "Recycling " + sses);
	    sses.recycle();
	    sses.setState( ServerSession.STATE_INVALID );
	}
    }
}
