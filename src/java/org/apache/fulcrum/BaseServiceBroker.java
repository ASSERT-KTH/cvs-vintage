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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Category;
import org.apache.log4j.helpers.NullEnumeration;

/**
 * A generic implementation of a <code>ServiceBroker</code> which
 * provides:
 *
 * <ul>
 * <li>Maintaining service name to class name mapping, allowing
 * plugable service implementations.</li>
 * <li>Providing <code>Services</code> with a configuration based on
 * system wide configuration mechanism.</li>
 * </ul>
 *
 * @author <a href="mailto:burton@apache.org">Kevin Burton</a>
 * @author <a href="mailto:krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @version $Id: BaseServiceBroker.java,v 1.2 2004/10/28 18:37:33 dep4b Exp $
 */
public abstract class BaseServiceBroker implements ServiceBroker
{
    /**
     * Mapping of Service names to class names.
     */
    protected Configuration mapping = (Configuration) new BaseConfiguration();

    /**
     * A repository of Service instances.
     */
    protected Hashtable services = new Hashtable();

    /**
     * Configuration for the services broker.
     * The configuration should be set by the application
     * in which the services framework is running.
     */
    protected Configuration configuration;

    /**
     * A prefix for <code>Service</code> properties in
     * TurbineResource.properties.
     */
    public static final String SERVICE_PREFIX = "services.";

    /**
     * A <code>Service</code> property determining its implementing
     * class name .
     */
    public static final String CLASSNAME_SUFFIX = ".classname";

    /**
     * True if logging should go throught
     * LoggingService, false if not.
     */
    protected boolean loggingConfigured;

    /**
     * These are objects that the parent application
     * can provide so that application specific
     * services have a mechanism to retrieve specialized
     * information. For example, in Turbine there are services
     * that require the RunData object: these services can
     * retrieve the RunData object that Turbine has placed
     * in the service manager. This alleviates us of
     * the requirement of having init(Object) all
     * together.
     */
    protected Hashtable serviceObjects = new Hashtable();

    /**
     * This is the log4j category that the parent application
     * has provided for logging. If a Category is not set
     * than all messages are sent to stout.
     */
    protected Category category = Category.getInstance(getClass().getName());

    /**
     * Application root path as set by the
     * parent application.
     */
    protected String applicationRoot;

    /**
     * Default constructor, protected as to only be useable by subclasses.
     *
     * This constructor does nothing.
     */
    protected BaseServiceBroker()
    {
    }

    /**
     * Determine whether log4j has already been configured.
     *
     * @return boolean Whether log4j is configured.
     */
    protected boolean isLoggingConfigured()
    {
        // This is a note from Ceki, taken from a message on the log4j
        // user list:
        //
        // Having defined categories does not necessarily mean
        // configuration. Remember that most categories are created
        // outside the configuration file. What you want to check for
        // is the existence of appenders. The correct procedure is to
        // first check for appenders in the root category and if that
        // returns no appenders to check in other categories.

        Enumeration appenders  = Category.getRoot().getAllAppenders();

        if (!(appenders instanceof NullEnumeration))
        {
            return true;
        }
        else
        {
            Enumeration cats =  Category.getCurrentCategories();
            while(cats.hasMoreElements())
            {
                Category c = (Category) cats.nextElement();
                if (!(c.getAllAppenders() instanceof NullEnumeration))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Set the configuration object for the services broker.
     * This is the configuration that contains information
     * about all services in the care of this service
     * manager.
     *
     * @param configuration Broker configuration.
     */
    public void setConfiguration(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * Get the configuration for this service manager.
     *
     * @return Broker configuration.
     */
    public Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Get the log4j Category used for logging. Used now
     * internally by services that have yet to implement the
     * Category c = Category.getInstance(getClass().getName());
     * methodology.
     *
     * @return Category
     */
    public Category getCategory()
    {
        return category;
    }

    /**
     * Initialize this service manager.
     */
    public void init() throws InitializationException
    {
        // Check:
        //
        // 1. The configuration has been set.
        // 2. Logging category has been set.
        // 3. Make sure the application root has been set.

        // FIXME: Make some service framework exceptions to throw in
        // the event these requirements aren't satisfied.

        // Check to see if a parent application has already
        // configured the logging.
        loggingConfigured = isLoggingConfigured();

        // Create the mapping between service names
        // and their classes.
        initMapping();

        // Start services that have their 'earlyInit'
        // property set to 'true'.
        initServices(false);
    }

    /**
     * Set an application specific service object
     * that can be used by application specific
     * services.
     *
     * @param String name of service object
     * @param Object value of service object
     */
    public void setServiceObject(String name, Object value)
    {
        serviceObjects.put(name, value);
    }

    /**
     * Get an application specific service object.
     *
     * @return Object application specific service object
     */
    public Object getServiceObject(String name)
    {
        return serviceObjects.get(name);
    }

    /**
     * Creates a mapping between Service names and class names.
     *
     * The mapping is built according to settings present in
     * TurbineResources.properties.  The entries should have the
     * following form:
     *
     * <pre>
     * services.MyService.classname=com.mycompany.MyServiceImpl
     * services.MyOtherService.classname=com.mycompany.MyOtherServiceImpl
     * </pre>
     *
     * <br>
     *
     * Generic ServiceBroker provides no Services.
     */
    protected void initMapping()
    {
        /*
         * These keys returned in an order that corresponds
         * to the order the services are listed in
         * the TR.props.
         *
         * When the mapping is created we use a Configuration
         * object to ensure that the we retain the order
         * in which the order the keys are returned.
         *
         * There's no point in retrieving an ordered set
         * of keys if they aren't kept in order :-)
         */
        Iterator keys = configuration.getKeys();

        while(keys.hasNext())
        {
            String key = (String)keys.next();
            String [] keyParts = StringUtils.split(key, ".");

            if ((keyParts.length == 3) 
                && (keyParts[0] + ".").equals(SERVICE_PREFIX) 
                && ("." + keyParts[2]).equals(CLASSNAME_SUFFIX))
            {
                String serviceKey = keyParts[1];
                notice ("Added Mapping for Service: " + serviceKey);

                if (! mapping.containsKey(serviceKey))
                {
                    mapping.setProperty(serviceKey,
                                        configuration.getString(key));
                }
            }
        }
    }

    /**
     * Determines whether a service is registered in the configured
     * <code>TurbineResources.properties</code>.
     *
     * @param serviceName The name of the service whose existance to check.
     * @return Registration predicate for the desired services.
     */
    public boolean isRegistered(String serviceName)
    {
        return (services.get(serviceName) != null);
    }

    /**
     * Returns an Iterator over all known service names.
     *
     * @return An Iterator of service names.
     */
    public Iterator getServiceNames()
    {
        return mapping.getKeys();
    }

    /**
     * Returns an Iterator over all known service names beginning with
     * the provided prefix.
     *
     * @param prefix The prefix against which to test.
     * @return An Iterator of service names which match the prefix.
     */
    public Iterator getServiceNames(String prefix)
    {
        return mapping.getKeys(prefix);
    }

    /**
     * Performs early initialization of the specified
     * <code>Service</code> implementation.
     *
     * @param name The name of the service (generally the
     * <code>SERVICE_NAME</code> constant of the service's interface
     * definition).
     * @param data An object to use for initialization activities.
     * @exception InitializationException Initialization of this
     * service was not successful.
     * @see org.apache.fulcrum.Service#init()
     */
    public synchronized void initService(String name)
        throws InitializationException
    {
        // Calling getServiceInstance(name) assures that the Service
        // implementation has its name and broker reference set before
        // initialization.
        Service instance = getServiceInstance(name);

        if (!instance.isInitialized())
        {
            // this call might result in an indirect recursion
            instance.init();
        }
    }

    /**
     * Performs early initialization of all services.  Failed early
     * initialization of a Service may be non-fatal to the system,
     * thus any exceptions are logged and the initialization process
     * continues.
     */
    public void initServices()
    {
        try
        {
            initServices(false);
        }
        catch (InstantiationException notThrown)
        {
        }
        catch (InitializationException notThrown)
        {
        }
    }

    /**
     * Performs early initialization of all services. You can decide
     * to handle failed initizalizations if you wish, but then
     * after one service fails, the other will not have the chance
     * to initialize.
     *
     * @param report <code>true</code> if you want exceptions thrown.
     */
    public void initServices(boolean report)
        throws InstantiationException, InitializationException
    {
        Iterator names = getServiceNames();
        if (report)
        {
            // Throw exceptions
            while (names.hasNext())
            {
                doInitService((String) names.next());
            }
        }
        else
        {
            // Eat exceptions
            while (names.hasNext())
            {
                try
                {
                    doInitService((String) names.next());
                }
                // In case of an exception, file an error message; the
                // system may be still functional, though.
                catch (InstantiationException e)
                {
                    error(e);
                }
                catch (InitializationException e)
                {
                    error(e);
                }
            }
        }
        notice("Finished initializing all services!");
    }

    /**
     * Internal utility method for use in {@link #initServices(boolean)}
     * to prevent duplication of code.
     */
    private void doInitService(String name)
        throws InstantiationException, InitializationException
    {
        // Only start up services that have their earlyInit flag set.
        if (getConfiguration(name).getBoolean("earlyInit", false))
        {
            notice("Start Initializing service (early): " + name);
            initService(name);
            notice("Finish Initializing service (early): " + name);
        }
    }

    /**
     * Shuts down a <code>Service</code>, releasing resources
     * allocated by an <code>Service</code>, and returns it to its
     * initial (uninitialized) state.
     *
     * @param name The name of the <code>Service</code> to be
     * uninitialized.
     * @see org.apache.fulcrum.Service#shutdown()
     */
    public synchronized void shutdownService(String name)
    {
        try
        {
            Service service = getServiceInstance(name);
            if (service != null && service.isInitialized())
            {
                service.shutdown();
                if (service.isInitialized() && service instanceof BaseService)
                {
                    // BaseService::shutdown() does this by default,
                    // but could've been overriden poorly.
                    ((BaseService) service).setInit(false);
                }
            }
        }
        catch (InstantiationException e)
        {
            // Assuming harmless -- log the error and continue.
            error(new ServiceException("Shutdown of a nonexistent Service '" +
                                       name + "' was requested", e));
        }
    }

    /**
     * Shuts down all Turbine services, releasing allocated resources and
     * returning them to their initial (uninitialized) state.
     */
    public void shutdownServices()
    {
        notice("Shutting down all services!");

        Iterator serviceNames = getServiceNames();
        String serviceName = null;

        /*
         * Now we want to reverse the order of
         * this list. This functionality should be added to
         * the ExtendedProperties in the commons but
         * this will fix the problem for now.
         */

        ArrayList reverseServicesList = new ArrayList();

        while (serviceNames.hasNext())
        {
            serviceName = (String) serviceNames.next();
            reverseServicesList.add(0, serviceName);
        }

        serviceNames = reverseServicesList.iterator();

        while (serviceNames.hasNext())
        {
            serviceName = (String) serviceNames.next();
            notice("Shutting down service: " + serviceName);
            shutdownService(serviceName);
        }
    }

    /**
     * Returns an instance of requested Service.
     *
     * @param name The name of the Service requested.
     * @return An instance of requested Service.
     * @exception InstantiationException, if the service is unknown or
     * can't be initialized.
     */
    public Service getService(String name) throws InstantiationException
    {
        Service service;
        try
        {
            service = getServiceInstance(name);
            if (!service.isInitialized())
            {
                synchronized (service.getClass())
                {
                    if (!service.isInitialized())
                    {
                        notice("Start Initializing service (late): " + name);
                        service.init();
                        notice("Finish Initializing service (late): " + name);
                    }
                }
            }
            if (!service.isInitialized())
            {
                // This exception will be caught & rethrown by this
                // very method.  isInitialized() returning false
                // indicates some initialization issue, which in turn
                // prevents the ServiceBroker from passing a
                // reference to a working instance of the service to
                // the client.
                throw new InitializationException(
                    "init() failed to initialize service " + name);
            }
            return service;
        }
        catch (InitializationException e)
        {
            throw new InstantiationException(
                "Service " + name + " failed to initialize", e);
        }
    }

    /**
     * <p>Retrieves an instance of a Service without triggering late
     * initialization.</p>
     *
     * <p>Early initialization of a Service can require access to
     * Service properties.  The Service must have its name and
     * ServiceBroker set by then (the class must be instantiated with
     * {@link #getServiceInstance(String)}, and {@link
     * org.apache.fulcrum.Service#setServiceBroker()} and {@link
     * org.apache.fulcrum.Service#setName()} must've been called).</p>
     *
     * @param name The name of the service requested.
     * @exception InstantiationException The service is unknown or
     * can't be initialized.
     */
    protected Service getServiceInstance(String name)
        throws InstantiationException
    {
        Service service = (Service) services.get(name);

        if (service == null)
        {
            String className = mapping.getString(name, null);

            if (className == null || className.length() == 0)
            {
                throw new InstantiationException(
                    "ServiceBroker: Unknown Service '" + name + "' requested");
            }
            try
            {
                service = (Service) services.get(className);

                if (service == null)
                {
                    try
                    {
                        service = (Service) Class.forName(className).newInstance();
                    }
                    // those two errors must be passed to the VM
                    catch (ThreadDeath t)
                    {
                        throw t;
                    }
                    catch (OutOfMemoryError t)
                    {
                        throw t;
                    }
                    catch (Throwable t)
                    {
                        // Used to indicate error condition.
                        String msg = null;

                        if (t instanceof NoClassDefFoundError)
                        {
                            msg = "A class referenced by " + className +
                                " is unavailable. Check your jars and classes.";
                        }
                        else if (t instanceof ClassNotFoundException)
                        {
                            msg = "Class " + className +
                                " is unavailable. Check your jars and classes.";
                        }
                        else if (t instanceof ClassCastException)
                        {
                            msg = "Class " + className +
                                " doesn't implement the Service interface";
                        }
                        else
                        {
                            msg = "Failed to instantiate " + className;
                        }

                        throw new InstantiationException(msg, t);
                    }
                }
            }
            catch (ClassCastException e)
            {
                throw new InstantiationException("ServiceBroker: Class "
                    + className + " does not implement Service interface.", e);
            }
            catch (InstantiationException e)
            {
                throw new InstantiationException(
                    "Failed to instantiate service " + name, e);
            }
            service.setServiceBroker(this);
            service.setName(name);
            services.put(name, service);
        }

        return service;
    }

    /**
     * Returns the configuration for the specified service.
     *
     * @param name The name of the service.
     */
    public Configuration getConfiguration( String name )
    {
        return configuration.subset(SERVICE_PREFIX + name);
    }

    /**
     * Output a diagnostic notice.
     *
     * This method is used by the service framework classes for producing
     * tracing mesages that might be useful for debugging.
     *
     * <p>Standard Turbine logging facilities are used.
     *
     * @param msg the message to print.
     */
    public void notice(String msg)
    {
        if (loggingConfigured)
        {
            category.info(msg);
        }
        else
        {
            System.out.println("NOTICE: " + msg);
        }
    }

    /**
     * Output an error message.
     *
     * This method is used by the service framework classes for displaying
     * stacktraces of any exceptions that might be caught during processing.
     *
     * <p>Standard Turbine logging facilities are used.
     *
     * @param msg the message to print.
     */
    public void error(Throwable t)
    {
        if (loggingConfigured)
        {
            category.info(t);
            category.info(stackTrace(t));
        }
        else
        {
            System.out.println("ERROR: " + t.getMessage());
            t.printStackTrace();
        }
    }

    /**
     * Returns the output of printStackTrace as a String.
     *
     * @param e The source to extract a stack trace from.
     * @return The extracted stack trace.
     */
    public String stackTrace(Throwable e)
    {
        String trace = null;
        try
        {
            // And show the Error Screen.
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            e.printStackTrace( new PrintWriter(buf, true) );
            trace = buf.toString();
        }
        catch (Exception f)
        {
            // Do nothing.
        }
        return trace;
    }

    /**
     * Set the application root.
     *
     * @param String application root
     */
    public void setApplicationRoot(String applicationRoot)
    {
        this.applicationRoot = applicationRoot;
    }

    /**
     * Get the application root as set by
     * the parent application.
     *
     * @return String application root
     */
    public String getApplicationRoot()
    {
        return applicationRoot;
    }

    public String getRealPath(String path)
    {
        return new File(getApplicationRoot(), path).getAbsolutePath();
    }
}
