/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.notification;

import java.io.Serializable;
import java.util.Random;

import javax.management.InstanceNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.jboss.jmx.connector.JMXConnector;

/**
* Basic Local Listener to receive Notification from a remote JMX Agent
**/
public abstract class ClientNotificationListener {

   private ObjectName               mSender;
   private ObjectName               mRemoteListener;
   protected NotificationListener   mClientListener;
   protected Object                 mHandback;
   private Random                   mRandom = new Random();
   
   public ClientNotificationListener(
      ObjectName pSender,
      NotificationListener pClientListener,
      Object pHandback
   ) {
      mSender = pSender;
      mClientListener = pClientListener;
      mHandback = pHandback;
   }
   
   public ObjectName createListener(
      JMXConnector pConnector,
      String mClass,
      Object[] pParameters,
      String[] pSignatures
   ) throws
      MalformedObjectNameException,
      ReflectionException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException
   {
      ObjectName lName = null;
      while( lName == null ) {
         try {
            lName = new ObjectName( "JMX:type=listener,id=" + mRandom.nextLong() );
            System.out.println( "ClientNotificationListener.createListener(), " +
               "class: " + mClass + ", name: " + lName +
               "parameters: " + java.util.Arrays.asList( pParameters ) +
               "signatures: " + java.util.Arrays.asList( pSignatures )
            );
            ObjectInstance lInstance = pConnector.createMBean(
               mClass,
               lName,
               pParameters,
               pSignatures
            );
            lName = lInstance.getObjectName();
         }
         catch( InstanceAlreadyExistsException iaee ) {
            lName = null;
         }
      }
      mRemoteListener = lName;
      return lName;
   }

   public void addNotificationListener(
      JMXConnector pConnector,
      NotificationFilter pFilter
   ) throws
      InstanceNotFoundException
   {
      pConnector.addNotificationListener(
         mSender,
         mRemoteListener,
         pFilter,
         null
      );
   }
   
   public ObjectName getSenderMBean() {
      return mSender;
   }
   
   public ObjectName getRemoteListenerName() {
      return mRemoteListener;
   }

   public boolean equals( Object pTest ) {
      if( pTest instanceof ClientNotificationListener ) {
         ClientNotificationListener lListener = (ClientNotificationListener) pTest;
         return
            mSender.equals( lListener.mSender ) &&
            mClientListener.equals( lListener.mClientListener );
      }
      return false;
   }

}
