/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.lang.reflect.Constructor;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.jboss.logging.Log;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.util.ServiceMBeanSupport;

/**
 * Scheduler Instance to allow clients to run this as a
 * scheduling service for any Schedulable instances.
 *
 * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
 **/
public class Scheduler
   extends ServiceMBeanSupport
   implements SchedulerMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static String JNDI_NAME = "scheduler:domain";
   public static String JMX_NAME = "scheduler";

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private MBeanServer mServer;
   private String mName;
   
   private long mSchedulePeriod;
   private int mRemainingRepetitions = 0;
   private int mActualSchedule = -1;
   private boolean mScheduleIsStarted = false;
   private boolean mWaitForNextCallToStop = false;
   private ObjectName mTimer;
   private Schedulable mSchedulable;
   
   private boolean mStartOnStart = false;
   private String mSchedulableClass;
   private Object[] mInitArguments;
   private String[] mInitTypes;
   private Date mStartDate;
   private long mPeriod;
   private int mRepetitions;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------  

   /**
    * Default (no-args) Constructor
    **/
   public Scheduler()
   {
      mName = null;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public Scheduler( String pName )
   {
      mName = pName;
   }

   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public Scheduler(
      String pName,
      String pSchedulableClass,
      String pInitArguments,
      String pInitTypes,
      long pInitialStartDate,
      long pSchedulePeriod,
      long pNumberOfRepetitions
   ) {
      mName = pName;
      mStartOnStart = true;
      mSchedulableClass = pSchedulableClass;
//      if( pInitArguments == null || pInitArguments.equals( "" ) ) {
         mInitArguments = new Object[ 0 ];
//      }
//      if( pInitTypes == null || pInitTypes.equals( "" ) ) {
         mInitArguments = new String[ 0 ];
//      }
      mStartDate = new Date( pInitialStartDate );
      mPeriod = pSchedulePeriod;
      mRepetitions = (int) pNumberOfRepetitions;
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Methods
   // -------------------------------------------------------------------------  
   
   public void startSchedule(
      String pSchedulableClass,
      Object[] pInitArguments,
      String[] pInitTypes,
      Date pInitialStartDate,
      long pSchedulePeriod,
      int pNumberOfRepetitions
   ) {
      // Check if not already started
      if( !isStarted() ) {
         try {
            // Try to load the Schedulable Class
            Class lSchedulableClass = Thread.currentThread().getContextClassLoader().loadClass( pSchedulableClass );
            // Create an instance of it
            if( pInitArguments == null ) {
               pInitArguments = new Object[ 0 ];
            }
            if( pInitTypes == null ) {
               pInitTypes = new String[ 0 ];
            }
            if( pInitArguments.length != pInitTypes.length ) {
               throw new InvalidParameterException( "Constructor Arguments and Data Types does not match" );
            }
            Class[]  lInitTypes = new Class[ pInitTypes.length ];
            for( int i = 0; i < pInitTypes.length; i++ ) {
               lInitTypes[ i ] = Thread.currentThread().getContextClassLoader().loadClass( pInitTypes[ i ] );
            }
            Constructor lSchedulableConstructor = lSchedulableClass.getConstructor( lInitTypes );
            mSchedulable = (Schedulable) lSchedulableConstructor.newInstance( pInitArguments );
            // Register the notificaiton listener at the MBeanServer
            mServer.addNotificationListener(
               mTimer, 
               new Listener( mSchedulable ),
               // No filter
               null,
               // No object handback necessary
               null
            );
            mRemainingRepetitions = pNumberOfRepetitions;
            mSchedulePeriod = pSchedulePeriod;
            // Register the Schedule at the Timer
            if( pInitialStartDate == null || pInitialStartDate.getTime() < new Date().getTime() ) {
               pInitialStartDate = new Date( new Date().getTime() + 1000 );
               // Start Schedule now
               System.out.println( "Start regular Schedule with period: " + getSchedulePeriod() );
               if( getRemainingRepetitions() > 0 ) {
                  System.out.println( "Start Schedule wtih " + getRemainingRepetitions() + " reps." );
                  mActualSchedule = ( (Integer) mServer.invoke(
                     mTimer,
                     "addNotification",
                     new Object[] {
                        "Schedule",
                        "Scheduler Notification",
                        null,
                        new Date( new Date().getTime() + 1000 ),
                        new Long( getSchedulePeriod() ),
                        new Long( (long) getRemainingRepetitions() )
                     },
                     new String[] {
                        "".getClass().getName(),
                        "".getClass().getName(),
                        "java.lang.Object",
                        Date.class.getName(),
                        Long.TYPE.getName(),
                        Long.TYPE.getName()
                     }
                  ) ).intValue();
               }
               else {
                  System.out.println( "Start Schedule with unlimited reps." );
                  mActualSchedule = ( (Integer) mServer.invoke(
                     mTimer,
                     "addNotification",
                     new Object[] {
                        "Schedule",
                        "Scheduler Notification",
                        null,
                        new Date( new Date().getTime() + 1000 ),
                        new Long( getSchedulePeriod() )
                     },
                     new String[] {
                        String.class.getName(),
                        String.class.getName(),
                        Object.class.getName(),
                        Date.class.getName(),
                        Long.TYPE.getName()
                     }
                  ) ).intValue();
               }
            }
            else {
               // Add an initial call
               mActualSchedule = ( (Integer) mServer.invoke(
                  mTimer,
                  "addNotification",
                  new Object[] {
                     "Schedule",
                     "Scheduler Notification",
                     pInitialStartDate
                  },
                  new String[] {
                     "".getClass().getName(),
                     "".getClass().getName(),
                     Date.class.getName(),
                  }
               ) ).intValue();
            }
            mScheduleIsStarted = true;
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }
   
   public void stopSchedule(
      boolean pDoItNow
   ) {
      try {
         if( pDoItNow ) {
            // Remove notification listener now
            mWaitForNextCallToStop = false;
            mServer.invoke(
               mTimer,
               "removeNotification",
               new Object[] {
                  new Integer( mActualSchedule )
               },
               new String[] {
                  Integer.TYPE.getName(),
               }
            );
            mActualSchedule = -1;
            mScheduleIsStarted = false;
         }
         else {
            mWaitForNextCallToStop = true;
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
   }
   
   public long getSchedulePeriod() {
      return mSchedulePeriod;
   }
   
   public int getRemainingRepetitions() {
      return mRemainingRepetitions;
   }
   
   public boolean isStarted() {
      return mScheduleIsStarted;
   }

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   public ObjectName getObjectName(
      MBeanServer pServer,
      ObjectName pName
   )
      throws MalformedObjectNameException
   {
      mServer = pServer;
      return pName;
   }
   
   public String getJNDIName() {
      if( mName != null ) {
         return JMX_NAME + ":" + mName;
      }
      else {
         return JMX_NAME;
      }
   }
   
   public String getName() {
      return "JBoss Scheduler MBean";
   }
   
   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------  

   protected void initService()
        throws Exception
   {
   }
   
   protected void startService()
        throws Exception
   {
      bind( this );
      try {
         // Create Timer MBean
         mTimer = new ObjectName( "DefaultDomain", "service", "Timer" );
         mServer.createMBean( "javax.management.timer.Timer", mTimer );
         // Now start the Timer
         mServer.invoke(
            mTimer,
            "start",
            new Object[] {},
            new String[] {}
         );
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      if( mStartOnStart ) {
         startSchedule(
            mSchedulableClass,
            mInitArguments,
            mInitTypes,
            mStartDate,
            mPeriod,
            mRepetitions
         );
      }
   }
   
   protected void stopService() {
      try {
         unbind();
      }
      catch( Exception e ) {
         log.exception( e );
      }
   }

   // -------------------------------------------------------------------------
   // Helper methods to bind/unbind the Management class
   // -------------------------------------------------------------------------

	private void bind( Scheduler pScheduler )
      throws
         NamingException
   {
		Context lContext = new InitialContext();
		String lJNDIName = getJNDIName();

		// Ah ! JBoss Server isn't serializable, so we use a helper class
		NonSerializableFactory.bind( lJNDIName, pScheduler );

      //AS Don't ask me what I am doing here
		Name lName = lContext.getNameParser("").parse( lJNDIName );
		while( lName.size() > 1 ) {
			String lContextName = lName.get( 0 );
			try {
				lContext = (Context) lContext.lookup(lContextName);
			}
			catch( NameNotFoundException e )	{
				lContext = lContext.createSubcontext(lContextName);
			}
			lName = lName.getSuffix( 1 );
		}

		// The helper class NonSerializableFactory uses address type nns, we go on to
		// use the helper class to bind the javax.mail.Session object in JNDI
		StringRefAddr lAddress = new StringRefAddr( "nns", lJNDIName );
		Reference lReference = new Reference(
         Scheduler.class.getName(),
         lAddress,
         NonSerializableFactory.class.getName(),
         null
      );
		lContext.bind( lName.get( 0 ), lReference );

		log.log( "JBoss Scheduler Service '" + getJNDIName() + "' bound to " + lJNDIName );
	}

	private void unbind() throws NamingException {
      String lJNDIName = getJNDIName();

      new InitialContext().unbind( lJNDIName );
      NonSerializableFactory.unbind( lJNDIName );
      log.log("JBoss Scheduler service '" + lJNDIName + "' removed from JNDI" );
	}

   // -------------------------------------------------------------------------
   // Inner Classes
   // -------------------------------------------------------------------------

   public class Listener
      implements NotificationListener
   {
      
      private Schedulable mDelegate;
      
      public Listener( Schedulable pDelegate ) {
         mDelegate = pDelegate;
      }
      
      public void handleNotification(
         Notification pNotification,
         Object pHandback 
      ) {
         System.out.println( "Listener.handleNotification(), notification: " + pNotification );
         try {
            // If schedule is started invoke the schedule method on the Schedulable instance
            if( isStarted() ) {
               if( getRemainingRepetitions() > 0 || getRemainingRepetitions() < 0 ) {
                  mDelegate.perform(
                     new Date(),
                     getRemainingRepetitions()
                  );
                  if( mRemainingRepetitions > 0 ) {
                     mRemainingRepetitions--;
                  }
                  if( getRemainingRepetitions() == 0 || mWaitForNextCallToStop ) {
                     stopSchedule( true );
                  }
                  else {
                     if( "InitialCall".equals( pNotification.getType() ) ) {
                        // When Initial Call then setup the regular schedule
                        // By first removing the initial one and then adding the
                        // regular one.
                        mServer.invoke(
                           mTimer,
                           "removeNotification",
                           new Object[] {
                              new Integer( mActualSchedule )
                           },
                           new String[] {
                              Integer.TYPE.getName(),
                           }
                        );
                        // Add regular schedule
                        mActualSchedule = ( (Integer) mServer.invoke(
                           mTimer,
                           "addNotification",
                           new Object[] {
                              "Schedule",
                              "Scheduler Notification",
                              new Date( new Date().getTime() + 1000 ),
                              new Long( getSchedulePeriod() ),
                              new Long( getRemainingRepetitions() )
                           },
                           new String[] {
                              "".getClass().getName(),
                              "".getClass().getName(),
                              Date.class.getName(),
                              Long.TYPE.getName(),
                              Long.TYPE.getName()
                           }
                        ) ).intValue();
                     }
                  }
               }
            }
            else {
               // Schedule is stopped therefore remove the Schedule
               mServer.invoke(
                  mTimer,
                  "removeNotification",
                  new Object[] {
                     new Integer( mActualSchedule )
                  },
                  new String[] {
                     Integer.TYPE.getName(),
                  }
               );
               mActualSchedule = -1;
            }
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }
   
   /**
    * A test class for a Schedulable Class
    **/
   public static class SchedulableExample
      implements Schedulable
   {
      
      /**
       * Just log the call
       **/
      public void perform(
         Date pTimeOfCall,
         int pRemainingRepetitions
      ) {
         System.out.println( "Schedulable Examples is called at: " + pTimeOfCall +
            ", remaining repetitions: " + pRemainingRepetitions );
      }
   }
}
