package org.apache.fulcrum.security.impl.db.entity;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.fulcrum.intake.Retrievable;
import org.apache.torque.TorqueException;
import org.apache.torque.om.BaseObject;
import org.apache.torque.om.ComboKey;
import org.apache.torque.om.DateKey;
import org.apache.torque.om.NumberKey;
import org.apache.torque.om.ObjectKey;
import org.apache.torque.om.SimpleKey;
import org.apache.torque.om.StringKey;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.torque.util.Transaction;


/**
 * You should not use this class directly.  It should not even be
 * extended all references should be to TurbinePermission
 */
public abstract class BaseTurbinePermission extends BaseObject
    implements org.apache.fulcrum.intake.Retrievable
{
    /** The Peer class */
    private static final TurbinePermissionPeer peer =
        new TurbinePermissionPeer();

        
    /** The value for the permissionId field */
    private Integer permissionId;
      
    /** The value for the name field */
    private String name;
  
    
    /**
     * Get the PermissionId
     *
     * @return Integer
     */
    public Integer getPermissionId()
    {
        return permissionId;
    }

                                              
    /**
     * Set the value of PermissionId
     *
     * @param v new value
     */
    public void setPermissionId(Integer v) throws TorqueException
    {
    
                  if (!ObjectUtils.equals(this.permissionId, v))
              {
            this.permissionId = v;
            setModified(true);
        }
    
          
                                  
        // update associated TurbineRolePermission
        if (collTurbineRolePermissions != null)
        {
            for (int i = 0; i < collTurbineRolePermissions.size(); i++)
            {
                ((TurbineRolePermission) collTurbineRolePermissions.get(i))
                    .setPermissionId(v);
            }
        }
                      }
  
    /**
     * Get the Name
     *
     * @return String
     */
    public String getName()
    {
        return name;
    }

                        
    /**
     * Set the value of Name
     *
     * @param v new value
     */
    public void setName(String v) 
    {
    
                  if (!ObjectUtils.equals(this.name, v))
              {
            this.name = v;
            setModified(true);
        }
    
          
              }
  
         
                                
            
    /**
     * Collection to store aggregation of collTurbineRolePermissions
     */
    protected List collTurbineRolePermissions;

    /**
     * Temporary storage of collTurbineRolePermissions to save a possible db hit in
     * the event objects are add to the collection, but the
     * complete collection is never requested.
     */
    protected void initTurbineRolePermissions()
    {
        if (collTurbineRolePermissions == null)
        {
            collTurbineRolePermissions = new ArrayList();
        }
    }

    /**
     * Method called to associate a TurbineRolePermission object to this object
     * through the TurbineRolePermission foreign key attribute
     *
     * @param l TurbineRolePermission
     * @throws TorqueException
     */
    public void addTurbineRolePermission(TurbineRolePermission l) throws TorqueException
    {
        getTurbineRolePermissions().add(l);
        l.setTurbinePermission((TurbinePermission) this);
    }

    /**
     * The criteria used to select the current contents of collTurbineRolePermissions
     */
    private Criteria lastTurbineRolePermissionsCriteria = null;

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of
     * getTurbineRolePermissions(new Criteria())
     *
     * @throws TorqueException
     */
    public List getTurbineRolePermissions() throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            collTurbineRolePermissions = getTurbineRolePermissions(new Criteria(10));
        }
        return collTurbineRolePermissions;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this TurbinePermission has previously
     * been saved, it will retrieve related TurbineRolePermissions from storage.
     * If this TurbinePermission is new, it will return
     * an empty collection or the current collection, the criteria
     * is ignored on a new object.
     *
     * @throws TorqueException
     */
    public List getTurbineRolePermissions(Criteria criteria) throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            if (isNew())
            {
               collTurbineRolePermissions = new ArrayList();
            }
            else
            {
                      criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId() );
                      collTurbineRolePermissions = TurbineRolePermissionPeer.doSelect(criteria);
            }
        }
        else
        {
            // criteria has no effect for a new object
            if (!isNew())
            {
                // the following code is to determine if a new query is
                // called for.  If the criteria is the same as the last
                // one, just return the collection.
                      criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                      if (!lastTurbineRolePermissionsCriteria.equals(criteria))
                {
                    collTurbineRolePermissions = TurbineRolePermissionPeer.doSelect(criteria);
                }
            }
        }
        lastTurbineRolePermissionsCriteria = criteria;

        return collTurbineRolePermissions;
    }

    /**
     * If this collection has already been initialized, returns
     * the collection. Otherwise returns the results of
     * getTurbineRolePermissions(new Criteria(),Connection)
     * This method takes in the Connection also as input so that
     * referenced objects can also be obtained using a Connection
     * that is taken as input
     */
    public List getTurbineRolePermissions(Connection con) throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            collTurbineRolePermissions = getTurbineRolePermissions(new Criteria(10), con);
        }
        return collTurbineRolePermissions;
    }

    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this TurbinePermission has previously
     * been saved, it will retrieve related TurbineRolePermissions from storage.
     * If this TurbinePermission is new, it will return
     * an empty collection or the current collection, the criteria
     * is ignored on a new object.
     * This method takes in the Connection also as input so that
     * referenced objects can also be obtained using a Connection
     * that is taken as input
     */
    public List getTurbineRolePermissions(Criteria criteria, Connection con)
            throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            if (isNew())
            {
               collTurbineRolePermissions = new ArrayList();
            }
            else
            {
                       criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                       collTurbineRolePermissions = TurbineRolePermissionPeer.doSelect(criteria, con);
             }
         }
         else
         {
             // criteria has no effect for a new object
             if (!isNew())
             {
                 // the following code is to determine if a new query is
                 // called for.  If the criteria is the same as the last
                 // one, just return the collection.
                       criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                       if (!lastTurbineRolePermissionsCriteria.equals(criteria))
                 {
                     collTurbineRolePermissions = TurbineRolePermissionPeer.doSelect(criteria, con);
                 }
             }
         }
         lastTurbineRolePermissionsCriteria = criteria;

         return collTurbineRolePermissions;
     }

                        
              
                    
                    
                                
                                                              
                                        
                    
                    
          
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this TurbinePermission is new, it will return
     * an empty collection; or if this TurbinePermission has previously
     * been saved, it will retrieve related TurbineRolePermissions from storage.
     *
     * This method is protected by default in order to keep the public
     * api reasonable.  You can provide public methods for those you
     * actually need in TurbinePermission.
     */
    protected List getTurbineRolePermissionsJoinTurbineRole(Criteria criteria)
        throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            if (isNew())
            {
               collTurbineRolePermissions = new ArrayList();
            }
            else
            {
                            criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                            collTurbineRolePermissions = TurbineRolePermissionPeer.doSelectJoinTurbineRole(criteria);
            }
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                        criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                        if (!lastTurbineRolePermissionsCriteria.equals(criteria))
            {
                collTurbineRolePermissions = TurbineRolePermissionPeer.doSelectJoinTurbineRole(criteria);
            }
        }
        lastTurbineRolePermissionsCriteria = criteria;

        return collTurbineRolePermissions;
    }
                  
                    
                              
                                
                                                              
                                        
                    
                    
          
    /**
     * If this collection has already been initialized with
     * an identical criteria, it returns the collection.
     * Otherwise if this TurbinePermission is new, it will return
     * an empty collection; or if this TurbinePermission has previously
     * been saved, it will retrieve related TurbineRolePermissions from storage.
     *
     * This method is protected by default in order to keep the public
     * api reasonable.  You can provide public methods for those you
     * actually need in TurbinePermission.
     */
    protected List getTurbineRolePermissionsJoinTurbinePermission(Criteria criteria)
        throws TorqueException
    {
        if (collTurbineRolePermissions == null)
        {
            if (isNew())
            {
               collTurbineRolePermissions = new ArrayList();
            }
            else
            {
                            criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                            collTurbineRolePermissions = TurbineRolePermissionPeer.doSelectJoinTurbinePermission(criteria);
            }
        }
        else
        {
            // the following code is to determine if a new query is
            // called for.  If the criteria is the same as the last
            // one, just return the collection.
            boolean newCriteria = true;
                        criteria.add(TurbineRolePermissionPeer.PERMISSION_ID, getPermissionId());
                        if (!lastTurbineRolePermissionsCriteria.equals(criteria))
            {
                collTurbineRolePermissions = TurbineRolePermissionPeer.doSelectJoinTurbinePermission(criteria);
            }
        }
        lastTurbineRolePermissionsCriteria = criteria;

        return collTurbineRolePermissions;
    }
                            


          
    private static List fieldNames = null;

    /**
     * Generate a list of field names.
     *
     * @return a list of field names
     */
    public static synchronized List getFieldNames()
    {
        if (fieldNames == null)
        {
            fieldNames = new ArrayList();
              fieldNames.add("PermissionId");
              fieldNames.add("Name");
              fieldNames = Collections.unmodifiableList(fieldNames);
        }
        return fieldNames;
    }

    /**
     * Retrieves a field from the object by name passed in as a String.
     *
     * @param name field name
     * @return value
     */
    public Object getByName(String name)
    {
          if (name.equals("PermissionId"))
        {
                return getPermissionId();
            }
          if (name.equals("Name"))
        {
                return getName();
            }
          return null;
    }
    
    /**
     * Retrieves a field from the object by name passed in
     * as a String.  The String must be one of the static
     * Strings defined in this Class' Peer.
     *
     * @param name peer name
     * @return value
     */
    public Object getByPeerName(String name)
    {
          if (name.equals(TurbinePermissionPeer.PERMISSION_ID))
        {
                return getPermissionId();
            }
          if (name.equals(TurbinePermissionPeer.PERMISSION_NAME))
        {
                return getName();
            }
          return null;
    }

    /**
     * Retrieves a field from the object by Position as specified
     * in the xml schema.  Zero-based.
     *
     * @param pos position in xml schema
     * @return value
     */
    public Object getByPosition(int pos)
    {
            if (pos == 0)
        {
                return getPermissionId();
            }
              if (pos == 1)
        {
                return getName();
            }
              return null;
    }
     
    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
     *
     * @throws Exception
     */
    public void save() throws Exception
    {
          save(TurbinePermissionPeer.getMapBuilder()
                .getDatabaseMap().getName());
      }

    /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.
       * Note: this code is here because the method body is
     * auto-generated conditionally and therefore needs to be
     * in this file instead of in the super class, BaseObject.
       *
     * @param dbName
     * @throws TorqueException
     */
    public void save(String dbName) throws TorqueException
    {
        Connection con = null;
          try
        {
            con = Transaction.begin(dbName);
            save(con);
            Transaction.commit(con);
        }
        catch(TorqueException e)
        {
            Transaction.safeRollback(con);
            throw e;
        }
      }

      /** flag to prevent endless save loop, if this object is referenced
        by another object which falls in this transaction. */
    private boolean alreadyInSave = false;
      /**
     * Stores the object in the database.  If the object is new,
     * it inserts it; otherwise an update is performed.  This method
     * is meant to be used as part of a transaction, otherwise use
     * the save() method and the connection details will be handled
     * internally
     *
     * @param con
     * @throws TorqueException
     */
    public void save(Connection con) throws TorqueException
    {
          if (!alreadyInSave)
        {
            alreadyInSave = true;


  
            // If this object has been modified, then save it to the database.
            if (isModified())
            {
                if (isNew())
                {
                    TurbinePermissionPeer.doInsert((TurbinePermission) this, con);
                    setNew(false);
                }
                else
                {
                    TurbinePermissionPeer.doUpdate((TurbinePermission) this, con);
                }
            }

                                      
                
            if (collTurbineRolePermissions != null)
            {
                for (int i = 0; i < collTurbineRolePermissions.size(); i++)
                {
                    ((TurbineRolePermission) collTurbineRolePermissions.get(i)).save(con);
                }
            }
                          alreadyInSave = false;
        }
      }


                          
      /**
     * Set the PrimaryKey using ObjectKey.
     *
     * @param  permissionId ObjectKey
     */
    public void setPrimaryKey(ObjectKey key)
        throws TorqueException
    {
            setPermissionId(new Integer(((NumberKey) key).intValue()));
        }

    /**
     * Set the PrimaryKey using a String.
     *
     * @param key
     */
    public void setPrimaryKey(String key) throws TorqueException
    {
            setPermissionId(new Integer(key));
        }

  
    /**
     * returns an id that differentiates this object from others
     * of its class.
     */
    public ObjectKey getPrimaryKey()
    {
          return SimpleKey.keyFor(getPermissionId());
      }

 
    /**
     * get an id that differentiates this object from others
     * of its class.
     */
    public String getQueryKey()
    {
        if (getPrimaryKey() == null)
        {
            return "";
        }
        else
        {
            return getPrimaryKey().toString();
        }
    }

    /**
     * set an id that differentiates this object from others
     * of its class.
     */
    public void setQueryKey(String key)
        throws TorqueException
    {
        setPrimaryKey(key);
    }

    /**
     * Makes a copy of this object.
     * It creates a new object filling in the simple attributes.
       * It then fills all the association collections and sets the
     * related objects to isNew=true.
       */
      public TurbinePermission copy() throws TorqueException
    {
        return copyInto(new TurbinePermission());
    }
  
    protected TurbinePermission copyInto(TurbinePermission copyObj) throws TorqueException
    {
          copyObj.setPermissionId(permissionId);
          copyObj.setName(name);
  
                    copyObj.setPermissionId((Integer)null);
                  
                                      
                
        List v = getTurbineRolePermissions();
        for (int i = 0; i < v.size(); i++)
        {
            TurbineRolePermission obj = (TurbineRolePermission) v.get(i);
            copyObj.addTurbineRolePermission(obj.copy());
        }
                    
        return copyObj;
    }

    /**
     * returns a peer instance associated with this om.  Since Peer classes
     * are not to have any instance attributes, this method returns the
     * same instance for all member of this class. The method could therefore
     * be static, but this would prevent one from overriding the behavior.
     */
    public TurbinePermissionPeer getPeer()
    {
        return peer;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("TurbinePermission:\n");
        str.append("PermissionId = ")
           .append(getPermissionId())
           .append("\n");
        str.append("Name = ")
           .append(getName())
           .append("\n");
        return(str.toString());
    }
}
