/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.pools;

import java.io.*;
import java.util.*;

/**
 * A generic object pool.  You must provide a PoolObjectFactory (or the class
 * of a Java Bean) so the pool knows what kind of objects to create.  It has
 * many configurable parameters, such as the minimum and maximum size of the
 * pool, whether to allow the pool to shrink, etc.  If the pooled objects
 * implement PooledObject, they will automatically be returned to the pool at
 * the appropriate times.
 * <P>In general, the appropriate way to use a pool is:</P>
 * <OL>
 *   <LI>Create it</LI>
 *   <LI>Configure it (set factory, name, parameters, etc.)</LI>
 *   <LI>Initialize it (once done, further configuration is not allowed)</LI>
 *   <LI>Use it</LI>
 *   <LI>Shut it down</LI>
 * </OL>
 * @see org.jboss.minerva.pools.PooledObject
 * @version $Revision: 1.2 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class ObjectPool implements PoolEventListener {
    private final static String INITIALIZED = "Pool already initialized!";
    private final static PoolGCThread collector = new PoolGCThread();
    static {
        collector.start();
    }

    private PoolObjectFactory factory;
    private String poolName;

    private HashMap objects = null;
    private int minSize = 0;
    private int maxSize = 0;
    private boolean shrinks = false;
    private boolean runGC = false;
    private float shrinkPercent = 0.33f;             // reclaim 1/3 of stale objects
    private long shrinkMinIdleMillis = 600000l; // must be unsued for 10 minutes
    private long gcMinIdleMillis = 1200000l;    // must be idle for 20 minutes
    private long gcIntervalMillis = 120000l;    // shrink & gc every 2 minutes
    private long lastGC = System.currentTimeMillis();
    private boolean blocking = false;
    private boolean trackLastUsed = false;
    private PrintWriter logWriter = null;

    /**
     * Creates a new pool.  It cannot be used until you specify a name and
     * object factory or bean class, and initialize it.
     * @see #setName
     * @see #setObjectFactory
     * @see #initialize
     */
    public ObjectPool() {}

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param factory The object factory that will create the objects to go in
     *    the pool.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(PoolObjectFactory factory, String poolName) {
        setObjectFactory(factory);
        setName(poolName);
    }

    /**
     * Creates a new pool with the specified parameters.  It cannot be used
     * until you initialize it.
     * @param javeBeanClass The Class of a Java Bean.  New instances for the
     *    pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.  Use a PoolObjectFactory if you want more control over
     *    the instances.
     * @param poolName The name of the pool.  This does not have to be unique
     *    across all pools, but it is strongly recommended (and it may be a
     *    requirement for certain uses of the pool).
     * @see #initialize
     */
    public ObjectPool(Class javaBeanClass, String poolName) {
        setObjectFactory(javaBeanClass);
        setName(poolName);
    }

    /**
     * Sets the object factory for the pool.  The object factory controls the
     * instances created for the pool, and can initialize instances given out
     * by the pool and cleanup instances returned to the pool.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(PoolObjectFactory factory) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        this.factory = factory;
    }

    /**
     * Sets the object factory as a new factory for Java Beans.  New instances
     *    for the pool will be created with the no-argument constructor, and no
     *    particular initialization or cleanup will be performed on the
     *    instances.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the object factory after the pool has been
     *    initialized.
     */
    public void setObjectFactory(Class javaBeanClass) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        factory = new BeanFactory(javaBeanClass);
    }

    /**
     * Sets the name of the pool.  This is not required to be unique across all
     * pools, but is strongly recommended.  Certain uses of the pool (such as
     * a JNDI object factory) may require it.  This must be set exactly once
     * for each pool (it may be set in the constructor).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the name of the pool more than once.
     */
    public void setName(String name) {
        if(poolName != null)
            throw new IllegalStateException("Cannot change pool name once set!");
        poolName = name;
    }

    /**
     * Gets the name of the pool.
     */
    public String getName() {return poolName;}

    /**
     * Gets a log writer used to record pool events.
     */
    public PrintWriter getLogWriter() throws java.sql.SQLException {
        return logWriter;
    }

    /**
     * Sets a log writer used to record pool events.
     */
    public void setLogWriter(PrintWriter writer) throws java.sql.SQLException {
        logWriter = writer;
    }

    /**
     * Sets the minimum size of the pool.  The pool always starts with zero
     * instances, but once running, it will never shrink below this size.  This
     * parameter has no effect if shrinking is not enabled.  The default is
     * zero.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the minimum size after the pool has been
     *    initialized.
     */
    public void setMinSize(int size) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        minSize = size;
    }

    /**
     * Gets the minimum size of the pool.
     * @see #setMinSize
     */
    public int getMinSize() {return minSize;}

    /**
     * Sets the maximum size of the pool.  Once the pool has grown to hold this
     * number of instances, it will not add any more instances.  If one of the
     * pooled instances is available when a request comes in, it will be
     * returned.  If none of the pooled instances are available, the pool will
     * either block until an instance is available, or return null.  The default
     * is no maximum size.
     * @see #setBlocking
     * @param size The maximum size of the pool, or 0 if the pool should grow
     *    indefinitely (not recommended).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the maximum size after the pool has been
     *    initialized.
     */
    public void setMaxSize(int size) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        maxSize = size;
    }

    /**
     * Gets the maximum size of the pool.
     * @see #setMaxSize
     */
    public int getMaxSize() {return maxSize;}

    /**
     * Sets whether the pool should release instances that have not been used
     * recently.  This is intended to reclaim resources (memory, database
     * connections, file handles, etc) during periods of inactivity.  This runs
     * as often as garbage collection (even if garbage collection is disabled,
     * this uses the same timing parameter), but the required period of
     * inactivity is different.  Also, you may choose to release only a fraction
     * of the eligible objects to slow the shrinking further.  So the algorithm
     * is:
     * <UL>
     *   <LI>Run every <I>n</I> milliseconds.</LI>
     *   <LI>Count the number of connections that are not in use, and whose last
     *     used time is greater than the period of inactivity.</LI>
     *   <LI>Multiply this by the fraction of connections to release, but if the
     *     last total was greater than one, this will always be at least one.</LI>
     *   <LI>Attempt to release this many connections, of the ones identified
     *     above.  Do not release any connection that has been marked as in use
     *     while this runs, and do not allow the pool to shrink below the
     *     specified minimum size.</LI>
     * </UL>
     * <P>The default is disabled.</P>
     * @see #setGCInterval
     * @see #setShrinkMinIdleTime
     * @see #setShrinkPercent
     * @see #setMinSize
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the shrinking state after the pool has been
     *    initialized.
     */
    public void setShrinkingEnabled(boolean allowShrinking) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        shrinks = allowShrinking;
    }

    /**
     * Gets whether shrinking of the pool is enabled.
     * @see #setShrinkingEnabled
     */
    public boolean isShrinkingEnabled() {return shrinks;}

    /**
     * Sets whether garbage collection is enabled.  This is the process of
     * returning objects to the pool if they have been checked out of the pool
     * but have not been used in a long periond of time.  This is meant to
     * reclaim resources, generally caused by unexpected failures on the part
     * of the pool client (which forestalled returning an object to the pool).
     * This runs on the same schedule as shrinking (if enabled), but objects
     * that were just garbage collected will not be eligible for shrinking
     * immediately (after all, they presumably represented "active" clients).
     * Connections that are garbage collected will be returned immediately if
     * a client is blocking waiting for an object.  The deafult value is
     * disabled.
     * @see #setGCMinIdleTime
     * @see #setGCInterval
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection state after the pool
     *    has been initialized.
     */
    public void setGCEnabled(boolean enabled) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        runGC = enabled;
    }

    /**
     * Gets whether garbage collection is enabled.
     * @see #setGCEnabled
     */
    public boolean isGCEnabled() {return runGC;}

    /**
     * Sets the shrink percent as a fraction between 0 and 1.  This controls
     * how many of the available connection which have been idle for too long
     * will be released.  If set to 1, all eligible connections will be
     * released (subject to the minimum size), and if set to 0, only one will
     * be released each time (if any are eligible - see the algorithm in
     * setShrinkingEnabled).  The default value is 33%.
     * @see #setShrinkingEnabled
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the shrinking percent after the pool
     *    has been initialized.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the percent parameter is not between 0 and 1.
     */
    public void setShrinkPercent(float percent) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        if(percent < 0f || percent > 1f)
            throw new IllegalArgumentException("Percent must be between 0 and 1!");
        shrinkPercent = percent;
    }

    /**
     * Gets the shrink percent as a fraction between 0 and 1.
     * @see #setShrinkPercent
     */
    public float getShrinkPercent() {return shrinkPercent;}

    /**
     * Sets the minimum idle time to make an object eligible for shrinking.  If
     * the object is not in use and has not been used for this amount of time,
     * it may be released from the pool.  If timestamps are enabled, the client
     * may update the last used time.  Otherwise, the last used time is only
     * updated when an object is acquired or released.  The default value is
     * 10 minutes.
     * @see #setShrinkingEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the shrinking idle time after the pool
     *    has been initialized.
     */
    public void setShrinkMinIdleTime(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        shrinkMinIdleMillis = millis;
    }

    /**
     * Gets the minimum idle time to make an object eligible for shrinking.
     * @see #setShrinkMinIdleTime
     */
    public long getShrinkMinIdleTime() {return shrinkMinIdleMillis;}

    /**
     * Sets the minimum idle time to make an object eligible for garbage
     * collection.  If the object is in use and has not been used for this
     * amount of time, it may be returned to the pool.  If timestamps are
     * enabled, the client may update the last used time (this is generally
     * recommended if garbage collection is enabled).  Otherwise, the last used
     * time is only updated when an object is acquired or released.  The default
     * value is 20 minutes.
     * @see #setGCEnabled
     * @param millis The idle time, in milliseconds.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection idle time after the
     *    pool has been initialized.
     */
    public void setGCMinIdleTime(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        gcMinIdleMillis = millis;
    }

    /**
     * Gets the minimum idle time to make an object eligible for garbage
     * collection.
     * @see #setGCMinIdleTime
     */
    public long getGCMinIdleTime() {return gcMinIdleMillis;}

    /**
     * Sets the length of time between garbage collection and shrinking runs.
     * This is inexact - if there are many pools with garbage collection and/or
     * shrinking enabled, there will not be a thread for each one, and several
     * nearby actions may be combined.  Likewise if the collection process is
     * lengthy for certain types of pooled objects (not recommended), other
     * actions may be delayed.  This is to prevend an unnecessary proliferation
     * of threads (the total number of which may be limited by your OS, e.g. in
     * a "native threads" VM implementation).  Note that this parameter controls
     * both garbage collection and shrinking - and they will be performed
     * together if both are enabled.  The deafult value is 2 minutes.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the garbage collection interval after the
     *    pool has been initialized.
     */
    public void setGCInterval(long millis) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        gcIntervalMillis = millis;
    }

    /**
     * Gets the length of time between garbage collection and shrinking runs.
     * @see #setGCInterval
     */
    public long getGCInterval() {return gcIntervalMillis;}

    /**
     * Sets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.  If set to block, the request
     * will not return until an object is available.  Otherwise, the request
     * will return null immediately (and may be retried).  If multiple
     * requests block, there is no guarantee which will return first.  The
     * default is not to block (to return null).
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the blocking parameter after the
     *    pool has been initialized.
     */
    public void setBlocking(boolean blocking) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        this.blocking = blocking;
    }

    /**
     * Gets whether a request for an object will block if the pool size is
     * maxed out and no objects are available.
     * @see #setBlocking
     */
    public boolean isBlocking() {return blocking;}

    /**
     * Sets whether object clients can update the last used time.  If not, the
     * last used time will only be updated when the object is given to a client
     * and returned to the pool.  This time is important if shrinking or
     * garbage collection are enabled (particularly the latter).  The default
     * is false.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to set the timestamp parameter after the
     *    pool has been initialized.
     */
    public void setTimestampUsed(boolean timestamp) {
        if(objects != null)
            throw new IllegalStateException(INITIALIZED);
        trackLastUsed = timestamp;
    }

    /**
     * Gets whether object clients can update the last used time.
     */
    public boolean isTimestampUsed() {return trackLastUsed;}

    /**
     * Prepares the pool for use.  This must be called exactly once before
     * getObject is even called.  The pool name and object factory must be set
     * before this call will succeed.
     * @throws java.lang.IllegalStateException
     *    Occurs when you try to initialize the pool without setting the object
     *    factory or name, or you initialize the pool more than once.
     */
    public void initialize() {
        if(factory == null || poolName == null)
            throw new IllegalStateException("Factory and Name must be set before pool initialization!");
        if(objects != null)
            throw new IllegalStateException("Cannot initialize more than once!");
        objects = new HashMap();
        factory.poolStarted(this, logWriter);
        lastGC = System.currentTimeMillis();
        collector.addPool(this);
    }

    /**
     * Shuts down the pool.  All outstanding objects are closed and all objects
     * are released from the pool.  No getObject or releaseObject calls will
     * succeed after this method is called - and they will probably fail during
     * this method call.
     */
    public void shutDown() {
        collector.removePool(this);
        factory.poolClosing(this);
        HashMap localObjects = objects;
        objects = null;

        // close all objects
        for(Iterator it = localObjects.values().iterator(); it.hasNext();) {
            ObjectRecord rec = (ObjectRecord)it.next();
            if(rec.isInUse())
                factory.returnObject(rec.getClientObject());
            factory.deleteObject(rec.getObject());
            rec.close();
        }

        localObjects.clear();
        factory = null;
        poolName = null;
    }

    /**
     * Gets an object from the pool.  If all the objects in the pool are in use,
     * creates a new object, adds it to the pool, and returns it.  If all
     * objects are in use and the pool is at maximum size, will block or
     * return null.
     * @see #setBlocking
     */
    public Object getObject() {
        while(true) {
            Iterator it = new HashSet(objects.values()).iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(!rec.isInUse()) {
                    try {
                        rec.setInUse(true);
                        Object ob = rec.getObject();
                        Object result = factory.prepareObject(ob);
                        if(result != ob) rec.setClientObject(result);
                        if(result instanceof PooledObject)
                            ((PooledObject)result).addPoolEventListener(this);
                        log("Pool "+this+" gave out pooled object: "+result);
                        return result;
                    } catch(ConcurrentModificationException e) {}
                }
            }

            // Serialize creating new connections
            synchronized(objects) {  // Don't let 2 threads add at the same time
                if(minSize == 0 || objects.size() < maxSize) {
                    Object ob = factory.createObject();
                    ObjectRecord rec = new ObjectRecord(ob);
                    objects.put(ob, rec);
                    Object result = factory.prepareObject(ob);
                    if(result != ob) rec.setClientObject(result);
                    if(result instanceof PooledObject)
                        ((PooledObject)result).addPoolEventListener(this);
                    log("Pool "+this+" gave out new object: "+result);
                    return result;
                } else System.out.println("Object Pool "+poolName+" is full ("+objects.size()+"/"+maxSize+")!");
            }

            if(blocking) {
                log("Pool "+this+" waiting for a free object");
                synchronized(this) {
                    try {
                        wait();
                    } catch(InterruptedException e) {}
                }
            } else {
                break;
            }
        }

        log("Pool "+this+" couldn't find an object to return!");
        return null;
    }

    /**
     * Sets the last used time for an object in the pool that is currently
     * in use.  If the timestamp parameter is not set, this call does nothing.
     * Otherwise, the object is marked as last used at the current time.
     * @see #setTimestampUsed
     */
    public void setLastUsed(Object object) {
        if(!trackLastUsed) return;
        Object ob = factory.translateObject(object);
        ObjectRecord rec = (ObjectRecord)objects.get(ob);
        rec.setLastUsed();
    }

    /**
     * Returns an object to the pool.  This must be the exact object that was
     * given out by getObject, and it must be returned to the same pool that
     * generated it.  If other clients are blocked waiting on an object, the
     * object may be re-released immediately.
     * @throws java.lang.IllegalArgumentException
     *    Occurs when the object is not in this pool.
     */
    public void releaseObject(Object object) {
        synchronized(object) {
            Object pooled = null;
            try {
                pooled = factory.translateObject(object);
            } catch(Exception e) {
                return;        // We can't release it if the factory can't recognize it
            }
            if(pooled == null) // We can't release it if the factory can't recognize it
                return;
            ObjectRecord rec = (ObjectRecord)objects.get(pooled);
            if(rec == null) // Factory understands it, but we don't
                throw new IllegalArgumentException("Object "+object+" is not in pool "+poolName+"!");
            if(!rec.isInUse()) return; // Must have been released by GC?
            if(object instanceof PooledObject)
                ((PooledObject)object).removePoolEventListener(this);
            factory.returnObject(object);
            rec.setInUse(false);
        }
        log("Pool "+this+" returned object "+object+" to the pool.");
        if(blocking) {
            synchronized(this) {
                notify();
            }
        }
    }

    private int getUsedCount() {
        if(objects == null) return 0;
        int total = 0;
        Iterator it = new HashSet(objects.values()).iterator();
        while(it.hasNext()) {
            ObjectRecord or = (ObjectRecord)it.next();
            if(or.isInUse()) ++total;
        }
        return total;
    }

    /**
     * Returns the pool name and status.
     */
    public String toString() {
        return poolName+" ["+getUsedCount()+"/"+(objects == null ? 0 : objects.size())+"/"+(maxSize == 0 ? "Unlimited" : Integer.toString(maxSize))+"]";
    }


    // ---- PoolEventListener Implementation ----

    /**
     * If the object has been closed, release it.
     */
    public void objectClosed(PoolEvent evt) {
        releaseObject(evt.getSource());
    }

    /**
     * If the object had an error, we assume this will propogate and preclude it
     * from being closed, so we will close it.
     */
    public void objectError(PoolEvent evt) {
        releaseObject(evt.getSource());
    }

    /**
     * If we're tracking the last used times, update the last used time for the
     * specified object.
     */
    public void objectUsed(PoolEvent evt) {
        if(!trackLastUsed) return;
        setLastUsed(evt.getSource());
    }

    long getNextGCMillis(long now) {
        if(!runGC) return Long.MAX_VALUE;
        return lastGC + gcIntervalMillis - now;
    }

    // Allow GC if we're within 10% of the desired interval
    boolean isTimeToGC() {
        return System.currentTimeMillis() >=
               lastGC + Math.round((float)gcIntervalMillis * 0.9f);
    }

    void runGCandShrink() {
        if(runGC) { // Garbage collection - return any object that's been out too long with no use
            Iterator it = new HashSet(objects.values()).iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(rec.isInUse() && rec.getMillisSinceLastUse() >= gcMinIdleMillis) {
                    releaseObject(rec.getClientObject());
                }
            }
        }
        if(shrinks) { // Shrinking the pool - remove objects from the pool if they have not been used in a long time
             // Find object eligible for removal
            HashSet eligible = new HashSet();
            Iterator it = new HashSet(objects.values()).iterator();
            while(it.hasNext()) {
                ObjectRecord rec = (ObjectRecord)it.next();
                if(!rec.isInUse() && rec.getMillisSinceLastUse() > shrinkMinIdleMillis)
                    eligible.add(rec);
            }
            // Calculate number of objects to remove
            int count = Math.round(eligible.size() * shrinkPercent);
            if(count == 0 && eligible.size() > 0) count = 1;
            // Attempt to remove that many objects
            it = eligible.iterator();
            for(int i=0; i<count; i++) {
                if(objects.size() <= minSize) break; // Don't fall below the minimum
                if(!it.hasNext()) break; // If the objects have meanwhile been checked out, we're done
                try {
                    ObjectRecord rec = (ObjectRecord)it.next();
                    rec.setInUse(true);  // Don't let someone use it while we destroy it
                    Object pooled = rec.getObject();
                    objects.remove(pooled);
                    try {
                        factory.deleteObject(pooled);
                    } catch(Exception e) {
                        log("Pool "+this+" factory ("+factory.getClass().getName()+" delete error: "+e);
                    }
                    rec.close();
                } catch(ConcurrentModificationException e) {
                    --i;
                }
            }
        }
        lastGC = System.currentTimeMillis();
    }

    private void log(String message) {
        if(logWriter != null)
            logWriter.println(message);
    }
}

class BeanFactory extends PoolObjectFactory {
    private Class beanClass;

    public BeanFactory(Class beanClass) {
        try {
            beanClass.getConstructor(new Class[0]);
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException("Bean class doesn't have no-arg constructor!");
        }
        this.beanClass = beanClass;
    }

    public Object createObject() {
        try {
            return beanClass.newInstance();
        } catch(Exception e) {
            System.out.println("Unable to create instance of "+beanClass.getName()+": "+e);
        }
        return null;
    }
}
