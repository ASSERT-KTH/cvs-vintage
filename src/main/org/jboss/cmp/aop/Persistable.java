package org.jboss.cmp.aop;

/**
 * The interface all the peristence aware instances implement.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public interface Persistable
   extends StateAware
{
   // Public -------------------------------------
   /**
    * Returns persistent instance's identity.
    * If there is no identity assigned, the method returns null.
    *
    * NOTE: for the moment, I don't think this method is useful for apllication developer.
    */
   Identity cmpGetIdentity();

   /**
    * Marks the instance as persistent. The instance will be stored in a physical store
    * at transaction commit time. After the call to this method:
    * - the instance has a non null identity assigned;
    * - the [managed] fields' values are stored for the purpose of restoring the state
    *   at rollback.
    *
    * Throws an exception if the instance's state doesn't allow
    * the call to this method.
    */
   void cmpMakePersistent() throws PersistenceException;

   /**
    * Marks the instance as removed. The instance will be removed from the physical store
    * at transaction commit time. After the call to this method, the instance retains
    * identity but managed fields are in undefined state.
    */
   void cmpDeletePersistent() throws PersistenceException;
}
