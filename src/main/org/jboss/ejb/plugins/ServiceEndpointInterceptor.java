/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

// $Id: ServiceEndpointInterceptor.java,v 1.1 2004/05/07 14:58:48 tdiesler Exp $

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.webservice.Constants;

import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.soap.SOAPMessageContext;

/**
 * This Interceptor does the ws4ee handler processing.
 *
 * According to the ws4ee spec the handler logic must be invoked after the container
 * applient method level security to the invocation. I don't think we can use Axis handlers
 * for ws4ee handler processing.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class ServiceEndpointInterceptor
        extends AbstractInterceptor
{
   // Interceptor implementation --------------------------------------
   
   public Object invoke(final Invocation mi) throws Exception
   {
      SOAPMessageContext msgContext = (SOAPMessageContext)mi.getPayloadValue(InvocationKey.SOAP_MESSAGE_CONTEXT);

      // nothing to do
      if (msgContext == null)
      {
         return getNext().invoke(mi);
      }

      HandlerChain handlerChain = (HandlerChain)msgContext.getProperty(Constants.HANDLER_CHAIN);
      if (handlerChain == null)
         throw new IllegalStateException("Cannot obtain handler chain from msg context");

      Object obj = null;
      try
      {
         if (handlerChain.handleRequest(msgContext))
         {
            obj = getNext().invoke(mi);
         }
         handlerChain.handleResponse(msgContext);
      }
      catch (Exception e)
      {
         msgContext.setProperty(Constants.LAST_FAULT, e);
         handlerChain.handleFault(msgContext);
         throw e;
      }

      return obj;
   }
}
