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
 * @version $Revision: 1.5 $
 */
public interface URLClassLoaderMBean 
{
   URL getKeyURL();
}

