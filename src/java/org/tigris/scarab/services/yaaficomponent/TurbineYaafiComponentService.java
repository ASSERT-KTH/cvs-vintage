package org.tigris.scarab.services.yaaficomponent;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.yaafi.framework.container.ServiceContainer;
import org.apache.fulcrum.yaafi.framework.factory.ServiceManagerFactory;
import org.apache.turbine.Turbine;


/**
 * An implementation of YaafiComponentService which loads all the
 * components given in the TurbineResources.properties File.
 * <p>
 * For component which require the location of the application or
 * context root, there are two ways to get it.
 * <ol>
 * <li>
 *   Implement the Contextualizable interface.  The full path to the
 *   correct OS directory can be found under the ComponentAppRoot key.
 * </li>
 * <li>
 *   The system property "applicationRoot" is also set to the full path
 *   of the correct OS directory.
 * </li>
 * </ol>
 * If you want to initialize a component by using the YaafiComponentService, you
 * must activate Torque at initialization time by specifying
 *
 * services.YaafiComponentService.lookup = org.apache.torque.Torque
 *
 * in your TurbineResources.properties.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 * @author <a href="mailto:hps@intermeta.de">Henning P. Schmiedehausen</a>
 * @version $Id: TurbineYaafiComponentService.java,v 1.2 2004/11/04 20:35:28 dep4b Exp $
 */
public class TurbineYaafiComponentService
        extends BaseService
        implements YaafiComponentService
{
    /** Logging */
    private static Log log = LogFactory.getLog(TurbineYaafiComponentService.class);

    /** YAFFI container */
    private ServiceContainer container = null;

    // -------------------------------------------------------------
    // Service initialization
    // -------------------------------------------------------------

    /**
	 * Load all configured components and initialize them. This is a zero parameter variant which
	 * queries the Turbine Servlet for its config.
	 *
	 * @throws InitializationException Something went wrong in the init stage
	 */
    public void init( Object data )
    	throws InitializationException
    {
        try
        {
            init();
            setInit(true);
        }
        catch (Exception e)
        {
            log.error("Exception caught initialising service: ", e);
            throw new InitializationException("init failed", e);
        }
    }

    /**
	 * Shuts the Component Service down, calls dispose on the components that implement this
	 * interface
	 *
	 */
    public void shutdown()
    {
        dispose();
        setInit(false);
    }

    // -------------------------------------------------------------
    // Avalon lifecycle interfaces
    // -------------------------------------------------------------

    /**
	 * Initializes the container
	 *
	 * @throws Exception generic exception
	 */
    public void init() throws InitializationException
    {
		org.apache.commons.configuration.Configuration conf = getConfiguration();

		// determine the home directory

        String homePath = Turbine.getApplicationRoot();
        File home = new File(homePath);

        // determine the location of the role configuraton file

        String roleConfigurationFileName = conf.getString(
                YaafiComponentService.COMPONENT_ROLE_KEYS,
                YaafiComponentService.COMPONENT_ROLE_VALUE
            );

        // determine the location of component configuration file

        String componentConfigurationFileName = conf.getString(
                YaafiComponentService.COMPONENT_CONFIG_KEY,
                YaafiComponentService.COMPONENT_CONFIG_VALUE
            );

        // determine the location of parameters file

        String parametersFileName = conf.getString(
                YaafiComponentService.COMPONENT_PARAMETERS_KEY,
                YaafiComponentService.COMPONENT_PARAMETERS_VALUE
            );

        // build up a default context

        DefaultContext context = new DefaultContext();
        context.put(COMPONENT_APP_ROOT, homePath);
        context.put(URN_AVALON_HOME, new File( homePath ) );
        context.put(URN_AVALON_TEMP, new File( homePath ) );

        try
        {
            this.container = ServiceManagerFactory.create(
                new Log4JLogger( org.apache.log4j.Logger.getLogger( TurbineYaafiComponentService.class ) ),
                roleConfigurationFileName,
                componentConfigurationFileName,
                parametersFileName,
                context
                );
            
        }
        catch (Throwable t)
        {
            throw new InitializationException(
                    "Failed to initialize YaafiComponentService",t); //EXCEPTION
        }
        
        List lookupComponents = conf.getList(COMPONENT_LOOKUP_KEY,
                new ArrayList());        
        
        for (Iterator it = lookupComponents.iterator(); it.hasNext();)
        {
            String component = (String) it.next();
            try
            {
                Object c = lookup(component);
                log.info("Lookup for Component " + c + " successful");
                release(c);
            }
            catch (Exception e)
            {
                log.error("Lookup for Component " + component + " failed!");
            }
        }        
        setInit(true);
    }

    /**
	 * Disposes of the container and releases resources
	 */
    public void dispose()
    {
        if (this.container != null)
        {
            this.container.dispose();
            this.container = null;
        }
    }

    /**
	 * Returns an instance of the named component
	 *
	 * @param roleName Name of the role the component fills.
	 * @return an instance of the named component
	 * @throws Exception generic exception
	 */
    public Object lookup(String path) throws Exception
    {
        return this.container.lookup(path);
    }

    /**
	 * Releases the component
	 *
	 * @param source. The path to the handler for this component For example, if the object is a
	 *            java.sql.Connection object sourced from the "/turbine-merlin/datasource"
	 *            component, the call would be :- release("/turbine-merlin/datasource", conn);
	 * @param component the component to release
	 */
    public void release(Object component)
    {
        this.container.release( component );
    }
}
