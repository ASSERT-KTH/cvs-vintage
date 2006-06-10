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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.columba.api.exception.AuthenticationException;
import org.columba.core.config.Config;
import org.columba.core.resourceloader.GlobalResourceLoader;

/**
 * Contains the logic necessary to search for running Columba sessions and
 * pass command line arguments to it or to start a new session.
 */
public class SessionController {
    
    /**
 * Tries to connect to a running ColumbaServer using a new ColumbaClient.
 * If this works, the given command line arguments are passed to the
 * running server and the startup process is terminated.
 * If this doesn't work, a new ColumbaServer instance is created and
 * the startup process isn't terminated.
 */
    public static void passToRunningSessionAndExit(String[] args) {
        //create new client and try to connect to server
        ColumbaClient client = new ColumbaClient();

        try {
            client.connect();
            client.sendCommandLine(args);
            System.exit(5);
        } catch (IOException ioe1) {
            //no server running, start our own
            try {
                ColumbaServer.getColumbaServer().start();
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
                //display error message
                System.exit(1);
            }
        } catch (AuthenticationException ae) {
            JOptionPane.showMessageDialog(null,
                GlobalResourceLoader.getString(ColumbaServer.RESOURCE_PATH,
                    "session", "err_auth_msg"),
                GlobalResourceLoader.getString(ColumbaServer.RESOURCE_PATH,
                    "session", "err_auth_title"),
                JOptionPane.ERROR_MESSAGE);
            System.exit(5);
        } finally {
            client.close();
        }
    }

    /**
 * Reads a stored port number from the file ".auth" in the config directory.
 */
    protected static int deserializePortNumber() throws IOException {
        File file = new File(Config.getInstance().getConfigDirectory(), ".auth");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));

            String line = reader.readLine();

            return Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            IOException ioe = new IOException();
            ioe.initCause(nfe);
            throw ioe;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
 * Stores the given port number in the file ".auth" in the config directory.
 * If the passed port number is -1, the existing file is deleted.
 */
    protected static void serializePortNumber(int port)
        throws IOException {
        File file = new File(Config.getInstance().getConfigDirectory(), ".auth");

        if (port == -1) {
            file.delete();
        } else {
            BufferedWriter writer = null;

            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(Integer.toString(port));
                writer.newLine();
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
}
