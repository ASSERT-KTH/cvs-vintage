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


package org.apache.tomcat.webdav.resources;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import org.apache.tomcat.webdav.util.StringManager;
/**
 * Convenience base class for implementations of the <b>Resources</b>
 * interface.  It is expected that subclasses of this class will be
 * created for each flavor of document root to be supported.
 * <p>
 * Included in the basic support provided by this class is provisions
 * for caching of resources according to configurable policy properties.
 * This will be especially useful for web applications with relatively
 * small amounts of static content (such as a 100% dynamic JSP based
 * application with just a few images), as well as environments where
 * accessing the underlying resources is relatively time consuming
 * (such as a local or remote JAR file).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/11/03 21:27:43 $
 */
public abstract class ResourcesBase
    implements Resources, Runnable { 

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this class with default values.
     */
    public ResourcesBase() {
	super();
    }

    // ----------------------------------------------------- Instance Variables


    /**
     * The interval (in seconds) at which our background task should check
     * for out-of-date cached resources, or zero for no checks.
     */
    protected int checkInterval = 0;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The document root for this component.
     */
    protected String docBase = null;

    /**
     * Should "directory" entries be expanded?
     */
    protected boolean expand = true;

    /**
     * The maximum number of resources to cache.
     */
    protected int maxCount = 0;

    /**
     * The maximum size of resources to be cached.
     */
    protected long maxSize = 0L;

    /**
     * The minimum size of resources to be cached.
     */
    protected long minSize = 0L;

    /**
     * The set of ResourceBean entries for this component,
     * keyed by the normalized context-relative resource URL.
     */
    protected HashMap resourcesCache = new HashMap();

    /**
     * The count of ResourceBean entries for which we have actually
     * cached data.  This can be different from the number of elements
     * in the <code>resourcesCache</code> collection.
     */
    protected int resourcesCount = 0;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
	StringManager.getManager("org.apache.tomcat.webdav.resources");


    /**
     * Has this component been started?
     */
    protected boolean started = false;

    /**
     * The background thread.
     */
    protected Thread thread = null;


    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;


    /**
     * The name to register for the background thread.
     */
    protected String threadName = "ResourcesBase";


    // ------------------------------------------------------------- Properties


    /**
     * Return the resource cache check interval.
     */
    public int getCheckInterval() {

	return (this.checkInterval);

    }


    /**
     * Set the resource cache check interval.
     *
     * @param checkInterval The new check interval
     */
    public void setCheckInterval(int checkInterval) {

	// Perform the property update
	int oldCheckInterval = this.checkInterval;
	this.checkInterval = checkInterval;

	// Start or stop the background thread (if necessary)
	if (started) {
	    if ((oldCheckInterval > 0) && (this.checkInterval <= 0))
		threadStop();
	    else if ((oldCheckInterval <= 0) && (this.checkInterval > 0))
		threadStart();
	}

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

	return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
	this.debug = debug;
    }


    /**
     * Return the document root for this component.
     */
    public String getDocBase() {
	return (this.docBase);
    }


    /**
     * Set the document root for this component.
     *
     * @param docBase The new document root
     *
     * @exception IllegalArgumentException if the specified value is not
     *  supported by this implementation
     * @exception IllegalArgumentException if this would create a
     *  malformed URL
     */
    public void setDocBase(String docBase) {
	this.docBase = docBase.toString();
	if (debug >= 1)
	    log("Setting docBase to '" + this.docBase + "'");
    }


    /**
     * Return the "expand directories" flag.
     */
    public boolean getExpand() {
	return (this.expand);
    }


    /**
     * Set the "expand directories" flag.
     *
     * @param expand The new "expand directories" flag
     */
    public void setExpand(boolean expand) {
	this.expand = expand;
    }



    /**
     * Return the maximum number of resources to cache.
     */
    public int getMaxCount() {
	return (this.maxCount);
    }


    /**
     * Set the maximum number of resources to cache.
     *
     * @param maxCount The new maximum count
     */
    public void setMaxCount(int maxCount) {
	this.maxCount = maxCount;
    }


    /**
     * Return the maximum size of resources to be cached.
     */
    public long getMaxSize() {
	return (this.maxSize);
    }


    /**
     * Set the maximum size of resources to be cached.
     *
     * @param maxSize The new maximum size
     */
    public void setMaxSize(long maxSize) {
	this.maxSize = maxSize;
    }


    /**
     * Return the minimum size of resources to be cached.
     */
    public long getMinSize() {
	return (this.minSize);
    }


    /**
     * Set the minimum size of resources to be cached.
     *
     * @param minSize The new minimum size
     */
    public void setMinSize(long minSize) {
	this.minSize = minSize;
    }


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() {

	// Validate and update our current component state
	if (started)
	    throw new RuntimeException
		(sm.getString("resources.alreadyStarted"));
// 	lifecycle.fireLifecycleEvent(START_EVENT, null);
	started = true;

	// Start the background expiration checking thread (if necessary)
	if (checkInterval > 0)
	    threadStart();

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop()  {

	// Validate and update our current state
	if (!started)
	    throw new RuntimeException
		(sm.getString("resources.notStarted"));

// 	lifecycle.fireLifecycleEvent(STOP_EVENT, null);
	started = false;

	// Stop the background expiration checking thread (if necessary)
	threadStop();

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Should the resource specified by our parameters be cached?
     *
     * @param name Name of the proposed resource
     * @param size Size (in bytes) of the proposed resource
     */
    protected boolean cacheable(String name, long size) {

	if ((size < minSize) || (size > maxSize))
	    return (false);
	else if (resourcesCount >= maxCount)
	    return (false);
	else
	    return (true);

    }


//     /**
//      * Return a File object representing the base directory for the
//      * entire servlet container (i.e. the Engine container if present).
//      */
//     protected File engineBase() {
// 	/*DEBUG*/ try {throw new Exception(); } catch(Exception ex) {ex.printStackTrace();}
// 	return (new File(System.getProperty("catalina.home")));

//     }


    // -------------------- Logging --------------------

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
	System.out.println("ResourceBase: " + message );
    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {
	System.out.println("ResourceBase: " + message );
	throwable.printStackTrace();
    }


    /**
     * Scan our cached resources, looking for cases where the underlying
     * resource has been modified since we cached it.
     */
    protected void threadProcess() {

	// Create a list of the cached resources we know about
	ResourceBean entries[] = new ResourceBean[0];
	synchronized(resourcesCache) {
	    entries =
		(ResourceBean[]) resourcesCache.values().toArray(entries);
	}

	// Check the last modified date on each entry
	for (int i = 0; i < entries.length; i++) {
	    if (entries[i].getLastModified() !=
		getResourceModified(entries[i].getName())) {
		synchronized (resourcesCache) {
		    resourcesCache.remove(entries[i].getName());
		}
	    }
	}

    }


    /**
     * Start the background thread that will periodically check for
     * session timeouts.
     */
    protected void threadStart() {

	if (thread != null)
	    return;

	threadDone = false;
	threadName = "ResourcesBase[" + docBase + "]";
	thread = new Thread(this, threadName);
	thread.setDaemon(true);
	thread.start();

    }


    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    protected void threadStop() {

	if (thread == null)
	    return;

	threadDone = true;
	thread.interrupt();
	try {
	    thread.join();
	} catch (InterruptedException e) {
	    ;
	}

	thread = null;

    }

    // ------------------------------------------------------ Background Thread


    /**
     * The background thread that checks for session timeouts and shutdown.
     */
    public void run() {

	// Loop until the termination semaphore is set
	while (!threadDone) {
	    try {
		Thread.sleep(checkInterval * 1000L);
	    } catch (InterruptedException e) {
		;
	    }
	    threadProcess();
	}

    }


}
