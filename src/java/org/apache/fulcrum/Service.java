package org.apache.fulcrum;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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
 */

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Category;

/**
 * <code>Services</code> are <code>Initables</code> that have a name,
 * and a set of properties.
 *
 * @author <a href="mailto:greg@shwoop.com">Greg Ritter</a>
 * @author <a href="mailto:bmclaugh@algx.net">Brett McLaughlin</a>
 * @author <a href="mailto:burton@apache.org">Kevin Burton</a>
 * @author <a href="mailto:krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:leonardr@collab.net">Leonard Richardson</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: Service.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public interface Service
{
    /**
     * The name of this service.
     */
    public static final String SERVICE_NAME = "Service";

    /**
     * Performs late initialization of an Initable.
     *
     * When your class is being requested from an InitableBroker, it
     * will call isInitialized(), and if it returns false, this method
     * will be invoked.  A typical implementation for classes
     * extending {@link org.apache.fulcrum.BaseService} will look
     * something like the following:
     *
     * <blockquote><code><pre>
     * if (!isInitialized())
     * {
     *     try
     *     {
     *         // Your initialization activities ...
     *         setInit(true);
     *     }
     *     catch (Exception e)
     *     {
     *         throw new InitializationException("Something bad happened " +
     *                                           "during " +
     *                                           Service.SERVICE_NAME +
     *                                           " initialization: " +
     *                                           e.getMessage());
     *     }
     * }
     * </pre></code></blockquote>
     *
     * @exception InitializationException, if initialization of this
     * class was not successful.
     */
    public void init( ) throws InitializationException;

    /**
     * Returns a <code>Service</code> to an uninitialized state.
     *
     * <p>This method must release all resources allocated by the
     * <code>Initable</code> implementation, and resetting its
     * internal state.  You may chose to implement this operation or
     * not. If you support this operation, {@link #isInitialized()}
     * should return false after successful shutdown of the service.
     */
    public void shutdown();

    /**
     * Returns initialization state.
     *
     * @return Whether the service has been initialized.
     */
    public boolean isInitialized();

    /**
     * Provides a Service with a reference to the ServiceBroker that
     * instantiated this object, so that it can ask for its properties
     * and access other Services.
     *
     * @param broker The ServiceBroker that instantiated this object.
     */
    public void setServiceBroker( ServiceBroker broker );

    /**
     * ServiceBroker uses this method to pass a Service its name.
     * Service uses its name to ask the broker for an apropriate set
     * of Properties.
     *
     * @param name The name of this Service.
     */
    public void setName( String name );

    /**
     * Returns the Configuration of this Service.  Every Service has at
     * least one property, which is "classname", containing the name
     * of the class implementing this service.  Note that the service
     * may chose to alter its configuration, therefore they may be
     * different from those returned by ServiceBroker.
     *
     * @return The configuration of this <code>Service</code>.
     */
    public Configuration getConfiguration();

    /**
     * Given a relative paths, gets the real path.
     */
    public String getRealPath(String path);

    /**
     * Gets the default logger for this service.
     */
    public Category getCategory();

    /**
     * Returns text describing the status of this Service instance.
     *
     * @return Text describing service status.
     */
    public String getStatus() throws ServiceException;
}
