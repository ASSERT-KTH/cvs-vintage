/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.metadata.XmlLoadable;
import org.jboss.ejb.InstancePool;

/**
 * Interface for bean instances Pool Feeder
 *
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 *
 * @version $Revision: 1.1 $
 */
public interface InstancePoolFeeder
      extends XmlLoadable
{

   /**
    * Start the pool feeder.
    */
   public void start();

   /**
    * Stop the pool feeder.
    */
   public void stop();

   /**
    * Sets the instance pool inside the pool feeder.
    *
    * @param ip the instance pool
    */
   public void setInstancePool(InstancePool ip);

   /**
    * Tells if the pool feeder is already started.
    * The reason is that we start the PF at first get() on the pool and we
    * want to give a warning to the user when the pool is empty.
    *
    * @return true if started
    */
   public boolean isStarted();

}
