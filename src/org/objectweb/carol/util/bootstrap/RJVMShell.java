/*
 * @(#) RJVMShell.java	1.0 02/07/15
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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import javax.naming.InitialContext;
/**
 * Class <code>RJVMShell</code>Provide a RMI shell access to a RJVM daemon
 * For the moment it's a basic (but full) rjvm shell 
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */
public class RJVMShell {

    // command line options (thanks jef)
    private static Options cmdLineOptions = null;

    /**
     * Remote server
     */
    private static RJVMServer rjvmServer;

    /**
     * rjvm host location 
     */ 
    public static String  RJVM_HOST_LOCATION = "localhost"; 

    /**
     * rjvm rmi registry port (default 9090)
     */
    public static int RJVM_PORT_NUMBER=9090;

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
     *     l [host location] rmi host location (Optional, default localhost)
     *     p [port number] rmi port number for this daemon (Optional, default 9090)
     */
    public static void main(String args[]) {

	// get the arguments
	cmdLineOptions = new Options();
	// option parameters are: short description (char), long description (String), has arguments (boolean), 
	//                        description (String), required (boolean), has multiple arguments (boolean) 
	cmdLineOptions.addOption('l', "host location", true, "Host location", false, false);         
	cmdLineOptions.addOption('p', "port", true, "daemon rmi port number", false, false);
	cmdLineOptions.addOption('c', "command", true, "start a command and exit", false, false);
	cmdLineOptions.addOption('h', "help", false, "print this message and exit", false, false);
	
        CommandLine cmd = null;
        try {
            cmd = cmdLineOptions.parse(args, true);
        } catch (ParseException e) {
            System.err.println("\n"+ e.getMessage());
            printHelp(cmdLineOptions);
            System.err.println();
            System.exit(1);
        }
        
        if (cmd.hasOption('h')) {
            printHelp(cmdLineOptions);
            System.exit(1);
        }
    
        if (cmd.hasOption('p')) {
              RJVM_PORT_NUMBER = (new Integer(cmd.getOptionValue('p'))).intValue();
        }
        if (cmd.hasOption('l')) {
              RJVM_HOST_LOCATION = cmd.getOptionValue('l');
        }

	Properties iprop =  new Properties();
	iprop.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
	iprop.put("java.naming.provider.url","rmi://"+RJVM_HOST_LOCATION+":"+ RJVM_PORT_NUMBER);
	try {
	    InitialContext in = new InitialContext(iprop);
	    rjvmServer = (RJVMServer)in.lookup("rjvm");
	} catch (Exception e) {
	    System.out.println("Can not start shell, can not contact rjvm server");
	    e.printStackTrace();
	}
	
	if (cmd.hasOption('c')) {
	    String javaCmd = cmd.getOptionValue('c');
	    startCommandLine(javaCmd); 
	    System.exit(0);
        }
	
	try {
	    BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));	  
	    // Open for reading from the keyboard
	    String line;    
	    System.out.println("Connecting to rjvm : rmi://"+ RJVM_HOST_LOCATION+":" + RJVM_PORT_NUMBER);
	    System.out.println("Start entering lines to manage remote jvm");
	    System.out.println("(use \"stop\" to stop)...");
	    System.out.print("rjvm>");
	    line = infile.readLine();
	    while ((!line.equalsIgnoreCase("stop")) &&(!line.equalsIgnoreCase("quit"))&&(!line.equalsIgnoreCase("exit"))) {
		startCommandLine(line);
		System.out.print("rjvm>");
		line = infile.readLine();   
	    }
	    System.out.println("ciao !");
	    infile.close();
	} catch(IOException e){
	    System.err.println(e);
	    return;
	}
    }


    public static void startCommandLine(String line) {	
	try {
	    if ((line.startsWith("help")) || (line.startsWith("?"))){
		printHelpJVM();
	    } else if (line.equalsIgnoreCase("list")) {
		
		//list function 
		for (Enumeration e = rjvmServer.getAllRJVMID().keys()  ; e.hasMoreElements() ;) {
		    System.out.println((String)e.nextElement());
		}
		
	    } else if (line.startsWith("conf")) {
		
		//conf function
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() < 2) {
		    System.out.println("the conf function take a jvm_name parametter:");
		    System.out.println("conf <jvm_name>");
		} else {
		    st.nextToken(); // conf command
		    String jvmName = st.nextToken();
		    RJVMConfiguration rjvmConf =  rjvmServer.getRJVMConfiguration(jvmName);
		    System.out.println("The jvm "+jvmName+" is launch in directory: " + rjvmConf.getCommandDirectory());
		    System.out.println("The jvm "+jvmName+" is launch with the command: java " + rjvmConf.getCommandString()); 
		}
	    } else if (line.equalsIgnoreCase("killall")) {
		
		//kill all function
		rjvmServer.killAllRJVM();
		
	    } else if (line.startsWith("kill")) {
		
		// kill function
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() < 2) {
		    System.out.println("the kill function take a jvm_name parametter:");
		    System.out.println("conf <jvm_name>");
		} else { 
		    st.nextToken(); // kill command
		    String jvmName = st.nextToken();
		    rjvmServer.killRJVM(jvmName);
		}
		
	    } else if (line.startsWith("ping")) {
		
		// ping function
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() < 2) {
		    System.out.println("the ping function take a jvm_name parametter:");
		    System.out.println("conf <jvm_name>");
		} else { 
		    st.nextToken(); //ping function
		    String jvmName = st.nextToken();
		    System.out.println(""+rjvmServer.pingRJVM(jvmName));
		}
		
	    } else if (line.startsWith("exitv")) {
		
		// exitv function
		StringTokenizer st = new StringTokenizer(line);
		if (st.countTokens() < 2) {
		    System.out.println("the exitv function take a jvm_name parametter:");
		    System.out.println("conf <jvm_name>");
		} else { 
		    st.nextToken(); //exitv function
		    String jvmName = st.nextToken();
		    System.out.println(""+rjvmServer.getRJVMExitValue(jvmName));
		}
		
	    } else if (line.startsWith("java")) {
		
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken();
		String jvmLine="";
		while (st.hasMoreTokens()) {
		    jvmLine+= " " + st.nextToken();
		}
		System.out.println("jvm name: " + rjvmServer.startRJVM(new RJVMConfiguration(jvmLine)));
		
	    } else {
		System.out.println("Unreconized function");
	    }	
	} catch(Exception e){
	    System.err.println(e);
	}	    

    }
    
    public static void printHelpJVM() {
	System.out.println("RJVM shell commands:");
	System.out.println("help or ?              -> print this help");
	System.out.println("list                   -> list jvm names");	
	System.out.println("conf  <jvm name>       -> list <jvm name> configuration");	
	System.out.println("ping  <jvm name>       -> true if the jvm is alive");	
	System.out.println("exitv <jvm name>       -> exit value of the jvm (-1) if the jvm is alive");		
	System.out.println("killall                -> kill all jvm");	
	System.out.println("kill  <jvm name>       -> kill the jvm <jvm name>");	
	System.out.println("java  <cmd>            -> the java command");		  
    }
}
