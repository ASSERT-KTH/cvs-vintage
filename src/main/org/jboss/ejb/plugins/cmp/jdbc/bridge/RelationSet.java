/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.util.Collection; 
import java.util.ConcurrentModificationException; 
import java.util.HashSet; 
import java.util.Iterator; 
import java.util.Set; 
import javax.ejb.EJBException; 
import javax.ejb.EJBLocalObject; 
import org.jboss.ejb.EntityEnterpriseContext; 
import org.jboss.ejb.LocalContainerInvoker; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge; 

/**
 * This is the relationship set.  An instance of this class
 * is returned when collection valued cmr field is accessed.
 * See the EJB 2.0 specification for a more detailed description
 * or the responsibilities of this class.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */                            
public class RelationSet implements Set {
   private JDBCCMRFieldBridge cmrField;
   private EntityEnterpriseContext ctx;
   private Set[] setHandle;
   private Class relatedLocalInterface;

   //
   // Most of this class is a boring wrapper arround the id set.
   // The only interesting hitch is the setHandle.  This class doesn't
   // have a direct referance to the related id set, it has a referance
   // to a referance to the set. When the transaction is completed the 
   // CMR field sets my referance to the set to null, so that I know that
   // this set is no longer valid. See the ejb spec for more info.
   //
   public RelationSet(JDBCCMRFieldBridge cmrField, EntityEnterpriseContext ctx, Set[] setHandle) {
      this.cmrField = cmrField;
      this.ctx = ctx;
      this.setHandle = setHandle;
      relatedLocalInterface = cmrField.getRelatedLocalInterface();   
   }
   
   private Set getIdSet() {
      if(setHandle[0] == null) {
         throw new IllegalStateException("A CMR collection may only be used within the transction in which it was created");
      }
      return setHandle[0];
   }
   
   public int size() {
      Set idSet = getIdSet();
      return idSet.size();
   }

   public boolean isEmpty() {
      Set idSet = getIdSet();
      return idSet.isEmpty();
   }

   public int hashCode() {
      Set idSet = getIdSet();
      return idSet.hashCode();
   }

   public boolean add(Object o) {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      if(!relatedLocalInterface.isInstance(o)) {
         throw new IllegalArgumentException("Object must be an instance of " + relatedLocalInterface.getName());
      }
      
      Object id = ((EJBLocalObject)o).getPrimaryKey();
      if(idSet.contains(id)) {
         return false;
      }
      cmrField.createRelationLinks(ctx, id);
      return true;
   }
   
   public boolean addAll(Collection c) {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      if(c == null) {
         throw new IllegalArgumentException("Collection is null");
      }
      
      boolean isModified = false;
      
      Iterator iterator = c.iterator();
      while(iterator.hasNext()) {
         isModified = isModified || add(iterator.next());
      }
      return isModified;
   }

   public boolean remove(Object o) {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      if(!relatedLocalInterface.isInstance(o)) {
         throw new IllegalArgumentException("Object must be an instance of " + relatedLocalInterface.getName());
      }

      Object id = ((EJBLocalObject)o).getPrimaryKey();
      if(!idSet.contains(id)) {
         return false;
      }
      cmrField.destroyRelationLinks(ctx, id);
      return true;
   }

   public boolean removeAll(Collection c) {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      if(c == null) {
         throw new IllegalArgumentException("Collection is null");
      }
      
      boolean isModified = false;
      
      Iterator iterator = c.iterator();
      while(iterator.hasNext()) {
         isModified = isModified || remove(iterator.next());
      }
      return isModified;
   }

   public void clear() {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      Iterator iterator = (new HashSet(idSet)).iterator();
      while(iterator.hasNext()) {
         cmrField.destroyRelationLinks(ctx, iterator.next());
      }
   }

   public boolean retainAll(Collection c) {
      Set idSet = getIdSet();
      if(cmrField.isReadOnly()) {
         throw new EJBException("Field is read-only: " + 
               cmrField.getFieldName());
      }

      if(c == null) {
         throw new IllegalArgumentException("Collection is null");
      }
      
      // get a set of the argument collection's ids
      HashSet argIds = new HashSet();
      Iterator iterator = ((Set)c).iterator();
      while(iterator.hasNext()) {
         argIds.add(((EJBLocalObject)iterator.next()).getPrimaryKey());
      }

      boolean isModified = false;

      iterator = idSet.iterator();
      while(iterator.hasNext()) {
         Object id = iterator.next();
         if(!argIds.contains(id)) {
            cmrField.destroyRelationLinks(ctx, id);
            isModified = true;
         }
      }
      return isModified;
   }

   public boolean equals(Object o) {
      Set idSet = getIdSet();

      if( !(o instanceof Set)) {
         return false;
      }
      
      // get a set of the argument collection's ids
      HashSet argIds = new HashSet();
      Iterator iterator = ((Set)o).iterator();
      while(iterator.hasNext()) {
         argIds.add(((EJBLocalObject)iterator.next()).getPrimaryKey());
      }

      return idSet.equals(argIds);
   }

   public boolean contains(Object o) {
      Set idSet = getIdSet();

      if(!relatedLocalInterface.isInstance(o)) {
         throw new IllegalArgumentException("Object must be an instance of " + relatedLocalInterface.getName());
      }
      
      Object id = ((EJBLocalObject)o).getPrimaryKey();
      return idSet.contains(id);
   }

   public boolean containsAll(Collection c) {
      Set idSet = getIdSet();

      if(c == null) {
         throw new IllegalArgumentException("Collection is null");
      }
      
      // get a set of the argument collection's ids
      HashSet argIds = new HashSet();
      Iterator iterator = c.iterator();
      while(iterator.hasNext()) {
         argIds.add(((EJBLocalObject)iterator.next()).getPrimaryKey());
      }
            
      return idSet.containsAll(argIds);
   }

   public Object[] toArray(Object a[]) {
      Set idSet = getIdSet();

      Collection c = cmrField.getRelatedInvoker().getEntityLocalCollection(idSet);
      return c.toArray(a);
   }

   public Object[] toArray() {
      Set idSet = getIdSet();

      Collection c = cmrField.getRelatedInvoker().getEntityLocalCollection(idSet);
      return c.toArray();
   }
   
   public Iterator iterator() {
      Set idSet = getIdSet();

      return new Iterator() {
         private final Iterator idIterator = getIdSet().iterator();
         private final LocalContainerInvoker containerInvoker = cmrField.getRelatedInvoker();
         private Object currentId;

         public boolean hasNext() {
            verifyIteratorIsValid();

            try {
               return idIterator.hasNext();
            } catch(ConcurrentModificationException e) {
               throw new IllegalStateException("Underlying collection has been modified");
            }
         }
         
         public Object next() {
            verifyIteratorIsValid();

            try {
               currentId = idIterator.next();
               return containerInvoker.getEntityEJBLocalObject(currentId);   
            } catch(ConcurrentModificationException e) {
               throw new IllegalStateException("Underlying collection has been modified");
            }
         }
         
         public void remove() {
            verifyIteratorIsValid();
            if(cmrField.isReadOnly()) {
               throw new EJBException("Field is read-only: " + 
                     cmrField.getFieldName());
            }

            try {
               idIterator.remove();
               cmrField.destroyRelationLinks(ctx, currentId, false);
            } catch(ConcurrentModificationException e) {
               throw new IllegalStateException("Underlying collection has been modified");
            }
         }
         
         private void verifyIteratorIsValid() {
            if(setHandle[0] == null) {
               throw new IllegalStateException("The iterator of a CMR collection may only be used within the transction in which it was created");
            }
         }            
      };
   }
}
