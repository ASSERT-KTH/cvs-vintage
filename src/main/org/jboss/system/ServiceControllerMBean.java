/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.system;

import javax.management.ObjectName;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

/** 
* This is the main Service Controller API.  
* A controller can deploy a service to a JBOSS-SYSTEM
* It installs by delegating, it configures by delegating
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @see org.jboss.system.Service
*
* @version $Revision: 1.1 $
*
*   <p><b>20010830 marc fleury:</b>
*   <ul>
*      initial import
*   <li> 
*   </ul>
*/

public interface ServiceControllerMBean
extends Service
{
   // Public --------------------------------------------------------
   
   /** The default object name. */
   public static final String OBJECT_NAME = "JBOSS-SYSTEM:spine=ServiceController";
   
	// high level calls on the MBean deployment
	public ObjectName deploy(Element mbean) throws Exception;
	public void undeploy(Element mbean) throws Exception;
	public void undeploy(ObjectName mbeanName) throws Exception;
		
	// State calls, init, start, stop, destroy
	public void init(ObjectName mbean) throws Exception;
	public void start(ObjectName mbean) throws Exception;
	public void stop(ObjectName mbean) throws Exception;
	public void destroy(ObjectName mbean) throws Exception;
	
	// Get a list of deployed elements, in the order they were deployed
   public ObjectName[] getDeployed();
	
	// Get configuration will output an XML file in the array order
	public String getConfiguration(ObjectName[] objectNames) throws Exception;
}

/*
* <p><b>Revisions:</b>
* <p><b>2001/06/21 marcf </b>
* <ol>
* <li>Initial version checked in
* </ol>
*/ 

