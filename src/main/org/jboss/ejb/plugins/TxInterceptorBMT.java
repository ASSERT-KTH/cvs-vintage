/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * This interceptor handles transactions for session BMT beans.
 *
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.27 $
 */
public final class TxInterceptorBMT extends TxInterceptorCMT
{


   //public void create() throws Exception
   public void start() throws Exception
   {
      // Do initialization in superclass.
      super.start();

   }


   //public void destroy()
   public void stop()
   {
      super.stop();
   }
}
