/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.net.URL;

import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;

/**
 * The management interface for the {@link ServiceLibraries} MBean.
 *
 * @see ServiceLibraries
 * 
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.5 $
 *
 * <p><b>Revisions:</b>
 * <p><b>2001/06/22 , marcf</b>:
 * <ul> 
 *    <li>Initial import
 * </ul>
 */
public interface ServiceLibrariesMBean
{
   ObjectName OBJECT_NAME =
      ObjectNameFactory.create("jboss.system", "service", "Libraries");

   // The ServicesLibraries MBean should expose "soft" information like
   // the dependencies graph
   // for example give a URL and find out what MBean need to be restarted
   // in case you cycle that URL

   /**
    * Obtain a listing of the URL for all UnifiedClassLoaders associated with
    * the ServiceLibraries
    */
   URL[] getURLs();
}
