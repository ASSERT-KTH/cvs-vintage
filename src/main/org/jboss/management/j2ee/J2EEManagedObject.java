/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Hashtable;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.management.j2ee.EventProvider;
import javax.management.j2ee.StateManageable;
import javax.management.j2ee.StatisticsProvider;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEManagedObject J2EEManagedObject}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.7 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines as well as adding some
 *      static helper methods
 * </ul>
 **/
public abstract class J2EEManagedObject
   extends ServiceMBeanSupport
   implements javax.management.j2ee.J2EEManagedObject, Serializable
{

   // Constants -----------------------------------------------------
   
   public static final String TYPE = "type";
   public static final String NAME = "name";
   
   // Attributes ----------------------------------------------------

   /** Class logger. */
   private static final Logger log =
      Logger.getLogger(J2EEManagedObject.class);
   
   private ObjectName mParent = null;
   private ObjectName mName = null;

   // Static --------------------------------------------------------
   
   private static String sDomainName = null;
   
   public static String getDomainName() {
      return sDomainName;
   }

   /**
    * Retrieves the type out of an JSR-77 object name
    *
    * @param pName Object Name to check if null then
    *              it will be treated like NO type found
    *
    * @return The type of the given Object Name or an EMPTY
    *         string if either Object Name null or type not found
    **/
   protected static String getType( ObjectName pName ) {
      String lType = null;
      if( pName != null ) {
         lType = (String) pName.getKeyPropertyList().get( TYPE );
      }
      // Return an empty string if type not found
      return lType == null ? "" : lType;
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * Constructor for the root J2EEDomain object
   *
   * @param pDomainName Name of the domain
   * @param pType Type of the Managed Object which must be defined
   * @param pName Name of the Managed Object which must be defined
   *
   * @throws InvalidParameterException If the given Domain Name, Type or Name is null
   **/
   public J2EEManagedObject( String pDomainName, String pType, String pName )
      throws
         MalformedObjectNameException
   {
      if( pDomainName == null ) {
         throw new InvalidParameterException( "Domain Name must be set" );
      }
      sDomainName = pDomainName;
      Hashtable lProperties = new Hashtable();
      lProperties.put( TYPE, pType );
      lProperties.put( NAME, pName );
      mName = new ObjectName( getDomainName(), lProperties );

      log.debug("create root with name: " + mName );
   }
   
   /**
   * Constructor for any Managed Object except the root J2EEMangement.
   *
   * @param pType Type of the Managed Object which must be defined
   * @param pName Name of the Managed Object which must be defined
   * @param pParent Object Name of the parent of this Managed Object
   *                which must be defined
   *
   * @throws InvalidParameterException If the given Type, Name or Parent is null
   **/
   public J2EEManagedObject( String pType, String pName, ObjectName pParent )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      Hashtable lProperties = (Hashtable) pParent.getKeyPropertyList().clone();
      lProperties.put( lProperties.get( "type" ), lProperties.get( "name" ) );
      lProperties.put( TYPE, pType );
      lProperties.put( NAME, pName );
      mName = new ObjectName( getDomainName(), lProperties );
      setParent( pParent );
   }

   // Public --------------------------------------------------------
   
   // J2EEManagedObjectMBean implementation ----------------------------------------------
   
   public ObjectName getObjectName() {
      log.debug("getObjectName(), name: " + mName );
      return mName;
   }

   public ObjectName getParent() {
      return mParent;
   }
   
   public void setParent( ObjectName pParent )
      throws
         InvalidParentException
   {
      if( pParent == null ) {
         throw new InvalidParameterException( "Parent must be set" );
      }
      mParent = pParent;
   }
   
   public void addChild( ObjectName pChild ) {
      //AS ToDo: Remove later is just here to compile
   }
   public void removeChild( ObjectName pChild ) {
      //AS ToDo: Remove later is just here to compile
   }

   // J2EEManagedObject implementation ----------------------------------------------
   
   public String getName() {
      return mName.toString();
   }
   
   public boolean isStateManageable() {
      return this instanceof StateManageable;
   }

   public boolean isStatisticsProvider() {
      return this instanceof StatisticsProvider;
   }
   
   public boolean isEventProvider() {
      return this instanceof EventProvider;
   }
   
   // ServiceMBeanSupport overrides ---------------------------------------------------
   
   public ObjectName getObjectName( MBeanServer pServer, ObjectName pName ) {
      return getObjectName();
   }
   
   public void postRegister( java.lang.Boolean pRegistrationDone ) {
      log.debug("postRegister(), parent: " + mParent );
      if( pRegistrationDone.booleanValue() && mParent != null ) {
         try {
            // Notify the parent about its new child
            getServer().invoke(
               mParent,
               "addChild",
               new Object[] { mName },
               new String [] { ObjectName.class.getName() }
            );
            super.postRegister( pRegistrationDone );
         }
         catch( JMException jme ) {
            jme.printStackTrace();
            // Stop it because of the error
            super.postRegister( new Boolean( false ) );
         }
      }
   }
   
   public void preDeregister()
      throws Exception
   {
      log.debug("preDeregister(), parent: " + mParent );
      try {
         // Only remove child if it is a child (root has not parent)
         if( mParent != null ) {
            // Notify the parent about its new child
            getServer().invoke(
               mParent,
               "removeChild",
               new Object[] { mName },
               new String [] { ObjectName.class.getName() }
            );
         }
      }
      catch( JMException jme ) {
         jme.printStackTrace();
      }
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "J2EEManagedObject [ name: " + mName + ", parent: " + mParent + " ];";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
