/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import javax.management.ObjectName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.util.SafeObjectNameFactory;

/** 
 * This is the main Service Controller API.
 * 
 * <p>A controller can deploy a service to a jboss.system
 *    It installs by delegating, it configures by delegating
 *
 * @see Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.9 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 *
 * <p><b>20011211 marc fleury:</b>
 * <ul>
 *   <li>API for mbean api install/remove
 *   <li>API for lifecycle of a service create/start/stop/destroy
 * </ul>
 */
public interface ServiceControllerMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME = SafeObjectNameFactory.create("jboss.system", 
                                                         "service", 
                                                         "ServiceController");

   /** Install a service, create the MBean and configure it**/
   ObjectName install(Element mbean) throws Exception;
   
   /** Create the service, service needs to be installed **/
   void create(ObjectName mbean) throws Exception;
   
   /** Start the service **/
   void start(ObjectName mbean) throws Exception;
   
   /** Stop the service **/   
   void stop(ObjectName mbean) throws Exception;
   
   /** Destroy the service, corresponds to create **/   
   void destroy(ObjectName mbean) throws Exception;
 
   /** Remove a service, corresponds to install **/ 
   void remove(Element mbean) throws Exception;
   void remove(ObjectName mbeanName) throws Exception;

   /** Get a list of deployed elements, in the order they were deployed */
   ObjectName[] getDeployed();

   /** Get configuration will output an XML file in the array order */
   String getConfiguration(ObjectName[] objectNames) throws Exception;

   void shutdown();
}
