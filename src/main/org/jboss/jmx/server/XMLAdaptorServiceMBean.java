/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.server;

import org.jboss.system.ServiceMBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* XML Adaptor allows to work on a MBeanServer with
* data specified in an XML foramt.
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
* @created   June 22, 2001
* @version   $Revision: 1.3 $
 */
public interface XMLAdaptorServiceMBean
  extends ServiceMBean
{
  // Constants -----------------------------------------------------
  public static final String OBJECT_NAME = "Adaptor:name=XML";

  // Public --------------------------------------------------------

  public Object[] invokeXML( Document pJmxOperations );

  public Object invokeXML( Element pJmxOperation );

}
