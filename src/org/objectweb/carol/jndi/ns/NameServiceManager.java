/*
 * @(#)NameServiceManager.java	1.0 02/07/15
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
 */
package org.objectweb.carol.jndi.ns;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;

//carol import
import org.objectweb.carol.util.configuration.RMIConfiguration;
import org.objectweb.carol.util.configuration.CarolConfiguration; 
import org.objectweb.carol.util.configuration.TraceCarol; 

/*
 * Class <code>NameServicemanager</code> is the CAROL Name Service manager.
 * This is the carol API for Nme services management
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/01/2003
 */
public class NameServiceManager {

    /**
     * Name Service Hashtable
     */
    public static Hashtable nsTable;
    /**
     * private constructor for singleton
     */
    private static NameServiceManager current = new NameServiceManager() ;

    /**
     * private constructor for unicicity
     */
    private NameServiceManager() {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.NameServiceManager()");
        }
	try {
	    nsTable = new Hashtable();
	    //get rmi configuration  hashtable 	    
	    Hashtable allRMIConfiguration = CarolConfiguration.getAllRMIConfiguration();	    
	    int nbProtocol = allRMIConfiguration.size();
	    for (Enumeration e = allRMIConfiguration.elements() ; e.hasMoreElements() ;) {
		RMIConfiguration currentConf = (RMIConfiguration)e.nextElement();
		String rmiName = currentConf.getName();
		NameService nsC = (NameService)Class.forName(currentConf.getNameService()).newInstance();
		nsC.setPort(currentConf.getPort());
		// get the Name Service
		nsTable.put(rmiName, nsC);
	    }
	} catch (Exception e) {
	    String msg = "NameServiceManager.NameServiceManager() fail"; 
	    TraceCarol.error(msg,e);
	}
    }
    
    /**
     * Method getCurrent
     *
     * @return NameServiceManager return the current 
     *
     */
    public static NameServiceManager getNSManagerCurrent() {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.getNSManagerCurrent()");
        }
	return current ;
    }

    
    /**
     * Start all names service
     * @throws NameServiceException if one of the name services is already start 
     */
    public static void startNS() throws NameServiceException {	
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.startNS()");
        }
	// test if one of the ns is allready started 
	for (Enumeration e = nsTable.keys() ; e.hasMoreElements() ;) {
	    String k = (String)e.nextElement();
	    NameService currentNS = (NameService)nsTable.get(k);
	    if (currentNS.isStarted ()) {
		throw new NameServiceException("The "+k+" name service is allready started");
	    }
	}
	// Start all name services
	startNonStartedNS();
    }

    /**
     * Start all non-started names service 
     * @throws NameServiceException if an exception occure at starting time
     */
    public static void startNonStartedNS() throws NameServiceException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.startNonStartedNS()");
        }
	// start name services 
	for (Enumeration e = nsTable.keys() ; e.hasMoreElements() ;) {
	    String k = (String)e.nextElement();
	    NameService currentNS = (NameService)nsTable.get(k);
	    
	    try {
		currentNS.start();
	    } catch (NameServiceException nse) {
		// do nothing, just trace		    
		if (TraceCarol.isDebugJndiCarol()) {
		    TraceCarol.debugJndiCarol("NameServiceManager.startNonStartedNS() can not start name service: "+k);
		}
	    }
	}
    }
    
    /**
     * Stop all name services
     * @throws NameServiceException if an exception occure at stoping time
     */
    public static void stopNS() throws NameServiceException {
	if (TraceCarol.isDebugJndiCarol()) {
            TraceCarol.debugJndiCarol("NameServiceManager.stopNS()");
        }
	// stop name services 
	for (Enumeration e = nsTable.keys() ; e.hasMoreElements() ;) {
	    String k = (String)e.nextElement();
	    NameService currentNS = (NameService)nsTable.get(k);
	    currentNS.stop();
	}
    }
}
