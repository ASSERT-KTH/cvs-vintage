/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.notification;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
/*
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
*/

import javax.management.Notification;
import javax.management.NotificationListener;

/**
* Local JMX Listener to receive the message and send to the listener
**/
public class JMSClientNotificationListener implements MessageListener {

   private NotificationListener         mLocalListener;
   private Object                       mHandback;
   
   public JMSClientNotificationListener(
      NotificationListener pLocalListener,
      Object pHandback
   ) {
      mLocalListener = pLocalListener;
      mHandback = pHandback;
   }

   public void onMessage( Message pMessage ) {
      try {
         Notification lNotification = (Notification) ( (ObjectMessage) pMessage ).getObject();
         mLocalListener.handleNotification( lNotification, mHandback );
      }
      catch( JMSException je ) {
         je.printStackTrace();
      }
   }

   /** Redesign it (AS) **/
   public NotificationListener getLocalListener() {
      return mLocalListener;
   }
   /**
   * Test if this and the given Object are equal. This is true if the given
   * object both refer to the same local listener
   *
   * @param pTest                  Other object to test if equal
   *
   * @return                     True if both are of same type and
   *                           refer to the same local listener
   **/
   public boolean equals( Object pTest ) {
      if( pTest instanceof JMSClientNotificationListener ) {
         return mLocalListener.equals(
            ( (JMSClientNotificationListener) pTest).mLocalListener
         );
      }
      return false;
   }
   /**
   * @return                     Hashcode of the local listener
   **/
   public int hashCode() {
      return mLocalListener.hashCode();
   }
}
