/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import org.jboss.util.NestedException;

/**
 * Thrown by a deployer if an application component could not be
 * deployed.
 *
 * @see DeployerMBean
 * 
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.6 $
 */
public class DeploymentException
   extends NestedException
{
   /**
    * Construct a <tt>DeploymentException</tt> with the specified detail 
    * message.
    *
    * @param msg  Detail message.
    */
   public DeploymentException(String msg) {
      super(msg);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with the specified detail 
    * message and nested <tt>Throwable</tt>.
    *
    * @param msg     Detail message.
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public DeploymentException(String msg, Throwable nested) {
      super(msg, nested);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with the specified
    * nested <tt>Throwable</tt>.
    *
    * @param nested  Nested <tt>Throwable</tt>.
    */
   public DeploymentException(Throwable nested) {
      super(nested);
   }

   /**
    * Construct a <tt>DeploymentException</tt> with no detail.
    */
   public DeploymentException() {
      super();
   }
}
