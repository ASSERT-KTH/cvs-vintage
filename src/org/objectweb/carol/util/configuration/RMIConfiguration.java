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
     * name service for this protocol
     */
    private String nameServiceName = null; 

    /**
     * port number for this protocol name servce
     */
    private int port = 0;

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
	    throw new RMIConfigurationException("The flag " + rmiPref + "." + CarolDefaultValues.PRO_PREFIX + " missing in the configuration file for the rmi name: " + name);
	} else {
	    pro = rmiProperties.getProperty( rmiPref + "." + CarolDefaultValues.PRO_PREFIX ).trim();
	}	
	
	// NameServiceClass flag (not mandatory)	
	if (rmiProperties.getProperty( rmiPref + "." + CarolDefaultValues.NS_PREFIX ) != null) {
	    nameServiceName = rmiProperties.getProperty( rmiPref + "." + CarolDefaultValues.NS_PREFIX ).trim();
	}	

	// search for the jndi configuration file
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
	port = getPortOfUrl(this.jndiProperties.getProperty(CarolDefaultValues.URL_PREFIX));
	
	// log this configuration
	if (TraceCarol.isDebugCarol()) {
	    TraceCarol.debugCarol("RMIConfiguration.RMIConfiguration(String name, Properties rmiProperties, Properties jndiProperties)");
	    String lg = "RMI " + rmiName + " Configuration ";
	    if (activate) {
		lg +=          "is activated";
	    } else {
		lg +=          "is NOT activated";
	    }
	    TraceCarol.debugCarol(lg);
   	    TraceCarol.debugCarol("Portable Remote Object Delegate Class: "+pro);
	    TraceCarol.debugCarol("JNDI Properties={");
	    for (Enumeration e = this.jndiProperties.propertyNames()  ; e.hasMoreElements() ;) {
		String k = (String)e.nextElement();
		if (e.hasMoreElements()) {
		    TraceCarol.debugCarol(k+"="+this.jndiProperties.getProperty(k));
		} else {
		    TraceCarol.debugCarol(k+"="+this.jndiProperties.getProperty(k)+"}");
		}
	    }
	    TraceCarol.debugCarol("name service name=" +nameServiceName);
	    TraceCarol.debugCarol("port number=" +port);
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
     * @return the jndi properties port for this protocol name service
     * -1 if the port is not configured
     */
    public int getPort() {
	return port;
    }

    /**
     * @return the name service class name
     */
    public String getNameService() {
	return nameServiceName;
    }
       
    /**
     * Parses the given url, and returns the port number.
     * 0 is given in error case)
     */
    static int getPortOfUrl(String url) {
	int portNumber = 0;
	try {
	    StringTokenizer st = new StringTokenizer(url,":");
	    st.nextToken();
	    st.nextToken();
	    if (st.hasMoreTokens()) {
		StringTokenizer lastst = new StringTokenizer(st.nextToken(),"/");
		String pts = lastst.nextToken().trim();
		portNumber = new Integer(pts).intValue();
	    }
	    return portNumber;
	} catch (Exception e) {
	    return -1;
	}
    }

}
