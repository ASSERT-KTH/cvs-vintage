/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * MBean interface.
 * @see Container
 */
public interface EJBDeployerMBean extends org.jboss.deployment.SubDeployerMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.ejb:service=EJBDeployer");

  boolean isCallByValue() ;

  void setCallByValue(boolean callByValue) ;

   /**
    * Returns the deployed applications.
    */
  java.util.Iterator listDeployedApplications() ;

   /**
    * Enables/disables the application bean verification upon deployment.
    * @param verify true to enable; false to disable    */
  void setVerifyDeployments(boolean verify) ;

   /**
    * Returns the state of bean verifier (on/off)
    * @return true if enabled; false otherwise    */
  boolean getVerifyDeployments() ;

   /**
    * Enables/disables the verbose mode on the verifier.
    * @param verbose true to enable; false to disable    */
  void setVerifierVerbose(boolean verbose) ;

   /**
    * Returns the state of the bean verifier (verbose/non-verbose mode)
    * @return true if enabled; false otherwise    */
  boolean getVerifierVerbose() ;

   /**
    * Enables/disables the strict mode on the verifier.
    * @param strictVerifier <code>true</code> to enable; <code>false</code> to disable    */
  void setStrictVerifier(boolean strictVerifier) ;

   /**
    * Returns the mode of the bean verifier (strict/non-strict mode)
    * @return <code>true</code> if the Verifier is in strict mode, <code>false</code> otherwise    */
  boolean getStrictVerifier() ;

   /**
    * Enables/disables the metrics interceptor for containers.
    * @param enable true to enable; false to disable    */
  void setMetricsEnabled(boolean enable) ;

   /**
    * Checks if this container factory initializes the metrics interceptor.
    * @return true if metrics are enabled; false otherwise    */
  boolean isMetricsEnabled() ;

   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &amp; jboss-web.dtd conforming documents should be validated against the DTD.
    */
  boolean getValidateDTDs() ;

   /**
    * Set the flag indicating that ejb-jar.dtd, jboss.dtd &amp; jboss-web.dtd conforming documents should be validated against the DTD.
    */
  void setValidateDTDs(boolean validate) ;

   /**
    * Get the WebServiceName value.
    * @return the WebServiceName value.
    */
  javax.management.ObjectName getWebServiceName() ;

   /**
    * Set the WebServiceName value.
    * @param webServiceName The new WebServiceName value.
    */
  void setWebServiceName(javax.management.ObjectName webServiceName) ;

   /**
    * Get the TransactionManagerServiceName value.
    * @return the TransactionManagerServiceName value.
    */
  javax.management.ObjectName getTransactionManagerServiceName() ;

   /**
    * Set the TransactionManagerServiceName value.
    * @param transactionManagerServiceName The new TransactionManagerServiceName value.
    */
  void setTransactionManagerServiceName(javax.management.ObjectName transactionManagerServiceName) ;

}
