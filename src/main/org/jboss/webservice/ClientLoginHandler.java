/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ClientLoginHandler.java,v 1.1 2004/04/28 14:34:52 tdiesler Exp $
package org.jboss.webservice;

// $Id: ClientLoginHandler.java,v 1.1 2004/04/28 14:34:52 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociation;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
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
public class ClientLoginHandler extends GenericHandler
{
   // provide logging
   private static final Logger log = Logger.getLogger(ClientLoginHandler.class);

   // The headers known to this handler
   private static QName[] headerNames = {
      new QName(Constants.WS4EE_NAMESPACE_URI, "username"),
      new QName(Constants.WS4EE_NAMESPACE_URI, "password")
   };

   /**
    * Gets the header blocks processed by this Handler instance.
    *
    * new QName("http://webservice.jboss.com/ws4ee", "username")
    * new QName("http://webservice.jboss.com/ws4ee", "password")
    *
    * @return Array of QNames of header blocks processed by this handler instance.
    * QName is the qualified name of the outermost element of the Header block.
    */
   public QName[] getHeaders()
   {
      return headerNames;
   }

   /**
    * Get principal/credential from the SecurityAssociation and add them as SOAP header elements.
    * @param context the message context
    * @return true/false
    */
   public boolean handleRequest(MessageContext context)
   {
      Principal principal = SecurityAssociation.getPrincipal();
      Object credential = SecurityAssociation.getCredential();

      try
      {
         SOAPMessageContext soapCtx = (SOAPMessageContext)context;
         SOAPMessage soapMessage = soapCtx.getMessage();
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
         log.error ("Cannot handle request: " + e.toString());
         throw new JAXRPCException(e);
      }

      return true;
   }
}
