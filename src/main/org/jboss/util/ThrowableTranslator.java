/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.util;

import javax.management.MBeanException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 * Translates exceptions which don't propertly show child exceptions in stack 
 * traces.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1 $
 */
public class ThrowableTranslator
{
   public static Exception translate(Exception e) {
      //
      // this is in no way complete... or even a good design, but damn
      // those JMX exceptions piss me off!
      //
      
      if (e instanceof MBeanException)
	 return ((MBeanException)e).getTargetException();

      if (e instanceof RuntimeMBeanException)
	 return ((RuntimeMBeanException)e).getTargetException();

      if (e instanceof RuntimeOperationsException)
	 return ((RuntimeOperationsException)e).getTargetException();

      return e;
   }
}
