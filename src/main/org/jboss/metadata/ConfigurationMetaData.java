/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

/** The configuration information for an EJB container.
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *   @version $Revision: 1.22 $
 *
 *  <p><b>Revisions:</b><br>
 *  <p><b>2001/08/02: marcf</b>
 *  <ol>
 *   <li>Added locking policy as optional tag in jboss.xml 
 *  </ol>
 *  <p><b>2001/10/16: billb</b>
 *  <ol>
 *   <li>Added clustering tags
 *  </ol>
 *
 */
public class ConfigurationMetaData extends MetaData 
{

   // Constants -----------------------------------------------------
   public static final String CMP_2x_13 = "Standard CMP 2.x EntityBean";
   public static final String CMP_1x_13 = "Standard CMP EntityBean";
   public static final String BMP_13 = "Standard BMP EntityBean";
   public static final String STATELESS_13 = "Standard Stateless SessionBean";
   public static final String STATEFUL_13 = "Standard Stateful SessionBean";
   public static final String MESSAGE_DRIVEN_13 = "Standard Message Driven Bean";
   public static final String CMP_12 = "jdk1.2.2 CMP EntityBean";
   public static final String BMP_12 = "jdk1.2.2 BMP EntityBean";
   public static final String STATELESS_12 = "jdk1.2.2 Stateless SessionBean";
   public static final String STATEFUL_12 = "jdk1.2.2 Stateful SessionBean";
   public static final String MESSAGE_DRIVEN_12 = "jdk1.2.2 Message Driven Bean";
   public static final String CLUSTERED_STATELESS_13 = "Clustered Stateless SessionBean"; // we do not support JDK < 1.3
   public static final String CLUSTERED_STATEFUL_13 = "Clustered Stateful SessionBean"; // we do not support JDK < 1.3
   public static final String CLUSTERED_CMP_2x_13 = "Clustered CMP 2.x EntityBean"; // we do not support JDK < 1.3
   public static final String CLUSTERED_CMP_1x_13 = "Clustered CMP EntityBean"; // we do not support JDK < 1.3
   public static final String CLUSTERED_BMP_13 = "Clustered BMP EntityBean"; // we do not support JDK < 1.3

   public static final byte A_COMMIT_OPTION = 0;
   public static final byte B_COMMIT_OPTION = 1;
   public static final byte C_COMMIT_OPTION = 2;
   /** D_COMMIT_OPTION is a lazy load option. By default it synchronizes every 30 seconds */
   public static final byte D_COMMIT_OPTION = 3;
   public static final String[] commitOptionStrings = { "A", "B", "C", "D"};

   // Attributes ----------------------------------------------------
   private String name;
   private String containerInvoker;
   private String instancePool;
   private String instanceCache;
   private String persistenceManager;
   private String transactionManager;
   // This is to provide backward compatibility with 2.4 series jboss.xml
   // but it should come from standardjboss alone 
   // marcf:FIXME deprecate the "hardcoded string"
   private String lockClass = "org.jboss.ejb.plugins.lock.QueuedPessimisticEJBLock";
   private byte commitOption;
   private long optionDRefreshRate = 30000;
   private boolean callLogging;
   private boolean readOnlyGetMethods;

   private String securityDomain;
   private String authenticationModule;
   private String roleMappingManager;

   private Element containerInvokerConf;
   private Element containerPoolConf;
   private Element containerCacheConf;
   private Element containerInterceptorsConf;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ConfigurationMetaData (String name) {
      this.name = name;
   }

   // Public --------------------------------------------------------

   public String getName() { return name; }

   public String getContainerInvoker() { return containerInvoker; }

   public String getInstancePool() { return instancePool; }

   public String getInstanceCache() { return instanceCache; }

   public String getPersistenceManager() { return persistenceManager; }

   public String getSecurityDomain() { return securityDomain; }
   public String getAuthenticationModule() { return authenticationModule; }

   public String getRoleMappingManager() { return roleMappingManager; }

   public String getTransactionManager() { return transactionManager; }

   public String getLockClass() {return lockClass;} 
	
   public Element getContainerInvokerConf() { return containerInvokerConf; }
   public Element getContainerPoolConf() { return containerPoolConf; }
   public Element getContainerCacheConf() { return containerCacheConf; }
   public Element getContainerInterceptorsConf() { return containerInterceptorsConf; }

   public boolean getCallLogging() { return callLogging; }

   public byte getCommitOption() { return commitOption; }

   public long getOptionDRefreshRate() { return optionDRefreshRate; }

   public boolean getReadOnlyGetMethods() { return readOnlyGetMethods; }

   public void importJbossXml(Element element) throws DeploymentException {

      // everything is optional to allow jboss.xml to modify part of a configuration
      // defined in standardjboss.xml

      // set call logging
      callLogging = Boolean.valueOf(getElementContent(getOptionalChild(element, "call-logging"), String.valueOf(callLogging))).booleanValue();

      // set read-only get methods
      readOnlyGetMethods = Boolean.valueOf(getElementContent(getOptionalChild(element, "read-only-get-methods"))).booleanValue();

      // set the container invoker
      containerInvoker = getElementContent(getOptionalChild(element, "container-invoker"), containerInvoker);

      // set the instance pool
      instancePool = getElementContent(getOptionalChild(element, "instance-pool"), instancePool);

      // set the instance cache
      instanceCache = getElementContent(getOptionalChild(element, "instance-cache"), instanceCache);

      // set the persistence manager
      persistenceManager = getElementContent(getOptionalChild(element, "persistence-manager"), persistenceManager);

      // set the transaction manager
      transactionManager = getElementContent(getOptionalChild(element, "transaction-manager"), transactionManager);

      // set the lock class
      lockClass = getElementContent(getOptionalChild(element, "locking-policy"), lockClass);
		 
      // set the security domain
      securityDomain = getElementContent(getOptionalChild(element, "security-domain"), securityDomain);
      // set the authentication module
      authenticationModule = getElementContent(getOptionalChild(element, "authentication-module"), authenticationModule);
      // set the role mapping manager
      roleMappingManager = getElementContent(getOptionalChild(element, "role-mapping-manager"), roleMappingManager);
      /* If the authenticationModule == roleMappingManager just set the securityDomain
         as this is the combination of the authentication-module and role-mapping-manager
         behaviors
      */
      if( authenticationModule != null && roleMappingManager != null &&
          roleMappingManager.equals(authenticationModule) )
      {
         securityDomain = authenticationModule;
         authenticationModule = null;
         roleMappingManager = null;
      }
      // Don't allow only a authentication-module or role-mapping-manager
      else if( (authenticationModule == null && roleMappingManager != null) 
               || (authenticationModule != null && roleMappingManager == null) )
      {
         String msg = "Either a security-domain or both authentication-module "
            + "and role-mapping-manager must be specified";
         throw new DeploymentException(msg);
      }

      // set the commit option
      String commit = getElementContent(getOptionalChild(element, "commit-option"), commitOptionToString(commitOption));

      commitOption = stringToCommitOption(commit);

      //get the refresh rate for option D
      String refresh = getElementContent(getOptionalChild(element, "optiond-refresh-rate"), Long.toString(optionDRefreshRate));
      optionDRefreshRate = stringToRefreshRate(refresh);

      // the classes which can understand the following are dynamically loaded during deployment :
      // We save the Elements for them to use later

      // The configuration for the container interceptors
      containerInterceptorsConf = getOptionalChild(element, "container-interceptors", containerInterceptorsConf);

      // configuration for container invoker
      containerInvokerConf = getOptionalChild(element, "container-invoker-conf", containerInvokerConf);

      // configuration for instance pool
      containerPoolConf = getOptionalChild(element, "container-pool-conf", containerPoolConf);

      // configuration for instance cache
      containerCacheConf = getOptionalChild(element, "container-cache-conf", containerCacheConf);

      // DEPRECATED: Remove this in JBoss 4.0
      if (containerInvoker.equals("org.jboss.ejb.plugins.jrmp12.server.JRMPContainerInvoker") ||
          containerInvoker.equals("org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker"))
      {
         System.out.println("Deprecated container invoker. Change to org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker");
         containerInvoker = "org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker";
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------
   private static String commitOptionToString(byte commitOption)
      throws DeploymentException {

      try {
         return commitOptionStrings[commitOption];
      } catch( ArrayIndexOutOfBoundsException e ) {
         throw new DeploymentException("Invalid commit option: " + commitOption);
      }
   }

   private static byte stringToCommitOption(String commitOption)
      throws DeploymentException {

      for( byte i=0; i<commitOptionStrings.length; ++i )
         if( commitOptionStrings[i].equals(commitOption) )
            return i;

      throw new DeploymentException("Invalid commit option: '" + commitOption + "'");
   }

   private static long stringToRefreshRate(String refreshRate) throws DeploymentException {
      try{
         return Long.parseLong(refreshRate);
      } catch ( Exception e){
         throw new DeploymentException("Invalid optiond-refresh-rate \"" + refreshRate + "\". Should be a number"); 
      }

   }

   // Inner classes -------------------------------------------------
}
