/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/shell/Attic/Constants.java,v 1.2 1999/12/31 01:18:36 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 1999/12/31 01:18:36 $
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

public class Constants {

    public static final String Package = "org.apache.tomcat.shell";

    public static class Server {
        public static final String ConfigFile = "server.xml";
        public static final String LogFile = "serverlog.txt";
    }

    public static class Arg {
        public static final String Help = "help";
        public static final String Config = "config";
        public static final String NoConfig = "noconfig";
        public static final String Validate = "validate";
        public static final String AdminPort = "adminport";
        public static final String ServiceId = "serviceid";
        public static final String Port = "port";
        public static final String Inet = "inet";
        public static final String HostName = "hostname";
        public static final String DocumentBase = "docbase";
        public static final String WorkDir = "workdir";
    }

    public static class XML {
        public static final boolean DefaultValidate = true;
    }

    public static class Element {
        public static final String SERVER = "Server";
        public static final String CONTEXT = "Context";
        public static final String CONNECTOR = "Connector";
        public static final String CONTEXT_MANAGER = "ContextManager";
        public static final String PARAMETER = "Parameter";
	public static final String INTERCEPTOR = "Interceptor";
    }

    public static class Attribute {
        public static final String Id = "id";
        public static final String AdminPort = "adminPort";
        public static final String Port = "port";
        public static final String HostName = "hostName";
        public static final String INet = "inet";
        public static final String DocumentBase = "docBase";
        public static final String IsWARExpanded = "isWARExpanded";
        public static final String IsWARValidated = "isWARValidated";
        public static final String WorkDir = "workDir";
        public static final String IsWorkDirPersistent =
            "isWorkDirPersistent";
        public static final String IsInvokerEnabled =
            "isInvokerEnabled";
        public static final String CONTEXT_ID = "contextId";
        public static final String Path = "path";
        public static final String DefaultSessionTimeOut =
            "defaultSessionTimeOut";
        public static final String CLASS_NAME = "className";
        public static final String PARAMETER_NAME = "name";
        public static final String PARAMETER_VALUE = "value";
    }

    public static final String[] WEBSERVER_ATTRIBUTES = {
        Attribute.AdminPort
    };

    public static final String[] SERVICE_ATTRIBUTES = {
        Attribute.Port,
        Attribute.HostName,
        Attribute.INet,
        Attribute.DocumentBase,
        Attribute.WorkDir
    };

    public static class Default {
        public static final int ADMIN_PORT = -1;
        public static final String WORK_DIR = "work";
        public static final boolean IS_WORK_DIR_PERSISTENT = false;
        public static final int SessionTimeOut = 30;
        public static final boolean IS_WAR_EXPANDED = true;
        public static final boolean IS_WAR_VALIDATED = false;
        public static final boolean IS_INVOKER_ENABLED = true;
        public static final int CONTEXT_MANAGER_PORT = 8080;
    }

    public static class Property {
        public static final String AdminPort = "admin.port";
    }

    public static class Registry {
        public static final String Service = "service";
    }

    public static final String[] REQUIRED_CLASSES = {
        "sun.tools.javac.Main"
    };

    public static class RMI {
        public static final int MAX_ADMIN_PORT_ATTEMPTS = 5;
        public static final int MIN_ADMIN_PORT = 2048;
        public static final int MAX_ADMIN_PORT = MIN_ADMIN_PORT * 4;
    }

    public static class Protocol {
        public static class WAR {
            public static final String PACKAGE =
                "org.apache.tomcat.protocol";
            public static final String SYSTEM_PROPERTY =
                "java.protocol.handler.pkgs";
        }
    }
 }
