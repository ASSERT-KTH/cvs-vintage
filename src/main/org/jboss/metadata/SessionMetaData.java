/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.HashMap;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/** The meta data information specific to session beans.
 *
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author Scott.Stark@jboss.org
 *   @author <a href="mailto:Christoph.Jung@infor.de">Christoph G. Jung</a>.
 *   @version $Revision: 1.23 $
 */
public class SessionMetaData extends BeanMetaData
{
   // Constants -----------------------------------------------------
   public static final String DEFAULT_STATEFUL_INVOKER = "stateful-rmi-invoker";
   public static final String DEFAULT_CLUSTERED_STATEFUL_INVOKER = "clustered-stateful-rmi-invoker";
   public static final String DEFAULT_STATELESS_INVOKER = "stateless-rmi-invoker";
   public static final String DEFAULT_CLUSTERED_STATELESS_INVOKER = "clustered-stateless-rmi-invoker";
   public static final String DEFAULT_WEBSERVICE_INVOKER = "session-webservice-invoker";
    
   // Attributes ----------------------------------------------------
   /** whether it is a stateful or stateless bean */
   private boolean stateful;
   /** the service-endpoint element contains the fully-qualified
    *  name of the session bean´s web service interface
    */
   protected String serviceEndpointClass;
   /** the jboss port-component binding for a ejb webservice
    */
   protected EjbPortComponentMetaData portComponent;

   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
   public SessionMetaData(ApplicationMetaData app)
   {
      super(app, BeanMetaData.SESSION_TYPE);
   }

   // Public --------------------------------------------------------
   public boolean isStateful()
   {
      return stateful;
   }

   public boolean isStateless()
   {
      return !stateful;
   }

   public boolean isWebservice()
   {
      return getServiceEndpoint() != null;
   }

   public String getServiceEndpoint()
   {
      return serviceEndpointClass;
   }

   public EjbPortComponentMetaData getPortComponent()
   {
      return portComponent;
   }

   public String getDefaultConfigurationName()
   {
      if( isStateful() )
      {
         if( this.isClustered() )
            return ConfigurationMetaData.CLUSTERED_STATEFUL_13;
         else
            return ConfigurationMetaData.STATEFUL_13;
      }
      else
      {
         if( this.isClustered() )
            return ConfigurationMetaData.CLUSTERED_STATELESS_13;
         else
            return ConfigurationMetaData.STATELESS_13;
      }
   }

   protected void defaultInvokerBindings()
   {
      invokerBindings = new HashMap();
      if( isClustered() )
      {
         if( stateful )
         {
            invokerBindings.put(
               DEFAULT_CLUSTERED_STATEFUL_INVOKER,
               getJndiName());
         }
         else
         {
            invokerBindings.put(
               DEFAULT_CLUSTERED_STATELESS_INVOKER,
               getJndiName());
         }
         if( isWebservice() )
            invokerBindings.put(DEFAULT_WEBSERVICE_INVOKER, getJndiName());
      }
      else
      {
         if( stateful )
         {
            invokerBindings.put(DEFAULT_STATEFUL_INVOKER, getJndiName());
         }
         else
         {
            invokerBindings.put(DEFAULT_STATELESS_INVOKER, getJndiName());
         }
         if( isWebservice() )
            invokerBindings.put(DEFAULT_WEBSERVICE_INVOKER, getJndiName());
      }
   }

   public void importEjbJarXml(Element element) throws DeploymentException
   {
      super.importEjbJarXml(element);
		
      // set the session type 
      String sessionType = getElementContent(getUniqueChild(element, "session-type"));
      if( sessionType.equals("Stateful") )
      {
         stateful = true;
      }
      else if( sessionType.equals("Stateless") )
      {
         stateful = false;
      }
      else
      {
         throw new DeploymentException("session type should be 'Stateful' or 'Stateless'");
      }
			
      // set the transaction type
      String transactionType = getElementContent(getUniqueChild(element, "transaction-type"));
      if( transactionType.equals("Bean") )
      {
         containerManagedTx = false;
      }
      else if( transactionType.equals("Container") )
      {
         containerManagedTx = true;
      }
      else
      {
         throw new DeploymentException("transaction type should be 'Bean' or 'Container'");
      }

      serviceEndpointClass = getElementContent(getOptionalChild(element, "service-endpoint"));
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      super.importJbossXml(element);
      // ior-security-config optional element
      Element portElement = getOptionalChild(element, "port-component");
      if( portElement != null )
      {
         portComponent = new EjbPortComponentMetaData();
         portComponent.importJBossXml(portElement);
      }
   }

}
