/*
 * Copyright (c) 1997-1999 The Java Apache Project.  All rights reserved.
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
 * 3. All advertising materials mentioning features or use of this
 *    software must display the following acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *
 * 4. The names "Apache JServ", "Apache JServ Servlet Engine" and 
 *    "Java Apache Project" must not be used to endorse or promote products 
 *    derived from this software without prior written permission.
 *
 * 5. Products derived from this software may not be called "Apache JServ"
 *    nor may "Apache" nor "Apache JServ" appear in their names without 
 *    prior written permission of the Java Apache Project.
 *
 * 6. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the Java Apache 
 *    Project for use in the Apache JServ servlet engine project
 *    <http://java.apache.org/>."
 *    
 * THIS SOFTWARE IS PROVIDED BY THE JAVA APACHE PROJECT "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JAVA APACHE PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Java Apache Group. For more information
 * on the Java Apache Project and the Apache JServ Servlet Engine project,
 * please see <http://java.apache.org/>.
 *
 */

package org.apache.tomcat.util.depend;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import java.security.*;

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
