/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

/**
 * Cache implementation that registers with an InvalidationManager when in
 * commit option A or D. Information is found in the EB meta-data (IM name,
 * IG name and commit-option)
 *
 * @see org.jboss.cache.invalidation.InvalidationManagerMBean
 * @see org.jboss.cache.invalidation.triggers.EntityBeanCacheBatchInvalidatorInterceptor
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.2 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>26 septembre 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public class InvalidableEntityInstanceCache 
   extends org.jboss.ejb.plugins.EntityInstanceCache 
   implements org.jboss.cache.invalidation.Invalidatable
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------      
   
   protected org.jboss.cache.invalidation.InvalidationManagerMBean invalMgr = null;
   protected org.jboss.cache.invalidation.InvalidationGroup ig = null;
   
   protected boolean isTraceEnabled = false;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public InvalidableEntityInstanceCache () { super (); }
   
   // Public --------------------------------------------------------
   
   // Invalidatable implementation ----------------------------------------------
   
   public void areInvalid (java.io.Serializable[] keys)
   {
      if (this.isTraceEnabled)
         log.trace ("Invalidating entry in cache. Quantity: " + keys.length);
      
      for (int i=0; i<keys.length; i++)
      {
         try
         {
            doInvalidate (keys[i]);
         }
         catch (Exception ignored) {log.debug (ignored);}
      }
   }
   
   public void isInvalid (java.io.Serializable key)
   {
     try
      {
         doInvalidate (key);
      }
      catch (Exception ignored) {log.debug (ignored);}
   }
   
   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   public void start () throws Exception
   {
      super.start ();
      
      log.debug ("Starting InvalidableEntityInstanceCache...");
      
      org.jboss.metadata.EntityMetaData emd = ((org.jboss.metadata.EntityMetaData)this.getContainer ().getBeanMetaData ());
      
      boolean participateInDistInvalidations = emd.doDistributedCacheInvalidations ();      
      byte co = emd.getContainerConfiguration ().getCommitOption ();
      
      if (participateInDistInvalidations &&
           (co == org.jboss.metadata.ConfigurationMetaData.A_COMMIT_OPTION || co == org.jboss.metadata.ConfigurationMetaData.D_COMMIT_OPTION) 
         )
      {
         // we are interested in receiving cache invalidation callbacks
         //
         String groupName = emd.getDistributedCacheInvalidationConfig ().getInvalidationGroupName ();
         String imName = emd.getDistributedCacheInvalidationConfig ().getInvalidationManagerName ();
         
         this.invalMgr = (org.jboss.cache.invalidation.InvalidationManagerMBean)org.jboss.system.Registry.lookup (imName);
         
         this.ig = this.invalMgr.getInvalidationGroup (groupName);
         
         this.ig.register (this);
         
         this.isTraceEnabled = log.isTraceEnabled ();
      }
      
   }
   
   public void stop ()
   {      
      try
      {
         this.ig.unregister (this);
         this.ig = null;
         this.invalMgr = null;
      }
      catch (Exception e)
      {
         log.debug (e);
      }
      
      super.stop ();
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void doInvalidate (java.io.Serializable key)
   {
      if (key == null)
         return;      
      else
            remove (key);
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
