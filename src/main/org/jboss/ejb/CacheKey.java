

/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

/**
*   CacheKey
* 
*   CacheKey is an encapsulation of both the PrimaryKey and any cache specific key
*   
* 	There are several strategies to implement the cache. 
* 	One is to use the hash and equals of this class to implement tables
* 	Another one is to work from the value of the CacheKey
*
*   @see org.jboss.ejb.plugins.NoPassivationInstanceCache.java
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.1 $
*/
public class CacheKey
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
	// The database primaryKey
	public Object id;
	
	// In case the cache doesn't use advanced cache keys, this key is virtual
    public boolean isVirtual = false;
	
    // Static --------------------------------------------------------  
    
    // Public --------------------------------------------------------
    
	public CacheKey() {
		// For externalization only
	}
	public CacheKey(Object id) {
		
		if (id == null) throw new Error("id may not be null");
			
		this.id = id;
	}
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // HashCode and Equals over write --------------------------------
    
	/**
	* these should be overwritten by extending Cache key
	* since they define what the cache does in the first place
	*/
    public int hashCode() {
        
        // we default to the pK id
        return id.hashCode();
    }
    
    
    public boolean equals(Object object) {
        
        if (object instanceof CacheKey) {
            
            return id.equals(((CacheKey) object).id);
        }
        return false;
    }
    
    // Inner classes -------------------------------------------------
}

