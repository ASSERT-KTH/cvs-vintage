/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.timer.TimerNotification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.jboss.logging.Logger;
import org.jboss.naming.NonSerializableFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
* Scheduler Instance to allow clients to run this as a scheduling service for
* any Schedulable instances.
* <br>
* ATTENTION: The scheduler instance only allows to run one schedule at a time.
* Therefore when you want to run two schedules create to instances with this
* MBean. Suggested Object Name for the MBean are:<br>
* jboss:service=Scheduler,schedule=<you schedule name><br>
* This way you should not run into a name conflict.
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
* @author Cameron (camtabor)
*
* <p><b>Revisions:</b></p>
* <p><b>20010814 Cameron:</b>
* <ul>
* <li>Checks if the TimerMBean is already loaded</li>
* <li>Created a SchedulerNotificationFilter so that each Scheduler only
*     get its notifications</li>
* <li>Stop was broken because removeNotification( Integer ) was broken</li>
* </ul>
* </p>
* <p><b>20011026 Andy:</b>
* <ul>
* <li>Move the SchedulerNotificationFilter to become an internal class
*     and renamed to NotificationFilter</li>
* <li>MBean is not bind/unbind to JNDI server anymore</li>
* </ul>
* </p>
* <p><b>20020117 Andy:</b>
* <ul>
* <li>Change the behaviour when the Start Date is in the past. Now the
*     Scheduler will behave as the Schedule is never stopped and find
*     the next available time to start with respect to the settings.
*     Therefore you can restart JBoss without adjust your Schedule
*     every time. BUT you will still loose the calls during the Schedule
*     was down.</li>
* <li>Added parsing capabilities to setInitialStartDate. Now NOW: current time,
*     and a string in a format the SimpleDataFormat understand in your environment
*     (US: m/d/yy h:m a) but of course the time in ms since 1/1/1970.</li>
* <li>Some fixes like the stopping a Schedule even if it already stopped etc.</li>
* </ul>
* </p>
* <p><b>20020118 Andy:</b>
* <ul>
* <li>Added the ability to call another MBean instead of an instance of the
*     given Schedulable class. Use setSchedulableMBean() to specify the JMX
*     Object Name pointing to the given MBean. Then if the MBean does not
*     contain the same method as the Schedulable instance you have to specify
*     the method with setSchedulableMBeanMethod(). There you can use some
*     constants.</li>
* </p>
* <p><b>20020119 Andy:</b>
* <ul>
* <li>Added a helper method isActive()</li>
* <li>Fixed a bug not indicating that no MBean is used when setSchedulableClass()
*     is called.</li>
* <li>Fixed a bug therefore that when NOW is set as initial start date a restart
*     of the Schedule will reset the start date to the current date</li>
* <li>Fixed a bug because the start date was update during recalculation of the
*     start date when the original start date is in the past (see above). With this
*     you could restart the schedule even after all hits were fired when the
*     schedule was not started immediately after the initial start date was set.</li>
* </p>
**/
public class Scheduler
   extends ServiceMBeanSupport
   implements SchedulerMBean
{

   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------
   
   /** Class logger. */
   private static final Logger log = Logger.getLogger( Scheduler.class );
   
   public static String JNDI_NAME = "scheduler:domain";
   public static String JMX_NAME = "scheduler";
   
   private static final int NOTIFICATION = 0;
   private static final int DATE = 1;
   private static final int REPETITIONS = 2;
   private static final int SCHEDULER_NAME = 3;
   private static final int NULL = 4;
   
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   
   private long mActualSchedulePeriod;
   private long mRemainingRepetitions = 0;
   private int mActualSchedule = -1;
   private ObjectName mTimer;
   private Schedulable mSchedulable;
//as   private Object mSchedulableMBeanObjectName;
   
   private boolean mScheduleIsStarted = false;
   private boolean mWaitForNextCallToStop = false;
   private boolean mStartOnStart = false;
   private boolean mIsRestartPending = true;
   
   // Pending values which can be different to the actual ones
   private boolean mUseMBean = false;
   
   private Class mSchedulableClass;
   private String mSchedulableArguments;
   private String[] mSchedulableArgumentList = new String[ 0 ];
   private String mSchedulableArgumentTypes;
   private Class[] mSchedulableArgumentTypeList = new Class[ 0 ];
   
   private ObjectName mSchedulableMBean;
   private String mSchedulableMBeanMethod;
   private String mSchedulableMBeanMethodName;
   private int[] mSchedulableMBeanArguments = new int[ 0 ];
   private String[] mSchedulableMBeanArgumentTypes = new String[ 0 ];
   
   private DateFormat mDateFormatter;
   private Date mStartDate;
   private String mStartDateString;
   private boolean mStartDateIsNow;
   private long mSchedulePeriod;
   private long mInitialRepetitions;
   
   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------
   
   /**
    * Default (no-args) Constructor
    **/
   public Scheduler()
   {
   }
   
   /**
    * Constructor with the necessary attributes to be set
    *
    * @param pName Name of the MBean
    **/
   public Scheduler(
      String pSchedulableClass,
      String pInitArguments,
      String pInitTypes,
      String pInitialStartDate,
      long pSchedulePeriod,
      long pNumberOfRepetitions
   ) {
      setStartAtStartup( true );
      setSchedulableClass( pSchedulableClass );
      setSchedulableArguments( pInitArguments );
      setSchedulableArgumentTypes( pInitTypes );
      setInitialStartDate( pInitialStartDate );
      setSchedulePeriod( pSchedulePeriod );
      setInitialRepetitions( pNumberOfRepetitions );
   }

   // -------------------------------------------------------------------------
   // SchedulerMBean Methods
   // -------------------------------------------------------------------------

   public void startSchedule() {
      // Check if not already started
      if( !isStarted() ) {
         try {
            // Check the given attributes if correct
            if( mUseMBean ) {
               if( mSchedulableMBean == null ) {
                  log.debug( "Schedulable MBean Object Name is not set" );
                  throw new InvalidParameterException(
                     "Schedulable MBean must be set"
                  );
               }
               if( mSchedulableMBeanMethodName == null ) {
                  mSchedulableMBeanMethodName = "perform";
                  mSchedulableMBeanArguments = new int[] { DATE, REPETITIONS };
                  mSchedulableMBeanArgumentTypes = new String[] {
                     Date.class.getName(),
                     Integer.TYPE.getName()
                  };
               }
            } else {
               if( mSchedulableClass == null ) {
                  log.debug( "Schedulable Class is not set" );
                  throw new InvalidParameterException(
                     "Schedulable Class must be set"
                  );
               }
               if( mSchedulableArgumentList.length != mSchedulableArgumentTypeList.length ) {
                  log.debug( "Schedulable Class Arguments and Types do not match in length" );
                  throw new InvalidParameterException(
                     "Schedulable Class Arguments and Types do not match in length"
                  );
               }
            }
            if( mSchedulePeriod <= 0 ) {
               log.debug( "Schedule Period is less than 0 (ms)" );
               throw new InvalidParameterException(
                  "Schedule Period must be set and greater than 0 (ms)"
               );
            }
            if( !mUseMBean ) {
               // Create all the Objects for the Constructor to be called
               Object[] lArgumentList = new Object[ mSchedulableArgumentTypeList.length ];
               try {
                  for( int i = 0; i < mSchedulableArgumentTypeList.length; i++ ) {
                     Class lClass = mSchedulableArgumentTypeList[ i ];
                     if( lClass == Boolean.TYPE ) {
                        lArgumentList[ i ] = new Boolean( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Integer.TYPE ) {
                        lArgumentList[ i ] = new Integer( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Long.TYPE ) {
                        lArgumentList[ i ] = new Long( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Short.TYPE ) {
                        lArgumentList[ i ] = new Short( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Float.TYPE ) {
                        lArgumentList[ i ] = new Float( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Double.TYPE ) {
                        lArgumentList[ i ] = new Double( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Byte.TYPE ) {
                        lArgumentList[ i ] = new Byte( mSchedulableArgumentList[ i ] );
                     } else
                     if( lClass == Character.TYPE ) {
                        lArgumentList[ i ] = new Character( mSchedulableArgumentList[ i ].charAt( 0 ) );
                     } else {
                        Constructor lConstructor = lClass.getConstructor( new Class[] { String.class } );
                        lArgumentList[ i ] = lConstructor.newInstance( new Object[] { mSchedulableArgumentList[ i ] } );
                     }
                  }
               }
               catch( Exception e ) {
                  log.error( "Could not load or create constructor argument", e );
                  throw new InvalidParameterException( "Could not load or create a constructor argument" );
               }
               try {
                  // Check if constructor is found
                  Constructor lSchedulableConstructor = mSchedulableClass.getConstructor( mSchedulableArgumentTypeList );
                  // Create an instance of it
                  mSchedulable = (Schedulable) lSchedulableConstructor.newInstance( lArgumentList );
               }
               catch( Exception e ) {
                  log.error( "Could not find the constructor or create Schedulable instance", e );
                  throw new InvalidParameterException( "Could not find the constructor or create the Schedulable Instance" );
               }
            }

            mRemainingRepetitions = mInitialRepetitions;
            mActualSchedulePeriod = mSchedulePeriod;
            Date lStartDate = null;
            // Register the Schedule at the Timer
            // If start date is NOW then take the current date
            if( mStartDateIsNow ) {
               mStartDate = new Date( new Date().getTime() + 1000 );
               lStartDate = mStartDate;
            } else {
               // Check if initial start date is in the past
               if( mStartDate.getTime() < new Date().getTime() ) {
                  // If then first check if a repetition is in the future
                  long lNow = new Date().getTime() + 100;
                  long lSkipRepeats = ( ( lNow - mStartDate.getTime() ) / mActualSchedulePeriod ) + 1;
                  log.debug( "Old start date: " + mStartDate + ", now: " + new Date( lNow ) + ", Skip repeats: " + lSkipRepeats );
                  if( mRemainingRepetitions > 0 ) {
                     // If not infinit loop
                     if( lSkipRepeats >= mRemainingRepetitions ) {
                        // No repetition left -> exit
                        log.info( "No repetitions left because start date is in the past and could " +
                           "not be reached by Initial Repetitions * Schedule Period" );
                        return;
                     } else {
                        // Reduce the missed hits
                        mRemainingRepetitions -= lSkipRepeats;
                     }
                  }
                  lStartDate = new Date( mStartDate.getTime() + ( lSkipRepeats * mActualSchedulePeriod ) );
               } else {
                  lStartDate = mStartDate;
               }
            }
            log.debug( "Schedule initial call to: " + lStartDate + ", remaining repetitions: " + mRemainingRepetitions );
            // Add an initial call
            mActualSchedule = ( (Integer) getServer().invoke(
               mTimer,
               "addNotification",
               new Object[] {
                  "Schedule",
                  "Scheduler Notification",
                  null,       // User Object
                  lStartDate,
                  new Long( mActualSchedulePeriod ),
                  mRemainingRepetitions < 0 ?
                     new Long( 0 ) :
                     new Long( mRemainingRepetitions )
               },
               new String[] {
                  String.class.getName(),
                  String.class.getName(),
                  Object.class.getName(),
                  Date.class.getName(),
                  Long.TYPE.getName(),
                  Long.TYPE.getName()
               }
            ) ).intValue();
            if( mUseMBean ) {
               // Register the notification listener at the MBeanServer
               getServer().addNotificationListener(
                  mTimer,
                  new MBeanListener( mSchedulableMBean ),
                  new Scheduler.NotificationFilter( new Integer( mActualSchedule ) ),
                  // No object handback necessary
                  null
               );
            } else {
               // Register the notification listener at the MBeanServer
               getServer().addNotificationListener(
                  mTimer,
                  new Listener( mSchedulable ),
                  new Scheduler.NotificationFilter( new Integer( mActualSchedule ) ),
                  // No object handback necessary
                  null
               );
            }
            mScheduleIsStarted = true;
            mIsRestartPending = false;
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
         if( mActualSchedule < 0 ) {
            mScheduleIsStarted = false;
            mWaitForNextCallToStop = false;
            return;
         }
         if( pDoItNow ) {
            // Remove notification listener now
            mWaitForNextCallToStop = false;
            log.debug( "stopSchedule(), schedule id: " + mActualSchedule );
            getServer().invoke(
               mTimer,
               "removeNotification",
               new Object[] {
                  new Integer( mActualSchedule )
               },
               new String[] {
                  "java.lang.Integer" ,
               }
            );
            log.debug( "stopSchedule(), removed schedule id: " + mActualSchedule );
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

   public void restartSchedule() {
      stopSchedule( true );
      startSchedule();
   }

   public String getSchedulableClass() {
      if( mSchedulableClass == null ) {
         return null;
      }
      return mSchedulableClass.getName();
   }

   public void setSchedulableClass( String pSchedulableClass )
      throws InvalidParameterException
   {
      if( pSchedulableClass == null || pSchedulableClass.equals( "" ) ) {
         throw new InvalidParameterException( "Schedulable Class cannot be empty or undefined" );
      }
      try {
         // Try to load the Schedulable Class
         mSchedulableClass = Thread.currentThread().getContextClassLoader().loadClass( pSchedulableClass );
         // Check if instance of Schedulable
         Class[] lInterfaces = mSchedulableClass.getInterfaces();
         boolean lFound = false;
         for( int i = 0; i < lInterfaces.length; i++ ) {
            if( lInterfaces[ i ] == Schedulable.class ) {
               lFound = true;
               break;
            }
         }
         if( !lFound ) {
            throw new InvalidParameterException(
               "Given class " + pSchedulableClass + " is not instance of Schedulable"
            );
         }
      }
      catch( ClassNotFoundException cnfe ) {
         throw new InvalidParameterException(
            "Given class " + pSchedulableClass + " is not valid or not found"
         );
      }
      mIsRestartPending = true;
      mUseMBean = false;
   }

   public String getSchedulableArguments() {
      return mSchedulableArguments;
   }

   public void setSchedulableArguments( String pArgumentList ) {
      if( pArgumentList == null || pArgumentList.equals( "" ) ) {
         mSchedulableArgumentList = new String[ 0 ];
      }
      else {
         StringTokenizer lTokenizer = new StringTokenizer( pArgumentList, "," );
         Vector lList = new Vector();
         while( lTokenizer.hasMoreTokens() ) {
            String lToken = lTokenizer.nextToken().trim();
            if( lToken.equals( "" ) ) {
               lList.add( "null" );
            }
            else {
               lList.add( lToken );
            }
         }
         mSchedulableArgumentList = (String[]) lList.toArray( new String[ 0 ] );
      }
      mSchedulableArguments = pArgumentList;
      mIsRestartPending = true;
   }

   public String getSchedulableArgumentTypes() {
      return mSchedulableArgumentTypes;
   }

   public void setSchedulableArgumentTypes( String pTypeList )
      throws InvalidParameterException
   {
      if( pTypeList == null || pTypeList.equals( "" ) ) {
         mSchedulableArgumentTypeList = new Class[ 0 ];
      }
      else {
         StringTokenizer lTokenizer = new StringTokenizer( pTypeList, "," );
         Vector lList = new Vector();
         while( lTokenizer.hasMoreTokens() ) {
            String lToken = lTokenizer.nextToken().trim();
            // Get the class
            Class lClass = null;
            if( lToken.equals( "short" ) ) {
              lClass = Short.TYPE;
            } else
            if( lToken.equals( "int" ) ) {
              lClass = Integer.TYPE;
            } else
            if( lToken.equals( "long" ) ) {
              lClass = Long.TYPE;
            } else
            if( lToken.equals( "byte" ) ) {
              lClass = Byte.TYPE;
            } else
            if( lToken.equals( "char" ) ) {
              lClass = Character.TYPE;
            } else
            if( lToken.equals( "float" ) ) {
              lClass = Float.TYPE;
            } else
            if( lToken.equals( "double" ) ) {
              lClass = Double.TYPE;
            } else
            if( lToken.equals( "boolean" ) ) {
              lClass = Boolean.TYPE;
            }
            if( lClass == null ) {
               try {
                  // Load class to check if available
                  lClass = Thread.currentThread().getContextClassLoader().loadClass( lToken );
               }
               catch( ClassNotFoundException cnfe ) {
                  throw new InvalidParameterException(
                     "The argument type: " + lToken + " is not a valid class or could not be found"
                  );
               }
            }
            lList.add( lClass );
         }
         mSchedulableArgumentTypeList = (Class[]) lList.toArray( new Class[ 0 ] );
      }
      mSchedulableArgumentTypes = pTypeList;
      mIsRestartPending = true;
   }
   
   public String getSchedulableMBean() {
      return mSchedulableMBean == null ?
         null :
         mSchedulableMBean.toString();
   }
   
   public void setSchedulableMBean( String pSchedulableMBean )
      throws InvalidParameterException
   {
      if( pSchedulableMBean == null ) {
         throw new InvalidParameterException( "Schedulable MBean must be specified" );
      }
      try {
         mSchedulableMBean = new ObjectName( pSchedulableMBean );
         mUseMBean = true;
      }
      catch( MalformedObjectNameException mone ) {
         log.error( "Schedulable MBean Object Name is malformed", mone );
         throw new InvalidParameterException( "Schedulable MBean is not correctly formatted" );
      }
   }
   
   public String getSchedulableMBeanMethod() {
      return mSchedulableMBeanMethod;
   }
   
   public void setSchedulableMBeanMethod( String pSchedulableMBeanMethod )
      throws InvalidParameterException
   {
      if( pSchedulableMBeanMethod == null ) {
         mSchedulableMBeanMethod = null;
         return;
      }
      int lIndex = pSchedulableMBeanMethod.indexOf( '(' );
      String lMethodName = "";
      if( lIndex < 0 ) {
         lMethodName = pSchedulableMBeanMethod.trim();
         mSchedulableMBeanArguments = new int[ 0 ];
         mSchedulableMBeanArgumentTypes = new String[ 0 ];
      } else
      if( lIndex > 0 ) {
         lMethodName = pSchedulableMBeanMethod.substring( 0, lIndex ).trim();
      }
      if( lMethodName.equals( "" ) ) {
         lMethodName = "perform";
      }
      if( lIndex >= 0 ) {
         int lIndex2 = pSchedulableMBeanMethod.indexOf( ')' );
         if( lIndex2 < lIndex ) {
            throw new InvalidParameterException( "Schedulable MBean Method: closing bracket must be after opening bracket" );
         }
         if( lIndex2 < pSchedulableMBeanMethod.length() - 1 ) {
            String lRest = pSchedulableMBeanMethod.substring( lIndex2 + 1 ).trim();
            if( lRest.length() > 0 ) {
               throw new InvalidParameterException( "Schedulable MBean Method: nothing should be after closing bracket" );
            }
         }
         String lArguments = pSchedulableMBeanMethod.substring( lIndex + 1, lIndex2 ).trim();
         if( lArguments.equals( "" ) ) {
            mSchedulableMBeanArguments = new int[ 0 ];
            mSchedulableMBeanArgumentTypes = new String[ 0 ];
         } else {
            StringTokenizer lTokenizer = new StringTokenizer( lArguments, "," );
            mSchedulableMBeanArguments = new int[ lTokenizer.countTokens() ];
            mSchedulableMBeanArgumentTypes = new String[ lTokenizer.countTokens() ];
            for( int i = 0; lTokenizer.hasMoreTokens(); i++ ) {
               String lToken = lTokenizer.nextToken().trim();
               if( lToken.equals( "NOTIFICATION" ) ) {
                  mSchedulableMBeanArguments[ i ] = NOTIFICATION;
                  mSchedulableMBeanArgumentTypes[ i ] = Notification.class.getName();
               } else
               if( lToken.equals( "DATE" ) ) {
                  mSchedulableMBeanArguments[ i ] = DATE;
                  mSchedulableMBeanArgumentTypes[ i ] = Date.class.getName();
               } else
               if( lToken.equals( "REPETITIONS" ) ) {
                  mSchedulableMBeanArguments[ i ] = REPETITIONS;
                  mSchedulableMBeanArgumentTypes[ i ] = Long.TYPE.getName();
               } else
               if( lToken.equals( "SCHEDULER_NAME" ) ) {
                  mSchedulableMBeanArguments[ i ] = SCHEDULER_NAME;
                  mSchedulableMBeanArgumentTypes[ i ] = ObjectName.class.getName();
               } else {
                  mSchedulableMBeanArguments[ i ] = NULL;
                  //AS ToDo: maybe later to check if this class exists !
                  mSchedulableMBeanArgumentTypes[ i ] = lToken;
               }
            }
         }
      }
      mSchedulableMBeanMethodName = lMethodName;
      mSchedulableMBeanMethod = pSchedulableMBeanMethod;
   }
   
   public boolean isUsingMBean() {
      return mUseMBean;
   }
   
   public long getSchedulePeriod() {
      return mSchedulePeriod;
   }

   public void setSchedulePeriod( long pPeriod ) {
      if( pPeriod <= 0 ) {
         throw new InvalidParameterException( "Schedulable Period may be not less or equals than 0" );
      }
      mSchedulePeriod = pPeriod;
      mIsRestartPending = true;
   }
   
   public String getInitialStartDate() {
      return mStartDateString;
   }
   
   public void setInitialStartDate( String pStartDate ) {
      mStartDateString = pStartDate == null ? "" : pStartDate.trim();
      if( mStartDateString.equals( "" ) ) {
         mStartDate = new Date( 0 );
      } else
      if( mStartDateString.equals( "NOW" ) ) {
         mStartDate = new Date( new Date().getTime() + 1000 );
         mStartDateIsNow = true;
      } else {
         try {
            long lDate = new Long( pStartDate ).longValue();
            mStartDate = new Date( lDate );
            mStartDateIsNow = false;
         }
         catch( Exception e ) {
            try {
               if( mDateFormatter == null ) {
                  mDateFormatter = new SimpleDateFormat();
               }
               mStartDate = mDateFormatter.parse( mStartDateString );
               mStartDateIsNow = false;
            }
            catch( Exception e2 ) {
               log.error( "Could not parse given date string: " + mStartDateString, e2 );
               throw new InvalidParameterException( "Schedulable Date is not of correct format" );
            }
         }
      }
      log.debug( "Initial Start Date is set to: " + mStartDate );
   }

   public long getInitialRepetitions() {
      return mInitialRepetitions;
   }

   public void setInitialRepetitions( long pNumberOfCalls ) {
      if( pNumberOfCalls <= 0 ) {
         pNumberOfCalls = -1;
      }
      mInitialRepetitions = pNumberOfCalls;
      mIsRestartPending = true;
   }

   public long getRemainingRepetitions() {
      return mRemainingRepetitions;
   }

   public boolean isStarted() {
      return mScheduleIsStarted;
   }

   public boolean isRestartPending() {
      return mIsRestartPending;
   }
   
   public boolean isStartAtStartup() {
      return mStartOnStart;
   }
   
   public void setStartAtStartup( boolean pStartAtStartup ) {
      mStartOnStart = pStartAtStartup;
   }
   
   public boolean isActive() {
      return isStarted() && mRemainingRepetitions != 0;
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
      return pName;
   }

   public String getName() {
      return "JBoss Scheduler MBean";
   }

   // -------------------------------------------------------------------------
   // ServiceMBean - Methods
   // -------------------------------------------------------------------------

   protected void startService()
        throws Exception
   {
      try {
         // Create Timer MBean if need be
         
         mTimer = new ObjectName( "DefaultDomain", "service", "Timer");
         if( !getServer().isRegistered( mTimer ) ) {
            getServer().createMBean( "javax.management.timer.Timer", mTimer );
            // Now start the Timer
            getServer().invoke(
               mTimer,
               "start",
               new Object[] {},
               new String[] {}
            );
         }
      }
      catch( Exception e ) {
         log.error( "Could not start Timer Service", e );
      }

      if( mStartOnStart ) {
         log.debug( "Start Scheduler on start up time" );
         startSchedule();
      }
   }

   protected void stopService() {
      // Stop the schedule right now !!
      stopSchedule( true );
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
         log.debug( "Listener.handleNotification(), notification: " + pNotification );
         try {
            // If schedule is started invoke the schedule method on the Schedulable instance
            log.debug( "Scheduler is started: " + isStarted() );
            Date lTimeStamp = new Date( pNotification.getTimeStamp() );
            if( isStarted() ) {
               if( getRemainingRepetitions() > 0 || getRemainingRepetitions() < 0 ) {
                  if( mRemainingRepetitions > 0 ) {
                     mRemainingRepetitions--;
                  }
                  mDelegate.perform(
                     lTimeStamp,
                     getRemainingRepetitions()
                  );
                  log.debug( "Remaining Repititions: " + getRemainingRepetitions() +
                     ", wait for next call to stop: " + mWaitForNextCallToStop );
                  if( getRemainingRepetitions() == 0 || mWaitForNextCallToStop ) {
                     stopSchedule( true );
                  }
               }
            }
            else {
               // Schedule is stopped therefore remove the Schedule
               getServer().invoke(
                  mTimer,
                  "removeNotification",
                  new Object[] {
                     new Integer( mActualSchedule )
                  },
                  new String[] {
                     "java.lang.Integer",
                  }
               );
               mActualSchedule = -1;
            }
         }
         catch( Exception e ) {
            log.error( "Handling a Scheduler call failed", e );
         }
      }
   }

   public class MBeanListener
      implements NotificationListener
   {

      private ObjectName mDelegate;

      public MBeanListener( ObjectName pDelegate ) {
         mDelegate = pDelegate;
      }

      public void handleNotification(
         Notification pNotification,
         Object pHandback
      ) {
         log.debug( "MBeanListener.handleNotification(), notification: " + pNotification );
         try {
            // If schedule is started invoke the schedule method on the Schedulable instance
            log.debug( "Scheduler is started: " + isStarted() );
            Date lTimeStamp = new Date( pNotification.getTimeStamp() );
            if( isStarted() ) {
               if( getRemainingRepetitions() > 0 || getRemainingRepetitions() < 0 ) {
                  if( mRemainingRepetitions > 0 ) {
                     mRemainingRepetitions--;
                  }
                  Object[] lArguments = new Object[ mSchedulableMBeanArguments.length ];
                  for( int i = 0; i < lArguments.length; i++ ) {
                     switch( mSchedulableMBeanArguments[ i ] ) {
                        case NOTIFICATION:
                           lArguments[ i ] = pNotification;
                           break;
                        case DATE:
                           lArguments[ i ] = lTimeStamp;
                           break;
                        case REPETITIONS:
                           lArguments[ i ] = new Long( mRemainingRepetitions );
                           break;
                        case SCHEDULER_NAME:
                           lArguments[ i ] = getServiceName();
                           break;
                        default:
                           lArguments[ i ] = null;
                     }
                  }
                  log.debug( "MBean Arguments are: " + java.util.Arrays.asList( lArguments ) );
                  log.debug( "MBean Arguments Types are: " + java.util.Arrays.asList( mSchedulableMBeanArgumentTypes ) );
                  try {
                     getServer().invoke(
                        mDelegate,
                        mSchedulableMBeanMethodName,
                        lArguments,
                        mSchedulableMBeanArgumentTypes
                     );
                  }
                  catch( javax.management.JMRuntimeException jmre ) {
                     log.error( "Invoke of the Schedulable MBean failed", jmre );
                  }
                  catch( javax.management.JMException jme ) {
                     log.error( "Invoke of the Schedulable MBean failed", jme );
                  }
                  log.debug( "Remaining Repititions: " + getRemainingRepetitions() +
                     ", wait for next call to stop: " + mWaitForNextCallToStop );
                  if( getRemainingRepetitions() == 0 || mWaitForNextCallToStop ) {
                     stopSchedule( true );
                  }
               }
            }
            else {
               // Schedule is stopped therefore remove the Schedule
               getServer().invoke(
                  mTimer,
                  "removeNotification",
                  new Object[] {
                     new Integer( mActualSchedule )
                  },
                  new String[] {
                     "java.lang.Integer",
                  }
               );
               mActualSchedule = -1;
            }
         }
         catch( Exception e ) {
            log.error( "Handling a Scheduler call failed", e );
         }
      }
   }

   /**
    * Filter to ensure that each Scheduler only gets notified when it is supposed to.
    */
   private static class NotificationFilter implements javax.management.NotificationFilter {
   
      private Integer mId;
   
      /**
       * Create a Filter.
       * @param id the Scheduler id
       */
      public NotificationFilter( Integer pId ){
         mId = pId;
      }
   
      /**
       * Determine if the notification should be sent to this Scheduler
       */
      public boolean isNotificationEnabled( Notification pNotification ) {
         if( pNotification instanceof TimerNotification ) {
            TimerNotification lTimerNotification = (TimerNotification) pNotification;
            return lTimerNotification.getNotificationID().equals( mId );
         }
         return false;
      }
   }

   /**
    * A test class for a Schedulable Class
    **/
   public static class SchedulableExample
      implements Schedulable
   {

      /** Class logger. */
      private static final Logger log = Logger.getLogger( Scheduler.SchedulableExample.class );
      
     private String mName;
      private int mValue;

      public SchedulableExample(
         String pName,
         int pValue
      ) {
         mName = pName;
         mValue = pValue;
      }

      /**
       * Just log the call
       **/
      public void perform(
         Date pTimeOfCall,
         long pRemainingRepetitions
      ) {
         log.info( "Schedulable Examples is called at: " + pTimeOfCall +
            ", remaining repetitions: " + pRemainingRepetitions +
            ", test, name: " + mName + ", value: " + mValue );
      }
   }
}
