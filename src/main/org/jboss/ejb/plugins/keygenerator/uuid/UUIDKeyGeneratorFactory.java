/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.uuid;

import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.util.naming.Util;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.Serializable;

/**
 * This is the factory for UUID key generator
 *
 * @author <a href="mailto:loubyansky@ukr.net">Alex Loubyansky</a>
 *
 * @version $Revision: 1.2 $
 */
public class UUIDKeyGeneratorFactory
   extends ServiceMBeanSupport
   implements KeyGeneratorFactory, UUIDKeyGeneratorFactoryMBean, Serializable
{
   // Attributes ---------------------------------------------------
   private String factoryName;

   // UUIDKeyGeneratorFactoryMBean implementation ------------------
   public void setFactoryName(String factoryName)
   {
      this.factoryName = factoryName;
   }

   /**
    * Returns the factory name
    */
   public String getFactoryName()
   {
      return factoryName;
   }

   // KeyGeneratorFactory implementation ---------------------------
   /**
    * Returns a newly constructed key generator
    */
   public KeyGenerator getKeyGenerator()
      throws Exception
   {
      return new UUIDKeyGenerator();
   }

   // ServiceMBeanSupport overridding ------------------------------
   public void startService()
   {
      // bind the factory
      try
      {
         Context ctx = new InitialContext();
         Util.rebind(ctx, factoryName, this);
      }
      catch(Exception e)
      {
         log.error("Caught exception during startService()", e);
      }
   }

   public void stopService()
   {
      // unbind the factory
      try
      {
         Context ctx = new InitialContext();
         Util.unbind(ctx, factoryName);
      }
      catch(Exception e)
      {
         log.error("Caught exception during stopService()", e);
      }
   }
}
