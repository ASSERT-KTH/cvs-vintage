/*
* jBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.ServerException;

import org.jboss.ejb.MethodInvocation;

import org.jboss.logging.Logger;

/**
 *   This interceptor handles transactions for message BMT beans. 
 *
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 *   @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *   @version $Revision: 1.6 $
 */
public class MessageDrivenTxInterceptorBMT
   extends AbstractTxInterceptorBMT
{
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      throw new ServerException("No home methods for message beans.");
   }

   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      return invokeNext(true, mi);
   }
}
