/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import javax.management.MalformedObjectNameException;

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.jboss.mx.loading.LoaderRepositoryFactory;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.webservice.metadata.ServiceRefMetaData;
import org.w3c.dom.Element;

/** A representation of the web.xml and jboss-web.xml deployment
 * descriptors as used by the AbstractWebContainer web container integration
 * support class.
 *
 * @see XmlLoadable
 * @see org.jboss.web.AbstractWebContainer
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.36 $
 */
public class WebMetaData extends MetaData
{
   private static Logger log = Logger.getLogger(WebMetaData.class);

   /** The web.xml servlet-mapping <String, String> */
   private HashMap servletMappings = new HashMap();
   /** The web.xml resource-refs <String, String> */
   private HashMap resourceReferences = new HashMap();
   /** The web.xml resource-env-refs <String, String> */
   private HashMap resourceEnvReferences = new HashMap();
   /** The web.xml message-destination-refs <String, MessageDestinationRefMetaData> */
   private HashMap messageDestinationReferences = new HashMap();
   /** The web.xml message-destination <String, MessageDestinationMetaData> */
   private HashMap messageDestinations = new HashMap();
   /** web.xml env-entrys */
   private ArrayList environmentEntries = new ArrayList();
   /** web.xml security-constraint <WebSecurityMetaData> */
   private ArrayList securityContraints = new ArrayList();
   /** The HashMap<String, SecurityRoleMetaData> for the security-roles */
   private HashMap securityRoles = new HashMap();
   /** web.xml ejb-refs */
   private HashMap ejbReferences = new HashMap();
   /** web.xml ejb-local-refs */
   private HashMap ejbLocalReferences = new HashMap();
   /** The web.xml service-refs */
   private HashMap serviceReferences = new HashMap();
   /** web.xml security-role-refs <String servlet-name, ArrayList<SecurityRoleRefMetaData>> */
   private HashMap securityRoleReferences = new HashMap();
   /** The web.xml distributable flag */
   private boolean distributable = false;
   /** The jboss-web.xml class-loading.java2ClassLoadingCompliance flag */
   private boolean java2ClassLoadingCompliance = false;
   /** The jboss-web.xml class-loading/loader-repository */
   private LoaderRepositoryConfig loaderConfig;
   /** The war context root as specified at the jboss-web.xml descriptor level. */
   private String contextRoot;
   /** The JACC context id for the container */
   private String jaccContextID;
   /** The jboss-web.xml server container virtual hosts the war should be deployed into */
   private ArrayList virtualHosts = new ArrayList();
   /** The jboss-web.xml JNDI name of the security domain implementation */
   private String securityDomain;

   /** The jboss-web.xml securityDomain flushOnSessionInvalidation attribute */
   private boolean flushOnSessionInvalidation;
   /** A HashMap<String, String> for webservice description publish locations */
   private HashMap webserviceDescriptions = new HashMap();

   /** The web context class loader used to create the java:comp context */
   private ClassLoader encLoader;
   /** The web context class loader, used to create the ws4ee service endpoint */
   private ClassLoader cxtLoader;

   private ArrayList depends = new ArrayList();

   public static final int SESSION_INVALIDATE_ACCESS =0;
   public static final int SESSION_INVALIDATE_SET_AND_GET =1;
   public static final int SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET =2;
   public static final int SESSION_INVALIDATE_SET =3;

   private int invalidateSessionPolicy = SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET;

   public static final int REPLICATION_TYPE_SYNC = 0;
   public static final int REPLICATION_TYPE_ASYNC = 1;

   /**
    * @deprecated Since JBoss3.2.6.
    */
   private int replicationType = REPLICATION_TYPE_SYNC;

   /** Specify the session replication granularity level: session --- whole session level,
    * attribute --- per attribute change, field --- fine grained user object level.
    *
    */
   public static final int REPLICATION_GRANULARITY_SESSION = 0;
   public static final int REPLICATION_GRANULARITY_ATTRIBUTE = 1;
   public static final int REPLICATION_GRANULARITY_FIELD = 2;
   private int replicationGranularity = REPLICATION_GRANULARITY_SESSION;

   /** Should the context use session cookies or use default */
   private int sessionCookies = SESSION_COOKIES_DEFAULT;

   public static final int SESSION_COOKIES_DEFAULT=0;
   public static final int SESSION_COOKIES_ENABLED=1;
   public static final int SESSION_COOKIES_DISABLED=2;

   /** The ClassLoader to load additional resources */
   private URLClassLoader resourceCl;

   /** Set the ClassLoader to load additional resources */
   public void setResourceClassLoader(URLClassLoader resourceCl)
   {
      this.resourceCl = resourceCl;
   }

   /** Return an iterator of the env-entry mappings.
    @return Iterator of EnvEntryMetaData objects.
    */
   public Iterator getEnvironmentEntries()
   {
      return environmentEntries.iterator();
   }
   /** Return an iterator of the ejb-ref mappings.
    @return Iterator of EjbRefMetaData objects.
    */
   public Iterator getEjbReferences()
   {
      return ejbReferences.values().iterator();
   }
   /** Return an iterator of the ejb-local-ref mappings.
    @return Iterator of EjbLocalRefMetaData objects.
    */
   public Iterator getEjbLocalReferences()
   {
      return ejbLocalReferences.values().iterator();
   }

   /** Return an iterator of the resource-ref mappings.
    @return Iterator of ResourceRefMetaData objects.
    */
   public Iterator getResourceReferences()
   {
      return resourceReferences.values().iterator();
   }
   /** Return an iterator of the resource-ref mappings.
    @return Iterator of ResourceEnvRefMetaData objects.
    */
   public Iterator getResourceEnvReferences()
   {
      return resourceEnvReferences.values().iterator();
   }

   /**
    * Return an iterator of message-destination-refs.
    * 
    * @return Iterator of MessageDestinationRefMetaData objects.
    */
   public Iterator getMessageDestinationReferences()
   {
      return messageDestinationReferences.values().iterator();
   }

   /** 
    * Get a message destination metadata
    * 
    * @param name the name of the message destination
    * @return the message destination metadata
    */
   public MessageDestinationMetaData getMessageDestination(String name)
   {
      return (MessageDestinationMetaData) messageDestinations.get(name);
   }

   /** Return an iterator of the service-ref mappings.
    * @return Iterator of ServiceRefMetaData objects
    */
   public Iterator getServiceReferences()
   {
      return serviceReferences.values().iterator();
   }

   /** This the the jboss-web.xml descriptor context-root and it
    *is only meaningful if a war is deployed outside of an ear.
    */
   public String getContextRoot()
   {
      return contextRoot;
   }
   public void setContextRoot(String contextRoot)
   {
      this.contextRoot = contextRoot;
   }

   /** Get the optional wsdl publish location from jboss-web.xml. */
   public String getWsdlPublishLocationByName(String name)
   {
      return (String) webserviceDescriptions.get(name);
   }

   public String getJaccContextID()
   {
      return jaccContextID;
   }
   public void setJaccContextID(String jaccContextID)
   {
      this.jaccContextID = jaccContextID;
   }

   /** Return the optional security-domain jboss-web.xml element.
    @return The jndiName of the security manager implementation that is
    responsible for security of the web application. May be null if
    there was no security-domain specified in the jboss-web.xml
    descriptor.
    */
   public String getSecurityDomain()
   {
      return securityDomain;
   }

   /** Set the security domain for this web application
    */
   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   /** The flag indicating whether the associated security domain cache
    * should be flushed when the session is invalidated.
    * @return true if the flush should occur, false otherwise.
    */ 
   public boolean isFlushOnSessionInvalidation()
   {
      return flushOnSessionInvalidation;
   }
   /** The flag indicating whether the associated security domain cache
    * should be flushed when the session is invalidated.
    * @param flag - true if the flush should occur, false otherwise.
    */ 
   public void setFlushOnSessionInvalidation(boolean flag)
   {
      this.flushOnSessionInvalidation = flag;
   }

   /** Get the security-constraint settings
    */
   public Iterator getSecurityContraints()
   {
      return securityContraints.iterator();
   }

   /**
    * 
    */
   public Map getSecurityRoleRefs()
   {
      return this.securityRoleReferences;
   }

   /** Get the optional map of security role/user mapping.
    * @return Map<String, SecurityRoleMetaData>
    */
   public Map getSecurityRoles()
   {
      return new HashMap(securityRoles);
   }

   /**
    * Get the security-role names from the web.xml descriptor
    * @return Set<String> of the security-role names from the web.xml
    */ 
   public Set getSecurityRoleNames()
   {
      return new HashSet(securityRoles.keySet());
   }

   /**
    * Get the servlet-name values from the web.xml descriptor
    * @return Set<String> of the servlet-names from the servlet-mappings
    */
   public HashMap getServletMappings()
   {
      return servletMappings;
   }

   /**
    * Get the servlet-name values from the web.xml descriptor
    * @return Set<String> of the servlet-names from the servlet-mappings
    */ 
   public Set getServletNames()
   {
      return new HashSet(servletMappings.keySet());
   }

   /**
    * Merge the security role/principal mapping defined in jboss-web.xml
    * with the one defined at jboss-app.xml.
    */
   public void mergeSecurityRoles(Map applRoles)
   {
      Iterator it = applRoles.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         String roleName = (String)entry.getKey();
         SecurityRoleMetaData appRole = (SecurityRoleMetaData)entry.getValue();
         SecurityRoleMetaData srMetaData = (SecurityRoleMetaData)securityRoles.get(roleName);
         if (srMetaData != null)
         {
            Set principalNames = appRole.getPrincipals();
            srMetaData.addPrincipalNames(principalNames);
         }
         else
         {
            securityRoles.put(roleName, entry.getValue());
         }
      }
   }

   /** The servlet container virtual host the war should be deployed into. If
    null then the servlet container default host should be used.
    */
   public Iterator getVirtualHosts()
   {
      return virtualHosts.iterator();
   }

   /**
     The distributable flag.
     @return true if the web-app is marked distributable
    */
   public boolean getDistributable()
   {
      return distributable;
   }

   /** Access the web application depends
    * @return Iterator of JMX ObjectNames the web app depends on.
    */ 
   public Collection getDepends()
   {
      return depends;
   }

   /** A flag indicating if the normal Java2 parent first class loading model
    * should be used over the servlet 2.3 web container first model.
    * @return true for parent first, false for the servlet 2.3 model
    */
   public boolean getJava2ClassLoadingCompliance()
   {
      return java2ClassLoadingCompliance;
   }
   public void setJava2ClassLoadingCompliance(boolean flag)
   {
      java2ClassLoadingCompliance = flag;
   }

   public LoaderRepositoryConfig getLoaderConfig()
   {
      return loaderConfig;
   }
   public void setLoaderConfig(LoaderRepositoryConfig loaderConfig)
   {
      this.loaderConfig = loaderConfig;
   }

   public ClassLoader getENCLoader()
   {
      return encLoader;
   }
   public void setENCLoader(ClassLoader encLoader)
   {
      this.encLoader = encLoader;
   }

   public ClassLoader getContextLoader()
   {
      return cxtLoader;
   }

   /** Make sure this is called during performDeploy */
   public void setContextLoader(ClassLoader cxtLoader)
   {
      this.cxtLoader = cxtLoader;
   }

   public int getSessionCookies()
   {
      return this.sessionCookies;
   }

   public int getInvalidateSessionPolicy()
   {
      return this.invalidateSessionPolicy;
   }

   public int getReplicationType()
   {
      return replicationType;
   }

   public int getReplicationGranularity()
   {
      return replicationGranularity;
   }

   public void importXml(Element element) throws DeploymentException
   {
      String rootTag = element.getOwnerDocument().getDocumentElement().getTagName();
      if( rootTag.equals("web-app") )
      {
         importWebXml(element);
      }
      else if( rootTag.equals("jboss-web") )
      {
         importJBossWebXml(element);
      }
   }
   
   /** Parse the elements of the web-app element used by the integration layer.
    */
   protected void importWebXml(Element webApp) throws DeploymentException
   {
      // Parse the web-app/servlet/security-role-ref elements
      Iterator iterator = getChildrenByTagName(webApp, "servlet");
      while( iterator.hasNext() )
      {
         Element servlet = (Element) iterator.next();
         String servletName = getElementContent(getUniqueChild(servlet, "servlet-name"));
         Iterator roleRefs = getChildrenByTagName(servlet, "security-role-ref");
         ArrayList roleNames = new ArrayList();
         while( roleRefs.hasNext() )
         {
            Element roleRefElem = (Element) roleRefs.next();
            SecurityRoleRefMetaData roleRef = new SecurityRoleRefMetaData(); 
            roleRef.importEjbJarXml(roleRefElem);
            roleNames.add(roleRef);
         }
         securityRoleReferences.put(servletName, roleNames);
      }

      // Parse the web-app/servlet-mapping elements
      iterator = getChildrenByTagName(webApp, "servlet-mapping");
      while( iterator.hasNext() )
      {
         Element servletMapping = (Element) iterator.next();
         String servletName = getElementContent(getUniqueChild(servletMapping, "servlet-name"));
         String urlPattern = getElementContent(getUniqueChild(servletMapping, "url-pattern"));
         servletMappings.put(servletName, urlPattern);
      }

      // Parse the web-app/resource-ref elements
      iterator = getChildrenByTagName(webApp, "resource-ref");
      while( iterator.hasNext() )
      {
         Element resourceRef = (Element) iterator.next();
         ResourceRefMetaData resourceRefMetaData = new ResourceRefMetaData();
         resourceRefMetaData.importEjbJarXml(resourceRef);
         resourceReferences.put(resourceRefMetaData.getRefName(), resourceRefMetaData);
      }

      // Parse the resource-env-ref elements
      iterator = getChildrenByTagName(webApp, "resource-env-ref");
      while (iterator.hasNext())
      {
         Element resourceRef = (Element) iterator.next();
         ResourceEnvRefMetaData refMetaData = new ResourceEnvRefMetaData();
         refMetaData.importEjbJarXml(resourceRef);
         resourceEnvReferences.put(refMetaData.getRefName(), refMetaData);
      }

      // set the message destination references
      iterator = getChildrenByTagName(webApp, "message-destination-ref");
      while (iterator.hasNext())
      {
         Element messageDestinationRef = (Element) iterator.next();
         MessageDestinationRefMetaData messageDestinationRefMetaData = new MessageDestinationRefMetaData();
         messageDestinationRefMetaData.importEjbJarXml(messageDestinationRef);
         messageDestinationReferences.put(messageDestinationRefMetaData.getRefName(), messageDestinationRefMetaData);
      }

      // set the message destinations (optional)
      iterator = getChildrenByTagName(webApp, "message-destination");
      while (iterator.hasNext())
      {
         Element messageDestination = (Element) iterator.next();
         try
         {
            MessageDestinationMetaData messageDestinationMetaData = new MessageDestinationMetaData();
            messageDestinationMetaData.importEjbJarXml(messageDestination);
            messageDestinations.put(messageDestinationMetaData.getName(), messageDestinationMetaData);
         }
         catch (Throwable t)
         {
            throw new DeploymentException("Error in web.xml " +
               "for message destination: " + t.getMessage());
         }
      }

      // Parse the web-app/env-entry elements
      iterator = getChildrenByTagName(webApp, "env-entry");
      while( iterator.hasNext() )
      {
         Element envEntry = (Element) iterator.next();
         EnvEntryMetaData envEntryMetaData = new EnvEntryMetaData();
         envEntryMetaData.importEjbJarXml(envEntry);
         environmentEntries.add(envEntryMetaData);
      }

      // Get the security-constraints
      iterator = getChildrenByTagName(webApp, "security-constraint");
      while (iterator.hasNext())
      {
         Element contraints = (Element) iterator.next();
         WebSecurityMetaData wsmd = new WebSecurityMetaData();
         securityContraints.add(wsmd);
         // Process the web-resource-collections
         Iterator iter2 = getChildrenByTagName(contraints, "web-resource-collection");
         while( iter2.hasNext() )
         {
            Element wrcElement = (Element) iter2.next();
            Element wrName = getUniqueChild(wrcElement, "web-resource-name");
            String name = getElementContent(wrName);
            WebSecurityMetaData.WebResourceCollection wrc = wsmd.addWebResource(name);
            Iterator iter21 = getChildrenByTagName(wrcElement, "url-pattern");
            while( iter21.hasNext() )
            {
               Element urlPattern = (Element) iter21.next();
               String pattern = getElementContent(urlPattern);
               wrc.addPattern(pattern);
            }

            Iterator iter22 = getChildrenByTagName(wrcElement, "http-method");
            while( iter22.hasNext() )
            {
               Element httpMethod = (Element) iter22.next();
               String method = getElementContent(httpMethod);
               wrc.addHttpMethod(method);               
            }
         }

         // Process the auth-constraints
         Element authContraint = getOptionalChild(contraints, "auth-constraint");
         if( authContraint != null )
         {
            Iterator iter3 = getChildrenByTagName(authContraint, "role-name");
            while( iter3.hasNext() )
            {
               Element roleName = (Element) iter3.next();
               String name = getElementContent(roleName);
               wsmd.addRole(name);
            }
            if( wsmd.getRoles().size() == 0 )
               wsmd.setExcluded(true);
         }
         else
         {
            wsmd.setUnchecked(true);
         }

         // Process the user-data-constraint
         Element userData = getOptionalChild(contraints, "user-data-constraint");
         if( userData != null )
         {
            Element transport = getUniqueChild(userData, "transport-guarantee");
            String type = getElementContent(transport);
            wsmd.setTransportGuarantee(type);
         }
      }

      // Get the web-app/security-role elements (optional)
      iterator = getChildrenByTagName(webApp, "security-role");
      while (iterator.hasNext())
      {
         Element securityRole = (Element) iterator.next();
         String roleName = getElementContent(getUniqueChild(securityRole, "role-name"));
         securityRoles.put(roleName, new SecurityRoleMetaData(roleName));
      }

      // Parse the web-app/ejb-ref elements
      iterator = getChildrenByTagName(webApp, "ejb-ref");
      while( iterator.hasNext() )
      {
         Element ejbRef = (Element) iterator.next();
         EjbRefMetaData ejbRefMetaData = new EjbRefMetaData();
         ejbRefMetaData.importEjbJarXml(ejbRef);
         ejbReferences.put(ejbRefMetaData.getName(), ejbRefMetaData);
      }

      // Parse the web-app/ejb-local-ref elements
      iterator = getChildrenByTagName(webApp, "ejb-local-ref");
      while( iterator.hasNext() )
      {
         Element ejbRef = (Element) iterator.next();
         EjbLocalRefMetaData ejbRefMetaData = new EjbLocalRefMetaData();
         ejbRefMetaData.importEjbJarXml(ejbRef);
         ejbLocalReferences.put(ejbRefMetaData.getName(), ejbRefMetaData);
      }

      // Parse the service-ref elements
      iterator = MetaData.getChildrenByTagName(webApp, "service-ref");
      while (iterator.hasNext())
      {
         Element serviceRef = (Element) iterator.next();
         ServiceRefMetaData refMetaData = new ServiceRefMetaData(resourceCl);
         refMetaData.importStandardXml(serviceRef);
         serviceReferences.put(refMetaData.getServiceRefName(), refMetaData);
      }

      // Is the web-app marked distributable?
      iterator = getChildrenByTagName(webApp, "distributable");
      if(iterator.hasNext())
      {
         distributable=true;
      }
   }

   /** Parse the elements of the jboss-web element used by the integration layer.
    */
   protected void importJBossWebXml(Element jbossWeb) throws DeploymentException
   {
      // Parse the jboss-web/root-context element
      Element contextRootElement = getOptionalChild(jbossWeb, "context-root");
      if( contextRootElement != null )
         contextRoot = getElementContent(contextRootElement);

      // Parse the jboss-web/security-domain element
      Element securityDomainElement = getOptionalChild(jbossWeb, "security-domain");
      if( securityDomainElement != null )
      {
         securityDomain = getElementContent(securityDomainElement);
         // Check the flushOnSessionInvalidation attribute
         Boolean flag = Boolean.valueOf(securityDomainElement.getAttribute("flushOnSessionInvalidation"));
         flushOnSessionInvalidation = flag.booleanValue();
      }

      // Parse the jboss-web/depends elements
      for( Iterator virtualHostElements = getChildrenByTagName(jbossWeb, "virtual-host");
         virtualHostElements.hasNext();)
      {
         Element virtualHostElement = (Element)virtualHostElements.next();
         String virtualHostName = getElementContent(virtualHostElement);
         virtualHosts.add(virtualHostName);
      } // end of for ()

      // Parse the jboss-web/resource-ref elements
      Iterator iterator = getChildrenByTagName(jbossWeb, "resource-ref");
      while( iterator.hasNext() )
      {
         Element resourceRef = (Element) iterator.next();
         String resRefName = getElementContent(getUniqueChild(resourceRef, "res-ref-name"));
         ResourceRefMetaData refMetaData = (ResourceRefMetaData) resourceReferences.get(resRefName);
         if( refMetaData == null )
         {
            throw new DeploymentException("resource-ref " + resRefName
               + " found in jboss-web.xml but not in web.xml");
         }
         refMetaData.importJbossXml(resourceRef);
      }

      // Parse the jboss-web/resource-env-ref elements
      iterator = getChildrenByTagName(jbossWeb, "resource-env-ref");
      while( iterator.hasNext() )
      {
         Element resourceRef = (Element) iterator.next();
         String resRefName = getElementContent(getUniqueChild(resourceRef, "resource-env-ref-name"));
         ResourceEnvRefMetaData refMetaData = (ResourceEnvRefMetaData) resourceEnvReferences.get(resRefName);
         if( refMetaData == null )
         {
            throw new DeploymentException("resource-env-ref " + resRefName
               + " found in jboss-web.xml but not in web.xml");
         }
         refMetaData.importJbossXml(resourceRef);
      }

      // update the message destination references (optional)
      iterator = getChildrenByTagName(jbossWeb, "message-destination-ref");
      while (iterator.hasNext())
      {
         Element messageDestinationRef = (Element) iterator.next();
         String messageDestinationRefName = getElementContent(getUniqueChild(messageDestinationRef, "message-destination-ref-name"));
         MessageDestinationRefMetaData messageDestinationRefMetaData = (MessageDestinationRefMetaData) messageDestinationReferences.get(messageDestinationRefName);
         if (messageDestinationRefMetaData == null)
            throw new DeploymentException("message-destination-ref " + messageDestinationRefName + " found in jboss-web.xml but not in web.xml");
         messageDestinationRefMetaData.importJbossXml(messageDestinationRef);
      }

      // set the message destinations (optional)
      iterator = getChildrenByTagName(jbossWeb, "message-destination");
      while (iterator.hasNext())
      {
         Element messageDestination = (Element) iterator.next();
         try
         {
            String messageDestinationName = getUniqueChildContent(messageDestination, "message-destination-name");
            MessageDestinationMetaData messageDestinationMetaData = (MessageDestinationMetaData) messageDestinations.get(messageDestinationName);
            if (messageDestinationMetaData == null)
               throw new DeploymentException("message-destination " + messageDestinationName + " found in jboss-web.xml but not in web.xml");
            messageDestinationMetaData.importJbossXml(messageDestination);
         }
         catch (Throwable t)
         {
            throw new DeploymentException("Error in web.xml " +
               "for message destination: " + t.getMessage());
         }
      }

      // set the security roles (optional)
      iterator = getChildrenByTagName(jbossWeb, "security-role");
      while (iterator.hasNext())
      {
         Element securityRole = (Element) iterator.next();
         String roleName = getElementContent(getUniqueChild(securityRole, "role-name"));
         SecurityRoleMetaData securityRoleMetaData = (SecurityRoleMetaData)securityRoles.get(roleName);
         if (securityRoleMetaData == null)
            throw new DeploymentException("Security role '" + roleName + "' defined in jboss-web.xml" +
                    "is not defined in web.xml");

         Iterator itPrincipalNames = getChildrenByTagName(securityRole, "principal-name");
         while (itPrincipalNames.hasNext())
         {
            String principalName = getElementContent((Element) itPrincipalNames.next());
            securityRoleMetaData.addPrincipalName(principalName);
         }
      }

      // Parse the jboss-web/ejb-ref elements
      iterator = getChildrenByTagName(jbossWeb, "ejb-ref");
      while( iterator.hasNext() )
      {
         Element ejbRef = (Element) iterator.next();
         String ejbRefName = getElementContent(getUniqueChild(ejbRef, "ejb-ref-name"));
         EjbRefMetaData ejbRefMetaData = (EjbRefMetaData) ejbReferences.get(ejbRefName);
         if( ejbRefMetaData == null )
         {
            throw new DeploymentException("ejb-ref " + ejbRefName
               + " found in jboss-web.xml but not in web.xml");
         }
         ejbRefMetaData.importJbossXml(ejbRef);
      }

      // Parse the jboss-web/ejb-local-ref elements
      iterator = getChildrenByTagName(jbossWeb, "ejb-local-ref");
      while( iterator.hasNext() )
      {
         Element ejbLocalRef = (Element) iterator.next();
         String ejbLocalRefName = getElementContent(getUniqueChild(ejbLocalRef, "ejb-ref-name"));
         EjbLocalRefMetaData ejbLocalRefMetaData = (EjbLocalRefMetaData) ejbLocalReferences.get(ejbLocalRefName);
         if( ejbLocalRefMetaData == null )
         {
            throw new DeploymentException("ejb-local-ref " + ejbLocalRefName
               + " found in jboss-web.xml but not in web.xml");
         }
         ejbLocalRefMetaData.importJbossXml(ejbLocalRef);
      }

      // Parse the service-ref elements
      iterator = MetaData.getChildrenByTagName(jbossWeb, "service-ref");
      while (iterator.hasNext())
      {
         Element serviceRef = (Element) iterator.next();
         String serviceRefName = MetaData.getUniqueChildContent(serviceRef, "service-ref-name");
         ServiceRefMetaData refMetaData = (ServiceRefMetaData)serviceReferences.get(serviceRefName);
         if (refMetaData == null)
         {
            throw new DeploymentException("service-ref " + serviceRefName
               + " found in jboss-web.xml but not in web.xml");
         }
         refMetaData.importJBossXml(serviceRef);
      }

      // WebserviceDescriptions
      iterator = getChildrenByTagName(jbossWeb, "webservice-description");
      while (iterator.hasNext())
      {
         Element wsd = (Element)iterator.next();
         String wsdName = getElementContent(getUniqueChild(wsd, "webservice-description-name"));
         String wsdlPublishLocation = getOptionalChildContent(wsd, "wsdl-publish-location");
         webserviceDescriptions.put(wsdName, wsdlPublishLocation);
      }

      // Parse the jboss-web/depends elements
      for( Iterator dependsElements = getChildrenByTagName(jbossWeb, "depends");
         dependsElements.hasNext();)
      {
         Element dependsElement = (Element)dependsElements.next();
         String dependsName = getElementContent(dependsElement);
         depends.add(ObjectNameFactory.create(dependsName));
      } // end of for ()

      // Parse the jboss-web/use-session-cookies element
      iterator = getChildrenByTagName(jbossWeb, "use-session-cookies");
      if ( iterator.hasNext() )
      {
         Element useCookiesElement = (Element) iterator.next();
         String useCookiesElementContent = getElementContent(useCookiesElement);
         Boolean useCookies=Boolean.valueOf(useCookiesElementContent);
         
         if (useCookies.booleanValue())
         {
            sessionCookies=SESSION_COOKIES_ENABLED;
         }
         else
         {
            sessionCookies=SESSION_COOKIES_DISABLED;
         }
      }

      // Parse the jboss-web/session-replication element

      Element sessionReplicationRootElement = getOptionalChild(jbossWeb, "replication-config");
      if( sessionReplicationRootElement != null )
      {
         // manage "replication-trigger" first ...
         //
         Element replicationTriggerElement = getOptionalChild(sessionReplicationRootElement, "replication-trigger");
         if (replicationTriggerElement != null)
         {
            String repMethod = getElementContent(replicationTriggerElement);
            if ("SET_AND_GET".equalsIgnoreCase(repMethod))
               this.invalidateSessionPolicy = SESSION_INVALIDATE_SET_AND_GET;
            else if ("SET_AND_NON_PRIMITIVE_GET".equalsIgnoreCase(repMethod))
               this.invalidateSessionPolicy = SESSION_INVALIDATE_SET_AND_NON_PRIMITIVE_GET;
            else if ("SET".equalsIgnoreCase(repMethod))
               this.invalidateSessionPolicy = SESSION_INVALIDATE_SET;
            else
               throw new DeploymentException("replication-trigger value set to a non-valid value: '" + repMethod
                  + "' (should be ['SET_AND_GET', 'SET_AND_NON_PRIMITIVE_GET', 'SET']) in jboss-web.xml");
         }

         // ... then manage "replication-type".
         //
         Element replicationTypeElement = getOptionalChild(sessionReplicationRootElement, "replication-type");
         if (replicationTypeElement != null)
         {
            String repType = getElementContent(replicationTypeElement);
            if ("SYNC".equalsIgnoreCase(repType))
               this.replicationType = REPLICATION_TYPE_SYNC;
            else if ("ASYNC".equalsIgnoreCase(repType))
               this.replicationType = REPLICATION_TYPE_ASYNC;
            else
               throw new DeploymentException("replication-type value set to a non-valid value: '" + repType
                  + "' (should be ['SYNC', 'ASYNC']) in jboss-web.xml");
         }

         // ... then manage "replication-type".
         //
         Element replicationGranularityElement = MetaData.getOptionalChild(sessionReplicationRootElement, "replication-granularity");
         if (replicationGranularityElement != null)
         {
            String repType = MetaData.getElementContent(replicationGranularityElement);
            if ("SESSION".equalsIgnoreCase(repType))
               this.replicationGranularity = REPLICATION_GRANULARITY_SESSION;
            else if ("ATTRIBUTE".equalsIgnoreCase(repType))
               this.replicationGranularity = REPLICATION_GRANULARITY_ATTRIBUTE;
            else
               throw new DeploymentException("replication-granularity value set to a non-valid value: '" + repType
                  + "' (should be ['SESSION', 'ATTRIBUTE']) in jboss-web.xml");
         }
      }

      // Check for a war level class loading config
      Element classLoading = MetaData.getOptionalChild(jbossWeb, "class-loading");
      if( classLoading != null )
      {
         String flagString = classLoading.getAttribute("java2ClassLoadingCompliance");
         if( flagString.length() == 0 )
            flagString = "true";
         boolean flag = Boolean.valueOf(flagString).booleanValue();
         setJava2ClassLoadingCompliance(flag);
         // Check for a loader-repository for scoping
         Element loader = MetaData.getOptionalChild(classLoading, "loader-repository");
         if( loader != null )
         {
            try
            {
               loaderConfig = LoaderRepositoryFactory.parseRepositoryConfig(loader);
            }
            catch (MalformedObjectNameException e)
            {
               throw new DeploymentException(e);
            }
         }
      }
   }
}
