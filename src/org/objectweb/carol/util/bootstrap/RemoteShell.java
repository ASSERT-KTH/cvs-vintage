/*
 * @(#) RemoteShell.java	1.0 02/07/15
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

import java.util.jar.JarInputStream;

import java.io.BufferedReader;
import java.io.PushbackReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

import javax.naming.InitialContext;
/**
 * Class <code>RemoteShell</code>Provide a RMI shell access to a RJVM daemon
 * For the moment it's a basic (but full) rjvm shell 
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */
public class RemoteShell {

    // command line options (thanks jef)
    private static Options cmdLineOptions = null;

    /**
     * Remote server
     */
    private static RemoteProcessesManager procServer;

    /**
     * proc host location 
     */ 
    public static String  HOST_LOCATION = "localhost"; 

    /**
     * proc rmi registry port (default 9090)
     */
    public static int PORT_NUMBER=9090;

    /**
     * static print help method (thaks to Jef and the JOTM team) 
     */
    public static void printHelp(Options cmdLineOptions) {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("Remot Proc Daemon Shell [options...]", cmdLineOptions);
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
              PORT_NUMBER = (new Integer(cmd.getOptionValue('p'))).intValue();
        }
        if (cmd.hasOption('l')) {
              HOST_LOCATION = cmd.getOptionValue('l');
        }

	Properties iprop =  new Properties();
	iprop.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
	iprop.put("java.naming.provider.url","rmi://"+HOST_LOCATION+":"+ PORT_NUMBER);
	try {
	    InitialContext in = new InitialContext(iprop);
	    procServer = (RemoteProcessesManager)in.lookup("proc");
	} catch (Exception e) {
	    System.out.println("Can not start shell, can not contact rproc server" +e );
	}
	
	if (cmd.hasOption('c')) {
	    String procCmd = cmd.getOptionValue('c');
	    startCommandLine(procCmd); 
	    System.exit(0);
        }
	
	try {
	    System.out.println("Connecting to remote proc : rmi://"+ HOST_LOCATION+":" + PORT_NUMBER);
	    System.out.println("Start entering lines to manage remote processes");
	    System.out.println("(use \"stop\" to stop)...");	    
	    //Basic shell with no completion or history  
	    BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));	  
	    //Open for reading from the keyboard
	    String line;    
	    
	    System.out.print("proc>");
	    line = infile.readLine();
	    while ((!line.equalsIgnoreCase("stop")) &&(!line.equalsIgnoreCase("quit"))&&(!line.equalsIgnoreCase("exit"))) {
		startCommandLine(line);
		System.out.print("proc>");
		line = infile.readLine();   
	    }
	    System.out.println("ciao !");
	    infile.close();
	
	} catch(Exception e){
	    System.err.println(e);
	    return;
	}
    }


    public static void startCommandLine(String line) {	
	try {
	    if (line.trim().equals("")) {
		// nothing ...
	    } else if ((line.trim().startsWith("help")) || (line.trim().startsWith("?"))) {
		
		//get help
		printHelpProcess();

	    } else if (line.trim().equalsIgnoreCase("list")) {
		
		printList();
			
	    } else if (line.trim().startsWith("conf")) {
		
		printConf(line);

	    }  else if (line.trim().startsWith("ping")) {
		
		printPing(line);
		
	    } else if (line.trim().startsWith("exitv")) {
		
		printExitValue(line);
		
	    } else if (line.trim().equalsIgnoreCase("killall")) {
		
		procServer.killAllProcesses();
		
	    } else if (line.trim().startsWith("kill")) {
		
		killProcess(line);
		
	    } else if ((line.trim().startsWith("java"))||(line.trim().startsWith("dirjava"))) {
		
		launchJVM(line);
		
	    } else if ((line.trim().startsWith("proc"))||(line.trim().startsWith("dirproc"))) {
		
		launchProcess(line);

	    } else if (line.trim().startsWith("errput")) {
		
		printError(line);

	    } else if (line.trim().startsWith("output")) {
		
		printOutput(line);
		
	    }  else if (line.trim().startsWith("input")) {

		writeInput(line);

	    } else if (line.trim().startsWith("copy")) {
		
		copyFile(line);
		
	    } else if (line.trim().startsWith("stopd")) {
		
		try {
		    procServer.stop();
		} catch (Exception e) {
		    // process stopped
		    System.out.println("deamon stopped, get out of the shell");
		    System.exit(0);
		}
	    } else {
		System.out.println("Unreconized function:" + line);
		printHelpProcess();
	    }	
	} catch(Exception e){
	    System.err.println(e);
	}	    

    }

    /**
     * List process key and command 
     */
    private static void printList() throws Exception {
	try {
	    Hashtable procHash = procServer.getAllProcess();
	    //list function 
	    for (Enumeration e = procHash.keys()  ; e.hasMoreElements() ;) {
		String skey = (String)e.nextElement();
		System.out.println("Process : " + skey + " command : " + (String)procHash.get(skey));
	    }
	} catch (Exception e) {
	    System.out.println("Error in printlist: "+e);
	}
    }
 
    /**
     * Print process configuration 
     * @param command line
     */
    private static  void printConf(String line) throws Exception {
	try {
	    //conf function
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.countTokens() < 2) {
		System.out.println("the conf function take a process name parametter:");
		System.out.println("conf <process name>");
	    } else {
		st.nextToken(); // conf command
		String procName = st.nextToken();
		System.out.println("The process "+ procName +" is launch in directory: " + procServer.getProcessDirectory(procName));
		System.out.println("The process "+ procName +" is launch with the command: " + procServer.getProcessCommand(procName)); 
	    }
	} catch(Exception e){
	    System.err.println(e);
	}	    
    }	    

    /**
     * Print true if the process is alive and false if not
     */
    private static  void printPing(String line) throws Exception {
	try {
	    // ping function
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.countTokens() < 2) {
		System.out.println("the ping function take a process name parametter:");
		System.out.println("ping <process name>");
	    } else { 
		st.nextToken(); //ping function
		String procName = st.nextToken();
		System.out.println(""+procServer.pingProcess(procName));
	    }
   	} catch(Exception e){
	    System.err.println(e);
	}	    
    }	    

    /**
     * Print the process exit value
     */
    private static void printExitValue(String line) throws Exception {
	try {
	    // exitv function
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.countTokens() < 2) {
		System.out.println("the exitv function take a process name parametter:");
		System.out.println("exitv <process name>");
	    } else { 
		st.nextToken(); //exitv function
		String procName = st.nextToken();
		System.out.println(""+procServer.getProcessExitValue(procName));
	    }
   	} catch(Exception e){
	    System.err.println(e);
	}	    
    }	    

    /**
     * Kill the process
     */
    private static void killProcess(String line) throws Exception {
	try {
	    // kill function
	    StringTokenizer st = new StringTokenizer(line);
	    if (st.countTokens() < 2) {
		System.out.println("the kill function take a process name parametter:");
		System.out.println("kill <process name>");
	    } else { 
		st.nextToken(); //kill function
		String procName = st.nextToken();
		procServer.killProcess(procName);
	    }
   	} catch(Exception e){
	    System.err.println(e);
	}	    
    }	    

    /**
     * Launch a JVM
     */
    private static void launchJVM(String line) throws Exception {
	if (line.trim().startsWith("java")) {
	    StringTokenizer st = new StringTokenizer(line);
	    st.nextToken();
	    String jvmLine="";
	    while (st.hasMoreTokens()) {
		jvmLine+= " " + st.nextToken();
	    }
	    System.out.println("jvm name: " + procServer.startJVM(new JVMConfiguration(jvmLine), null));
	} else if (line.trim().startsWith("dirjava")) {
	    
	    StringTokenizer st = new StringTokenizer(line);
	    st.nextToken();
	    String dirName=st.nextToken();
	    String jvmLine="";
	    while (st.hasMoreTokens()) {
		jvmLine+= " " + st.nextToken();
	    }
	    System.out.println("jvm name: " + procServer.startJVM(new JVMConfiguration(jvmLine), null,dirName));
	}
    }
    

    /**
     * Launch a Process
     */
    private static void launchProcess(String line) throws Exception {
	if (line.trim().startsWith("proc")) {
	    StringTokenizer st = new StringTokenizer(line);
	    st.nextToken();
	    String procLine="";
	    while (st.hasMoreTokens()) {
		procLine+= " " + st.nextToken();
	    }
	    System.out.println("process name: " + procServer.startProcess(procLine, null));
	} else if (line.trim().startsWith("dirproc")) {	    
	    StringTokenizer st = new StringTokenizer(line);
	    st.nextToken();
	    String dirName=st.nextToken();
	    String procLine="";
	    while (st.hasMoreTokens()) {
		procLine+= " " + st.nextToken();
	    }
	    System.out.println("process name: " + procServer.startProcess(procLine, null,dirName));
	}	
    }

    /**
     * Print error put ouf the process
     */
    public static void printError(String line) throws Exception {
	// err function
	StringTokenizer st = new StringTokenizer(line);
	if (st.countTokens() < 2) {
	    System.out.println("the errput function take a process name parametter:");
	    System.out.println("errput <process name>");
	} else { 
	    st.nextToken(); //err function
	    String procName = st.nextToken();
	    System.out.println("make ^C or kill the process for stop this process");
	    while (true) {
		try {
		    Thread.sleep(3000);
		    String es = procServer.readProcessError(procName);
		    if (es.trim().length()!=0) {	
			System.out.println(es);
		    }		    
		} catch (Exception e) {
		    System.out.println ("Process connection killed");
		    System.exit(0);
		}
	    }
	}		
    }



    /**
     * Print output put ouf the process
     */
    public static void printOutput(String line) throws Exception {
	// out function
	StringTokenizer st = new StringTokenizer(line);
	if (st.countTokens() < 2) {
	    System.out.println("the output function take a process name parametter:");
	    System.out.println("output <process name>");
	} else { 
	    st.nextToken(); //out function
	    String procName = st.nextToken();
	    System.out.println("make ^C or kill the process for stop this process");
	    while (true) {
		try {
		    Thread.sleep(3000);
		    String es = procServer.readProcessOutput(procName);
		    if (es.trim().length()!=0) {	
			System.out.println(es);
		    }		    
		} catch (Exception e) {
		    System.out.println ("Process connection killed");
		    System.exit(0);
		}
	    }
	}		
    }


   /**
     * Write an imput in the process
     */
    public static void writeInput(String line) throws Exception {

	StringTokenizer st = new StringTokenizer(line);
	if (st.countTokens() < 3) {
	    System.out.println("the input function take a process name and line parametter:");
	    System.out.println("input <process name> <line>");
	} else {
	    st.nextToken(); //input command
	    String procName=st.nextToken();
	    String procLine="";
	    while (st.hasMoreTokens()) {
		procLine+= " " + st.nextToken();
	    }
	    procServer.writeProcessInput(procName, procLine + "\n");
	    System.out.println(procLine + " sended to " + procName);
	}
    }


    /**
     * Copy a File (jar or ascii) to remote host in remote directory
     * @throws Exception if the local file doen't existe or 
     * if a Remote exception occurs
    */
    public static void copyFile(String line) throws Exception {
	
	StringTokenizer st = new StringTokenizer(line);
	if (st.countTokens() < 4) {
	    System.out.println("the copy function take a process name and line parametter:");
	    System.out.println("copy <local file> <remote dir> <remote name>");
	} else {
	    st.nextToken(); //input command
	    String asciiFile=st.nextToken();
	  
	    File f = new File(asciiFile);
	    if ((!(f.exists()))||(f.isDirectory())) {
		throw new Exception("File not existed");
	    }

	    FileInputStream aInput = new FileInputStream(f);
	    byte [] b = new byte [aInput.available()];
	    aInput.read(b);
	    aInput.close();

	    String remoteDir=st.nextToken();
	    String remoteName=st.nextToken();
	    procServer.sendFile(remoteDir, remoteName, b);
	    
	}


    }

    public static void printHelpProcess() {
	System.out.println("Remote Process shell commands:");
	System.out.println("help or ?                            -> print this help");
	System.out.println("list                                 -> list process names and command");	
	System.out.println("conf   <process name>                -> get <process name> configuration");
	System.out.println("ping   <process name>                -> true if the process is alive");	
	System.out.println("exitv  <process name>                -> exit value of the process (-1) if the process is alive");		
	System.out.println("killall                              -> kill all process");	
	System.out.println("kill     <process name>              -> kill the process <process name>");	
	System.out.println("java     <cmd>                       -> launch a java command in arbitrary directory");	
	System.out.println("proc     <string>                    -> launch a new process in arbitrary directory");
	System.out.println("dirjava  <dir>        <cmd>          -> launch a java command in dir (create if necessary)");	
	System.out.println("dirproc  <dir>        <string>       -> launch a new process in dir (create if necessary)");
	System.out.println("output   <process name>              -> get output stream of the process every 3 seconds");
	System.out.println("errput   <process name>              -> get error stream of the process every 3 seconds");
	System.out.println("input    <process name> <string>     -> send a string to the input stream of a process");
	System.out.println("copy     <local file> <remote dir>");  
	System.out.println("                      <remote name> -> copy ascii fille in dir (create if necessary)");
	
    }
}
