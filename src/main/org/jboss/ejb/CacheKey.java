/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import java.rmi.MarshalledObject;

import org.apache.log4j.Category;

/**
 * CacheKey is an encapsulation of both the PrimaryKey and a
 * cache specific key.
 *   
 * <p>This implementation is a safe implementation in the sense that it
 *    doesn't rely on the user supplied hashcode and equals.   It is also
 *    fast since the hashCode operation is pre-calculated.
 *
 * @see org.jboss.ejb.plugins.NoPassivationInstanceCache.java
 * @see org.jboss.ejb.plugins.EntityInstanceCache
 * @see org.jboss.ejb.plugins.EntityProxy
 * 
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.17 $
 */
public class CacheKey
   implements Externalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   /**
    * The database primaryKey.
    * 
    * This primaryKey is used by:
    *
    * org.jboss.ejb.plugins.EntityInstanceCache.setKey() - to set the EntityEnterpriseContext id
    * org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy.invoke():
    * - implementing Entity.toString() --> cacheKey.getId().toString()
    * - implementing Entity.hashCode() --> cacheKey.getId().hashCode()
    * - etc...
    * org.jboss.ejb.plugins.local.BaseLocalContainerInvoker.EntityProxy.getId()
    */
   protected Object id;
   
   public Object getId()
   {
      return id;
   }
     
   /** The Marshalled Object representing the key */
   protected MarshalledObject mo;
    
   /** The Marshalled Object's hashcode */
   protected int hashCode;
    
   // Static --------------------------------------------------------  
    
   // Public --------------------------------------------------------
    
   public CacheKey() {
      // For externalization only
   }

   public CacheKey(Object id) {
      // why does this throw an error and not an IllegalArgumentException ?
      if (id == null) throw new Error("id may not be null");
         
      this.id = null;
        
      try {
         

         /*
         * FIXME MARCF: The reuse of the primary key is an "exception" and this fix makes 
          everyone pay an hefty price.  If we really want this behavior we can put it in 
          the cache.  Is there a test for this? 
         
         // Equals rely on the MarshalledObject itself
         mo =  new MarshalledObject(id);
         // Make a copy of the id to enforce copy semantics and 
         // allow reuse of the original primary key
         this.id = mo.get();
         
         */
         
         // Precompute the hashCode (speed)
         hashCode = mo.hashCode();
      }
      catch (Exception e) {
         //
         // should probably throw a nested exception here, but
         // for now instead of printStackTrace, lets log it
         //
         Category log = Category.getInstance(this.getClass());
         log.error("failed to initialize", e);
      }
   }
    
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
    
   public void writeExternal(ObjectOutput out)
      throws IOException
   {
      out.writeObject(id);
      out.writeObject(mo);
      out.writeInt(hashCode);
   }
   
   public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException
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
