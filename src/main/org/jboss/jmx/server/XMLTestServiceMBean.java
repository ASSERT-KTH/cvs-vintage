/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.server;

import org.jboss.util.ServiceMBean;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* XML Test-Adaptor allows to work on a MBeanServer with
* data specified in an XML foramt.
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
* @created   June 22, 2001
* @version   $Revision: 1.1 $
 */
public interface XMLTestServiceMBean
  extends ServiceMBean
{
  // Constants -----------------------------------------------------
  public static final String OBJECT_NAME = "test:name=XML";

  // Public --------------------------------------------------------

}
