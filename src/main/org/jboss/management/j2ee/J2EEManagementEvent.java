/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.io.Serializable;

import javax.management.Notification;

/**
 * Event to be send by an event emitter. Implementation of
 * {@link javax.management.j2ee.J2EEManagementEvent J2EEManagementEvent}.
 * This instance contains the JMX Notification used to transport the event
 * inside the JMX Agent. Afterwards it is just used as data container.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public class J2EEManagementEvent
   implements javax.management.j2ee.J2EEManagementEvent, Serializable
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private Notification mJMX;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Creates a J2EE Management Event where the timestamp is set to the current
   * time
   *
   * @param pType Type of the Event
   * @param pSource Source Object Name of the Event
   * @param pSequenceNumber Sequence Number within the source object
   **/
   public J2EEManagementEvent( String pType, String pSource, long pSequenceNumber ) {
      this( pType, pSource, pSequenceNumber, 0, "" );
   }
   
   /**
   * Creates a J2EE Management Event where the timestamp is set to the current
   * time
   *
   * @param pType Type of the Event
   * @param pSource Source Object Name of the Event
   * @param pSequenceNumber Sequence Number within the source object
   * @param pMessage Message delivered with the event
   **/
   public J2EEManagementEvent( String pType, String pSource, long pSequenceNumber, String pMessage ) {
      this( pType, pSource, pSequenceNumber, 0, pMessage );
   }
   
   /**
   * Creates a J2EE Management Event where the timestamp is set to the current
   * time
   *
   * @param pType Type of the Event
   * @param pSource Source Object Name of the Event
   * @param pSequenceNumber Sequence Number within the source object
   * @param pTimeStamp Time of the events emission (in milliseconds since 1/1/1970 00:00:00)
   **/
   public J2EEManagementEvent( String pType, String pSource, long pSequenceNumber, long pTimeStamp ) {
      this( pType, pSource, pSequenceNumber, pTimeStamp, "" );
   }

   /**
   * Creates a J2EE Management Event where the timestamp is set to the current
   * time
   *
   * @param pType Type of the Event
   * @param pSource Source Object Name of the Event
   * @param pSequenceNumber Sequence Number within the source object
   * @param pTimeStamp Time of the events emission (in milliseconds since 1/1/1970 00:00:00)
   * @param pMessage Message delivered with the event
   **/
   public J2EEManagementEvent( String pType, String pSource, long pSequenceNumber, long pTimeStamp, String pMessage ) {
      mJMX = new Notification( pType, pSource, pSequenceNumber, pTimeStamp, pMessage );
      mJMX.setUserData( this );
   }
   
   // Public --------------------------------------------------------
   
   public Notification getNotification()
   {
      return mJMX;
   }
   
   // javax.management.j2ee.J2EEManagementEvent implementation ------
   
   public String getMessage()
   {
      return mJMX.getMessage();
   }
   
   public long getSequence()
   {
      return mJMX.getSequenceNumber();
   }
   
   public String getSource()
   {
      return (String) mJMX.getSource();
   }
   
   public String getType() {
      return mJMX.getType();
   }
   
   public long getWhen() {
      return mJMX.getTimeStamp();
   }
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "J2EEManagementEvent {} [ " + mJMX.toString() + " ];";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
