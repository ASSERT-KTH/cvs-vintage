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

    boolean isRmi=false; 
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

	Registry registry=null;
	if( isRmi )
	    registry = createRegistry(serverConfig.getAdminPort());
	Enumeration contextManagers = serverConfig.getContextManagers();

	while (contextManagers.hasMoreElements()) {
	    ContextManagerConfig contextManagerConfig =
	        (ContextManagerConfig)contextManagers.nextElement();
	    ContextManager contextManager = new ContextManager();
	    Enumeration contexts = contextManagerConfig.getContextConfigs();

	    while (contexts.hasMoreElements()) {
	        ContextConfig contextConfig =
		    (ContextConfig)contexts.nextElement();

		Context context = new Context();
		context.setContextManager(contextManager);
		context.setPath(contextConfig.getPath());
		context.setDocumentBase(contextConfig.getDocumentBase());

                contextManager.addContext(context);

		context.setSessionTimeOut(
		    contextConfig.getDefaultSessionTimeOut());

		// this is the semantic of disable invoker.
		if( ! contextConfig.isInvokerEnabled() ) {
		    context.addServlet(org.apache.tomcat.core.Constants.INVOKER_SERVLET_NAME,
				       "org.apache.tomcat.core.NoInvokerServlet", null);
		    context.addMapping(org.apache.tomcat.core.Constants.INVOKER_SERVLET_NAME,
				       "/servlet");
		}

		context.setIsWARExpanded(contextConfig.isWARExpanded());
		context.setIsWARValidated(contextConfig.isWARValidated());
		context.setWorkDirPersistent(contextConfig.isWorkDirPersistent());
		context.setIsWARValidated(contextConfig.isWARValidated());

		// no need to check if it's the "default" context ( == root contex == "/"),
		// we treat the root as a normal context

		// Register the global service and lifecycle interceptors
		// with each new context
		for (Enumeration e=contextConfig.getServiceInterceptors();e.hasMoreElements(); ) {
		    InterceptorAdapter.addServiceInterceptor(context, (ServiceInterceptor)e.nextElement());
		}
		for (Enumeration e=contextConfig.getLifecycleInterceptors();e.hasMoreElements(); ) {
		    LifecycleInterceptor interceptor=(LifecycleInterceptor)e.nextElement();
		    InterceptorAdapter.addInitInterceptor(context, interceptor);
		    InterceptorAdapter.addDestroyInterceptor(context, interceptor);
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

		Enumeration conE=contextManagerConfig.getConnectorConfigs();
		while( conE.hasMoreElements() ) {
		    ConnectorConfig conC=(ConnectorConfig) conE.nextElement();
		    String cn=conC.getClassName();
		    ServerConnector conn=null;

		    try {
			Class c=Class.forName( cn );
			conn=(ServerConnector)c.newInstance();
		    } catch(Exception ex) {
			ex.printStackTrace();
			// XXX 
		    }
		    Enumeration props=conC.getParameterKeys();
		    while( props.hasMoreElements() ) {
			String k=(String)props.nextElement();
			String v=(String)conC.getParameter( k );
			conn.setProperty( k, v );
		    }

		    server.addConnector( conn );
		}
		
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
		hse.printStackTrace();
                throw new StartupException();
		// "problem starting server" can't help
		// the user detect that the port is taken.
		// ( or another tcp-related problem )
		// Please, let the stack trace until we have a better
		// message ( that shows what failed)
	    } catch (RemoteException re) {
	        String msg = sm.getString("startup.server.re");

	        System.out.println(msg);
		re.printStackTrace();
                throw new StartupException();
		// The original message is useless,
		// I had no idea what to do - if we can't figure a better
		// message, please let the stack trace in

		// in my case - I had no eth card, and it couldn't find the hostname
	    } catch (AlreadyBoundException abe) {
	        String msg = sm.getString("startup.server.abe");
	        System.out.println(msg);
		abe.printStackTrace();
                throw new StartupException();
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
	    // System.out.println(e.getMessage());
            System.exit(1);
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

//     private String getContextWorkDirPath(ServerConfig serverConfig,
// 	ContextManagerConfig contextManagerConfig, String path) {
//         String s = "";
// 	String baseDir = serverConfig.getWorkDir();
// 	String hostName = contextManagerConfig.getHostName();
// 	String iNet = contextManagerConfig.getINet();
// 	int port = contextManagerConfig.getPort();

// 	if (hostName.trim().length() > 0) {
// 	    s += hostName;
// 	} else if (iNet.trim().length() > 0) {
// 	    s += iNet;
// 	}

// 	if (s.length() != 0) {
// 	    s += ":";
// 	}

// 	if (port > -1) {
// 	    s += Integer.toString(port);
// 	}

// 	return baseDir + File.separator + URLEncoder.encode(s + path);
//     }
}
