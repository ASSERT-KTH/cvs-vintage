/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.security.InvalidParameterException;
import java.util.Date;

import org.jboss.system.ServiceMBean;

/**
 * This interface defines the manageable interface for a Scheduler Service
 * allowing the client to create a Schedulable instance which is then run
 * by this service at given times or to use a MBean which is called by this
 * service at given times.
 * <br>
 * <b>Attention: </b> You have two ways to specify the Schedulable. Either
 * you specify a Schedulable Class which is created and used by the Scheduler
 * or you specify a JMX MBean which the specified method is called. Note that
 * the last method of {@link #setSchedulableClass} or {@link #setSchedulableMBean}
 * defines which one is used. Therefore you should <b>never mixed these two</b>.
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public interface SchedulerMBean
   extends ServiceMBean
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static final String OBJECT_NAME = "jboss:service=Scheduler";

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Starts the schedule if the schedule is stopped otherwise nothing will happen.
    * The Schedule is immediately set to started even the first call is in the
    * future.
    *
    * @throws InvalidParameterException If any of the necessary values are not set
    *                                   or invalid (especially for the Schedulable
    *                                   class attributes).
    **/
   public void startSchedule();
   
   /**
    * Stops the schedule because it is either not used anymore or to restart it with
    * new values.
    *
    * @param pDoItNow If true the schedule will be stopped without waiting for the next
    *                 scheduled call otherwise the next call will be performed before
    *                 the schedule is stopped.
    **/
   public void stopSchedule(
      boolean pDoItNow
   );
   
   /**
    * Stops the server right now and starts it right now.
    **/
   public void restartSchedule();
   
   /**
    * @return Full qualified Class name of the schedulable class called by the schedule or
    *         null if not set.
    **/
   public String getSchedulableClass();
   
   /**
    * Sets the fully qualified Class name of the Schedulable Class being called by the
    * Scheduler. Must be set before the Schedule is started. Please also set the
    * {@link #setSchedulableArguments} and {@link #setSchedulableArgumentTypes}.
    *
    * @param pSchedulableClass Fully Qualified Schedulable Class.
    *
    * @throws InvalidParameterException If the given value is not a valid class or cannot
    *                                   be loaded by the Scheduler or is not of instance
    *                                   Schedulable.
    **/
   public void setSchedulableClass( String pSchedulableClass )
      throws InvalidParameterException;
   
   /**
    * @return Comma seperated list of Constructor Arguments used to instantiate the
    *         Schedulable class instance. Right now only basic data types, String and
    *         Classes with a Constructor with a String as only argument are supported.
    **/
   public String getSchedulableArguments();
   
   /**
    * Sets the comma seperated list of arguments for the Schedulable class. Note that
    * this list must have as many elements as the Schedulable Argument Type list otherwise
    * the start of the Scheduler will fail. Right now only basic data types, String and
    * Classes with a Constructor with a String as only argument are supported.
    *
    * @param pArgumentList List of arguments used to create the Schedulable intance. If
    *                      the list is null or empty then the no-args constructor is used.
    **/
   public void setSchedulableArguments( String pArgumentList );
   
   /**
    * @return A comma seperated list of Argument Types which should match the list of
    *         arguments.
    **/
   public String getSchedulableArgumentTypes();
   
   /**
    * Sets the comma seperated list of argument types for the Schedulable class. This will
    * be used to find the right constructor and to created the right instances to call the
    * constructor with. This list must have as many elements as the Schedulable Arguments
    * list otherwise the start of the Scheduler will fail. Right now only basic data types,
    * String and Classes with a Constructor with a String as only argument are supported.
    *
    * @param pTypeList List of arguments used to create the Schedulable intance. If
    *                  the list is null or empty then the no-args constructor is used.
    *
    * @throws InvalidParameterException If the given list contains a unknow datat type.
    **/
   public void setSchedulableArgumentTypes( String pTypeList )
      throws InvalidParameterException;
   
   /**
    * @return Object Name if a Schedulalbe MBean is set
    **/
   public String getSchedulableMBean();
   
   /**
    * Sets the fully qualified JMX MBean name of the Schedulable MBean to be called.
    * <b>Attention: </b>if set the all values set by {@link #setSchedulableClass},
    * {@link #setSchedulableArguments} and {@link #setSchedulableArgumentTypes} are
    * cleared and not used anymore. Therefore only use either Schedulable Class or
    * Schedulable MBean. If {@link #setSchedulableMBeanMethod} is not set then the
    * schedule method as in the {@link Schedulable#perform} will be called with the
    * same arguments. Also note that the Object Name will not be checked if the
    * MBean is available. If the MBean is not available it will not be called but
    * the remaining repetitions will be decreased.
    *
    * @param pSchedulableMBean JMX MBean Object Name which should be called.
    *
    * @throws InvalidParameterException If the given value is an valid Object Name.
    **/
   public void setSchedulableMBean( String pSchedulableMBean )
      throws InvalidParameterException;
   
   /**
    * @return Schedulable MBean Method description if set
    **/
   public String getSchedulableMBeanMethod();
   
   /**
    * Sets the method name to be called on the Schedulable MBean. It can optionally be
    * followed by an opening bracket, list of attributes (see below) and a closing bracket.
    * The list of attributes can contain:
    * <ul>
    * <li>NOTIFICATION which will be replaced by the timers notification instance
    *     (javax.management.Notification)</li>
    * <li>DATE which will be replaced by the date of the notification call
    *     (java.util.Date)</li>
    * <li>REPETITIONS which will be replaced by the number of remaining repetitions
    *     (long)</li>
    * <li>SCHEDULER_NAME which will be replaced by the Object Name of the Scheduler
    *     (javax.management.ObjectName)</li>
    * <li>any full qualified Class name which the Scheduler will be set a "null" value
    *     for it</li>
    * </ul>
    * <br>
    * An example could be: "doSomething( NOTIFICATION, REPETITIONS, java.lang.String )"
    * where the Scheduler will pass the timer's notification instance, the remaining
    * repetitions as int and a null to the MBean's doSomething() method which must
    * have the following signature: doSomething( javax.management.Notification, long,
    * java.lang.String ).
    *
    * @param pSchedulableMBeanMethod Name of the method to be called optional followed
    *                                by method arguments (see above).
    *
    * @throws InvalidParameterException If the given value is not of the right
    *                                   format
    **/
   public void setSchedulableMBeanMethod( String pSchedulableMBeanMethod )
      throws InvalidParameterException;
   
   /**
   * @return True if the Scheduler uses a Schedulable MBean, false if it uses a
   *         Schedulable class
   **/
   public boolean isUsingMBean();
   
   /**
    * @return Schedule Period between two scheduled calls in Milliseconds. It will always
    *         be bigger than 0 except it returns -1 then the schedule is stopped.
    **/
   public long getSchedulePeriod();
   
   /**
    * Sets the Schedule Period between two scheduled call.
    *
    * @param pPeriod Time between to scheduled calls (after the initial call) in Milliseconds.
    *                This value must be bigger than 0.
    *
    * @throws InvalidParameterException If the given value is less or equal than 0
    **/
   public void setSchedulePeriod( long pPeriod );
   
   /**
   * @return Date (and time) of the first scheduled. For value see {@link #setInitialStartDate
   *         setInitialStartDate()} method.
   **/
   public String getInitialStartDate();
   
   /**
   * Sets the first scheduled call. If the date is in the past the scheduler tries to find the
   * next available start date.
   *
   * @param pStartDate Date when the initial call is scheduled. It can be either:
   *                   <ul>
   *                      <li>
   *                         NOW: date will be the current date (new Date()) plus 1 seconds
   *                      </li><li>
   *                         Date as String able to be parsed by SimpleDateFormat with default format
   *                      </li><li>
   *                         Milliseconds since 1/1/1970
   *                      </li>
   *                   </ul>
   *                   If the date is in the past the Scheduler
   *                   will search a start date in the future with respect to the initial repe-
   *                   titions and the period between calls. This means that when you restart
   *                   the MBean (restarting JBoss etc.) it will start at the next scheduled
   *                   time. When no start date is available in the future the Scheduler will
   *                   not start.<br>
   *                   Example: if you start your Schedulable everyday at Noon and you restart
   *                   your JBoss server then it will start at the next Noon (the same if started
   *                   before Noon or the next day if start after Noon).
   **/
   public void setInitialStartDate( String pStartDate );
   
   /**
    * @return Number of scheduled calls initially. If -1 then there is not limit.
    **/
   public long getInitialRepetitions();
   
   /**
    * Sets the initial number of scheduled calls.
    *
    * @param pNumberOfCalls Initial Number of scheduled calls. If -1 then the number
    *                       is unlimted.
    *
    * @throws InvalidParameterException If the given value is less or equal than 0
    **/
   public void setInitialRepetitions( long pNumberOfCalls );

   /**
    * @return Number of remaining repetitions. If -1 then there is no limit.
    **/
   public long getRemainingRepetitions();
   
   /**
    * @return True if the schedule is up and running. If you want to start the schedule
    *         with another values by using {@ #startSchedule} you have to stop the schedule
    *         first with {@ #stopSchedule} and wait until this method returns false.
    **/
   public boolean isStarted();
   
   /**
    * @return True if any attributes are changed but the Schedule is not restarted yet.
    **/
   public boolean isRestartPending();
   
   /**
   * @return True if the Schedule when the Scheduler is started
   **/
   public boolean isStartAtStartup();
   
   /**
   * Set the scheduler to start when MBean started or not. Note that this method only
   * affects when the {@link #startService startService()} gets called (normally at
   * startup time.
   *
   * @param pStartAtStartup True if Schedule has to be started at startup time
   **/
   public void setStartAtStartup( boolean pStartAtStartup );

}
