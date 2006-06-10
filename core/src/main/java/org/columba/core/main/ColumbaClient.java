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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;

import org.columba.api.exception.AuthenticationException;
import org.columba.core.versioninfo.VersionInfo;

/**
 * Client connecting to the {@link ColumbaServer} to check if
 * a session of Columba is already running.
 * <p>
 * If a session is running the client tries to authenticate.
 *
 * @author fdietz
 */
public class ColumbaClient {
    protected static final String NEWLINE = "\r\n";
    protected Socket socket;
    protected Writer writer;
    protected BufferedReader reader;

    public ColumbaClient() {
    }

    /**
 * Tries to connect to a running server.
 */
    public void connect() throws IOException, AuthenticationException {
        socket = new Socket("127.0.0.1",
                SessionController.deserializePortNumber());
        writer = new PrintWriter(socket.getOutputStream());
        writer.write("Columba " + VersionInfo.getVersion());
        writer.write(NEWLINE);
        writer.flush();

        writer.write("User " +
            System.getProperty("user.name", ColumbaServer.ANONYMOUS_USER));
        writer.write(NEWLINE);
        writer.flush();
        reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        String response = reader.readLine();
        if (response.equals("WRONG USER")) {
            throw new AuthenticationException();
        }
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
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
        }
    }
}
