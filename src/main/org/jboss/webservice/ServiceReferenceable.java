/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: ServiceReferenceable.java,v 1.2 2004/05/04 08:48:43 tdiesler Exp $
package org.jboss.webservice;

// $Id: ServiceReferenceable.java,v 1.2 2004/05/04 08:48:43 tdiesler Exp $

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.xml.namespace.QName;
import java.net.URL;

/**
 * A JNDI reference to a javax.xml.rpc.Service
 *
 * It holds the String information to reconstrut the javax.xml.rpc.Service
 * when the client does a JNDI lookup.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-April-2004
 */
public class ServiceReferenceable implements Referenceable
{
   public static final String SERVICE_ENDPOINT_INTERFACE = "SERVICE_ENDPOINT_INTERFACE";
   public static final String SERVICE_NAMESPACE = "SERVICE_NAMESPACE";
   public static final String SERVICE_LOCALPART = "SERVICE_LOCALPART";
   public static final String DEPLOYMENT_URL = "LOCAL_DEPLOYMENT_URL";
   public static final String WSDL_OVERRIDE_URL = "WSDL_OVERRIDE_URL";
   public static final String WSDL_FILE = "WSDL_FILE";

   // The mandatory service interface
   private String serviceInterface;

   // The optional service name
   private QName serviceName;

   // The deployment URL of the web service client deployment and the wsdl file contained within
   private URL deploymentUrl;
   private String wsdlFile;

   // The wsdl override
   private URL wsdlOverride;

   /** A service referenceable for an empty Service,
    * the client is expected to fill in the details.
    * @param sei The service endpoint interface class name
    */
   public ServiceReferenceable(String sei)
   {
      this.serviceInterface = sei;
   }

   /** A service referenceable for a direct URL to the WSDL document
    * @param sei The service endpoint interface class name
    * @param serviceName The QName of the service
    * @param wsdlOverride The URL to the actual WSDL file
    */
   public ServiceReferenceable(String sei, QName serviceName, URL wsdlOverride)
   {
      this.serviceInterface = sei;
      this.wsdlOverride = wsdlOverride;
      this.serviceName = serviceName;
   }

   /** A service referenceable for a WSDL document that is part of the deployment
    * @param sei The service endpoint interface class name
    * @param serviceName The QName of the service
    * @param url The URL of the client deployment
    * @param wsdlFile The location of the WSDL within the client deployment
    */
   public ServiceReferenceable(String sei, QName serviceName, URL url, String wsdlFile)
   {
      this.serviceInterface = sei;
      this.deploymentUrl = url;
      this.wsdlFile = wsdlFile;
      this.serviceName = serviceName;
   }

   /**
    * Retrieves the Reference of this object.
    *
    * @return The non-null Reference of this object.
    * @exception NamingException If a naming exception was encountered while retrieving the reference.
    */
   public Reference getReference() throws NamingException
   {
      Reference myRef = new Reference(ServiceReferenceable.class.getName(), ServiceObjectFactory.class.getName(), null);
      myRef.add(new StringRefAddr(SERVICE_ENDPOINT_INTERFACE, serviceInterface));

      // If the service name was not given, the client is
      // expected to fill in the details
      if (serviceName != null)
      {
         myRef.add(new StringRefAddr(SERVICE_NAMESPACE, serviceName.getNamespaceURI()));
         myRef.add(new StringRefAddr(SERVICE_LOCALPART, serviceName.getLocalPart()));

         if (wsdlOverride != null)
         {
            myRef.add(new StringRefAddr(WSDL_OVERRIDE_URL, wsdlOverride.toExternalForm()));
         }
         else
         {
            myRef.add(new StringRefAddr(DEPLOYMENT_URL, deploymentUrl.toExternalForm()));
            myRef.add(new StringRefAddr(WSDL_FILE, wsdlFile));
         }
      }

      return myRef;
   }
}
