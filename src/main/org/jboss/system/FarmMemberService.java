/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.JMRuntimeException;
import javax.management.MBeanException;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.deployment.DeploymentException;
import org.jboss.jmx.connector.RemoteMBeanServer;

/**
 * This class indicates that this JBoss instance is a member of a farm
 * builded by all the classes connecting to each other.
 * <br>
 * How to set up a farm:
 * <ul>
 * <li>Start a Farm Member Service MBean on each node becoming a member
 *     of the farm
 * <li>Add each node to an existing member of the farm. When there is
 *     no node added then first node which adds another node becomes
 *     the initial member of the farm. To do so use {@link #addMember
 *     addMember()} method or add the new members in bulk with
 *     {@link #setMembers setMembers()} method.
 * </ul>
 *
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 1.1 $ <p>
 *
 * <b>Revisions:</b> <p>
 *
 * <p><b>20011015 andreas schaefer:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 **/
public class FarmMemberService
   extends ServiceMBeanSupport
   implements FarmMemberServiceMBean, MBeanRegistration
{

   // Attributes ----------------------------------------------------

   /** A callback to the JMX Agent **/
   private MBeanServer mServer;
   
   private ObjectName mDeployerService = null;

   private Member mLocal = new Member( null, null );
   
   private String mInitialMembers = null;
   
   private ArrayList mMembers = new ArrayList();
   private Hashtable mDeployedServices = new Hashtable();
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   /**
   * Creates a undefined farm member service. Both the local JNDI-Server Name
   * as well as the local Adaptor JNDI-Name must be set before starting this
   * MBean.
   **/
   public FarmMemberService() {
   }
   
   /**
   * Creates the farm member service which is the first step to build
   * up a farm. Afterwards each server must be added to any of the
   * existing member of the farm.
   *
   * @param pLocalJNDIServerName Name of the JNDI-Server hosting the local
   *                             Adaptor
   * @param pLocalAdaptorJNDIName JNDI-Name of the Adaptor available for
   *                              this member which is subsequently used
   *                              by all other members. Therefore I must
   *                              not be null
   **/
   public FarmMemberService(
      String pLocalJNDIServerName,
      String pLocalAdaptorJNDIName
   ) throws
      NamingException
   {
      setLocalJNDIServerName( pLocalJNDIServerName );
      setLocalAdaptorJNDIName( pLocalAdaptorJNDIName );
      System.out.println( "Local Member: " + mLocal );
   }

   // Public --------------------------------------------------------
   
   public void setLocalJNDIServerName( String pLocalJNDIServerName ) {
      mLocal.setJNDIServerName( pLocalJNDIServerName );
   }
   
   public void setLocalAdaptorJNDIName( String pLocalAdaptorJNDIName ) {
      mLocal.setAdaptorJNDIName( pLocalAdaptorJNDIName );
   }

   public List getMemberList() {
      return mMembers;
   }
   
   public Member addMember(
      String pJNDIServerName,
      String pAdaptorJNDIName
   ) {
      try {
         Member lServer = new Member( pJNDIServerName, pAdaptorJNDIName );
         // If already registered to nothing
         if( mMembers.indexOf( lServer ) >= 0 ) {
            log.info( "Farm Member already registered: " + lServer );
         } else {
            RemoteMBeanServer lConnector = lServer.getConnector();
            // Check if the Remote FMS is up an running
            Set lMemberServices = lConnector.queryNames(
               new ObjectName( OBJECT_NAME ),
               null
            );
            if( lMemberServices.size() == 1 ) {
               // Member Service found add it to its own list
               mMembers.add( lServer );
               // Now ensure that all other service now it
               Iterator i = mMembers.iterator();
               while( i.hasNext() ) {
                  Member lMember = (Member) i.next();
                  // Only call the addMember() method when not local
                  if( !mLocal.equals( lMember ) ) {
                     log.info( "Invoke addMember() on the remote member: " + lMember );
                     lMember.getConnector().invoke(
                        new ObjectName( OBJECT_NAME ),
                        "addMember",
                        new Object[] { pJNDIServerName, pAdaptorJNDIName },
                        new String[] { String.class.getName(), String.class.getName() }
                     );
                  }
               }
            } else {
               if( lMemberServices.size() == 0 ) {
                  log.info( "No Farm Member Service found on the remote server" );
               } else {
                  log.info( "More than one Farm Member Service found on the remote server" );
               }
            }
         }
         return lServer;
      }
      catch( Exception e ) {
         logException( e );
         return null;
      }
   }
   
   public void removeMember(
      String pJNDIServerName,
      String pAdaptorJNDIName
   ) {
      try {
         Member lTemp = new Member( pJNDIServerName, pAdaptorJNDIName );
         mMembers.remove( lTemp );
         Iterator i = mMembers.iterator();
         while( i.hasNext() ) {
            Member lMember = (Member) i.next();
            if( !mLocal.equals( lMember ) ) {
               lMember.getConnector().invoke(
                  new ObjectName( OBJECT_NAME ),
                  "removeMember",
                  new Object[] { pJNDIServerName, pAdaptorJNDIName },
                  new String[] { String.class.getName(), String.class.getName() }
               );
            }
         }
      }
      catch( JMException jme ) {
         logException( jme );
      }
      catch( NamingException ne ) {
         log.error( ne );
      }
   }
   
   public Member getMembership() {
      return mLocal;
   }
   
   public void setMembers( String pMemberData ) {
      if( getState() != STARTING && getState() != STARTED ) {
         mInitialMembers = pMemberData;
      } else {
         mInitialMembers = null;
         StringTokenizer lTokenizer = new StringTokenizer( pMemberData, "\n" );
         while( lTokenizer.hasMoreTokens() ) {
            String lToken = lTokenizer.nextToken();
            int lIndex = lToken.indexOf( ',' );
            if( lIndex > 0 && lIndex < ( lToken.length() - 1 ) ) {
               String lServer = lToken.substring( 0, lIndex ).trim();
               String lAdaptor = lToken.substring( lIndex + 1 ).trim();
               log.info( "Add new member: " + lServer + ", " + lAdaptor );
               addMember( lServer, lAdaptor );
            }
         }
      }
   }

   public FilenameFilter getDeployableFilter() {
      try {
         FilenameFilter lFilter = (FilenameFilter) mServer.getAttribute(
            mDeployerService,
            "DeployableFilter"
         );
         log.info( "Deployable Filter is: " + lFilter );
         return lFilter;
      }
      catch( JMException jme ) {
         logException( jme );
      }
      return null;
   }
   
   public void deploy( String pFileURL )
      throws
         MalformedURLException, 
         IOException, 
         DeploymentException
   {
      deploy( new URL( pFileURL ) );
   }

   public void undeploy( String pFileURL )
      throws
         MalformedURLException, 
         IOException, 
         DeploymentException
   {
//      undeploy( new URL( pFileURL ) );
   }

   public boolean isDeployed( String pFileURL )
      throws
         MalformedURLException, 
         DeploymentException
   {
      try {
         return ( (Boolean) mServer.getAttribute(
            mDeployerService,
            "Deployed"
         ) ).booleanValue();
      }
      catch( JMException jme ) {
         logException( jme );
         throw new DeploymentException( "Could not retrieve isDeployed attribute from local Deployer Service", jme );
      }
   }

   public void deploy( URL pFile ) {
      try {
         log.info( "deploy(), file: " + pFile );
         // Get the date of the file
         File lFile = new File( pFile.getFile() );
         Date lFileDate = new Date( lFile.lastModified() );
         Iterator i = mMembers.iterator();
         if( i.hasNext() ) {
            // Create File ByteArray
            byte[] lBuffer = new byte[ 1024 ];
            InputStream lInput = new FileInputStream( lFile );
            ByteArrayOutputStream lOutput = new ByteArrayOutputStream();
            int j = 0;
            while( ( j = lInput.read( lBuffer ) ) > 0 ) {
               lOutput.write( lBuffer, 0, j );
            }
            FileArray lFileArray = new FileArray( lFile, lOutput.toByteArray() );
            while( i.hasNext() ) {
               Member lMember = (Member) i.next();
               lMember.getConnector().invoke(
                  new ObjectName( OBJECT_NAME ),
                  "deployOnMember",
                  new Object[] {
                     lFileArray,
                     lFileDate
                  },
                  new String[] {
                     FileArray.class.getName(),
                     Date.class.getName()
                  }
               );
            }
         }
      }
      catch( Exception e ) {
         logException( e );
      }
   }
   
   public void deployOnMember( FileArray pFile, Date pDate ) {
      try {
         // Create File locally and use it
         File lFile = new File( "../tmp", pFile.mFile.getName() );
         FileOutputStream lOutput = new FileOutputStream( lFile );
         lOutput.write( pFile.mContent );
         lOutput.close();
         log.info( "deployOnMember(), File: " + lFile + ", data: " + pDate );
         Date lLastDate = (Date) mDeployedServices.get( lFile.getName() );
         if( lLastDate == null || lLastDate.before( pDate ) ) {
            log.info( "deployOnMember(), deploy locally: " + lFile );
            // Deploy file on Service Deployer
            mServer.invoke(
               mDeployerService,
               "deploy",
               new Object[] { lFile.toURL().toString() },
               new String[] { String.class.getName() }
            );
            log.info( "deployOnMember(), add file served" );
            mDeployedServices.put( lFile.getName(), pDate );
         }
      }
      catch( Exception e ) {
         logException( e );
      }
   }
   
   // MBeanRegistration implementation ----------------------------------------

   /**
    * #Description of the Method
    *
    * @param server Description of Parameter
    * @param name Description of Parameter
    * @return Description of the Returned Value
    * @exception Exception Description of Exception
    */
   public ObjectName preRegister(
      MBeanServer pServer,
      ObjectName pName
   ) throws
      Exception
   {
      mServer = pServer;

      log.info("Farm Member Service MBean online");
      return new ObjectName( OBJECT_NAME );
   }

   // Service implementation ----------------------------------------

   public String getName()
   {
      return "Farm Member Service";
   }

   protected void startService()
      throws Exception
   {
      mDeployerService = new ObjectName( "JBOSS-SYSTEM:service=ServiceDeployer" );
      if( mInitialMembers != null ) {
         log.info( "start(), state: " + getState() );
         setMembers( mInitialMembers );
      }
   }
   
   protected void stopService()
   {
      // Stopping means that this server is removed as a member
      // of the farm
      removeMember(
         mLocal.getJNDIServerName(),
         mLocal.getAdaptorJNDIName()
      );
   }

   // Protected -----------------------------------------------------

   /**
    * Go through the myriad of nested JMX exception to pull out the true
    * exception if possible and log it.
    *
    * @param e The exception to be logged.
    */
   private void logException(Throwable e)
   {
      if (e instanceof RuntimeErrorException)
      {
         e = ((RuntimeErrorException)e).getTargetError();
      }
      else if (e instanceof RuntimeMBeanException)
      {
         e = ((RuntimeMBeanException)e).getTargetException();
      }
      else if (e instanceof RuntimeOperationsException)
      {
         e = ((RuntimeOperationsException)e).getTargetException();
      }
      else if (e instanceof MBeanException)
      {
         e = ((MBeanException)e).getTargetException();
      }
      else if (e instanceof ReflectionException)
      {
         e = ((ReflectionException)e).getTargetException();
      }
      e.printStackTrace();
      log.error(e);
   }

}
