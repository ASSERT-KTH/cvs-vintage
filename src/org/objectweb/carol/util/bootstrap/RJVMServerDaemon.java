/*
 * @(#) RJVMServerDaemon.java	1.0 02/07/15
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

import java.io.OutputStream;
import java.io.InputStream;


import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;

/**
 * Class <code>RJVMServerDaemon</code>Provide a RMI accessible jvm daemon
 * for boostraping a java Virtual Machine. You can pass througth RMI all 
 * JVM configuration utilities.
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */

public class RJVMServerDaemon extends PortableRemoteObject implements RJVMServer {

    // command line options (thanks jef)
    private static Options cmdLineOptions = null;

    /**
     * Registry 
     */
    Registry registry = null;
 
    /**
     * Verbose boolean (default false)
     */
    private static boolean verbose = false;

    /**
     * Debug boolean (default false)
     */
    private static boolean debug = false;

    /**
     * time to wait for the JVM starting (default 5 second)
     */
    public static int STARTING_JVM_WAIT_TIME=1;

    /**
     * time to wait for the JVM starting (default 5 second)
     */
    public static String JVM_COMMAND_NAME="java";

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
     * JVM Processes Hashtable with id
     */
    public static Hashtable jvmProcesses = new Hashtable();
    
    /**
     * JVM configuration with id
     */
    public static Hashtable jvmConfigurations = new Hashtable();

    /**
     * JVM id variable
     */
    private static int idIcrement = 0; 

    /**
     * empty constructor 
     */
    public RJVMServerDaemon() throws RemoteException {
	super();
    }

    /**
     * get a new JVM id
     */
    private synchronized String getNewID() {
	idIcrement ++;
	return "carol_jvm_"+idIcrement;
    }   
    
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
	cmdLineOptions.addOption('d', "debug", false, "debug mode", false, false);        
        cmdLineOptions.addOption('v', "verbose", false, "verbose mode", false, false);
	cmdLineOptions.addOption('j', "java command name", true, "Name of the java command", false, false);        
	cmdLineOptions.addOption('c', "clean", false, "clean jvm hashtables mode", false, false);
	cmdLineOptions.addOption('w', "factory", true, "JNDI URL of the transaction factory", false, false);        
	cmdLineOptions.addOption('p', "port", true, "daemon rmi port number", false, false);
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
        
        debug = cmd.hasOption('d');
        verbose = cmd.hasOption('v');
        if (cmd.hasOption('h')) {
            printHelp(cmdLineOptions);
            System.exit(1);
        }
        if (cmd.hasOption('c')) {
             CLEAN_JVM_PROCESSES = cmd.hasOption('c');
        }

        if (cmd.hasOption('w')) {
             STARTING_JVM_WAIT_TIME = (new Integer(cmd.getOptionValue('w'))).intValue();
        }

        if (cmd.hasOption('p')) {
              RJVM_PORT_NUMBER = (new Integer(cmd.getOptionValue('p'))).intValue();
        }

	try {
	    // start a server
	    RJVMServerDaemon rdaemon = new RJVMServerDaemon();
	    rdaemon.start();
	} catch (Exception e) {
	    System.err.println("Can not start remote daemon"); 
	    e.printStackTrace();
	} 
	System.out.println("Daemon server started on port: " + RJVM_PORT_NUMBER);
    }

    /**
     * start method
     */
    public void start() {
	try {
	    // start registry
	    registry = LocateRegistry.createRegistry(RJVM_PORT_NUMBER);
	    
	    // bind the RJVM daemon 
	    registry.bind("rjvm", this);
	} catch (Exception e) {
	    System.err.println("Can not start the rjvm daemon:");
	    e.printStackTrace();
	    System.exit(0);
	}
    }


    /**
     * Start JVM with RJVMConfiguration and an arbitrary id and wait STARTING_JVM_WAIT_TIME
     * @return String this id
     * @throws RJVMException if an exception occurs at bootstrapting:
     * - JVM standard starting exception
     * - JVM Path doesn't existe on local machine
     */
    public String startRJVM(RJVMConfiguration conf) throws RJVMException, RemoteException {
	String newid = getNewID();
	startRJVM(conf, newid);
	return newid;
    }

    /**
     * Start JVM with RJVMConfiguration and id id 
     * @param RJVMConfiguration the configuration
     * @param id the JVM id 
     * @throws RJVMException if an exception occurs at bootstrapting:
     * - JVM standard starting exception
     * - JVM Path doesn't existe on local machine
     * - JVM id already use
    */
    public synchronized void startRJVM(RJVMConfiguration conf, String id) throws RJVMException, RemoteException {
	Process p = null;
	if (verbose) {
	    System.out.println("Start a new JVM with id: " + id);
	    System.out.println("and with : " + JVM_COMMAND_NAME +" "+ conf.getCommandString());
	    System.out.println("in : " + conf.getCommandDirectory());
	}
	if ((jvmConfigurations.get(id)!=null) || (jvmProcesses.get(id)!=null)) {
	    throw new RJVMException("JVM Name already exist");
	} else {
	    try {
		p = Runtime.getRuntime().exec(JVM_COMMAND_NAME + conf.getCommandString(), null, conf.getCommandDirectory());
		Thread.sleep(STARTING_JVM_WAIT_TIME*1000);
		// get the Err and Out stream of the process 
		int ev = p.exitValue();

		// get the Err and Out stream of the process
		InputStream pErrorStream = p. getErrorStream();
		InputStream pOutputStream = p.getInputStream();
		byte [] b;
		b = new byte[pErrorStream.available()];
		pErrorStream.read(b);
		String processErr = "\n\n----------------------------------------------";
		processErr += "\nJava Process error  :\n" + new String(b);


		b = new byte[pOutputStream.available()];
		pOutputStream.read(b); 
		String processOut = "\n\n----------------------------------------------";
		processOut +="\nJava Process output :\n" + new String(b);

		throw new RJVMException("The JVM is not started, exit value= " + ev +  processErr + processOut);

	    } catch (IllegalThreadStateException ite) {
		// the Tread is not yet terminated, OK continue 
		jvmProcesses.put(id, p);
		jvmConfigurations.put(id, conf);
		// lauch a new jvm shudown thread if CLEAN_JVM_PROCESSES true
		if (CLEAN_JVM_PROCESSES) {
		    JVMStopThread s = new JVMStopThread(p, id);
		    s.start();
		}
	    } catch (Exception e) {
		int evexit = -1;
		String processErr = null;
		String processOut = null;
		try {
		    // try to get the process out/err output
		    if (p != null) {
			evexit = p.exitValue();
			// get the Err and Out stream of the process
			InputStream pErrorStream = p. getErrorStream();
			InputStream pOutputStream = p.getInputStream();
			byte [] b;
			b = new byte[pErrorStream.available()];
			pErrorStream.read(b);
			processErr =  "\n\n----------------------------------------------";
			processErr += "\nJava Process error  :\n" + new String(b);
			
			
			b = new byte[pOutputStream.available()];
			pOutputStream.read(b); 
			processOut =  "\n----------------------------------------------";
			processOut += "\nJava Process output :\n" + new String(b);
			throw new RJVMException(e);
		    } else {
			throw new RJVMException("The JVM is not started, exit value= " + evexit +  processErr + processOut + e);
		    }
		} catch (Exception pe) {
		    throw new RJVMException(pe);
		}
		
	    }
	}
    }

    /**
     * Kill a JVM process (if existe) and remove it's process id and configuration
     * @param id the JVM id 
     * @throws RJVMException if the id doesn't existe
     */
    public synchronized void killRJVM(String id) throws RJVMException, RemoteException {
	if (verbose) {
	    System.out.println("Kill a JVM with id: " + id);
	}
	if (jvmProcesses.containsKey(id)) {
	    ((Process)jvmProcesses.get(id)).destroy();
	    jvmProcesses.remove(id);
	    jvmConfigurations.remove(id);  
	} else {
	    jvmConfigurations.remove(id);
	    throw new RJVMException("JVM with id: "+"id"+" doesn't exist");
	} 
    }

    /**
     * Kill all jvm processes and remove all process id and configuration
     */
    public synchronized void killAllRJVM() throws RemoteException {
	if (verbose) {
	    System.out.println("Kill all JVM");
	}
	jvmConfigurations.clear();
	for (Enumeration e = jvmProcesses.keys() ; e.hasMoreElements() ;) {
	    ((Process)jvmProcesses.get(e.nextElement())).destroy();
	}
	jvmProcesses.clear();
    }

    /**
     * Test if a JVM is always alive
     * @param id the JVM id 
     * @return true if the JVM is always alive and false if this JVM doens't 
     * existe anymore or if the process of this JVM is stopped
     */
    public synchronized boolean pingRJVM(String id) throws RemoteException {
	if (verbose) {
	    System.out.println("ping JVM");
	}
	if (jvmProcesses.containsKey(id)) {
	    try {
		((Process)jvmProcesses.get(id)).exitValue();
		return false;
	    } catch (IllegalThreadStateException ite) {
		return true;
	    }
	} else {
	    return false;
	}
    }


    /**
     * Test if a JVM is not alive the exit value
     * @param id the jvm id
     * @return int the JVM is always alive
     * @throws RJVMException if
     * - the id doen'st existe (with the CLEAN_JVM_PROCESSES=true for example)
     * - teh jvm with this id is not yet terminated
     */
    public int getRJVMExitValue(String id) throws RJVMException, RemoteException {
	if (verbose) {
	    System.out.println("search exit value of JVM with id: " + id);
	}
	if (jvmProcesses.containsKey(id)) {
	    try {
		int ev_value = ((Process)jvmProcesses.get(id)).exitValue();
		return ev_value;
	    } catch (IllegalThreadStateException ite) {
		// the JVM isn'nt stopped for the moment
		throw new RJVMException("JVM with id: "+"id"+" is not yet stopped");
	    }
	} else {
	    throw new RJVMException("JVM with id: "+"id" + "doens'nt exist");
	}
    }

    /**
     * Get the JVM configuration
     * @param String id this id
     * @return RJVMConfiguration the jvm configuration
     * @throws RJVMException if:
     * - The JVM id doesn't exist
     * - The JVM process is stop
     *
     */
    public RJVMConfiguration getRJVMConfiguration(String id) throws RJVMException, RemoteException {
	if (verbose) {
	    System.out.println("get JVM id " +id +" configuration");
	}
	if (jvmProcesses.containsKey(id)) {
	    try {
		int ev_value = ((Process)jvmProcesses.get(id)).exitValue();
		throw new RJVMException("JVM with id: "+id+" is stop with exit value: " + ev_value);
	    } catch (IllegalThreadStateException ite) {
		// the Tread is not yet terminated, OK continue 
		return (RJVMConfiguration)jvmConfigurations.get(id);
	    }
	} else {
	    throw new RJVMException ("JVM with id: "+id+ "doens'nt exist");
	}
	
    }

    /**
     * Get the all JVM id
     * @return Hashtable the jvm id
     */
    public Hashtable getAllRJVMID() throws RemoteException {
	if (verbose) {
	    System.out.println("get all JVM id");
	}
	return jvmConfigurations;
    }

    /**
     * Stop this server
     */
    public void stop() throws RemoteException {
	if (verbose) {
	    System.out.println("stop rjvm server");
	}
	// for the moment ...
	System.exit(0);
    }
  
    /**
     * JVM Shudown process Thread
     * For cleanong the JVM's Hashtable
     */
    public class JVMStopThread extends Thread {

	/**
	 * The associated process
	 */
	Process jvmProcess;
	
	/**
	 * The JVM id 
	 */
	String jvmID;

	/**
	 * constructor
	 * @param p JVM process
	 * @param id JVM id
	 */
	JVMStopThread(Process p, String id) {
	    this.jvmProcess=p;
	    this.jvmID=id;
	}
	
	/**
	 * Thread run method
	 */
	public void run() {
	    try {
		jvmProcess.waitFor();
		if (verbose) {
		    System.out.println("Stopping jvm " + jvmID);
		}
		synchronized (this) {
		    jvmProcesses.remove(jvmID);
		    jvmConfigurations.remove(jvmID);
		}
	    } catch (InterruptedException ie) {
		ie.printStackTrace();
	    }
	}
    }
   
}    

