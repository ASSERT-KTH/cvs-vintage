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

import java.io.*;

import java.net.Socket;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;

/**
 * Client connecting to the {@link ColumbaServer} to check if
 * a session of Columba is already running.
 * <p>
 * If a session is running the client is able to pass requests
 * to the server.
 *
 * @author fdietz
 */
public class ColumbaClient {
    protected static final String NEWLINE = "\r\n";
    
    protected Socket socket;
    protected Writer writer;
    
    public ColumbaClient() {}
    
    /**
     * Tries to connect to a running server.
     */
    public boolean connect() {
        try {
            socket = new Socket("127.0.0.1", ColumbaServer.PORT);
            writer = new PrintWriter(socket.getOutputStream());
            writer.write("Columba " + MainInterface.version);
            writer.write(NEWLINE);
            writer.flush();
            
            writer.write("User " + System.getProperty("user.name",
                    ColumbaServer.ANONYMOUS_USER));
            writer.write(NEWLINE);
            writer.flush();
            return true;
        } catch (IOException ex) {
        }
        return false;
    }
    
    /**
     * Submits the given command line options to the server.
     */
    public void sendCommandLine(String[] args) throws IOException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            buf.append(args[i]);
            buf.append('%');
        }

        writer.write(buf.toString());
        writer.write(NEWLINE);
        writer.flush();
    }
    
    /**
     * Closes this client.
     */
    public void close() {
        try {
            writer.close();
            socket.close();
        } catch (IOException ioe) {}
    }
}
