/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/Attic/Container.java,v 1.1 2000/01/09 03:20:02 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2000/01/09 03:20:02 $
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


package org.apache.tomcat;


import java.io.IOException;
import javax.servlet.ServletException;


/**
 * A <b>Container</b> is an object that can execute requests received from
 * a client, and return responses based on those requests.  Containers
 * support an <b>Interceptor</b>-based architecture for injecting customized
 * request functionality at configuration time.
 * <p>
 * Containers will exist at several conceptual levels within Tomcat.  The
 * following examples represent common cases:
 * <ul>
 * <li><b>Engine</b> - Representation of the entire Tomcat servlet engine,
 *     most likely containing one or more subcontainers that are either Host
 *     or Context implementations, or other custom groups.
 * <li><b>Host</b> - Representation of a virtual host containing a number
 *     of Contexts.  This is useful when you wish to interpose one or more
 *     Interceptors on all requests processed by a particular virtual host.
 * <li><b>Context</b> - Representation of a single ServletContext, which will
 *     typically contain one or more Wrappers for the supported servlets.
 * <li><b>Wrapper</b> - Representation of an individual servlet definition
 *     (which may support multiple servlet instances if the servlet itself
 *     implements SingleThreadModel).
 * </ul>
 * A given deployment of Tomcat need not include Containers at all of the
 * levels described above.  For example, an administration application
 * embedded within a network device (such as a router) might only contain
 * a single Context and a few Wrappers, or even a single Wrapper if the
 * application is relatively small.  Therefore, Container implementations
 * need to be designed so that they will operate correctly in the absence
 * of parent Containers in a given deployment.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2000/01/09 03:20:02 $
 */

public interface Container {


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the Loader with which this Container is associated.  If there is
     * no associated Loader, return the Loader associated with our parent
     * Container (if any); otherwise, return <code>null</code>.
     */
    public Loader getLoader();


    /**
     * Set the Loader with which this Container is associated.
     *
     * @param loader The newly associated loader
     */
    public void setLoader(Loader loader);


    /**
     * Return the Logger with which this Container is associated.  If there is
     * no associated Logger, return the Logger associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Logger getLogger();


    /**
     * Set the Logger with which this Container is associated.
     *
     * @param logger The newly associated Logger
     */
    public void setLogger(Logger logger);


    /**
     * Return the Manager with which this Container is associated.  If there is
     * no associated Manager, return the Manager associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Manager getManager();


    /**
     * Set the Manager with which this Container is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager);


    /**
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     */
    public String getName();


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
    public void setName(String name);


    /**
     * Return the Container for which this Container is a child, if there is
     * one.  If there is no defined parent, return <code>null</code>.
     */
    public Container getParent();


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
    public void setParent(Container container);


    /**
     * Return the Realm with which this Container is associated.  If there is
     * no associated Realm, return the Realm associated with our parent
     * Container (if any); otherwise return <code>null</code>.
     */
    public Realm getRealm();


    /**
     * Set the Realm with which this Container is associated.
     *
     * @param realm The newly associated Realm
     */
    public void setRealm(Realm realm);


    /**
     * Return the Resources with which this Container is associated.  If there
     * is no associated Resources object, return the Resources associated with
     * our parent Container (if any); otherwise return <code>null</code>.
     */
    public Resources getResources();


    /**
     * Set the Resources object with which this Container is associated.
     *
     * @param resources The newly associated Resources
     */
    public void setResources(Resources resources);


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
    public void addChild(Container child);


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
     * @exception IllegalArgumentException if this Container refuses to
     *  accept the specified Interceptor
     * @exception IllegalArgumentException if this Interceptor refuses to
     *  be attached to this Container
     * @exception IllegalStateException if this Container does not support
     *  Interceptors
     */
    public void addInterceptor(Interceptor interceptor);


    /**
     * Return the child Container, associated with this Container, with
     * the specified name (if any); otherwise, return <code>null</code>
     *
     * @param name Name of the child Container to be retrieved
     */
    public Container findChild(String name);


    /**
     * Return the set of children Containers associated with this Container.
     * If this Container has no children, a zero-length array is returned.
     */
    public Container[] findChildren();


    /**
     * Return the set of Interceptors associated with this Container, such
     * that the zero-th element contains the Interceptor most recently added
     * with <code>addInterceptor()</code>.  If this Container has no
     * Interceptors, a zero-length array is returned.
     */
    public Interceptor[] findInterceptors();


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
	throws IOException, ServletException;


    /**
     * Perform the Container-specific processing supported by this particular
     * Container, independent of any Interceptors that have been added.  This
     * is a convenience method for use in developing implementations -- most
     * external users of this Container should call <code>invoke()</code>.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     */
    public void service(Request request, Response response)
        throws IOException, ServletException;


}
