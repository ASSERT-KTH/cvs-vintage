/*
 * @(#) RmiConfiguration.java	1.0 02/07/15
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
import java.util.Properties;
import java.util.Enumeration;


/*
 * Class <code>RmiConfiguration</code> implement the Properties way 
 * representing the rmi configuration
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class RMIConfiguration {


    /**
     * Carol prefix
     */
    public static String CAROL_PREFIX="carol";

    /**
     * RMI Prefix 
     */
    public static String RMI_PREFIX="rmi";

    /**
     * JNDI Prefix
     */
    public static String JNDI_PREFIX="jndi";

    /**
     * JVM Prefix
     */
    public static String JVM_PREFIX="jvm";

    /**
     * activation Prefix
     */
    public static String ACTIVATION_PREFIX="activate";

    /**
     * default Prefix
     */
    public static String DEFAULT_PREFIX="default";

    /**
     * portable remote object Prefix
     */
    public static String PRO_PREFIX="PortableRemoteObjectClass";

    /**
     * jndi factory Prefix
     */
    public static String FACTORY_PREFIX="java.naming.factory.initial";

    /**
     * jndi url  Prefix
     */
    public static String URL_PREFIX="java.naming.provider.url";

    /**
     * RMI Architecture name
     */
    public String rmiName = null;

    /**
     * boolean for activation
     */
    private boolean activate;
        
    /**
     * Portable Remote Delegate class for this protocol
     */
    private String pro = null;

    /**
     * extra system properties
     */
    private Properties jndiProperties = null;



    /**
     * Constructor,
     * This constructor make a validation 
     * of the properties
     * @param name the RMI architecture name
     * @param rmiProperties The rmi properties, can not be null
     * @param jndiProperties The jndi properties, should be null 
     *                       if the jndi properties informations 
     *                       are set in the rmiProperties
     * @throws RMIConfigurationException if one of the properties below missing:
     * - 
     * - to be set (see the carol specifications)
     * -
     */
    public RMIConfiguration(String name, Properties rmiProperties, Properties jndiProperties) throws RMIConfigurationException {
	String rmiPref = CAROL_PREFIX + "." +  RMI_PREFIX + "." + name;
	String jndiPref = CAROL_PREFIX + "." +  JNDI_PREFIX + "." + name;

	// RMI Properties
	rmiName=name;
	// activation flag
	if (rmiProperties.getProperty( rmiPref + "." + ACTIVATION_PREFIX ) == null) {
	    throw new RMIConfigurationException("The flag " + rmiPref + "." + ACTIVATION_PREFIX + " missing in the configuration file");
	} else {
	    activate = new Boolean(rmiProperties.getProperty( rmiPref + "." + ACTIVATION_PREFIX ).trim()).booleanValue();
	}

	// PortableRemoteObjectClass flag	
	if (rmiProperties.getProperty( rmiPref + "." + PRO_PREFIX ) == null) {
	    throw new RMIConfigurationException("The flag " + rmiPref + "." + PRO_PREFIX + " missing in the configuration file");
	} else {
	    pro = rmiProperties.getProperty( rmiPref + "." + PRO_PREFIX ).trim();
	}	
	
	// jndi properties

	// search for the configuration file
	boolean inRmiFile = false;	
	for (Enumeration e = rmiProperties.propertyNames() ; e.hasMoreElements() ;) {
	    if (((String)e.nextElement()).startsWith(jndiPref)) {
		inRmiFile = true;
		break;
	    }
	}
 
    
	if (inRmiFile) {
	    if (rmiProperties.getProperty(jndiPref + "." + FACTORY_PREFIX ) == null) {
		throw new RMIConfigurationException("The flag " + jndiPref + "." + FACTORY_PREFIX + " missing in the rmi configuration file");
	    }
	    if (rmiProperties.getProperty(jndiPref + "." +  URL_PREFIX ) == null) {
		throw new RMIConfigurationException("The flag " + jndiPref + "." + URL_PREFIX + " missing in the rmi configuration file");
	    }
	    this.jndiProperties= new Properties();
	    for (Enumeration e = rmiProperties.propertyNames() ; e.hasMoreElements() ;) {
		String current = ((String)e.nextElement()).trim();
		if (current.startsWith(jndiPref)) {
		    this.jndiProperties.setProperty(current.substring(jndiPref.length()+1), rmiProperties.getProperty(current));    
		}
	    }
	} else {
	    if (jndiProperties == null) {
		throw new RMIConfigurationException("Missing JNDI Configuration for: " + rmiName);
	    } else {
		this.jndiProperties=jndiProperties;
	    }
	}

    }
  
    /**
     * @return name 
     */
    public String getName() {
	return rmiName;
    }

    /**
     * @return boolean for activation
     */
    public boolean isActivate() {
	return activate;
    }
        
    /**
     * @return Portable Remote Delegate for this protocol
     */
    public String getPro() {
	return pro;
    }

 
    /**
     * @return the jndi properties for this protocol
     */
    public Properties getJndiProperties() {
	return jndiProperties;
    }

    /**
     * to String method return the String for this context
     * @return String environement
     */
    public String toString() {
	String result =                  "RMI " + rmiName + " Configuration: \n";
	if (activate) result +=          "is activate\n";
        result +="Portable Remote Object Delegate Class: "+pro+"\n";
	result +="JNDI Properties =\n" +jndiProperties +"\n";
	return result;
    }
}
