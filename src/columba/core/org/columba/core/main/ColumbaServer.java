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

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Opens a server socket to manage multiple sessions of Columba
 * capable of  passing commands to the main session.
 * <p>
 * It tries to find an unused socket using a random number
 * generator. The port number is saved in the file <b>.auth</b>
 * in the users home directory.
 * <p>
 * Clients should use this file to determine the port number.
 *
 * <p>
 * Basic idea taken from www.jext.org (author Roman Guy)
 *
 * @author fdietz
 */
public class ColumbaServer implements Runnable {
    /**
     * default server port
     * <p>
     * TODO: better port determination
     *       we should add a random number and test if
     *       the server socket is already in use from
     *       someone else
     */
    public final static int COLUMBA_PORT = 50000;

    /**
     * server port
     */
    private static int port = COLUMBA_PORT;

    /**
     * file in the users-home directory containing the
     * port number, which is used by the server
     */
    private static File keyFile;

    /**
     * Server runs in its own thread
     */
    private Thread thread;

    /**
     * server socket
     */
    private ServerSocket serverSocket;

    /**
     * Constructor
     *
     */
    public ColumbaServer() {
        // open server socket
        openSocket();

        // start thread
        thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    /**
     * Open server socket.
     * <p>
     * Use a random number generator to find an unused port.
     *
     */
    private void openSocket() {
        try {
            // just increment the server port
            port += 1;

            // init server socket
            serverSocket = new ServerSocket(port);

            // create port number file
            createPortNumberFile(port);
        } catch (Exception ex) {
            // this port is probably blocked
            // TODO: what if a firewall blocks this port?
            //       are we able to distinguish that?
            ex.printStackTrace();

            // try again, using a different port
            openSocket();
        }
    }

    /**
     * Write port number of server to a file.
     * <p>
     * This is used by the client to find out on which
     * port its server is running.
     *
     * @param portNumber    port number of server
     */
    private void createPortNumberFile(int portNumber) {
        keyFile = new File(MainInterface.config.getConfigDirectory(), ".auth");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(keyFile));

            String portStr = Integer.toString(portNumber);
            writer.write(portStr);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO: add error dialog here
            e.printStackTrace();
        }
    }

    /**
     * Stop server
     *
     */
    public synchronized void stop() {
        ColumbaLogger.log.info("Stopping Columba server...");
        // stop thread
        thread.interrupt();
        thread = null;

        try {
            // close socket
            if (serverSocket != null) {
                serverSocket.close();
            }

            // delete auth file
            keyFile.delete();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Check if server is already running
     * @return      true, if server is running. False, otherwise
     */
    public synchronized boolean isRunning() {
        return thread != null;
    }

    /**
     * run method of thread
     */
    public void run() {
        while (isRunning()) {
            try {
                // does a client trying to connect to server ?
                Socket client = serverSocket.accept();

                if (client == null) {
                    continue;
                }

                // only accept client from local machine
                String host = client.getLocalAddress().getHostAddress();

                if (!(host.equals("127.0.0.1"))) {
                    // client isn't from local machine
                    client.close();
                }

                // try to read possible arguments
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                            client.getInputStream()));

                StringBuffer arguments = new StringBuffer();
                arguments.append(reader.readLine());

                if (!(arguments.toString().startsWith("columba:"))) {
                    // client isn't a Columba client
                    client.close();
                }

                if (MainInterface.DEBUG) {
                    ColumbaLogger.log.info(
                        "passing to running Columba session:\n" +
                        arguments.toString());
                }

                // do something with the arguments..
                handleArgs(arguments.toString());

                client.close();
            } catch (Exception ex) {
                if (ex instanceof SocketException) {
                    // socket closed by Columba
                } else {
                    ex.printStackTrace();
                }
            }
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
    protected void handleArgs(String argumentString) {
        // remove trailing "columba:"
        argumentString = argumentString.substring(8, argumentString.length());
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
}
