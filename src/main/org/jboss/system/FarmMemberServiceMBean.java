/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.management.ObjectName;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeployerMBean;
import org.jboss.jmx.adaptor.interfaces.Adaptor;
import org.jboss.jmx.adaptor.interfaces.AdaptorHome;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.jmx.connector.RemoteMBeanServer;
import org.jboss.jmx.connector.ejb.EJBConnector;
import org.jboss.jmx.connector.rmi.RMIConnectorImpl;

/** 
 * This is the main Service Controller API.
 * 
 * <p>A controller can deploy a service to a JBOSS-SYSTEM
 *    It installs by delegating, it configures by delegating
 * <b>Attention:</b><br>
 * JNDI-Server Name must be the same used on ALL members of
 * the farm. Therefore when you use "www.gugus.com" on one
 * member then you can't use "gugus.com" on another member
 * because the Name is used to compare the members. Also
 * local names must be full qualified if a member of the farm
 * is outsite therefore "box1.gugus.com" must be used everywhere
 * and cannot be shortent to "box1" on some of the members.
 *
 * @see Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.1 $
 *
 * <p><b>20011015 andreas schaefer:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public interface FarmMemberServiceMBean
   extends Service, DeployerMBean
{
   /** The default object name. */
   String OBJECT_NAME = "JBOSS-SYSTEM:spine=FarmMember";

   /**
   * Sets the local JNDI-Server Name
   *
   * @param pLocalJNDIServerName Name of the JNDI-Server which must not
   *                             be null
   **/
   public void setLocalJNDIServerName( String pLocalJNDIServerName );
   
   /**
   * Sets the local Adaptor JNDI-Name
   *
   * @param pLocalAdaptorJNDIName Name of the Adaptor registered on the
   *                              local JNDI-Server
   **/
   public void setLocalAdaptorJNDIName( String pLocalAdaptorJNDIName );
   
   /**
   * @return A list of Members registered at the farm currently. Each
   *         item is of type {@link Member Member}.
   **/
   public List getMemberList();
   
   /**
   * Adds a server to the farm. This method ensures that after adding
   * the given server as new member that all other members has it as
   * new member
   *
   * @param pJNDIServerName Server name where the JNDI-Server is hosted
   *                        to look up the Adaptor
   * @param pAdaptorJNDIName Name of the Adaptor on the JNDI-Server
   **/
   public Member addMember( String pJNDIServerName, String pAdaptorJNDIName );

   /**
   * Adds all the servers given by the parameter and adds them as new
   * members of the farm.
   *
   * @param pMemberData List of members which will be added as new members.
   *                    The list contains on each line first the JNDI-Server
   *                    name and then seperated by a comma the Adaptor
   *                    JNDI-Name
   **/
   public void setMembers( String pMemberData );
   
   /**
   * Removes a member of the farm from the farm. The method ensures that after
   * removing the member that all other members has it removed, too.
   *
   * @param pJNDIServerName Server name where the JNDI-Server is hosted
   *                        to look up the Adaptor
   * @param pAdaptorJNDIName Name of the Adaptor on the JNDI-Server
   **/
   public void removeMember( String pJNDIServerName, String pAdaptorJNDIName );
   
   /**
   * @return Own membership data of this service
   **/
   public Member getMembership();
   
   /**
   * Deploys a service on all members of a farm
   *
   * @param pFile File to be deployed
   **/
   public void deploy( URL pFile );
   
   /**
   * Deploys the service if not already done on this
   * member
   *
   * @param pFIle File of the service to be deployed
   * @param pDate Date of the file to be used to compare
   *              if already deployed
   **/
   public void deployOnMember( FileArray pFile, Date pDate );

   public class Member
      implements Serializable
   {
      private transient RemoteMBeanServer mConnector;
      
      private String mJNDIServerName;
      private String mAdaptorJNDIName;
      
      public Member( String pJNDIServerName, String pAdaptorJNDIName ) {
         mJNDIServerName = pJNDIServerName;
         mAdaptorJNDIName = pAdaptorJNDIName;
      }
      
      public RemoteMBeanServer getConnector()
         throws NamingException
      {
         try {
         if( mConnector == null ) {
            Hashtable lProperties = new Hashtable();
            lProperties.put( Context.PROVIDER_URL, mJNDIServerName );
            Context lContext = new InitialContext( lProperties );
            Object lObject = lContext.lookup( mAdaptorJNDIName );
            if( lObject instanceof RMIAdaptor ) {
               mConnector = new RMIConnectorImpl( (RMIAdaptor) lObject );
            } else
            if( lObject instanceof AdaptorHome ) {
               mConnector = new EJBConnector( (AdaptorHome) lObject );
            } else {
               throw new RuntimeException( "Object: " + lObject + " is not recognized JMX Adaptor" );
            }
         }
         return mConnector;
         }
         catch( NamingException ne ) {
            ne.printStackTrace();
            ne.getRootCause().printStackTrace();
            throw ne;
         }
      }
      
      public String getJNDIServerName() {
         return mJNDIServerName;
      }
      
      public void setJNDIServerName( String pName ) {
         mJNDIServerName = pName;
      }
      
      public String getAdaptorJNDIName() {
         return mAdaptorJNDIName;
      }
      
      public void setAdaptorJNDIName( String pName ) {
         mAdaptorJNDIName = pName;
      }
      
      public boolean equals( Object pTest ) {
         if( pTest instanceof Member ) {
            Member lTest = (Member) pTest;
            return
               getJNDIServerName().equals( lTest.getJNDIServerName() ) &&
               getAdaptorJNDIName().equals( lTest.getAdaptorJNDIName() );
         }
         return false;
      }
      
      public String toString() {
         return "FarmMemberServiceMBean.Member [ " +
            "JNDI-Server name: " + getJNDIServerName() +
            ", Adaptor JNDI-Name: " + getAdaptorJNDIName() +
            ", Connector: " + mConnector;
      }
   }
   
   public class FileArray implements Serializable {
      public File mFile;
      public byte[] mContent;
      
      public FileArray( File pFile, byte[] pContent ) {
         mFile = pFile;
         mContent = pContent;
      }
   }
}
