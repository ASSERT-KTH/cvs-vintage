/*
 * @(#) CarolConfiguration.java	1.0 02/07/15
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
import java.util.TreeMap;
import java.util.Iterator;
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
 * Interface <code>CarolConfiguration</code> for Carol environment
 * You must have a communication.xml and communication.dtd in your 
 * classpath for the definition of this context
 */

public class CarolConfiguration {

    /**
     * boolean true if the protocol context where load from thefile
     */
    private static boolean configurationLoaded = false;

    /**
     * boolean to start name server need to launch
     */
    private static boolean startNS;
    /**
     * boolean to start carol rmi
     */
    private static boolean startRMI;
    /**
     * boolean to start carol jndi
     */
    private static boolean startJNDI;

    /**
     * defaults carol properties
     */
    private static Properties defaultsProps = null;

    /**
     * carol properties
     */	
    private static Properties carolProps = null;

    /**
     * jndi properties
     */    
    private static Properties jndiProps = null;
    
    /**
     * String of the actvated RMI
     */
    private static String activated;

    /**
     * Boolean for multi RMI
     */
    private static boolean multiRMI;

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
     * jndi name of the Protocol
     */
    private static String jndiRMIName = null;

    /**
     * carol defaults properties file name 
     */
    public static final String DEFAULTS_FILE_NAME="carol-defaults.properties";

    /**
     * rmi properties file name 
     */
    public static final String CAROL_FILE_NAME="carol.properties";

    /**
     * jndi properties file name
     */
    public static final String JNDI_FILE_NAME="jndi.properties";

    /**
     * init the Carol configuration,
     * A server can call this static method
     * for instantiate the carol communication layer
     */
    public static void initCarol() throws RMIConfigurationException {	
	// init Trace 
	TraceCarol.configure();
	new CarolConfiguration();
    }

    
    /**
     * Constructor 
     * Read the communication context
     */
    public CarolConfiguration() throws RMIConfigurationException {
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
	// init Trace 
	TraceCarol.configure();
	// load the configuration files	
	try {
	    
	    defaultsProps=getDefaultsProperties();
	    carolProps=getCarolProperties();
	    jndiProps=getJndiProperties();
	    
	} catch(Exception e) { 
	    TraceCarol.error("Exception occur when loading default/carol/jndi configuration file: ", e);
        }

	boolean cc = checkCarolConfiguration();
	// Check this properties and load the properties file 
	if (!cc) throw new RMIConfigurationException("Can't start Carol, configuration check fail");
	
	// translate existing jndi properties
	if (jndiProps != null) jndiProps = jndi2Carol(jndiProps);

	// build a general properties object
	Properties allProps = new Properties();
	
	// default properties can not be null (if null, checkCarolConfiguration should stop)
	allProps.putAll(defaultsProps);
	// first the jndi (extented) file 
	if (jndiProps != null) allProps.putAll(jndiProps);
	// second the carol file
	if (carolProps != null) allProps.putAll(carolProps);

	loadCarolConfiguration(allProps);
	
    }

    /**
     * This method read a carol configuration from a Properties object 
     * @throws RMIConfigurationException if a there is a problem with those environment (field missing for example)
     */ 
    public static synchronized void loadCarolConfiguration(Properties allProps) throws RMIConfigurationException {
	
	// prefix
	String rmiPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.RMI_PREFIX;
	String jvmPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JVM_PREFIX;
	
	// get the general properties
	// activated property : if existe use it, else use the jndi url property, else use the default property
	String act=allProps.getProperty(CarolDefaultValues.ACTIVATION_KEY);
	if (act!=null) {
	    activated=act.trim();
	    allProps.remove(CarolDefaultValues.ACTIVATION_KEY);
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("Carol use carol file to activate RMI: " +activated);
	    }
	} else {	   
	    //try the jndi rmi name 
	    if (jndiRMIName!=null) {
		activated=jndiRMIName;
		if (TraceCarol.isDebugCarol()) {
		    TraceCarol.debugCarol("Carol use jndi file to activate RMI: "+activated);
		}
	    } else { //use the default
		activated = allProps.getProperty(CarolDefaultValues.DEFAULT_ACTIVATION_KEY).trim();
		if (TraceCarol.isDebugCarol()) {
		    TraceCarol.debugCarol("Carol use default file to activate RMI "+activated);
		}
	    }
	}
	startNS=new Boolean(allProps.getProperty(CarolDefaultValues.START_NS_KEY).trim()).booleanValue();
        //allProps.remove(CarolDefaultValues.START_NS_KEY);
	
	startRMI=new Boolean(allProps.getProperty(CarolDefaultValues.START_RMI_KEY).trim()).booleanValue();
	//allProps.remove(CarolDefaultValues.START_RMI_KEY);
	
	startJNDI=new Boolean(allProps.getProperty(CarolDefaultValues.START_JNDI_KEY).trim()).booleanValue();
	//allProps.remove(CarolDefaultValues.START_JNDI_KEY);
	
	//get all rmi name
	StringTokenizer pTok = new StringTokenizer(activated, ",");
	if (pTok.countTokens()>1) {
	    multiRMI = true;
	    // get all multi rmi function
	    for (Enumeration e =  allProps.propertyNames() ; e.hasMoreElements() ;) {
		String pkey = ((String)e.nextElement()).trim();
		if (pkey.startsWith(CarolDefaultValues.MULTI_RMI_PREFIX)) {
		    allProps.setProperty(CarolDefaultValues.CAROL_PREFIX + "." +
					 pkey.substring(CarolDefaultValues.MULTI_RMI_PREFIX.length()+1), 
					 (allProps.getProperty(pkey)).trim());
		    // set all multi rmi function
		    allProps.remove(pkey);
		}
	    }
	    
	} else {
	    multiRMI = false;
	    // remove all multi rmi function
	    for (Enumeration e =  allProps.propertyNames() ; e.hasMoreElements() ;) {
		String pkey = ((String)e.nextElement()).trim();
		if (pkey.startsWith(CarolDefaultValues.MULTI_RMI_PREFIX)) {
		    // set all multi rmi function
		    allProps.remove(pkey);
		}
	    }	    
	}

	
	// load all RMI 
	defaultRMI = pTok.nextToken().trim();	
	RMIConfiguration rmiConfDefault =  new RMIConfiguration(defaultRMI, allProps);
	rmiConfigurationTable.put(defaultRMI, rmiConfDefault);
	
	String rmiName;
	while (pTok.hasMoreTokens()) {
	    rmiName = pTok.nextToken().trim();
	    RMIConfiguration rmiConf =  new RMIConfiguration(rmiName, allProps);
	    rmiConfigurationTable.put(rmiName, rmiConf);
	}
	
    	//Parse jvm the properties
	Properties jvmProps = new Properties();	    
	jvmProps.putAll(System.getProperties());
	
	// get all rmi configuration
	for (Enumeration e =  allProps.propertyNames() ; e.hasMoreElements() ;) {
	    String pkey = ((String)e.nextElement()).trim();
	    if (pkey.startsWith(jvmPref)) { // jvm properties
		jvmProps.setProperty(pkey.substring(jvmPref.length()+1), (allProps.getProperty(pkey)).trim());	
	    } 
	}
	
	if (multiRMI) {
	    // Set the system properties
	    if (startRMI) {
		jvmProps.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass",CarolDefaultValues.MULTI_PROD);
	    }
	    
	    if (startJNDI) {
		jvmProps.setProperty("java.naming.factory.initial",CarolDefaultValues.MULTI_JNDI);
	    }
	} else {
	    // Set the system properties for only one protocol
	    if (startRMI) {
		jvmProps.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass",
				   ((RMIConfiguration)rmiConfigurationTable.get(defaultRMI)).getPro());
	    }
	    // Set the system properties for only one protocol
	    if (startJNDI) {
		jvmProps.putAll(((RMIConfiguration)rmiConfigurationTable.get(defaultRMI)).getJndiProperties());
	    }	    
	    
	}


 	// add the jvm properties in the jvm 
	System.setProperties(jvmProps);

	// Trace Carol configuration
	if (TraceCarol.isDebugCarol()) {
	    TraceCarol.debugCarol("Global Carol configuration is:");
	    TraceCarol.debugCarol("Multiple RMI is "+multiRMI);
	    // get all carol configuration
	    // SortedMap of allPorps
	    TreeMap map = new TreeMap(allProps);
	    String k;
	    for (Iterator e =  map.keySet().iterator() ; e.hasNext();) {
		k = (String)e.next();
		TraceCarol.debugCarol(k +"="+ allProps.getProperty(k));	
	    }
	}

	configurationLoaded = true;	
	// start naming service
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
     * private static method mapping jndi properties to carol properties
     * @param jndi properties
     * @return carol jndi properties
     */
    private static Properties jndi2Carol(Properties p) {	
	String jndiPref = CarolDefaultValues.CAROL_PREFIX + "." + CarolDefaultValues.JNDI_PREFIX;
	Properties result = new Properties();
	// get the rmi name 
	jndiRMIName = CarolDefaultValues.getRMIProtocol(p.getProperty(CarolDefaultValues.URL_PREFIX));
	if (jndiRMIName==null) {
	    return null;
	} else {
	    for (Enumeration e = p.propertyNames() ; e.hasMoreElements() ;) {
		String current = ((String)e.nextElement()).trim();
		if (current.trim().equals(CarolDefaultValues.PKGS_PREFIX)) {
		    // pkgs for other context, only in use for all protocol in the jvm
		    result.setProperty(CarolDefaultValues.CAROL_PREFIX+"."+CarolDefaultValues.JVM_PREFIX+"."+current, p.getProperty(current));
		} else if (current.startsWith(jndiPref)) {
		    result.setProperty(jndiPref +"."+jndiRMIName+"."+current, p.getProperty(current));
		}		    
	    }
	}
	return result;
    }

    /**
     * get defaults properties from file
     * @return Properties default properties
    */
    private static Properties getDefaultsProperties() throws Exception {
	Properties defaultsProps = null;
	// load the defaults configuration file
	InputStream defaultsFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(DEFAULTS_FILE_NAME); 
	if (defaultsFileInputStream != null) {
	    defaultsProps = new Properties();
	    defaultsProps.load(defaultsFileInputStream);
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("Default carol file used is " +DEFAULTS_FILE_NAME+" in " 
				      +Thread.currentThread().getContextClassLoader().getSystemResource(DEFAULTS_FILE_NAME).getPath());
	    }
	} else {
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("No "+DEFAULTS_FILE_NAME+" file found");
	    }
	}
	return defaultsProps;
    }

    /**
     * get carol properties from file
     * @return Properties carol properties
    */
    private static Properties getCarolProperties() throws Exception {
	Properties carolProps=null;
	// load the defaults configuration file
	InputStream carolFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(CAROL_FILE_NAME); 
	if (carolFileInputStream != null) {
	    carolProps= new Properties();
	    carolProps.load(carolFileInputStream);
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("Carol file used is " +CAROL_FILE_NAME+" in " 
				      +Thread.currentThread().getContextClassLoader().getSystemResource(CAROL_FILE_NAME).getPath());
	    }
	} else {
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("No "+CAROL_FILE_NAME+" file found");
	    }
	}
	return carolProps;
    }

    /**
     * get jndi properties from file
     * @return Properties default properties
    */
    private static Properties getJndiProperties() throws Exception {
	Properties jndiProps=null;
	// load the jndi configuration file
	InputStream jndiFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(JNDI_FILE_NAME); 
	if (jndiFileInputStream != null) {
	    jndiProps= new Properties();
	    jndiProps.load(jndiFileInputStream);
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("Jndi file used is " +JNDI_FILE_NAME+" in " 
				      +Thread.currentThread().getContextClassLoader().getSystemResource(JNDI_FILE_NAME).getPath());
	    }
	} else {
	    if (TraceCarol.isDebugCarol()) {
		TraceCarol.debugCarol("No "+JNDI_FILE_NAME+" file found");
	    }
	}
	return jndiProps;
    }

    /**
     * public static boolean check communication configuration method
     * @param carol properties
     * @return boolean true if the configuration seam to be ok
     */
    public static boolean checkCarolConfiguration() {
	boolean result = true;
	
	//check if there is a default properties 
	if (defaultsProps == null) result=false;

	//this is a carol check with 
	return true;
    }

    /**
     * public static boolean check communication configuration method
     * @return boolean true if the configuration seam to be ok
     */
    public static String getCarolConfiguration() {
	String result="";
	Properties dProps=null;
	Properties cProps=null;
	Properties jProps=null;

	try {

	    InputStream defaultsFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(DEFAULTS_FILE_NAME); 
	    if (defaultsFileInputStream != null) {
		dProps= new Properties();
		dProps.load(defaultsFileInputStream);
		result+="Defaults file used is " +DEFAULTS_FILE_NAME+" in " 
		    +Thread.currentThread().getContextClassLoader().getSystemResource(DEFAULTS_FILE_NAME).getPath()+"\n";
	    } else {
		result+="ERROR: No "+DEFAULTS_FILE_NAME+" file found\n";
	    }

	    InputStream jndiFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(JNDI_FILE_NAME); 
	    if (jndiFileInputStream != null) {
		jProps= new Properties();
		jProps.load(jndiFileInputStream);
		result+="Jndi file used is " +JNDI_FILE_NAME+" in " 
		    +Thread.currentThread().getContextClassLoader().getSystemResource(JNDI_FILE_NAME).getPath()+"\n";
	    } else {
		result+="No "+JNDI_FILE_NAME+" file found\n";
	    }

	    InputStream carolFileInputStream  =  Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(CAROL_FILE_NAME); 
	    if (carolFileInputStream != null) {
		cProps= new Properties();
		cProps.load(carolFileInputStream);
		result+="Carol file used is " +CAROL_FILE_NAME+" in " 
		    +Thread.currentThread().getContextClassLoader().getSystemResource(CAROL_FILE_NAME).getPath()+"\n";
	    } else {
		result+="No "+CAROL_FILE_NAME+" file found\n";
	    }

	} catch (Exception e) {
	    result+="There is a problem with the configuration loading:" + e;
	}

	//check if there is a default properties 
	if (defaultsProps == null) result+="Default carol configuration file missing\n";
	// build a general properties object
	Properties allProps = new Properties();
	
	// default properties can not be null (if null, checkCarolConfiguration should stop)
	if (defaultsProps != null) allProps.putAll(defaultsProps);
	// first the jndi (extented) file 
	if (jndiProps != null) allProps.putAll(jndiProps);
	// second the carol file
	if (carolProps != null) allProps.putAll(carolProps);
	result+="Global Carol configuration is:";
	// get all carol configuration
	// SortedMap of allPorps
	TreeMap map = new TreeMap(allProps);
	String k;
	for (Iterator e =  map.keySet().iterator() ; e.hasNext();) {
	    k = (String)e.next();
	    result+=k+"="+ allProps.getProperty(k);	
	}
	return result;
    }

}
