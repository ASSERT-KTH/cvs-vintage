/*
 * @(#) DefaultCarolValues.java	1.0 02/07/15
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
import java.util.StringTokenizer;
import java.util.Enumeration;

/*
 * Class <code>DefaultCarolValues</code> get default carol value for the properties file and
 * get carol properties with defaults from jndi Standard properties
 */
public class CarolDefaultValues {

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
    public static String ACTIVATION_PREFIX="activated";    

    /**
     * name service class prefix
     */
    public static String NS_PREFIX="NameServiceClass"; 

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
     * start name service Prefix
     */
    public static String START_NS_PREFIX="start.all.ns";
    
    /**
     * default CAROL Properties with all configuration
     */ 
    private Properties defaultProperties;


    // default for jrmp
    public static String jrmpName="jrmp";
    public static String jrmpJNDIPrefix="rmi";
    public static String jrmpPROD="org.objectweb.carol.rmi.multi.JrmpPRODelegate";
    public static String jrmpNS="org.objectweb.carol.jndi.ns.JRMPRegistry";
    public static Properties jrmpProps = new Properties();

    // default for iiop
    public static String iiopName="iiop";   
    public static String iiopJNDIPrefix="iiop";
    public static String iiopPROD="com.sun.corba.se.internal.javax.rmi.PortableRemoteObject";
    public static String iiopNS="org.objectweb.carol.jndi.ns.IIOPCosNaming";
    public static Properties iiopProps = new Properties();

    //default for jeremie
    public static String jeremieName="jeremie";
    public static String jeremieJNDIPrefix="jrmi";
    public static String jeremiePROD="org.objectweb.carol.rmi.multi.JeremiePRODelegate";
    public static String jeremieNS="org.objectweb.carol.jndi.ns.JeremieRegistry";
    public static Properties jeremieProps = new Properties();

    static {

	// add jrmp default configuration 
	jrmpProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+ACTIVATION_PREFIX,jrmpName); 
	jrmpProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+jrmpName+"."+PRO_PREFIX,jrmpPROD);
	jrmpProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+jrmpName+"."+NS_PREFIX,jrmpNS); 

	// add iiop default configuration
	iiopProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+ACTIVATION_PREFIX,iiopName); 
	iiopProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+iiopName+"."+PRO_PREFIX,iiopPROD);
	iiopProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+iiopName+"."+NS_PREFIX,iiopNS);
	// add jeremie default configuration
	jeremieProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+ACTIVATION_PREFIX,jeremieName); 
	jeremieProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+jeremieName+"."+PRO_PREFIX,jeremiePROD); 
	jeremieProps.setProperty(CAROL_PREFIX+"."+RMI_PREFIX+"."+jeremieName+"."+NS_PREFIX,jeremieNS);
    }

    /**
     * Return a default carol properties link toi the jndi properties
     * @param carol properties (can be null) 
     * @param jndi properties
     * @return carol properties (without the jndi properties)
     * @throws RMIConfigurationException if the jndi property url java.naming.provider.url is not set to one of the default *
     * protocol (iiop, jrmp or jeremie)
     */
    public static Properties getCarolProperties(Properties rmiP, Properties jndiP) throws RMIConfigurationException {
	if (rmiP==null) {	    
	    if (jndiP==null) throw new RMIConfigurationException("No carol or jndi properties found");
	    String url = jndiP.getProperty(URL_PREFIX);
	    if (url != null) {
		String protocol = getRMIProtocol(url);
		if (protocol.equals(jrmpName)) {
		    return jrmpProps;
		} else if (protocol.equals(iiopName)) {
		    return iiopProps;
		} else if (protocol.equals(jeremieName)){
		    return jeremieProps;
		} else  {
		    throw new RMIConfigurationException("Can not load default protocol configuration, rmi protocol unknow:" + protocol);
   		} 
	    } else {
	    	throw new RMIConfigurationException("Rmi protocol unknow, the jndi property " + URL_PREFIX + " is not set");  
	    }
	} else if (!rmiConfigurationExist(rmiP)) {  //rmiP is not null but there is no rmi configuration inside
	    if (jndiP==null) throw new RMIConfigurationException("No carol or jndi properties found");
	    String url = jndiP.getProperty(URL_PREFIX);
	    if (url != null) {
		String protocol = getRMIProtocol(url);
		Properties r = new Properties();
		r.putAll(rmiP);
		if (protocol.equals(jrmpName)) {
		    r.putAll(jrmpProps);
		    return r;
		} else if (protocol.equals(iiopName)) {
		    r.putAll(iiopProps);
		    return r;
		} else if (protocol.equals(jeremieName)){
		    r.putAll(jeremieProps);
		    return r;
		} else  {
		    throw new RMIConfigurationException("Can not load default protocol configuration, rmi protocol unknow:" + protocol);
   		} 
	    } else {
	    	throw new RMIConfigurationException("Rmi protocol unknow, the jndi property " + URL_PREFIX + " is not set");  
	    }
	} else { //rmiP is not null and there is rmi configuration inside
	    return rmiP;
	}		
    }


    /**
     * return false if there is no rmi configuration inside the properties
     * @param p the properties to check
     * @return boolean the result of this check
     */
    public static boolean rmiConfigurationExist(Properties p){
	boolean result = false;
	for (Enumeration e =  p.propertyNames() ; e.hasMoreElements() ;) {
	    String pkey = ((String)e.nextElement()).trim();
	    if  (pkey.startsWith(CAROL_PREFIX+"."+RMI_PREFIX+"."+ACTIVATION_PREFIX)) { 
		//there is 
		result=true;
	    }
	}
	return result;
    }


    /**
     * return protocol name from url
     * @return protocol name
     * @param protocol jndi url
     */
    public static String getRMIProtocol(String url) {
	StringTokenizer st = new StringTokenizer(url, "://");
	String pref = st.nextToken().trim();
	if (pref.equals(jrmpJNDIPrefix)) {
	    return jrmpName;
	} else if (pref.equals(iiopJNDIPrefix)) {
	    return iiopName;
	} else if (pref.equals(jeremieJNDIPrefix)) {
	    return jeremieName;
	} else {
	    return pref;
	}
    }
}
