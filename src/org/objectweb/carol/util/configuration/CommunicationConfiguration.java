/*
 * @(#) CommunicationConfiguration.java	1.0 02/07/15
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
package org.objectweb.carol.util.configuration;

//java import 
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.*;

//javax import 
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

// carol import 
import org.objectweb.carol.util.multi.ProtocolCurrent;
import org.objectweb.carol.jndi.ns.NameServiceManager;
import org.objectweb.carol.jndi.ns.NameServiceException;

/*
 * Interface <code>CommunicationConfiguration</code> for Communication environment
 * You must have a communication.xml and communication.dtd in your 
 * classpath for the definition of this context
 */

public class CommunicationConfiguration {

    /**
     * boolean true if the protocol context where load from thefile
     */
    private static boolean configurationLoaded = false;

    /**
     * boolean true if non started name server need to launch
     */
    private static boolean startNS = false;

    /** 
     * Protocol environement hashtable, all rmi Configuration 
     * are classified by there architecture name (jrmp, iiop, ...)
     */
    private static Hashtable rmiConfigurationTable  = new Hashtable();

    /**
     * defaultProtocol
     */
    private static String defaultRMI = null;

    /**
     * rmi properties file name 
     */
    public static final String CAROL_FILE_NAME="carol.properties";

    /**
     * jndi properties file name
     */
    public static final String JNDI_FILE_NAME="jndi.properties";

    
    /**
     * Constructor 
     * Read the communication context
     */
    public CommunicationConfiguration() throws RMIConfigurationException {
	if (!configurationLoaded) {
	    loadCarolConfiguration();
	}
    }


    /**
     * Get a RMI environment with his architecture name 
     * @return RMIConfiguration the environment, null if not existe
     */
    public static RMIConfiguration getRMIConfiguration(String name)  throws RMIConfigurationException {
	if (configurationLoaded) {
	    return (RMIConfiguration)rmiConfigurationTable.get(name);
	} else {
	    loadCarolConfiguration();
	    return (RMIConfiguration)rmiConfigurationTable.get(name);
	}
    }

    /**
     * Get all RMI environment
     * @return Hashtable the rmi configuration hashtable 
     */
    public static Hashtable getAllRMIConfiguration()  throws RMIConfigurationException {
	if (configurationLoaded) {
	    return rmiConfigurationTable;
	} else {
	    loadCarolConfiguration();
	    return rmiConfigurationTable;
	}
    }
    /**
     * Get the default rmi
     * @return RMIConfiguration default RMI  Configuration
     */
    public static RMIConfiguration getDefaultProtocol()  throws RMIConfigurationException {
	if (configurationLoaded) {
	    return (RMIConfiguration)rmiConfigurationTable.get(defaultRMI);
	} else {
	    loadCarolConfiguration();
	    return (RMIConfiguration)rmiConfigurationTable.get(defaultRMI);
	}
    }


    /**
     * This method read all the the orbX.properties, jndiX.properties file
     * for protocols configurations
     * @throws RMIConfigurationException if a problem occurs in the configuration loading
     */ 
    public static void loadCarolConfiguration() throws RMIConfigurationException {
	Properties rmiProps = new Properties();
	Properties jndiProps = new Properties();
	// load the configuration files	
	try {
	    // load the rmi configuration file
	    InputStream rmiFileInputStream  =  ClassLoader.getSystemResourceAsStream(CAROL_FILE_NAME); 
	    if (rmiFileInputStream != null) {
		rmiProps.load(rmiFileInputStream);
	    } else {
		rmiProps = null;
	    }

	    // load the jndi configuration file
	    InputStream jndiFileInputStream =  ClassLoader.getSystemResourceAsStream(JNDI_FILE_NAME);
	    if (jndiFileInputStream != null) {
		jndiProps.load(jndiFileInputStream);
	    } else {
		jndiProps = null;
	    }
	    
	} catch(Exception e) {
	    throw new RMIConfigurationException("Exception occur when loading rmi/jndi configuration file: " + e);
        }	
	loadCarolConfiguration(rmiProps , jndiProps);
    }

    /**
     * This method read a rmi configuration from 2 Properties 
     * @param rmiProps The orbX environment (can be null) 
     * @param jndiProps The jndiX environment (can be null) 
     * @throws RMIConfigurationException if a there is a problem with those environment (field missing for example)
     */ 
      public static synchronized void loadCarolConfiguration(Properties rmiProps, Properties jndiProps) throws RMIConfigurationException {
	// init Trace 
	TraceCarol.configure();
	Properties carolProps = CarolDefaultValues.getCarolProperties(rmiProps, jndiProps);	  
	Properties jvmProps = new Properties();	    
	jvmProps.putAll(System.getProperties());
	  
	String jvmPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JVM_PREFIX;
	String rmiPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.RMI_PREFIX;
	String jndiPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JNDI_PREFIX;
	String activationPrefix = rmiPref + "."  + CarolDefaultValues.ACTIVATION_PREFIX;
	String nsPrefix =  CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.START_NS_PREFIX;
	
    	//Parse the properties
	for (Enumeration e =  carolProps.propertyNames() ; e.hasMoreElements() ;) {

	    String pkey = ((String)e.nextElement()).trim();
	    if  (pkey.startsWith(activationPrefix)) { // get default rmi name : the first activated rmi
		StringTokenizer pTok = new StringTokenizer(carolProps.getProperty(pkey), ",");
		if (pTok.hasMoreTokens()) {
		    defaultRMI = (pTok.nextToken()).trim();
		} else {
		    String msg = "There is no rmi activated in the file " + CAROL_FILE_NAME;
		    TraceCarol.error(msg);
		    throw new RMIConfigurationException(msg);
		}
	    } else if (pkey.startsWith(nsPrefix)) { // start or not non started name service
	       	startNS = new Boolean(carolProps.getProperty(pkey).trim()).booleanValue();
	    } else if (pkey.startsWith(jvmPref)) { // jvm properties
		jvmProps.setProperty(pkey.substring(jvmPref.length()+1), (carolProps.getProperty(pkey)).trim());	
	    } else if ((pkey.startsWith(rmiPref)) || (pkey.startsWith(jndiPref))) { // this is a carol properties
		StringTokenizer pkeyToken = new StringTokenizer(pkey, ".");
		pkeyToken.nextToken();
		pkeyToken.nextToken();
		String rmiName = (pkeyToken.nextToken()).trim();
		
		if  (!rmiConfigurationTable.containsKey(rmiName)) {
		    RMIConfiguration rmiConf =  new RMIConfiguration(rmiName, carolProps, jndiProps);
		    rmiConfigurationTable.put(rmiName, rmiConf);
		}
	    } else { // this is not a carol properties
		String msg = "The properties " + pkey + "can not be set in the file " + CAROL_FILE_NAME;
		TraceCarol.error(msg);
		throw new RMIConfigurationException(msg);
	    }
	}

 	// add the jvm properties in the jvm 
	System.setProperties(jvmProps);
	configurationLoaded = true;
	if (getDefaultProtocol() == null) {
	    String msg = "The default protocol : " + defaultRMI + " must be configured inside the carol properties files";
	    TraceCarol.error(msg);

	    throw new RMIConfigurationException(msg);
	}

	if (startNS) {
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("Start non started Name Servers");
	    }
	    try {
		NameServiceManager.getNSManagerCurrent().startNonStartedNS();
	    } catch (NameServiceException nse) {
		String msg = "Can't start Name Servers";
		TraceCarol.error(msg, nse);
		throw new RMIConfigurationException(msg);
	    }
	}
      }

    /**
     * public static boolean check communication configuration method
     * @return boolean true if the configuration seam to be ok
     */
    public static boolean checkCarolConfiguration() {
	try {
	    if (!configurationLoaded) {
		loadCarolConfiguration();
	    }
	    return true;
	} catch (RMIConfigurationException rmi) {
	    return false;
	}
    }

    /**
     * This method activate a rmi architecture with is name. You need to load the CAROL configuration before using thid method.
     * @param String rmi name 
     * @throws RMIConfigurationException if a the carol configuration is not loaded (by the loadCarolConfiguration method)
     * @throws RMIConfigurationException if the rmi name doesn't exist in the carol configuration
     */
    public static void activateRMI(String rmiName) throws RMIConfigurationException {
	if (TraceCarol.isDebugCarol()) {
            TraceCarol.debugCarol("CommunicationConfiguration.activateRMI("+rmiName+")");
        }
	if (!configurationLoaded) {
	    String msg = "call for rmi "
		+rmiName
		+ " activation with no protocols configuration load \n please, call before the loadCarolConfiguration() static method in this class";
	    TraceCarol.error(msg);
	    throw new RMIConfigurationException(msg);
	}
	RMIConfiguration rmiC =  getRMIConfiguration(rmiName);
	if (rmiC == null) {
	    String msg = "try to activate a non existant rmi configuration :" +rmiName;
	    TraceCarol.error(msg);
	    throw new RMIConfigurationException(msg); 
	} else {
	    rmiC.activate();
	}
    }

    /**
     * This method desactivate a rmi architecture with is name. You need to load the CAROL configuration before using thid method.
     * @param String rmi name 
     * @throws RMIConfigurationException if a the carol configuration is not loaded (by the loadCarolConfiguration method)
     * @throws RMIConfigurationException if the rmi name doesn't exist in the carol configuration
     */
    public static void desactivateRMI(String rmiName) throws RMIConfigurationException {
	if (TraceCarol.isDebugCarol()) {
            TraceCarol.debugCarol("CommunicationConfiguration.desactivateRMI("+rmiName+")");
        }
	if (!configurationLoaded) {
	    String msg = "call for rmi "
		+rmiName
		+ " desactivation with no protocols configuration load \n please, call before the loadCarolConfiguration() static method in this class";
	    TraceCarol.error(msg);
	    throw new RMIConfigurationException(msg);
	}
	RMIConfiguration rmiC =  getRMIConfiguration(rmiName);
	if (rmiC == null) {
	    String msg = "try to desactivate a non existant rmi configuration :" +rmiName;
	    TraceCarol.error(msg);
	    throw new RMIConfigurationException(msg); 
	} else {
	    rmiC.desactivate();
	}
    }
}
