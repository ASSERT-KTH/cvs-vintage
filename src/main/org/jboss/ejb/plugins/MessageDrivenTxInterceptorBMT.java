/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.util.Map;
import java.rmi.ServerException;

import org.jboss.invocation.Invocation;


/**
 *   This interceptor handles transactions for message BMT beans.
 *
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 *   @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *   @version $Revision: 1.10 $
 */
public class MessageDrivenTxInterceptorBMT
   extends AbstractTxInterceptorBMT
{
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      throw new ServerException("No home methods for message beans.");
   }

   public Object invoke(Invocation mi)
      throws Exception
   {
      return invokeNext(true, mi);
   }
  // Monitorable implementation ------------------------------------
  public void sample(Object s)
  {
    // Just here to because Monitorable request it but will be removed soon
  }
  public Map retrieveStatistic()
  {
    return null;
  }
  public void resetStatistic()
  {
  }
}
