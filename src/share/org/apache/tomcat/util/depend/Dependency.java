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

import java.io.File;

/** Represents a dependency between a real file and a server object.
 *  The servler object has a timestamp, and it is compared with the
 *  file lastModified time to detect changes.
 * 
 *  The DependManager will do the checkings ( with the minimal possible
 *  overhead ). 
 */
public final class Dependency {
    
    private File origin;
    private long lastModified;
    private Object target;
    private boolean localDep=false;
    private boolean expired=false;
    
    public Dependency() {
    }

    /**
     * The time when the server-side object has been loaded/modified.
     * 
     * @param v  modification time 
     */
    public void setLastModified(long  v) {
	this.lastModified = v;
    }

    public long getLastModified() {
	return lastModified;
    }

    public void reset() {
	expired=false;
	lastModified=origin.lastModified();
    }
    
    /**
     * If set, the dependency will be "local", i.e. will be marked as
     * expired but the DependManager will not triger an expire at a higher
     * level ( example: if a JSP changes, no need to reload the context )
     */
    public void setLocal(boolean b) {
	localDep=b;
    }

    public boolean isLocal() {
	return localDep;
    }

    /** Mark this dependency as expired.
     */
    public void setExpired( boolean b ) {
	expired=b;
    }

    public boolean isExpired() {
	return expired;
    }
    
    /**
     * The file on which the server-side object depends or has been
     * loaded from.
     * 
     * @param v  Value to assign to origin.
     */
    public void setOrigin(File  v) {
	this.origin = v;
    }
    
    public File getOrigin() {
	return origin;
    }
    
    
    /**
     * Server-side object that is checked for dependency on the file.
     *
     * @param v  Value to assign to target.
     */
    public void setTarget(Object  v) {
	this.target = v;
    }
    
    public Object getTarget() {
	return target;
    }
    
    public String toString() {
	return "Dep(O=" + origin + " LM=" + lastModified +
	    " OLM=" + ((origin!=null) ? origin.lastModified() :0) +
	    " E=" + expired + ") ";
    }
    
    // -------------------- methods --------------------

    /** Check if the origin changed since target's was lastModified.
     *  This will be called periodically by DependManager or can
     *  be called to force a check for this particular dependency.
     */
    public boolean checkExpiry() {
	if( lastModified < origin.lastModified() ) {
	    expired=true;
	    return true;
	}
	return false;
    }
}
