

/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb;

import java.rmi.MarshalledObject;

/**
*   CacheKey
* 
*   CacheKey is an encapsulation of both the PrimaryKey and a cache specific key
*   
*   This implementation is a safe implementation in the sense that it doesn't rely 
*   on the user supplied hashcode and equals.   It is also fast since the hashCode operation 
*   is pre-calculated.
*
*   @see org.jboss.ejb.plugins.NoPassivationInstanceCache.java
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.11 $
*/
public class CacheKey
    implements java.io.Externalizable
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    // The database primaryKey
    // This primaryKey is used by
    //
    // org.jboss.ejb.plugins.EntityInstanceCache.setKey() - to set the EntityEnterpriseContext id
    // org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy.invoke():
    // - implementing Entity.toString() --> cacheKey.getId().toString()
    // - implementing Entity.hashCode() --> cacheKey.getId().hashCode()
    // - etc...
    // org.jboss.ejb.plugins.local.BaseLocalContainerInvoker.EntityProxy.getId()
    //
    protected Object id;
    public Object getId()
    {
	return id;
    }
     
    // The Marshalled Object representing the key
    protected MarshalledObject mo;
    
    // The Marshalled Object's hashcode
    protected int hashCode;
    
    // Static --------------------------------------------------------  
    
    // Public --------------------------------------------------------
    
    public CacheKey() {
	// For externalization only
    }
    public CacheKey(Object id) {
       
	if (id == null) throw new Error("id may not be null");
         
	this.id = null;
        
	try {
	    // Equals rely on the MarshalledObject itself
	    mo =  new MarshalledObject(id);
	    // Make a copy of the id to enforce copy semantics and 
	    // allow reuse of the original primary key
	    this.id = mo.get();
	    // Precompute the hashCode (speed)
	    hashCode = mo.hashCode();
    	}
	catch (Exception e) {e.printStackTrace();}
    }
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    public void writeExternal(java.io.ObjectOutput out)
	throws java.io.IOException
    {
        out.writeObject(id);
	out.writeObject(mo);
       	out.writeInt(hashCode);
    }
   
    public void readExternal(java.io.ObjectInput in)
	throws java.io.IOException, ClassNotFoundException
    {
        id = in.readObject();
	mo = (MarshalledObject) in.readObject();
        hashCode = in.readInt();
    }

    // HashCode and Equals over write --------------------------------
    
    /**
     * these should be overwritten by extending Cache key
     * since they define what the cache does in the first place
     */
    public int hashCode() {
        
        // we default to the pK id
        return hashCode;
    }
    
    
    /**
     * equals()
     *
     * We base the equals on the equality of the underlying key
     * in this fashion we make sure that we cannot have duplicate 
     * hashes in the maps. 
     * The fast way (and right way) to do this implementation 
     * is with a incremented cachekey.  It is more complex but worth
     * the effort this is a FIXME (MF)
     * The following implementation is fool-proof
     */
    public boolean equals(Object object) {
        
        if (object instanceof CacheKey) {
            
            return (mo.equals(((CacheKey) object).mo));
        }
        return false;
    }
	
    public String toString()
    {
	return id.toString();
    }
    
    // Inner classes -------------------------------------------------
}

