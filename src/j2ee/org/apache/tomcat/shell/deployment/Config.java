/*
 * $Header: /tmp/cvs-vintage/tomcat/src/j2ee/org/apache/tomcat/shell/deployment/Attic/Config.java,v 1.2 2000/02/29 22:42:43 costin Exp $
 * $Revision: 1.2 $
 * $Date: 2000/02/29 22:42:43 $
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


package org.apache.tomcat.shell.deployment;

import org.apache.tomcat.shell.Constants;
import org.apache.tomcat.shell.StartupException;
import org.apache.tomcat.util.StringManager;
import org.apache.tomcat.util.XMLParser;
import org.apache.tomcat.util.XMLTree;
import org.apache.tomcat.util.URLUtil;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author James Todd [gonzo@eng.sun.com]
 */

public class Config {

    private StringManager sm =
        StringManager.getManager(Constants.Package);
    private String[] args;
    private URL configURL = null;
    private XMLTree config = null;
    private ServerConfig serverConfig = null;
    
    /**
     *
     */

    public Config()
    throws StartupException {
        this(null);
    }

    /**
     *
     */
    
    public Config(String[] args)
    throws StartupException {
	this.args = args;
    }

    /**
     *
     */

    public void loadConfig(String configFile)
    throws StartupException {
        loadConfig(configFile, Constants.XML.DefaultValidate);
    }

    /**
     *
     */

    public void loadConfig(String configFile, boolean validate)
    throws StartupException {
	File f = new File(configFile);

	if (! f.exists() &&
	    (! isArg(Constants.Arg.NoConfig))) {

	    /* Creating a "default" config with examples and root
	       is confusing and doesn't work anyway ( since examples and root are in
	       different directories, we can't guess the location )
	       Also, it means something is wrong - and it's better to
	       warn the user, so he can correct it
	    */
	    System.out.println("No config file found " + configFile );
	    throw new StartupException("No config file found " + configFile );
	    
	    // 	    System.out.println("No config file found " + configFile );
	    // 	    InputStream is =
	    // 	        this.getClass().getResourceAsStream(configFile);
	    
	    // 	    loadConfig(is, validate);
	} else {
	    try {
	        configURL = URLUtil.resolve(configFile);
	    } catch (MalformedURLException mue) {
	        String msg = sm.getString("startup.loadconfig.mue",
		    configFile);

		throw new StartupException(msg);
	    }

	    loadConfig(configURL, validate);
	}
    }

    /**
     *
     */ 

    public void loadConfig(URL configURL)
    throws StartupException {
        loadConfig(configURL, Constants.XML.DefaultValidate);
    }

    /**
     *
     */ 

    public void loadConfig(URL configURL, boolean validate)
    throws StartupException {
	if (isArg(Constants.Arg.Validate)) {
	    String validateStr = getArg(Constants.Arg.Validate);

	    validate = Boolean.valueOf(validateStr).booleanValue();
	}

	System.out.println(sm.getString("startup.loadconfig.msg",
            configURL));

	XMLParser parser = new XMLParser();

	try {
	    this.config = parser.process(configURL, validate);
	} catch (Exception e) {
	    throw new StartupException(e.getMessage());
	}

        processArgs(this.config);
    }

    /**
     *
     */

    public void loadConfig(InputStream is, boolean validate) 
    throws StartupException {
        XMLParser parser = new XMLParser();

	try {
	    this.config = parser.process(is, validate);
	} catch (Exception e) {
	    throw new StartupException(e.getMessage());
	}

	processArgs(this.config);
    }

    /**
     * Determines if the given string is amongst the arguments given
     * on the command line.
     *
     * @param arg
     */

    public boolean isArg(String arg) {
        boolean searchResults = false;

	for (int i = 0; i < this.args.length; i++) {
	    if (this.args[i].equalsIgnoreCase("-" + arg)) {
	        searchResults = true;

		break;
	    }
	}
	
	return searchResults;
    }

    /**
     * Gets the value associated with the given argument. If the argument
     * doesn't have a value, or if the argument doesn't exist in the set
     * of arguments, then return null.
     *
     * @param arg
     */

    public String getArg(String arg) {
        String value = null;

	for (int i = 0; i < this.args.length; i++) {
	    if (this.args[i].equalsIgnoreCase("-" + arg) &&
                i < this.args.length - 1) {
		value = this.args[i + 1];

                break;
	    }
	}
	
	return value;
    }

    /**
     *
     */

    public XMLTree getConfig() {
        return this.config;
    }

    /**
     *
     */

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    /**
     *
     */

    public static void main(String[] args) {
        try {
	    new Config(args);
	} catch (StartupException e) {
	    System.out.println(e.getMessage());
	}
    }

    /**
     *
     */

    private void createConfigFile(String configFile)
    throws StartupException {
        try {
	    InputStream is =
                this.getClass().getResourceAsStream(configFile);
	    FileOutputStream out = new FileOutputStream(configFile);
	    byte[] buf = new byte[1024];
	    int read = 0;

	    do {
	        out.write(buf, 0, read);
		read = is.read(buf, 0, buf.length);
	    } while (read > -1);

	    is.close();
	    out.close();
	} catch (IOException e) {
	    String msg = sm.getString("startup.loadconfig.ioe",
	        configFile);

	    throw new StartupException(msg);
	}
    }

    /**
     *
     */

    private void processArgs(XMLTree config) {
	String adminPortStr =
            (String)config.getAttribute(Constants.Attribute.AdminPort);

        for (int i = 0; i < Constants.WEBSERVER_ATTRIBUTES.length; i++) {
	    String key = Constants.WEBSERVER_ATTRIBUTES[i];

	    if (isArg(key)) {
	        config.addAttribute(key, getArg(key));
	    }
	}

	Enumeration enum = config.elements();

	while (enum.hasMoreElements()) {
	    XMLTree configElement = (XMLTree)enum.nextElement();
	    String id =
	        (String)configElement.getAttribute(Constants.Attribute.Id);

            for (int i = 0; i < Constants.SERVICE_ATTRIBUTES.length; i++) {
	        String key = Constants.SERVICE_ATTRIBUTES[i];

		if (isArg(key + ":" + id)) {
		    configElement.addAttribute(key, getArg(key + ":" + id));
		} else if (isArg(key + ":*")) {
		    configElement.addAttribute(key, getArg(key + ":*"));
		} else if (isArg(key) &&
		    (config.getElements().size() == 1 ||
		        (id.equals(getArg(Constants.Arg.ServiceId))))) {
		    configElement.addAttribute(key, getArg(key));
		}
	    }
	}

	this.serverConfig = new ServerConfig(this.config);
    }
}
