/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.net.URL;

/**
 * The management interface for the {@link URLClassLoader} mbean.
 * 
 * @author <a href="marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 */
public interface UnifiedClassLoaderMBean 
{
   public URL getURL();
   // Empty, just a trick for the MBean base man jmx is dumb sometimes
}

