/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/shell/Attic/Shutdown.java,v 1.1 2000/02/11 00:22:37 costin Exp $
 * $Revision: 1.1 $
 * $Date: 2000/02/11 00:22:37 $
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


package org.apache.tomcat.shell;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

import org.apache.tomcat.server.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.shell.deployment.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

/**
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public class Shutdown {

    private StringManager sm =
        StringManager.getManager(Constants.Package);

    // add war protocol handler to system properties

    static {
        String warPackage = Constants.Protocol.WAR.PACKAGE;
        String protocolKey = Constants.Protocol.WAR.SYSTEM_PROPERTY;
        String protocolHandlers =
            System.getProperties().getProperty(protocolKey);
        System.getProperties().put(protocolKey,
            (protocolHandlers == null) ?
            warPackage : protocolHandlers + "|" + warPackage);
    };

    /**
     *
     */

    public Shutdown(String[] args)
    throws StartupException {
        Config config = getConfig(args);

        if (config == null) {
            return;
        }

	String portStr = null;

	if (config.isArg(Constants.Arg.AdminPort)) {
	    portStr = (String)config.getConfig().getAttribute(
                Constants.Attribute.AdminPort);
	} else {
	    Properties props = getProperties();

	    portStr = props.getProperty(Constants.Property.AdminPort);
	}

	Registry registry = getRegistry(portStr);
	String[] services = getServices(registry);
	int counter = 0;

	for (int i = 0; i < services.length; i++) {
	    String service = services[i];
 
	    if (service.startsWith(Constants.Registry.Service + ":")) {
	        boolean exit =
		    (++counter == services.length) ? true : false;

	        try {
		    ((Admin)(registry.lookup(service))).stopService(exit);
		    registry.unbind(service);
		} catch (Exception e) {
		    System.out.println(sm.getString("shutdown.service.e"));
		    System.out.println(e.toString());

		    e.printStackTrace();
		}
	    }
	}

	services = getServices(registry);
	
	if (services.length == 0) {
	    File f = null;

	    try {
	        f = new File(Constants.Server.LogFile);
	    } catch (NullPointerException npe) {
	        System.out.println(sm.getString("shutdown.log.npe",
	            Constants.Server.ConfigFile));
	    }

	    try {
	        if (f.exists() &&
		    f.isFile()) {
		  f.delete();
		}
	    } catch (SecurityException se) {
	        System.out.println(sm.getString("shutdown.log.se",
		    Constants.Server.LogFile));
	    }
	}
    }

    /**
     *
     */

    private Properties getProperties() {
        Properties properties = new Properties();
	File f = null;

	try {
	    f = new File(Constants.Server.LogFile);
	} catch (NullPointerException npe) {
	    System.out.println(sm.getString("shutdown.log.npe",
	        Constants.Server.ConfigFile));
	}

	if (f != null) {
	    try {
	        FileInputStream fis = new FileInputStream(f);

		properties.load(fis);
	    } catch (FileNotFoundException se) {
	        System.out.println(sm.getString("shutdown.log.fnfe",
		    Constants.Server.LogFile));
	    } catch (SecurityException se) {
	        System.out.println(sm.getString("shutdown.log.se",
		    Constants.Server.LogFile));
	    } catch (IOException ioe) {
	        System.out.println(sm.getString("shutdown.log.ioe",
                    Constants.Server.LogFile));
	    }
	}

	return properties;
    }

    /**
     *
     */

    private Registry getRegistry(String portStr)
    throws StartupException {
        Registry registry = null;
	int port = -1;

	try {
	    port = Integer.parseInt(portStr);
	} catch (NumberFormatException nfe) {
            String msg = sm.getString("shutdown.setport.nfe", portStr);

	    throw new StartupException(msg);
	}

	try {
	    registry = LocateRegistry.getRegistry(port);
	} catch (Exception e) {
            String msg = sm.getString("shutdown.registry.e", portStr);

	    throw new StartupException(msg);
	}

	return registry;
    }

    /**
     *
     */

    private String[] getServices(Registry registry)
    throws StartupException {
	String[] services = null;

	try {
	    services = registry.list();
	} catch (AccessException ae) {
            String msg = sm.getString("shutdown.registry.ae");

	    throw new StartupException(msg);
	} catch (RemoteException re) {
            String msg = sm.getString("shutdown.registry.re");

	    throw new StartupException(msg);
	}

	return services;
    }

    /**
     *
     */

    public static void main(String[] args) {
        try {
	    new Shutdown(args);
	} catch (StartupException e) {
	    System.out.println(e.getMessage());
	}
    }

    /**
     * file and command line configuration processing
     */  

    private Config getConfig(String args[])
    throws StartupException {
        Config config = new Config(args);

        System.out.println(sm.getString("startup.banner"));

        if (config.isArg(Constants.Arg.Help)) {

            System.out.println(sm.getString("startup.help"));
            System.out.println();

            return null;
        }

        String configFile = Constants.Server.ConfigFile;

        if (config.isArg(Constants.Arg.Config)) {
            configFile = config.getArg(Constants.Arg.Config);
        }

        config.loadConfig(configFile);

        return config;
    }
}
