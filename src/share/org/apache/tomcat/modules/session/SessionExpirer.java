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

package org.apache.tomcat.modules.session;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.ServerSession;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.buf.TimeStamp;
import org.apache.tomcat.util.threads.Expirer;


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
