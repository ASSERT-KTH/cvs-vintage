/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.adaptor.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * XML Test-Adaptor allows to work on a MBeanServer with
 * data specified in an XML foramt.
 *
 * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
 * @created   June 22, 2001
 * @version   $Revision: 1.2 $
 */
public interface XMLTestServiceMBean
  extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("test:name=XML");
}
