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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

/**
 * Client connecting to the {@link ColumbaServer} to check if
 * a session of Columba is already running.
 * <p>
 * If a session is running the client is able to pass requests
 * to the server. Otherwise it starts the {@link ColumbaServer}.
 *
 * @author fdietz
 */
public class ColumbaClient {
    /**
     * server instance
     */
    private static ColumbaServer columbaServer;

    /**
     * file in the users-home directory containing the
     * port number, which is used by the server
     */
    private static File keyFile;

    /**
     * Load Columba only once.
     *
     * @param arguments     commandline arguments
     */
    public static void loadInVMInstance(String[] arguments) {
        try {

            // read port from file
            int port = readPortFromFile();
            
            // init socket          
            Socket clientSocket =
                new Socket("127.0.0.1", port);

            PrintWriter writer =
                new PrintWriter(clientSocket.getOutputStream());

            StringBuffer buf = new StringBuffer();
            buf.append("columba:");

            for (int i = 0; i < arguments.length; i++) {
                buf.append(arguments[i]);
                buf.append("%");
            }

            ColumbaLogger.log.info(
                "Trying to pass command line arguments to a running Columba session:\n"
                    + buf.toString());

            writer.write(buf.toString());
            writer.flush();
            writer.close();

            clientSocket.close();

            System.exit(5);
        } catch (Exception ex) { // we get a java.net.ConnectException: Connection refused
            columbaServer = new ColumbaServer();
        }
    }

    /**
     * Open <b>.auth</b> file and read the port number
     * used by the server from it.
     * 
     * @return      port number
     */
    public static int readPortFromFile() throws FileNotFoundException{
        keyFile = new File(MainInterface.config.getConfigDirectory(), ".auth");
        if ( !keyFile.exists() ) return -1;
        
        String s;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(keyFile));

            s = reader.readLine();

            reader.close();

            return Integer.parseInt(s);
        }  catch (IOException e) {
            // TODO: add better error handling
            e.printStackTrace();
        }

        return -1;
    }
    
    /**
     * Get columba server.
     * 
     * @return  columba server
     */
    public static ColumbaServer getColumbaServer() {
        return columbaServer;
    }
}
