/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.util.Map;

/**
 * The management interface for the Info bean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:marc.fleurY@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.4 $
 */
public interface InfoMBean
{
   String OBJECT_NAME = ":service=Info";

   String getHostName();

   String getHostAddress();
   
   Map showProperties();
   
   String showThreads();
}
