

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
*   CacheKey is an encapsulation of both the PrimaryKey and a cache specific key
*   
*   This implementation is a safe implementation in the sense that it doesn't rely 
*   on the user supplied hashcode and equals.   It is also fast since the hashCode operation 
*   is pre-calculated.
*
*   @see org.jboss.ejb.plugins.NoPassivationInstanceCache.java
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.5 $
*/
public class CacheKey
    implements java.io.Externalizable
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    // The database primaryKey
    public Object id;
     
    private int hashCode;
    
    // Static --------------------------------------------------------  
    
    // Public --------------------------------------------------------
    
    public CacheKey() {
       // For externalization only
    }
    public CacheKey(Object id) {
       
       if (id == null) throw new Error("id may not be null");
         
       this.id = id;
        
		try {
        	hashCode = (new java.rmi.MarshalledObject(id)).hashCode();
			System.out.println("CacheKeyHash:" +hashCode);
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
       	out.writeInt(hashCode);
   
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws java.io.IOException, ClassNotFoundException
   {
        id = in.readObject();
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
    
    
    public boolean equals(Object object) {
        
        if (object instanceof CacheKey) {
            
            return (hashCode ==(((CacheKey) object).hashCode));
        }
        return false;
    }
    
    // Inner classes -------------------------------------------------
}

