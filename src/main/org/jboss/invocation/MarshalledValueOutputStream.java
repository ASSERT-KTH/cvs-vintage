/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;

/**
 * An ObjectOutputStream subclass used by the MarshalledValue class to
 * ensure the classes and proxies are loaded using the thread context
 * class loader. Currently this does not do anything as neither class or
 * proxy annotations are used.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public class MarshalledValueOutputStream
   extends ObjectOutputStream
{
   /**
    * Creates a new instance of MarshalledValueOutputStream
    */
   public MarshalledValueOutputStream(OutputStream os) throws IOException
   {
      super(os);
   }

   /**
    * @throws IOException   Any exception thrown by the underlying OutputStream.
    */
   protected void annotateClass(Class cl) throws IOException
   {
      super.annotateClass(cl);
   }
   
   /**
    * @throws IOException   Any exception thrown by the underlying OutputStream.
    */
   protected void annotateProxyClass(Class cl) throws IOException
   {
      super.annotateProxyClass(cl);
   }
}
