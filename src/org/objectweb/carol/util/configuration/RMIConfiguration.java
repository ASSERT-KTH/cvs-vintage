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
     * Portable Remote Delegate class for this protocol
     */
    private String pro = null;

    /**
     * Intitail JNDI factory class for this protocol
     */
    private String factory = null;

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
    public RMIConfiguration(String name, Properties carolProperties) throws RMIConfigurationException {

	String rmiPref = CarolDefaultValues.CAROL_PREFIX + "." +  CarolDefaultValues.RMI_PREFIX + "." + name;
	String jndiPref = CarolDefaultValues.CAROL_PREFIX + "." +  CarolDefaultValues.JNDI_PREFIX + "." + name;

	// RMI Properties
	rmiName=name;

	// PortableRemoteObjectClass flag	
	pro = carolProperties.getProperty( rmiPref + "." + CarolDefaultValues.PRO_PREFIX ).trim();	
	
	// NameServiceClass flag (not mandatory)	
	if (carolProperties.getProperty( rmiPref + "." + CarolDefaultValues.NS_PREFIX ) != null) {
	    nameServiceName = carolProperties.getProperty( rmiPref + "." + CarolDefaultValues.NS_PREFIX ).trim();
	}	
    
	
	this.jndiProperties= new Properties();

	for (Enumeration e = carolProperties.propertyNames() ; e.hasMoreElements() ;) {
	    String current = ((String)e.nextElement()).trim();
	    if ((!current.trim().equals(CarolDefaultValues.PKGS_PREFIX))&&(current.startsWith(jndiPref))) {
		jndiProperties.setProperty(current.substring(jndiPref.length()+1), carolProperties.getProperty(current));
	    }		    
	}

	
	port = getPortOfUrl(this.jndiProperties.getProperty(CarolDefaultValues.URL_PREFIX));
    }
  
    /**
     * @return name 
     */
    public String getName() {
	return rmiName;
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
