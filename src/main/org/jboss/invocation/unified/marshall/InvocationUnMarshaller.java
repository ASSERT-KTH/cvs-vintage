/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.unified.marshall;

import org.jboss.remoting.marshal.serializable.SerializableUnMarshaller;
import org.jboss.remoting.InvocationRequest;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.tm.TransactionPropagationContextUtil;
import org.jboss.tm.TransactionPropagationContextImporter;

import java.io.InputStream;
import java.io.IOException;

/**
 * This is a hollow implementation in that it only over rides the DATATYPE
 * value.  All behavior is that of SerializableUnMarshaller.
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class InvocationUnMarshaller extends SerializableUnMarshaller
{
   public final static String DATATYPE = "invoker";

   public Object read(InputStream inputStream) throws IOException, ClassNotFoundException
   {
      Object ret = super.read(inputStream);

      if(ret instanceof InvocationRequest)
      {
         InvocationRequest remoteInv = (InvocationRequest) ret;
         Object param = remoteInv.getParameter();

         if(param instanceof MarshalledInvocation)
         {
            MarshalledInvocation mi = (MarshalledInvocation) param;
            TransactionPropagationContextImporter tpcImporter = TransactionPropagationContextUtil.getTPCImporter();
            mi.setTransaction(tpcImporter.importTransactionPropagationContext(mi.getTransactionPropagationContext()));
         }
      }

      return ret;
   }
}