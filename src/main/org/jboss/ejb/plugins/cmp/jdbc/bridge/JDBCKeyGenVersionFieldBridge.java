/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;
import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.deployment.DeploymentException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 */
public class JDBCKeyGenVersionFieldBridge extends JDBCCMP2xVersionFieldBridge
{
   private final KeyGenerator keyGenerator;

   public JDBCKeyGenVersionFieldBridge(JDBCStoreManager manager,
                                       JDBCCMPFieldMetaData metadata,
                                       String keygenFactoryName)
      throws DeploymentException
   {
      super(manager, metadata);

      try
      {
         InitialContext ctx = new InitialContext();
         KeyGeneratorFactory keygenFactory = (KeyGeneratorFactory)
            ctx.lookup(keygenFactoryName);
         keyGenerator = keygenFactory.getKeyGenerator();
      }
      catch(NamingException e)
      {
         throw new DeploymentException("Could not lookup key generator factory: "
            + keygenFactoryName, e);
      }
      catch(Exception e)
      {
         throw new DeploymentException("Could not create KeyGenerator instance.", e);
      }
   }

   public void setFirstVersion(EntityEnterpriseContext ctx)
   {
      Object version = keyGenerator.generateKey();
      setInstanceValue(ctx, version);
   }

   public Object updateVersion(EntityEnterpriseContext ctx)
   {
      Object next = keyGenerator.generateKey();
      setInstanceValue(ctx, next);
      return next;
   }
}
