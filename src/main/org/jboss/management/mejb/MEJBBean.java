/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.mejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.management.j2ee.MEJBServer;
import javax.management.j2ee.Attribute;
import javax.management.j2ee.AttributeList;
/*
import javax.management.j2ee.;
import javax.management.j2ee.;
import javax.management.j2ee.;
import javax.management.j2ee.;
import javax.management.j2ee.;
import javax.management.j2ee.;
*/

/**
* Management Session Bean to enable the client to manage the
* server its is deployed on.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
* @version $Revision: 1.3 $
*
* @ejb:bean name="MEJB"
*           display-name="JBoss Management EJB (MEJB)"
*           type="Stateless"
*           jndi-name="ejb/mgmt/J2EEManagement"
* @ejb:interface extends="javax.management.j2ee.MEJBServer"
* @--ejb:ejb-ref ejb-name="jboss/survey/Survey"
*
**/
public class MEJBBean
   implements SessionBean
{
   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------
   
   private SessionContext mContext;
   
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Object getAttribute( ObjectName pName, String pAttribute )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public AttributeList getAttributes( ObjectName pName, String pAttributes )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public String getDefaultDomain()
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Integer getManagedObjectCount()
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Object invoke( ObjectName pName, String pOperationName, Object[] pParams, String[] pSignature )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public boolean isRegistered( ObjectName pName )
      throws RemoteException
   {
      return false;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Set queryNames( ObjectName pName )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Attribute setAttribute( ObjectName pName, Attribute pAttribute )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public AttributeList setAttributes( ObjectName pName, AttributeList pAttributes )
      throws RemoteException
   {
      return null;
   }
   
   /**
   * Create the Session Bean
   *
   * @throws CreateException 
   *
   * @ejb:create-method view-type="remote"
   **/
   public void ejbCreate()
      throws
         CreateException
   {
   }
   
   /**
   * Describes the instance and its content for debugging purpose
   *
   * @return Debugging information about the instance and its content
   **/
   public String toString()
   {
      return "MEJB [ " + " ]";
   }
   
   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------  
   
   /**
   * Set the associated session context. The container invokes this method on 
   * an instance after the instance has been created. 
   * <p>This method is called with no transaction context.
   *
   * @param aContext A SessionContext interface for the instance. The instance 
   *  should store the reference to the context in an instance variable.
   * @throws EJBException Should something go wrong while seting the context,
   *  an EJBException will be thrown.
   **/
   public void setSessionContext( SessionContext aContext )
      throws
         EJBException
   {
      mContext = aContext;
   }
   
   
   /**
   * The activate method is called when the instance is activated from its 
   * "passive" state. The instance should acquire any resource that it has 
   * released earlier in the ejbPassivate() method. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbActivate()
      throws
         EJBException
   {
   }
   
   
   /**
   * The passivate method is called before the instance enters the "passive" 
   * state. The instance should release any resources that it can re-acquire 
   * later in the ejbActivate() method. 
   * <p>After the passivate method completes, the instance must be in a state 
   * that allows the container to use the Java Serialization protocol to 
   * externalize and store away the instance's state. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbPassivate()
      throws
         EJBException
   {
   }
   
   
   /**
   * A container invokes this method before it ends the life of the session 
   * object. This happens as a result of a client's invoking a remove 
   * operation, or when a container decides to terminate the session object 
   * after a timeout. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbRemove()
      throws
         EJBException
   {
   }
}
