/*
 * @(#) RemoteProcessesManager.java	1.0 02/07/15
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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * Interface <code>RemoteProcessesManager</code>Provide a Process Manager
 * for boostraping Process and send file to a process directory
 * Thie class extends a remote interface for RMI calls
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/11/2002
 *
 */
public interface RemoteProcessesManager extends Remote {

    /**
     * Start a jvm process on the remote host in a tmp directory
     * @param JVMConfiguration configuration
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @return String the process id
     * @throws ProcessException if an exception occurs at bootstrapting
     */
    public String startJVM(JVMConfiguration jvmConf, String [] envp) throws ProcessException, RemoteException;

    /**
     * Start a jvm process on the remote host
     * @param JVMConfiguration configuration
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @param String processDir directory where to launch the process
     * @return String the process id
     * @throws ProcessException if an exception occurs at bootstrapting
     */
    public String startJVM(JVMConfiguration jvmConf, String [] envp, String processDir) throws ProcessException, RemoteException;

    /**
     * Start a jvm process on the remote host
     * @param JVMConfiguration configuration
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @param String processDir directory where to launch the process 
     *               (inside the current directory and without file separator);
     * @param String the process id
     * @throws RProcessException if an exception occurs at bootstrapting
     */
    public void startJVM(JVMConfiguration jvmConf, String [] envp, String processDir, String id) throws ProcessException, RemoteException;

    /**
     * Start a process on the remote host in a tmp directory
     * @param String processLine to launch the process
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @return String the process id
     * @throws ProcessException if an exception occurs at bootstrapting
     */
    public String startProcess(String processLine, String [] envp) throws ProcessException, RemoteException;

    /**
     * Start a process on the remote host
     * @param String processLine to launch the process
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @param String processDir directory where to launch the process
     * @return String the process id
     * @throws ProcessException if an exception occurs at bootstrapting
     */
    public String startProcess(String processLine, String [] envp, String processDir) throws ProcessException, RemoteException;

    /**
     * Start a process on the remote host
     * @param String processLine to launch the process
     * @param String [] env property: "pkey=pvalue" can be null if there is no proprerty
     * @param String processDir directory where to launch the process 
     *               (inside the current directory and without file separator);
     * @param String the process id
     * @throws RProcessException if an exception occurs at bootstrapting
     */
    public void startProcess(String processLine, String [] envp, String processDir, String id) throws ProcessException, RemoteException;
	

    /**
     * Kill a process (if existe) and remove it's process id and configuration
     * @param id the Process id 
     * @throws ProcessException if the id doesn't existe
     */
    public void killProcess(String id) throws ProcessException, RemoteException;

    /**
     * Kill all processes and remove all process id and configuration
     */
    public void killAllProcesses() throws  RemoteException;

    /**
     * Test if a Process is always alive
     * @param id the Process String id 
     * @return true if the Process is always alive and false if this Process doens't 
     * existe anymore or if the process of this Process is stopped
     */
    public boolean pingProcess(String id) throws ProcessException, RemoteException;


    /**
     * Test if a Process is not alive the exit value
     * @param id the jvm id
     * @return int the Process is always alive
     * @throws ProcessException if
     * - the id doen'st existe (with the CLEAN_Process_PROCESSES=true for example)
     * - teh jvm with this id is not yet terminated
     */
    public int getProcessExitValue(String id) throws ProcessException, RemoteException;

    /**
     * Get the Process command line
     * @param String id this id
     * @return String the process command line
     * @throws ProcessException if:
     * - The Process id doesn't exist
     * - The Process process is stop
     *
     */
    public String getProcessCommand(String id) throws ProcessException, RemoteException;

    /**
     * Get the Process directory
     * @param String id this id
     * @return String the process directory
     * @throws ProcessException if:
     * - The Process id doesn't exist
     * - The Process process is stop
     *
     */
    public String getProcessDirectory(String id) throws ProcessException, RemoteException;

    /**
     * Get the all Process id with there command line
     * @return Hashtable the process id and his command line
     */
    public Hashtable getAllProcess() throws  RemoteException;

   /**
     * get the rproc OutputStream
     * @param id the proc id
     * @throws ProcessException if
     * - the id doen'st existe
     */
    public String readProcessOutput(String id) throws ProcessException, RemoteException;

   /**
     * get the rjvm ErrorStream
     * @param id the jvm id
     * @throws ProcessException if
     * - the id doen'st existe
     */
    public String readProcessError(String id) throws ProcessException, RemoteException;

    /**
     * send a String to the rjvm inputStream
     * @param s String to send to the InputStream
     * @param id the jvm id
     * @throws ProcessException if
     * - the id doen'st existe
     */
    public void writeProcessInput(String id, String s) throws  ProcessException, RemoteException;
        
    /**
     * Send a file to a directory
     * (FileImputStream/FileOutputStream format)
     * this method build a directory in the current directory
     * if the directory
     * does not exite. Your are not allow to 
     * write some thing outside of the current 
     * directory 
     *
     * @param byte [] array of this ascii file 
     * @param String directory name in the current directory 
     */
    public void sendFile(String dirName, String fileName, byte [] b) throws  RemoteException;

    /**
     * Stop the damemon and kill all the process
     */
    public void stop() throws RemoteException;
}
