/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * Interface that specifies a policy for caches. <p>
 * Implementation classes can implement a LRU policy, a random one, 
 * a MRU one, or any other suitable policy.
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.3 $
 */
public interface CachePolicy extends Service
{
	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	 * Returns the object paired with the specified key if it's 
	 * present in the cache, otherwise must return null. <br>
	 * Implementations of this method must have complexity of order O(1).
	 * Differently from {@link #peek} this method not only return whether
	 * the object is present in the cache or not, but also 
	 * applies the implemented policy that will "refresh" the cached 
	 * object in the cache, because this cached object
	 * was really requested.
	 * @param key the key paired with the object
	 * @see #peek
	 */
	public Object get(Object key);
	/**
	 * Returns the object paired with the specified key if it's 
	 * present in the cache, otherwise must return null. <br>
	 * Implementations of this method must have complexity of order O(1).
	 * This method should not apply the implemented caching policy to the 
	 * object paired with the given key, so that a client can 
	 * query if an object is cached without "refresh" its cache status. Real 
	 * requests for the object must be done using {@link #get}.
	 * @param key the key paired with the object
	 * @see #get
	 */	
	public Object peek(Object key);
	/**
	 * Inserts the specified object into the cache following the 
	 * implemented policy. <br>
	 * Implementations of this method must have complexity of order O(1).
	 * @param key the key paired with the object
	 * @param object the object to cache
	 * @see #remove
	 */
	public void insert(Object key, Object object);
	/**
	 * Remove the cached object paired with the specified key. <br>
	 * Implementations of this method must have complexity of order O(1).
	 * @param key the key paired with the object
	 * @see #insert
	 */
	public void remove(Object key);
	/**
	 * Flushes the cached objects from the cache.
	 */
	public void flush();
}
