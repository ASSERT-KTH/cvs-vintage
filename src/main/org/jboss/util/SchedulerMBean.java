/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.util.Date;

import org.jboss.util.ServiceMBean;

/**
 * This interface defines the manageable interface for a Scheduler Service
 * allowing the client to create a Schedulable instance which is then run
 * by this service at given times.
 *
 * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
 **/
public interface SchedulerMBean
   extends ServiceMBean
{
   // -------------------------------------------------------------------------
   // Constants
   // -------------------------------------------------------------------------  

   public static final String OBJECT_NAME = "DefaultDomain:service=Scheduler";

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Starts the schedule if the schedule is stopped otherwise nothing will happen.
    * The scheduled is immediately set to started even the first call is in the
    * future.
    *
    * @param pSchedulableClass Full qaulified Class Name of the class implementing
    *                          the {@link org.jboss.util.Schedulable} interface.
    * @param pInitArguments Arrays of arguments for the constructor. It must have as
    *                       many elements as the Initial Types array. If null then
    *                       an empty array is assumed.
    * @param pInitTypes Arrays of data types to look up the constructor. It must have
    *                   as many elements as the Init Arguments array. If null then an
    *                   empty array is assumed.
    * @param pInitialStartDate Date when the schedule will be started initially. If
    *                          null of older than now it will started NOW.
    * @param pSchedulePeriod Time in Milliseconds between two scheduled calls after
    *                        the initial call.
    * @param pNumberOfRepetitions Number of repetitions this schedule is suppossed to
    *                             call. If less or equal than 0 it will repeat
    *                             unlimited.
    **/
   public void startSchedule(
      String pSchedulableClass,
      Object[] pInitArguments,
      String[] pInitTypes,
      Date pInitialStartDate,
      long pSchedulePeriod,
      int pNumberOfRepetitions
   );
   
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
    * @return Schedule Period between two scheduled calls in Milliseconds. It will always
    *         be bigger than 0 except it returns -1 then the schedule is stopped.
    **/
   public long getSchedulePeriod();
   
   /**
    * @return Number of remaining repetitions. If -1 then there is no limit.
    **/
   public int getRemainingRepetitions();
   
   /**
    * @return True if the schedule is up and running. If you want to start the schedule
    *         with another values by using {@ #startSchedule} you have to stop the schedule
    *         first with {@ #stopSchedule} and wait until this method returns false.
    **/
   public boolean isStarted();

}
