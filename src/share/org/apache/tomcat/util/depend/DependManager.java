/*   
 *  Copyright 1997-2004 The Apache Sofware Foundation.
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

package org.apache.tomcat.util.depend;


/** How it works:
    - A DependManager gets loaded with a number of Dependency
    - each Dependency includes a File and a timestamp.
    - If any of the Files is changed after timestamp this DependManager
    will set "expired" to true
    - One check at a time, but without sync
    - if a check was done recently ( delay property ) - assume nothing changed

    It is also possible to do the checks in background, but for big
    servers ( with many contexts) it have scalability problems.
 */
public class DependManager {
    static org.apache.commons.logging.Log logger =
	org.apache.commons.logging.LogFactory.getLog(DependManager.class);

    int delay=4000;
    Dependency deps[];
    int depsCount=0;
    long lastCheck=0;
    boolean checking=false;
    long checkTime=0;
    int checkCount=0;

    private boolean expired=false;

    static final int INITIAL_DEP_SIZE=32;
    
    public DependManager() {
	this( INITIAL_DEP_SIZE );
    }

    public DependManager(int initial_size) {
	deps=new Dependency[initial_size];
    }

    /** Reset the depend manager - all dependencies are reset too.
	This will be called after a reload
    */
    public void reset() {
	expired=false;
	for( int i=0; i<depsCount; i++ ) {
	    Dependency d=deps[i];
	    d.reset();
	}
    }
    
    public void setDelay( int d ) {
	delay=d;
    }

    // statistics
    public long getCheckTime() {
	return checkTime;
    }

    public long getCheckCount() {
	return checkCount;
    }

    private static boolean noWarnBadVM=true;
    public boolean shouldReload() {
	boolean b=shouldReload1();
	if( b!=expired && noWarnBadVM ) {
	    logger.info("BUG ( VM or Tomcat? ) shouldReload returns expired=" + b +
		" and the real value is " + expired);
	    noWarnBadVM=false;
	}
	return expired;
    }

    // Not synchronized - we do that inside
    public boolean shouldReload1() {
	// somebody else is checking, so we don't know yet.
	// assume we're fine - reduce the need for sync
	if( expired ) {
	    if(logger.isDebugEnabled())
		logger.debug( "ShouldReload1 E=" + expired + " C=" + checking);
	}
	if( checking ) return expired;

	synchronized(this) {
	    try {
		// someone else got here and did it before me
		if( expired ) {
		    if(logger.isDebugEnabled())
			logger.debug( "ShouldReload2 E=" + expired + 
				      " C=" + checking);
		}
		if( checking ) return expired;
			
		// did a check in the last N seconds
		long startCheck=System.currentTimeMillis();
		if( startCheck - lastCheck < delay ) {
		    if( expired ) {
			if(logger.isDebugEnabled())
			    logger.debug( "ShouldReload3 E=" + expired + 
					  " C=" + checking);
		    }
		    return expired;
		}
		
		checking=true;

		// it's ok if a new dep is added - this is not
		//exact science ( and no dep can be removed)
		for( int i=0; i<depsCount; i++ ) {
		    Dependency d=deps[i];
		    if( d.checkExpiry() ) {
			// something got modified
			if( logger.isDebugEnabled())
			    logger.debug("Found expired file " +
				d.getOrigin().getName());

			if( ! d.isLocal() ) {
			    // if d is local, it'll just be marked as expired,
			    // the DependManager will not.
			    expired=true;
			}
		    }
		}
		checkTime += lastCheck-startCheck;
		checkCount++;
		lastCheck=startCheck;
	    } finally {
		checking=false;
	    }
	    if( expired ) {
		if(logger.isDebugEnabled()) 
		    logger.debug( "ShouldReload5 E=" + expired + " C=" + checking);
	    }
	    return expired;
	}
    }

    /** Update all times, so next "shouldReload" will happen if
     *  any time changes ( after the specified time )
     */
    public void setLastModified( long time ) {
	for( int i=0; i<depsCount; i++ ) {
	    deps[i].setLastModified( time );
	}
    }

    public void setExpired( boolean e ) {
	if( logger.isDebugEnabled() ) {
	    logger.debug( "SetExpired " + e );
 	}
	for( int i=0; i<depsCount; i++ ) {
	    deps[i].setExpired( e );
	}
    }
    
    public synchronized void addDependency( Dependency dep ) {
	if( depsCount >= deps.length ) {
	    Dependency deps1[]=new Dependency[ deps.length *2 ];
	    System.arraycopy( deps, 0, deps1, 0 , depsCount );
	    deps=deps1;
	}
	deps[depsCount++]= dep ;
	if( logger.isDebugEnabled() )
	    logger.debug( "Added " + dep.getOrigin() + " " + dep.getTarget());
    }

    // -------------------- Private 

    private int debug=0;

    public void setDebug( int i ) {
	debug=i;
    }

}
