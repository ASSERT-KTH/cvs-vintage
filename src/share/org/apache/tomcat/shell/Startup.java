/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/shell/Attic/Startup.java,v 1.2 1999/10/22 01:47:10 costin Exp $
 * $Revision: 1.2 $
 * $Date: 1999/10/22 01:47:10 $
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

import org.apache.tomcat.shell.deployment.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.server.*;
import org.apache.tomcat.util.StringManager;
import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.*;

/**
 * Command line entry point to start up tomcat.
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 */

public class Startup {

    protected StringManager sm =
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

    public Startup() {
    }

    public void configure(String[] args)
    throws StartupException {
        Config config = getConfig(args);

	if (config == null) {
            return;
	}

	checkClassDependencies();

	ServerConfig serverConfig = config.getServerConfig();
	Registry registry =  createRegistry(serverConfig.getAdminPort());

	Enumeration contextManagers = serverConfig.getContextManagers();

	while (contextManagers.hasMoreElements()) {
	    ContextManagerConfig contextManagerConfig =
	        (ContextManagerConfig)contextManagers.nextElement();
	    ContextManager contextManager = new ContextManager();
	    Enumeration contexts = contextManagerConfig.getContextConfigs();

	    while (contexts.hasMoreElements()) {
	        ContextConfig contextConfig =
		    (ContextConfig)contexts.nextElement();
                Context context = contextManager.addContext(
		    contextConfig.getPath(),
		    contextConfig.getDocumentBase());
		String contextWorkDirPath =
		    getContextWorkDirPath(serverConfig,
			contextManagerConfig,
		        contextConfig.getPath());

		context.setSessionTimeOut(
		    contextConfig.getDefaultSessionTimeOut());
		context.setInvokerEnabled(contextConfig.isInvokerEnabled());
		context.setIsWARExpanded(contextConfig.isWARExpanded());
		context.setIsWARValidated(contextConfig.isWARValidated());
		context.setWorkDir(contextWorkDirPath,
		    contextConfig.isWorkDirPersistent());
		context.setIsWARValidated(contextConfig.isWARValidated());

		if (contextConfig.getPath().equals(
                    org.apache.tomcat.core.Constants.Context.Default.Path)) {
		    contextManager.setDefaultContext(context);
		    contextManager.setServerInfo(
		        contextManager.getDefaultContext().getEngineHeader());
		}
	    }

	    InetAddress inetAddress = null;

	    try {
	        inetAddress = InetAddress.getByName(
		    contextManagerConfig.getINet());
	    } catch (java.net.UnknownHostException uhe) {
                String msg = sm.getString("startup.setinit.uhe1",
		    contextManagerConfig.getINet());

		System.out.println(msg);
            }  

	    try {
		HttpServer server = new HttpServer(
		    contextManagerConfig.getPort(), inetAddress,
		    contextManagerConfig.getHostName(), contextManager);

		// XXX
		// instead of HTTPServer it should be EndpointManager,
		//   ContextManager, Handler, etc
	        if( registry != null )
		    registry.bind(Constants.Registry.Service + ":" +
				  server.getPort(), new AdminImpl(server));

		// XXX
		// start/stop individual components
		server.start();
	    } catch (HttpServerException hse) {
	        String msg = sm.getString("startup.server.hse");

	        System.out.println(msg);
	    } catch (RemoteException re) {
	        String msg = sm.getString("startup.server.re");

	        System.out.println(msg);
	    } catch (AlreadyBoundException abe) {
	        String msg = sm.getString("startup.server.abe");

	        System.out.println(msg);
	    }
	}
    }

    /**
     * class dependencies
     */

    public void checkClassDependencies() {
        for (int i = 0; i < Constants.REQUIRED_CLASSES.length; i++) {
	    String clazz = (String)Constants.REQUIRED_CLASSES[i];

	    try {
	        Class.forName(clazz);
	    } catch (ClassNotFoundException cnfe) {
	        String msg = sm.getString("startup.classes.cnfe", clazz);

		System.out.println("warning: " + msg);
	    }
	}
    }

    /**
     *
     */

    protected Registry createRegistry(int port)
    throws StartupException {
        Registry registry = null;
	int numberAttempts = 0;

	if (port==0) return null;
			 
	if (port < 0) {
	    port = newPort();
	}

	while (true) {
	    String msg = null;

	    try {
	        registry = LocateRegistry.createRegistry(port);
		createLog(port);
	    } catch (Exception e) {
	        msg = sm.getString("startup.registry.e");
	    }

	    if (registry == null &&
		numberAttempts++ < Constants.RMI.MAX_ADMIN_PORT_ATTEMPTS) {
	        port = newPort();
	    } else if (registry != null) {
	        break;
	    } else {
	        throw new StartupException(msg);
	    }
	}

	return registry;
    }

    /**
     *
     */

    protected int newPort() {
        Random r = new Random();
	double mul = r.nextDouble();
	int min = Constants.RMI.MIN_ADMIN_PORT;
	int max = Constants.RMI.MAX_ADMIN_PORT;

	return (int)Math.round(min + mul * (max - min));
    }

    /**
     *
     */

    protected void createLog(int port) {
        File f = null;

        try {
	    f = new File(Constants.Server.LogFile);
	} catch (NullPointerException npe) {
	    System.out.println(sm.getString("startup.log.npe",
	        Constants.Server.ConfigFile));
	}

	if (f != null) {
	    try {
	        if (f.exists() &&
		    f.isFile()) {
		    f.delete();
		}
	    } catch (SecurityException se) {
	        System.out.println(sm.getString("startup.log.se",
		    Constants.Server.ConfigFile));
	    }

	    try {
	        BufferedWriter bw =
                    new BufferedWriter(new FileWriter(f));

		bw.write(Constants.Property.AdminPort + ":" + port);
		bw.newLine();

		bw.close();
	    } catch (IOException ioe) {
	        System.out.println(sm.getString("startup.log.ioe",
		    Constants.Server.ConfigFile));
	    }
	}
    }
    
    /**
     *
     */
    
    public static void main(String[] args) {
	try {
	    Startup start = new Startup();

	    start.configure(args);
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

    private String getContextWorkDirPath(ServerConfig serverConfig,
	ContextManagerConfig contextManagerConfig, String path) {
        String s = "";
	String baseDir = serverConfig.getWorkDir();
	String hostName = contextManagerConfig.getHostName();
	String iNet = contextManagerConfig.getINet();
	int port = contextManagerConfig.getPort();

	if (hostName.trim().length() > 0) {
	    s += hostName;
	} else if (iNet.trim().length() > 0) {
	    s += iNet;
	}

	if (s.length() != 0) {
	    s += ":";
	}

	if (port > -1) {
	    s += Integer.toString(port);
	}

	return baseDir + File.separator + URLEncoder.encode(s + path);
    }
}
