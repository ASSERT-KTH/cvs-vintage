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
import java.util.StringTokenizer;


/*
 * Class <code>RmiConfiguration</code> implement the Properties way 
 * representing the rmi configuration
 *
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class RMIConfiguration {


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
	String rmiPref = CarolDefaultValues.CAROL_PREFIX + "." +  CarolDefaultValues.RMI_PREFIX + "." + name;
	String jndiPref = CarolDefaultValues.CAROL_PREFIX + "." +  CarolDefaultValues.JNDI_PREFIX + "." + name;
	String activatedPref = CarolDefaultValues.CAROL_PREFIX + "." +  CarolDefaultValues.RMI_PREFIX + "." + CarolDefaultValues.ACTIVATION_PREFIX;
	

	// RMI Properties
	rmiName=name;
	// activation flag
	// search if the rmi name existe in the activated prefix
	if (rmiProperties.getProperty( activatedPref ) == null) {
	    throw new RMIConfigurationException("The flag " + activatedPref + " missing in the configuration file");
	} else {
	    // use a String Tokennizer to parse the properties
	    activate = false;
	    StringTokenizer st = new StringTokenizer(rmiProperties.getProperty(activatedPref), ",");
	    while (st.hasMoreTokens()) {
		if (((st.nextToken()).trim()).equals(name)) activate = true;
	    }
	}

	// PortableRemoteObjectClass flag	
	if (rmiProperties.getProperty( rmiPref + "." + CarolDefaultValues.PRO_PREFIX ) == null) {
	    throw new RMIConfigurationException("The flag " + rmiPref + "." + CarolDefaultValues.PRO_PREFIX + " missing in the configuration file");
	} else {
	    pro = rmiProperties.getProperty( rmiPref + "." + CarolDefaultValues.PRO_PREFIX ).trim();
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
	    if (rmiProperties.getProperty(jndiPref + "." + CarolDefaultValues.FACTORY_PREFIX ) == null) {
		throw new RMIConfigurationException("The flag " + jndiPref + "." + CarolDefaultValues.FACTORY_PREFIX + " missing in the rmi configuration file");
	    }
	    if (rmiProperties.getProperty(jndiPref + "." +  CarolDefaultValues.URL_PREFIX ) == null) {
		throw new RMIConfigurationException("The flag " + jndiPref + "." + CarolDefaultValues.URL_PREFIX + " missing in the rmi configuration file");
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
     * activate this rmi
     */
    public void activate() {
	activate = true;
    }

    /**
     * desactivate this rmi
     */
    public void desactivate() {
	activate = false;
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
	if (activate) {
	    result +=          "is activated\n";
	} else {
	    result +=          "is NOT activated\n";
	}
        result +="Portable Remote Object Delegate Class: "+pro+"\n";
	result +="JNDI Properties =\n" +jndiProperties +"\n";
	return result;
    }
}
