package org.apache.jasper34.utils;

import java.util.*;

/**
 * 	A simple, straight-forward cache storage class based on HashMap
 *  that implements least-recently-used flushing as well as optional
 *  expiration flushing on an object-by-object basis.  Stored objects that
 * 	need to be able to mark themselves as 'expired' should
 * 	implement the {@link Expirable <code>Expirable</code>} interface.
 * 	<br></br>
 * 	Otherwise, this cache class is simple to use.  Use it just
 * 	like any <i>Hashtable</i> implementation.  If an object
 * 	has been flushed, requests for it will return null.  Cache
 * 	cleanup is enforced first by the least-recently-used algorithm
 * 	which is controlled by the <i>maxSize</i> and <i>flushIncrement</i> parameters.
 * 	Second, if objects pass the least-recently-used test but are
 * 	<i>Expirable </i> and return true for the <code>isExpired()</code> test, they
 * 	will be flushed.
 * 	<br></br>	
 * 	@author Mel Martinez
*/
public class MapCache extends HashMap implements Cache{

    /**
 * 		An <i>ArrayList</i> to track the usage of entries in this <i>SimpleCache</i>.
    */
    private List useage = new ArrayList(); //used to track most recent useage

    /**
 *     	The default maximum number of entries allowed in this <i>SimpleCache</i>.
    */
    private static final int MAXSIZE = 512;

    /**
 *     	The default number of least-recently-used entries to flush when <i>maxSize</i> is reached.
    */
    private static final int FLUSHINCREMENT = 128;

    /**
 *     	The actual maximum number of entries allowed in this <i>SimpleCache</i>.
    */
    private int maxSize = MAXSIZE;

    /**
 * 	The actual number of least-recently-used entries to flush when <i>maxSize</i> is reached.
    */
    private int flushIncrement = FLUSHINCREMENT;

    /**
 *     	Constructor method for a <i>SimpleCache</i> with default parameters and 
 *     	the specified <i>maxSize</i>.  A <i>SimpleCache</i> created with this 
 *     	constructor method can store <i>maxSize</i> entries; when this limit is 
 *     	reached the 128 least-recently-used entries are removed.  A <i>SimpleCache</i>
 *     	created with this constructor method also starts at an initial size of 
 *     	<i>maxSize</i>/2 if <i>maxSize</i> >= 128 or maxSize if <i>maxSize</i> < 128 
 *     	and with a load factor of 0.75.
 *     	<p>
 *     	@param <b>maxSize</b> the maximum number of entries allowed in this 
 *     			SimpleCache before the least-recently-used entries are flushed
    */
    public MapCache(int maxSize){
    	this(maxSize,FLUSHINCREMENT);
    }

    /**
 *     	Constructor method for a <i>SimpleCache</i> with default parameters and 
 *     	the specified <i>maxSize</i> and <i>flushIncrement</i>.  A <i>SimpleCache</i> 
 *     	created with this constructor method can store <i>maxSize</i> entries; when this 
 *     	limit is reached the <i>flushIncrement</i> least-recently-used entries are removed.
 *     	A <i>SimpleCache</i> created with this constructor method also starts at an initial 
 *     	size of <i>maxSize</i>/2 if <i>maxSize</i> >= 128 or <i>flushIncrement</i> if 
 *     	<i>maxSize</i> < 128 and with a load factor of 0.75.
 *     	<p>
 *     	@param <b>maxSize</b> the maximum number of entries allowed in this SimpleCache 
 *     		before the least-recently-used entries are flushed
 *     	@param <b>flushIncrement</b> the number of least-recently-used entries to flush 
 *     		when the maxSize is reached
    */
    public MapCache(int maxSize,int flushIncrement){
    	this(maxSize,flushIncrement,maxSize>=128?maxSize/2:flushIncrement);
    }

    /**
 *     	Constructor method for a <i>SimpleCache</i> with the specified <i>maxSize</i>, 
 *     	<i>flushIncrement</i>, and <i>initializeSize</i>.  A <i>SimpleCache</i> created 
 *     	with this constructor method can store <i>maxSize</i> entries; when this limit is
 *     	reached the <i>flushIncrement</i> least-recently-used entries are removed.  A 
 *     	<i>SimpleCache</i> created with this constructor method also starts at an initial 
 *     	size of <i>initialSize</i> and with a load factor of 0.75.
 *     	<p>
 *     	@param <b>maxSize</b> the maximum number of entries allowed in this SimpleCache 
 *     			before the least-recently-used entries are flushed
 *     	@param <b>flushIncrement</b> the number of least-recently-used entries to flush 
 *     			when the maxSize is reached
 *     	@param <b>initialSize</b> the initial capacity of the SimpleCache Hashtable; 
 *     			this is not the same as maxSize
    */
    public MapCache(int maxSize,int flushIncrement, int initialSize){
		this(maxSize,flushIncrement,initialSize, (float)0.75);
    }

    /**
 *     	Constructor method for a <i>SimpleCache</i> with default parameters.  By 
 *     	default a <i>SimpleCache</i> can store 512 entries; when this limit is 
 *     	reached the 128 least-recently-used entries are removed.  A default 
 *     	<i>SimpleCache</i> also starts at an initial size of 256 and with a load 
 *     	factor of 0.75.
    */
    public MapCache(){
    	this(MAXSIZE);
    }

    /**
 *     	Constructor method for a <i>SimpleCache</i> with the specified <i>maxSize</i>, 
 *     	<i>flushIncrement</i>, <i>initializeSize</i>, and <i>loadFactor</i>.  A 
 *     	<i>SimpleCache</i> created with this constructor method can store <i>maxSize</i> 
 *     	entries; when this limit is reached the <i>flushIncrement</i> least-recently-used 
 *     	entries are removed.  A <i>SimpleCache</i> created with this constructor method 
 *     	also starts at an initial size of <i>initialSize</i> and with a load factor of 
 *     	<i>loadFactor</i>.
 *     	<p>
 *     	@param <b>maxSize</b> the maximum number of entries allowed in this SimpleCache 
 *     			before the least-recently-used entries are flushed
 *     	@param <b>flushIncrement</b> the number of least-recently-used entries to flush 
 *     			when the maxSize is reached
 *     	@param <b>initialSize</b> the initial capacity of the SimpleCache Hashtable; this 
 *     			is not the same as maxSize
 *     	@param <b>loadFactor</b> the load factor of the SimpleCache Hashtable; indicates 
 *     			when rehashing should occur
    */
    public MapCache(int maxSize,int flushIncrement, int initialSize,float loadFactor){
		super(	initialSize>0?initialSize:64,
				(float)(loadFactor<=0?0.75:loadFactor>1.0?0.75:loadFactor));
		setMaxSize(maxSize);
		setFlushIncrement(flushIncrement);
    }

    public void setMaxSize(int maxSize){
	 	this.maxSize = maxSize>0?maxSize:MAXSIZE;
		setFlushIncrement(flushIncrement); //validate flushIncrement
    }

    public int getMaxSize(){
 		return maxSize;
    }

    public void setFlushIncrement(int flushIncrement){
	 	this.flushIncrement = flushIncrement<=0 ? 0:
	 				flushIncrement>maxSize ? maxSize:
	 				flushIncrement;
    }

     public int getFlushIncrement(){
 		return flushIncrement;
    }

    /**
 * 	 	Puts the <i>value</i> Object in the cache using <i>key</i> as its identifier.  
 * 	 	When the maximum number of allowed entries is reached ({@link SimpleCache#getMaxSize() 
 * 	 	<code>getMaxSize()</code>}) this method will also flush the least-recently-used 
 * 	 	ones (up to {@link SimpleCache#getFlushIncrement() <code>getFlushIncrement()</code>} 
 * 	 	entries).
 * 	 	<p>
 * 	 	@param <b>key</b> the identifier associated with the value to store
 * 	 	@param <b>value</b> the Object to store in the cache.
 * 	 	@return the previous value associated with the specified key or null if none was so
    */
    public synchronized Object put(Object key,Object value){
        Object oldVal = null;
        if(containsKey(key)) {
            useage.remove(useage.indexOf(key)); //pull existing key from useage list
        }
        oldVal = super.put(key,value);
        useage.add(key);    //    put key at top of useage list
        if(size() > maxSize) cleanCache(flushIncrement);
        return oldVal;
    }


    /**
 *     	Retrieves the value from the cache associated with the specified <i>key</i>.  
 *     	If the value implements the {@link Expirable <code>Expirable</code>} interface 
 *     	and has expired it will be removed from the cache and null will be returned.
 *     	<p>
 * 		@param <b>key</b> the identifier associated with the value to retrieve
 *     	@return the cache value mapped to by key or null if it is not in the cache 
 *     			or has expired
    */
    public synchronized Object get(Object key){
        if(containsKey(key)){
            Object value = super.get(key);
            useage.remove(useage.indexOf(key));
            if((value instanceof Expirable) && ((Expirable)value).isExpired()){
                remove(key);
                return null;
            }
            useage.add(key);    //    move key to top of useage list
            return value;
        }
        return null;
    }

    /**
 * 		This method removes the <i>increment</i> least-recently-used entries from 
 * 		both the useage list and this <i>SimpleCache</i>.
 * 		<p>
 * 		@param <b>increment</b> the number of least-recently-used entries to remove
    */
    private synchronized void cleanCache(int increment){
        if(increment > 0){
            for(int i=0;(i<increment)&&(useage.size()>0);i++){
                remove(useage.remove(0));
            }
        }
    }

    /**
 *     	This method is used to clear the entire <i>SimpleCache</i> of its contents.
    */
    public synchronized void emptyCache(){
    	cleanCache(size());
    }
    
}//end SimpleCache
