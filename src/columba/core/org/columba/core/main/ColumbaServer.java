//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.main;

import org.columba.core.main.MainInterface;
import org.columba.core.logging.ColumbaLogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Opens a server socket to manage multiple sessions of Columba
 * capable of  passing commands to the main session.
 * <p>
 * This class is a singleton because there can only be one server
 * per Columba session.
 * <p>
 * Basic idea taken from www.jext.org (author Roman Guy)
 *
 * @author fdietz
 */
public class ColumbaServer {
    
    /**
     * The port the Columba server runs on.
     */
    public final static int PORT = 50000;
    
    /**
     * The anonymous user for single-user systems without user name.
     */
    public final static String ANONYMOUS_USER = "anonymous";
    
    /**
     * The singleton instance of this class.
     */
    private static ColumbaServer instance;

    /**
     * Server runs in its own thread.
     */
    protected Thread thread;

    /**
     * The ServerSocket used by the server.
     */
    protected ServerSocket serverSocket;

    /**
     * Constructor
     */
    protected ColumbaServer() {
        thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        handleClient(serverSocket.accept());
                    } catch (SocketTimeoutException ste) {
                        //do nothing here, just continue
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        //what to do here? we could start a new server...
                    }
                }
                try {
                    serverSocket.close();
                } catch (IOException ioe) {}
            }
        }, "ColumbaServer");
        thread.setDaemon(true);
    }

    /**
     * Starts the server.
     */
    public synchronized void start() throws IOException {
        if (!isRunning()) {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(2000);
            thread.start();
        }
    }

    /**
     * Stops the server.
     */
    public synchronized void stop() {
        thread.interrupt();
    }

    /**
     * Check if server is already running
     * @return      true, if server is running. False, otherwise
     */
    public synchronized boolean isRunning() {
        return thread.isAlive();
    }
    
    protected void handleClient(Socket client) {
        try {
            // only accept client from local machine
            String host = client.getLocalAddress().getHostAddress();
            if (!(host.equals("127.0.0.1"))) {
                // client isn't from local machine
                client.close();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                        client.getInputStream()));
            String line = reader.readLine();
            if (!line.startsWith("Columba ")) {
                client.close();
            }
            
            line = reader.readLine();
            if (!line.startsWith("User ")) {
                client.close();
            }
            if (!line.substring(5).equals(System.getProperty("user.name", ANONYMOUS_USER))) {
                client.close();
            }
            
            line = reader.readLine();

            ColumbaLogger.log.info("Passing to running Columba session: " + line);

            // do something with the arguments..
            handleCommandLine(line);

            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Parsing the given argumentString and split this String into a StringArray. The separator is
     * the character %, thus the whole arguments should not have this character inside. The
     * character itselfs is added in Main.java @see Main#loadInVMInstance(String[]). After splitting
     * is finished the CmdLineArgumentHandler is called, to do things with the arguments
     * @see CmdLineArgumentHandler
     * @param argumentString String which holds any arguments seperated by <br>%</br> character
     */
    protected void handleCommandLine(String argumentString) {
        List list = new LinkedList();

        StringTokenizer st = new StringTokenizer(argumentString, "%");
        while (st.hasMoreTokens()) {
            String tok = (String) st.nextToken();
            list.add(tok);
        }

        ColumbaCmdLineParser cmdLineParser = new ColumbaCmdLineParser();
        try {
            cmdLineParser.parseCmdLine((String[])list.toArray(new String[0]));
            new CmdLineArgumentHandler(cmdLineParser);
        } catch (IllegalArgumentException e) {}
    }
    
    /**
     * Returns the singleton instance of this class.
     */
    public synchronized static ColumbaServer getColumbaServer() {
        if (instance == null) {
            instance = new ColumbaServer();
        }
        return instance;
    }
}
