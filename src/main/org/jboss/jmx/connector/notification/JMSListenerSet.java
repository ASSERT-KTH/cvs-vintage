/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.notification;

import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
* Container for a JMS Listener Set to find later on the
* Remote Listener based on the Object Name it is register
* on and the local Notification Listener
**/
public class JMSListenerSet {
   
   private ObjectName mName;
   private NotificationListener mListener;
   private JMSNotificationListener mRemoteListener;
   
   public JMSListenerSet(
      ObjectName pName,
      NotificationListener pListener,
      JMSNotificationListener pRemoteListener
   ) {
      mName = pName;
      mListener = pListener;
      mRemoteListener = pRemoteListener;
   }
   
   public NotificationListener getRemoteListener() {
      return mRemoteListener;
   }
   
   public boolean equals( Object pTest ) {
      if( pTest instanceof JMSListenerSet ) {
         JMSListenerSet lTest = (JMSListenerSet) pTest;
         return mName.equals( lTest.mName ) &&
            mListener.equals( lTest.mListener );
      }
      return false;
   }
}
