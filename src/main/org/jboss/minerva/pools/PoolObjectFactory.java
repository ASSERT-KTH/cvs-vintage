/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.pools;

import java.io.PrintWriter;

/**
 * Creates objects to be used in an object pool.  This is a class instead of
 * an interface so you can ignore any of the methods you don't need.
 * @version $Revision: 1.3 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public abstract class PoolObjectFactory {
    /**
     * Creates a new object to be stored in an object pool.  This is the
     * instance that will actually be sotred in the pool and reused.  If you
     * want to wrap it somehow, or return instances of a different type that
     * refers to these, you can implement prepareObject.
     * @see #prepareObject
     */
    public abstract Object createObject();

    /**
     * Indicates to the factory that the pool has started up.  This will be
     * called before any other methods of the factory are called (on behalf of
     * this pool).
     * @param pool The pool that is starting.  You may decide to allow
     *    multiple pools you use your factory, or to restrict it to a one-to-one
     *    relationship.
     * @param log A writer you can use to log messages.  Use this in preference
     *    to System.xxx.println.
     * @throws java.lang.IllegalArgumentException
     *         Occurs when the pool is null.
     */
    public void poolStarted(ObjectPool pool, PrintWriter log) {
        if(pool == null)
            throw new IllegalArgumentException("Cannot start factory with null pool!");
    }

    /**
     * Prepares an object to be returned to the client.  This may be used to
     * configure the object somehow, or actually return a completely different
     * object (so long as the original can be recovered in translateObject or
     * returnObject).  This will be called whenever an object is returned to
     * the client, whether a new object or a previously pooled object.
     * @param pooledObject The object in the pool, as created by createObject.
     * @return The object to return to the client.  If different, the pooled
     *    object must be recoverable by translateObject and returnObject.
     */
    public Object prepareObject(Object pooledObject) {
        return pooledObject;
    }

    /**
     * If the objects supplied to the client are different than the objects in
     * the pool, extracts a pool object from a client object.  This should only
     * be called between prepareObject and returnObject for any given pool
     * object (and associated client object).  However, it may be called once
     * after an object has been released if the garbage collector and a client
     * attempt to release an object at the same time.  In this case, this
     * method may work, return null, or throw an exception and the pool will
     * handle it gracefully.  The default implementation returns the parameter
     * object (assumes client and pooled objects are the same).
     * @param clientObject The client object, as returned by prepareObject
     * @return The pooled object, as originally returned by createObject
     */
    public Object translateObject(Object clientObject) {
        return clientObject;
    }

    /**
     * Prepares an object to be returned to the pool.  Any cleanup or reset
     * actions should be performed here.  This also has the same effect as
     * translateObject (only relevant if the pooled objects are different than
     * the objects supplied to the client).
     * @param clientObject The client object, as returned by prepareObject
     * @return The pooled object, as originally returned by createObject, ready
     *     to be put back in the pool and reused.
     */
    public Object returnObject(Object clientObject) {
        return clientObject;
    }

    /**
     * Indicates to the factory that the pool is closing down.  This will be
     * called before all the instances are destroyed.  There may be calls to
     * returnObject or translateObject after this, but no calls to
     * createObject or prepareObject (on behalf of this pool).
     * @param pool The pool that is closing.  You may decide to allow
     *    multiple pools you use your factory, or to restrict it to a one-to-one
     *    relationship.
     * @throws java.lang.IllegalArgumentException
     *         Occurs when the pool is null.
     */
    public void poolClosing(ObjectPool pool) {
        if(pool == null)
            throw new IllegalArgumentException("Cannot close factory with a null pool!");
    }

    /**
     * Permanently closes an object, after it is removed from the pool.  The
     * object will not be returned to the pool - after this, it is gone.  This
     * is called when the pool shrinks, and when the pool is shut down.
     */
    public void deleteObject(Object pooledObject) {
    }
}