/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJBContext;
import javax.ejb.TimerService;

import org.jboss.ejb.Container;
import org.jboss.system.ServiceMBeanSupport;


/**
 * This class is the bridge between an (external) Timer Source
 * used to manage Timers and to provide the Container with the
 * timed event to invoke the Bean's ejbTimeout() method
 *
 * @jmx:mbean name="jboss:service=EJBTimerSource"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public abstract class AbstractTimerSource
   extends ServiceMBeanSupport
   implements AbstractTimerSourceMBean
{   
   private HashMap mContainerTimerServices = new HashMap();
   
   /**
    * @return True if the Timer Source is able to participate in a Tx
    **/
   public abstract boolean isTransactional();
   
   /**
    * @return True if the Timer Source is persistent and CAN recover
    *         after a crash or server restart
    **/
   public abstract boolean isPersistent();
   
   /**
    * Creates a new timer to be invoked once, a definte or infinite number
    * of times.
    *
    * @param ??
    *
    * @return Id of the Timer
    **/
   public abstract String createTimer( String pContainerId, Date pStartDate, long pInterval );
   
   /**
    * Removes the given Timer if still active or not
    *
    * @param pId Id returned from {@link #createTimer createTimer()}
    **/
   public abstract void removeTimer( String pId );
   
   /**
    * Recover the all the timers for the given Container.
    *
    * When the server goes down and comes back no timer are recovered
    * initially. Every container hosting a EJB implementing the Timed-
    * Object interface will call this method to recover its timers.
    * The implementation can choose to recover the timers immediately
    * but has to make sure that no timed events are sent to the Container
    * until it this method is called.
    *
    * @param pContainerId Id of the container to be recovered
    **/
    public abstract void recover( long pContainerId );
    
    /**
     * Create a Timer Service for a Container
     *
     * @param pContainerId Identification of the Container
     *
     * @jmx:managed-operation
     **/
    public TimerService createTimerService( String pContainerId, Container pContainer, EJBContext pContext )
    {
        ContainerTimerService lService = null;
        if( mContainerTimerServices.containsKey( pContainerId ) ) {
            return getTimerService( pContainerId );
        } else {
            lService = new ContainerTimerService( this, pContainerId, pContainer, pContext );
            mContainerTimerServices.put( pContainerId, lService );
        }
        return lService;
    }
    
    /**
     * Removes the Timer Service for the given Container
     *
     * @param pContainerId Id of the Container to be removed
     **/
    public void removeTimerService( String pContainerId ) {
        mContainerTimerServices.remove( pContainerId );
    }
    
    /**
     * Delivers the Timer Service for a given Container Id
     *
     * @param pContainerId Id of the Container to look for its Timer Service
     *
     * @return Timer Service if found otherwise null
     **/
    protected TimerService getTimerService( String pContainerId ) {
        return (TimerService) mContainerTimerServices.get( pContainerId );
    }
}