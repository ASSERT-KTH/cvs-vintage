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

package org.columba.core.session;

import java.io.*;

import org.columba.core.main.MainInterface;

public class SessionController {
    public static void passToRunningSessionAndExit(String[] args) {
        //create new client and try to connect to server
        ColumbaClient client = new ColumbaClient();
        if (client.connect()) {
            try {
                client.sendCommandLine(args);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                //display error message
            } finally {
                client.close();
            }
            System.exit(5);
        }
        //no server running, start our own
        try {
            ColumbaServer.getColumbaServer().start();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            //display error message
            System.exit(1);
        }
    }
    
    protected static int deserializePortNumber() throws IOException {
        File file = new File(MainInterface.config.getConfigDirectory(), ".auth");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            return Integer.parseInt(line);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    protected static void serializePortNumber(int port) throws IOException {
        File file = new File(MainInterface.config.getConfigDirectory(), ".auth");
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
