/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.util;

/**
*   FastKey
* 
*   FastKey is a hack to enable fool proof and fast operation of caches for Entity
*   In the case of complex PK if a developer misses hash and equals the maps won't
*   properly work in jboss.  Here we provide an wrapper to the DB Key and the hash
*   is over-written so that we never miss a hit in cache and have constant speed.
*   
*   @see org.jboss.ejb.plugins.NoPassivationInstanceCache.java
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.4 $
*/
public class FastKey
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    // The fastKey that identifies the association between EJBObject and context
    public Long fastKey;
    
    // The id that the instance holding this fastKey is supposed to represent (DB)
    public Object id;
    
    // Static --------------------------------------------------------  
    
    // The seed for the fastKey id
    // MF FIXME: I suspect this is weak, if somebody ask for these all the time (heavy server)
    // then a server restart will recieve requests from previous servers and miss these... 
    // Think more about it.
    private static long seedKey = System.currentTimeMillis();
    
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    
    public FastKey(Object id) {
        
        fastKey = nextFastKey();
        
        this.id = id;
    }
    
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    protected Long nextFastKey()
    {
        //increment the timeStamp
        return new Long(seedKey++);
    }
    
    // Private -------------------------------------------------------
    
    // HashCode and Equals over write --------------------------------
    
    public int hashCode() {
        
        // the fastKey is always assigned
        return fastKey.intValue();
    }
    
    
    public boolean equals(Object object) {
        
        if (object instanceof FastKey) {
            
            return fastKey.equals(((FastKey) object).fastKey);
        }
        return false;
    }
    
    // Inner classes -------------------------------------------------
}

