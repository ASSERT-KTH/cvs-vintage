/*
 * $Header: /tmp/cvs-vintage/tomcat/proposals/tomcat.next/Attic/ContainerBase.java,v 1.1 2000/01/08 03:54:03 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/08 03:54:03 $
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


// package org.apache.tomcat;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.ServletException;


/**
 * Abstract basic implementation of the <b>Container</b> interface,
 * providing common functionality required by nearly every implementation.
 * Classes extending this base class must implement <code>getInfo()</code>
 * and <code>service()</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/08 03:54:03 $
 */

public abstract class ContainerBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The child Containers belonging to this Container, keyed by name.
     */
    protected Hashtable children = new Hashtable();


    /**
     * The set of Interceptors that have been added to this Container, in
     * reverse order of their addition.
     */
    protected Interceptor interceptors[] = new Interceptor[0];


    /**
     * The Loader implementation with which this Container is associated.
     */
    protected Loader loader = null;


    /**
     * The Logger implementation with which this Container is associated.
     */
    protected Logger logger = null;


    /**
     * The Manager implementation with which this Container is associated.
     */
    protected Manager manager = null;


    /**
     * The human-readable name of this Container.
     */
    protected String name = null;


    /**
     * The parent Container to which this Container is a child.
     */
    protected Container parent = null;


    /**
     * The Realm with which this Container is associated.
     */
    protected Realm realm = null;


    /**
     * The Resources object with which this Container is associated.
     */
    protected Resources resources = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public abstract String getInfo();


    /**
     * Return the Loader with which this Container is associated.  If there is
     * no associated Loader, return the Loader associated with our parent
     * Container (if any); otherwise, return <code>null</code>.
     */
    public Loader getLoader() {

	if (loader != null)
	    return (loader);
	if (parent != null)
	    return (parent.getLoader());
	return (null);

    }


    /**
     * Set the Loader with which this Container is associated.
     *
     * @param loader The newly associated loader
     */
    public void setLoader(Loader loader) {

	this.loader = loader;

    }


    /**
     * Return the Logger with which this Container is associated.  If there is
     * no associated Logger, return the Logger associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Logger getLogger() {

	if (logger != null)
	    return (logger);
	if (parent != null)
	    return (parent.getLogger());
	return (null);

    }


    /**
     * Set the Logger with which this Container is associated.
     *
     * @param logger The newly associated Logger
     */
    public void setLogger(Logger logger) {

	this.logger = logger;

    }


    /**
     * Return the Manager with which this Container is associated.  If there is
     * no associated Manager, return the Manager associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Manager getManager() {

	if (manager != null)
	    return (manager);
	if (parent != null)
	    return (parent.getManager());
	return (null);

    }


    /**
     * Set the Manager with which this Container is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {

	this.manager = manager;

    }


    /**
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     */
    public String getName() {

	return (name);

    }


    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    public void setName(String name) {

	this.name = name;

    }


    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent() {

	return (parent);

    }


    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    public void setParent(Container container) {

	this.parent = parent;

    }


    /**
     * Return the Realm with which this Container is associated.  If there is
     * no associated Realm, return the Realm associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Realm getRealm() {

	if (realm != null)
	    return (realm);
	if (parent != null)
	    return (parent.getRealm());
	return (null);

    }


    /**
     * Set the Realm with which this Container is associated.
     *
     * @param realm The newly associated Realm
     */
    public void setRealm(Realm realm) {

	this.realm = realm;

    }


    /**
     * Return the Resources with which this Container is associated.  If there
     * is no associated Resources object, return the Resources associated with
     * our parent Container (if any); otherwise return <code>null</code>.
     */
    public Resources getResources() {

	if (resources != null)
	    return (resources);
	if (parent != null)
	    return (parent.getResources());
	return (null);

    }


    /**
     * Set the Resources object with which this Container is associated.
     *
     * @param resources The newly associated Resources
     */
    public void setResources(Resources resources) {

	this.resources = resources;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new child Container to those associated with this Container,
     * if supported.  Prior to adding this Container to the set of children,
     * the child's <code>setParent()</code> method must be called, with this
     * Container as an argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Container chooses not
     * to be attached to the specified Container, in which case it is not added
     *
     * @param child New child Container to be added
     *
     * @exception IllegalArgumentException if this exception is thrown by
     *  the <code>setParent()</code> method of the child Container
     * @exception IllegalArgumentException if the new child does not have
     *  a name unique from that of existing children of this Container
     * @exception IllegalStateException if this Container does not support
     *  child Containers
     */
    public void addChild(Container child) {

	synchronized(children) {
	    if (children.get(child.getName()) != null)
		throw new IllegalArgumentException("addChild:  Child name '" +
						   child.getName() +
						   "' is not unique");
	    child.setParent((Container) this);	// May throw IAE
	    children.put(child.getName(), child);
	}

    }


    /**
     * Add a new Interceptor to those associated with this Container, such
     * that the <code>preService()</code> method of this Interceptor will
     * be the first one called prior to servicing the request, and that the
     * <code>postService()</code> method of this Interceptor will be the
     * last one called after servicing the request.  Conceptually, this
     * Interceptor is pushed onto a stack of Interceptors.
     * <p>
     * Prior to adding this Interceptor to the stack, the Interceptor's
     * <code>setContainer()</code> method must be called, with this Container
     * as the argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Interceptor chooses
     * not to be attached to the specified Container, in which case it will
     * not be added to the stack.
     *
     * @param interceptor New Interceptor to be added
     *
     * @exception IllegalArgumentException if this Interceptor refuses to
     *  be attached to this Container
     * @exception IllegalStateException if this Container does not support
     *  Interceptors
     */
    public void addInterceptor(Interceptor interceptor) {

	synchronized (interceptors) {
	    interceptor.setContainer((Container) this);	// May throw IAE
	    Interceptor temp[] = new Interceptor[interceptors.length + 1];
	    temp[0] = interceptor;
	    for (int i = 1; i < temp.length; i++)
		temp[i] = interceptors[i - 1];
	    interceptors = temp;
	}

    }


    /**
     * Return the child Container, associated with this Container, with
     * the specified name (if any); otherwise, return <code>null</code>
     *
     * @param name Name of the child Container to be retrieved
     */
    public Container findChild(String name) {

	return ((Container) children.get(name));

    }


    /**
     * Return the set of children Containers associated with this Container.
     * If this Container has no children, a zero-length array is returned.
     */
    public Container[] findChildren() {

	synchronized (children) {
	    int n = children.size();
	    Container results[] = new Container[n];
	    Enumeration containers = children.elements();
	    for (int i = 0; i < n; i++)
		results[i] = (Container) containers.nextElement();
	    return (results);
	}

    }


    /**
     * Return the set of Interceptors associated with this Container, such
     * that the zero-th element contains the Interceptor most recently added
     * with <code>addInterceptor()</code>.  If this Container has no
     * Interceptors, a zero-length array is returned.
     */
    public Interceptor[] findInterceptors() {

	return (interceptors);

    }


    /**
     * Process the specified Request, thereby producing the specified Response,
     * according to the following algorithm:
     * <ul>
     * <li><b>FIXME:  Describe algorithm concisely</b>
     * </ul>
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred while
     *  processing
     * @exception ServletException if a ServletException was thrown
     *  while processing this request
     */
    public void invoke(Request request, Response response)
	throws IOException, ServletException {

	// Acquire a private copy of the current Interceptor list
	// IMPLEMENTATION NOTE:  Not required if interceptors can only
	// be added at startup time!
	Interceptor list[] = null;
	synchronized (interceptors) {
	    list = new Interceptor[interceptors.length];
	    for (int i = 0; i < interceptors.length; i++)
		list[i] = interceptors[i];
	}

	// Call the preService() methods of all defined Interceptors
	int last = -1;
	for (int i = 0; i < list.length; i++) {
	    last = i;
	    if (!list[i].preService(request, response))
		break;
	}

	// Call the service() method of this Container
	service(request, response);

	// Call the postService() methods of all defined Interceptors
	for (int i = list.length - 1; i >= 0; i--) {
	    if (i > last)
		continue;
	    list[i].postService(request, response);
	}


    }


    /**
     * Perform the Container-specific processing supported by this particular
     * Container, independent of any Interceptors that have been added.  This
     * is a convenience method for use in developing implementations -- most
     * external users of this Container should call <code>invoke()</code>.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     */
    public abstract void service(Request request, Response response)
        throws IOException, ServletException;


}
