/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ClientLoginHandler.java,v 1.2 2004/04/30 07:41:26 tdiesler Exp $
package org.jboss.webservice;

// $Id: ClientLoginHandler.java,v 1.2 2004/04/30 07:41:26 tdiesler Exp $

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import java.security.Principal;

/**
 * Add username/password from the SecurityAssociation as
 * SOAP header elements.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-April-2004
 */
public class ClientLoginHandler extends BasicHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(ClientLoginHandler.class);

   /**
    * Invoke is called to do the actual work of the Handler object.
    * If there is a fault during the processing of this method it is
    * invoke's job to catch the exception and undo any partial work
    * that has been completed.  Once we leave 'invoke' if a fault
    * is thrown, this classes 'onFault' method will be called.
    * Invoke should rethrow any exceptions it catches, wrapped in
    * an AxisFault.
    *
    * @param msgContext the <code>MessageContext</code> to process with this <code>Handler</code>.
    * @throws org.apache.axis.AxisFault if the handler encounters an error
    */
   public void invoke(MessageContext msgContext) throws AxisFault
   {
      Principal principal = SecurityAssociation.getPrincipal();
      Object credential = SecurityAssociation.getCredential();

      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPPart().getEnvelope().getHeader();
         SOAPFactory soapFactory = SOAPFactory.newInstance();

         if (principal != null)
         {
            Name usrName = soapFactory.createName("username", "jbws", Constants.WS4EE_NAMESPACE_URI);
            SOAPHeaderElement usrElement = soapHeader.addHeaderElement(usrName);
            usrElement.setActor(Constants.WS4EE_NAMESPACE_URI + "/login");
            usrElement.addTextNode(principal.getName());
         }

         if (credential != null)
         {
            Name pwdName = soapFactory.createName("password", "jbws", Constants.WS4EE_NAMESPACE_URI);
            SOAPHeaderElement pwdElement = soapHeader.addHeaderElement(pwdName);
            pwdElement.setActor(Constants.WS4EE_NAMESPACE_URI + "/login");
            pwdElement.addTextNode(credential.toString());
         }
      }
      catch (SOAPException e)
      {
         log.error("Client login failed: " + e.toString());
         throw new AxisFault("Client login failed", e);
      }
   }
}
