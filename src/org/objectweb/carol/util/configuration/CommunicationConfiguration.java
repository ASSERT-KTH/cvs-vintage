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

/*
 * Interface <code>CommunicationConfiguration</code> for Communication environment
 * You must have a communication.xml and communication.dtd in your 
 * classpath for the definition of this context
 */

public class CommunicationConfiguration {

    /**
     * boolean true if the protocol context where load from the comunication.xml file
     */
    private static boolean configurationLoaded = false;

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
    public static String CAROL_FILE_NAME="carol.properties";

    /**
     * jndi properties file name
     */
    public static String JNDI_FILE_NAME="jndi.properties";

    
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
		throw new RMIConfigurationException("Missing " + CAROL_FILE_NAME + " in the CLASSPATH");
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

	loadCarolConfiguration(rmiProps, jndiProps);
    }

    /**
     * This method read a rmi configuration from 2 Properties 
     * @param rmiProps The orbX environment
     * @param jndiProps The jndiX environment
     * @throws RMIConfigurationException if a there is a problem with those environment (field missing for example)
     */ 
      public static synchronized void loadCarolConfiguration(Properties rmiProps, Properties jndiProps) throws RMIConfigurationException {

	Properties jvmProps = new Properties();	    
	jvmProps.putAll(System.getProperties());
 
	String defaultPref = RMIConfiguration.CAROL_PREFIX + "." + RMIConfiguration.RMI_PREFIX + "." + RMIConfiguration.DEFAULT_PREFIX ;
	String jvmPref = RMIConfiguration.CAROL_PREFIX + "." + RMIConfiguration.JVM_PREFIX;
	String rmiPref = RMIConfiguration.CAROL_PREFIX + "." + RMIConfiguration.RMI_PREFIX;
	String jndiPref = RMIConfiguration.CAROL_PREFIX + "." + RMIConfiguration.JNDI_PREFIX;
	String activation_prefix = rmiPref + "."  + RMIConfiguration.ACTIVATION_PREFIX;

    	//Parse the properties
	for (Enumeration e =  rmiProps.propertyNames() ; e.hasMoreElements() ;) {

	    String pkey = (String)e.nextElement();
	    if  (pkey.startsWith(activation_prefix)) { // get default rmi name : the first activated rmi
		StringTokenizer pTok = new StringTokenizer(rmiProps.getProperty(pkey), ",");
		if (pTok.hasMoreTokens()) {
		    defaultRMI = (pTok.nextToken());
		} else {
		   throw new RMIConfigurationException("There is no rmi activated in the file " + CAROL_FILE_NAME); 
		}
	    } else if (pkey.startsWith(jvmPref)) { // jvm properties
		jvmProps.setProperty(pkey.substring(jvmPref.length()+1), rmiProps.getProperty(pkey));	
	    } else if ((pkey.startsWith(rmiPref)) || (pkey.startsWith(jndiPref))) { // this is a carol properties
		StringTokenizer pkeyToken = new StringTokenizer(pkey, ".");
		pkeyToken.nextToken();
		pkeyToken.nextToken();
		String rmiName = pkeyToken.nextToken();
		
		if  (!rmiConfigurationTable.containsKey(rmiName)) {
		    RMIConfiguration rmiConf =  new RMIConfiguration(rmiName, rmiProps, jndiProps);
		    rmiConfigurationTable.put(rmiName, rmiConf);
		}
	    } else { // this is not a carol properties 
		throw new RMIConfigurationException("The properties " + pkey + "can not be set in the file " + CAROL_FILE_NAME);
	    }
	}
 	// add the jvm properties in the jvm 
	System.setProperties(jvmProps);

	configurationLoaded = true;

	// is there a  default protocol activate ? 
	if (defaultRMI==null) {
	    throw new RMIConfigurationException("There is no default RMI");
	} else if (!getDefaultProtocol().isActivate()) {
	    throw new RMIConfigurationException("The default protocol : " + defaultRMI + " must be activate");
	}
      }

    
    /**
     * This method activate a rmi architecture with is name. You need to load the CAROL configuration before using thid method.
     * @param String rmi name 
     * @throws RMIConfigurationException if a the carol configuration is not loaded (by the loadCarolConfiguration method)
     * @throws RMIConfigurationException if the rmi name doesn't exist in the carol configuration
     */
    public static void activateRMI(String rmiName) throws RMIConfigurationException {
	if (!configurationLoaded) {
	    throw new RMIConfigurationException("call for rmi "+rmiName+ " activation with no protocols configuration load \n please, call before the loadCarolConfiguration() static method in this class");
	}
	RMIConfiguration rmiC =  getRMIConfiguration(rmiName);
	if (rmiC == null) {
	     throw new RMIConfigurationException("try to activate a non existant rmi configuration :" +rmiName); 
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
	if (!configurationLoaded) {
	    throw new RMIConfigurationException("call for rmi "+rmiName+ " desactivation with no protocols configuration load \n please, call before the loadCarolConfiguration() static method in this class");
	}
	RMIConfiguration rmiC =  getRMIConfiguration(rmiName);
	if (rmiC == null) {
	     throw new RMIConfigurationException("try to desactivate a non existant rmi configuration :" +rmiName); 
	} else {
	    rmiC.desactivate();
	}
    }

 
}
