/*
 * @(#) RJVMServer.java	1.0 02/07/15
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

import java.util.Hashtable;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface <code>RJVMServerServer</code>Provide a RMI accessible jvm
 * 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002
 *
 */

public interface RJVMServer extends Remote {

    /**
     * Start JVM with JVMConfiguration and an arbitrary id
     * @return String this id
     * @throws RemoteJVMException if an exception occurs at bootstrapting:
     * - JVM standard starting exception
     * - JVM Path doesn't existe on local machine
    */
    public String startRJVM(RJVMConfiguration conf) throws RJVMException, RemoteException;

    /**
     * Start JVM with JVMConfiguration and id id 
     * @param JVMConfiguration the configuration
     * @param id the JVM id 
     * @throws RemoteJVMException if an exception occurs at bootstrapting:
     * - JVM standard starting exception
     * - JVM Path doesn't existe on local machine
     * - JVM id already use
    */
    public void startRJVM(RJVMConfiguration conf, String id) throws RJVMException, RemoteException;

    /**
     * Kill a JVM process
     * @param JVMConfiguration the configuration
     * @param id the JVM id 
     * 
     */
    public void killRJVM(String id) throws RJVMException, RemoteException;

    /**
     * Kill all jvm processes
     */
    public void killAllRJVM() throws RemoteException;

    /**
     * Test if a JVM is always alive
     * @param id the JVM id 
     * @return true if the JVM is always alive
     */
    public boolean pingRJVM(String id) throws RemoteException;


    /**
     * Get the JVM configuration
     * @param String id this id
     * @return JVMConfiguration the jvm configuration
     * @throws RemoteJVMException if:
     * - The JVM id doesn't exist
     * - The JVM process is stop
     *
     */
    public RJVMConfiguration getRJVMConfiguration(String id) throws RJVMException, RemoteException;

    /**
     * Test if a JVM is not alive the exit value
     * @param id the jvm id
     * @return int the JVM is always alive
     * @throws RJVMException if
     * - the id doen'st existe (with the CLEAN_JVM_PROCESSES=true for example)
     * - teh jvm with this id is not yet terminated
     */
    public int getRJVMExitValue(String id) throws RJVMException, RemoteException;

    /**
     * Get the all JVM id
     * @return String [] the jvm id
     */
    public Hashtable getAllRJVMID() throws RemoteException;   

    /**
     * Stop this server
     */
     public void stop() throws RemoteException;   

}
