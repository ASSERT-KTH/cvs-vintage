/*
 * @(#) ProcessesDaemon.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 *
 */
package org.objectweb.carol.util.bootstrap;

// Jakarta CLI 
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.rmi.PortableRemoteObject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Class <code>ProcessesDaemon</code>Provide a RMI accessible jvm daemon
 * for boostraping a Processes. You can pass througth RMI all 
 * process configuration utilities, zip and ascii file.
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */

public class ProcessesDaemon {

    // command line options (thanks jef)
    private static Options cmdLineOptions = null;

    /**
     * Registry 
     */
    private static Registry registry = null;
 
    /**
     * rjvm rmi registry port (default 9090)
     */
    public static int RJVM_PORT_NUMBER=9090;

    /**
     * clean JVM processes/configuration hashtable at shudow (default TRUE) 
     * Be carful, TRUE for this variable mean one more shudow THREAD by JVM $
     * in the daemon  
     */
    public static boolean CLEAN_JVM_PROCESSES=true;
     
    /**
     * static print help method (thaks to Jef and the JOTM team) 
     */
    public static void printHelp(Options cmdLineOptions) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("RJVM Daemon [options...]", cmdLineOptions);
    }


    /**
     * Main method, starting the RJVM Daemon
     * @param args [] the arguments :
     *     c [cmd] the java jvm command name (Optional, default "java")
     *     d  [true/false] clean the jvm process when they stop. (Optional, default "true")
     *                            (see the  public static boolean CLEAN_JVM_PROCESSES definition in this class)
     *     w [time in second] Waiting time after a jvm start (Optional, default 1 second)
     *     p [port number] rmi port number for this daemon (Optional, default 9090)
     */
    public static void main(String args[]) {

	// get the arguments
	cmdLineOptions = new Options();
	// option parameters are: short description (char), long description (String), has arguments (boolean), 
	//                        description (String), required (boolean), has multiple arguments (boolean)    
	cmdLineOptions.addOption('c', "clean", false, "clean jvm hashtables mode", false, false);      
	cmdLineOptions.addOption('p', "port", true, "daemon rmi port number", false, false);
	cmdLineOptions.addOption('h', "help", false, "print this message and exit", false, false);
	cmdLineOptions.addOption('v', "verbose", false, "verbose mode", false, false);

        CommandLine cmd = null;
        try {
            cmd = cmdLineOptions.parse(args, true);
        } catch (ParseException e) {
            System.err.println("\n"+ e.getMessage());
            printHelp(cmdLineOptions);
            System.err.println();
            System.exit(1);
        }
        
        boolean verbose = cmd.hasOption('v');
        if (cmd.hasOption('h')) {
            printHelp(cmdLineOptions);
            System.exit(1);
        }

        if (cmd.hasOption('c')) {
             CLEAN_JVM_PROCESSES = cmd.hasOption('c');
        }

        if (cmd.hasOption('p')) {
              RJVM_PORT_NUMBER = (new Integer(cmd.getOptionValue('p'))).intValue();
        }

	try {
	    // start a server
	    RemoteProcessesManager pmanager = new ProcessesManager(CLEAN_JVM_PROCESSES, verbose);
	    PortableRemoteObject.exportObject(pmanager);
	    registry = LocateRegistry.createRegistry(RJVM_PORT_NUMBER);
	    registry.bind("proc", pmanager);
	} catch (Exception e) {
	    System.err.println("Can not start remote daemon " + e); 
	} 
	System.out.println("Daemon server started on port: " + RJVM_PORT_NUMBER);
    }   
}    

