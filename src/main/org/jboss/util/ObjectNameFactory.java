/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.util;

import java.util.Hashtable;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.system.BootstrapLogger;
   
/**
 * A simple factory for creating safe object names.  This factory
 * will <b>not</b> throw MalformedObjectNameException.  Any such 
 * exceptions will be translated into Errors.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1 $
 */
public class ObjectNameFactory
{
   /** Class logger */
   private static final BootstrapLogger log = 
      BootstrapLogger.getLogger(ObjectNameFactory.class);
   
   public static ObjectName create(String name) {
      try {
	 return new ObjectName(name);
      }
      catch (MalformedObjectNameException e) {
         log.fatal("Invalid ObjectName: " + name, e);
	 throw new Error("Invalid ObjectName: " + name + "; " + e);
      }
   }

   public static ObjectName create(String domain, String key, String value) {
      try {
	 return new ObjectName(domain, key, value);
      }
      catch (MalformedObjectNameException e) {
         log.fatal("Invalid ObjectName: " + domain + "," + key + "," + value, e);
	 throw new Error("Invalid ObjectName: " + domain + "," + key + "," + value + "; " + e);
      }
   }

   public static ObjectName create(String domain, Hashtable table) {
      try {
	 return new ObjectName(domain, table);
      }
      catch (MalformedObjectNameException e) {
         log.fatal("Invalid ObjectName: " + domain + "," + table, e);
	 throw new Error("Invalid ObjectName: " + domain + "," + table + "; " + e);
      }
   }
}
