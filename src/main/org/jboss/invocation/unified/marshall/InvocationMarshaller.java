/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.unified.marshall;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.marshal.serializable.SerializableMarshaller;
import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextUtil;

import javax.transaction.SystemException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This marshaller is to be used in conjunction with the UnifiedInvoker and will
 * look for an InvocationRequest to be passed to it, which is specific to EJB
 * invocations.
 *
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 */
public class InvocationMarshaller extends SerializableMarshaller
{
   public final static String DATATYPE = "invocation";

   private static final Logger log = Logger.getLogger(InvocationMarshaller.class);

   /**
    * Marshaller will need to take the dataObject and convert
    * into primitive java data types and write to the
    * given output.  Will check to see if dataObject being passed is
    * an InvocationRequest, and if is, process it (including handling propagation of
    * transaction).  If is not an instance of InvocationRequest, will default back to
    * SerializableMarshaller for processing.
    *
    * @param dataObject Object to be writen to output
    * @param output     The data output to write the object
    *                   data to.
    */
   public void write(Object dataObject, OutputStream output) throws IOException
   {
      if(dataObject instanceof InvocationRequest)
      {
         InvocationRequest remoteInv = (InvocationRequest) dataObject;

         if(remoteInv.getParameter() instanceof Invocation)
         {
            Invocation inv = (Invocation) remoteInv.getParameter();

            MarshalledInvocation marshInv = new MarshalledInvocation(inv);

            if(inv != null)
            {
               // now that have invocation object related to ejb invocations,
               // need to get the possible known payload objects and make sure
               // they get serialized.

               try
               {
                  marshInv.setTransactionPropagationContext(getTransactionPropagationContext());
               }
               catch(SystemException e)
               {
                  log.error("Error setting transaction propagation context.", e);
                  throw new IOException("Error setting transaction context.  Message: " + e.getMessage());
               }

               // reset the invocation parameter within remote invocation
               remoteInv.setParameter(marshInv);
            }
            else
            {
               //Should never get here, but will check anyways
               log.error("Attempting to marshall Invocation but is null.  Can not proceed.");
               throw new IOException("Can not process data object due to the InvocationRequest's parameter being null.");
            }

            super.write(dataObject, output);
         }
         else
         {
            log.error("Attempting to marshall Invocation but InvocationRequest parameter was not of type Invocation.  Can not proceed.");
            throw new IOException("Can not process data object due to the InvocationRequest's parameter was not of type Invocation.");
         }
      }
      else  // assume this is going to be the response
      {
         super.write(dataObject, output);
      }
   }

   public Object getTransactionPropagationContext()
         throws SystemException
   {
      TransactionPropagationContextFactory tpcFactory = TransactionPropagationContextUtil.getTPCFactoryClientSide();
      return (tpcFactory == null) ? null : tpcFactory.getTransactionPropagationContext();
   }

}