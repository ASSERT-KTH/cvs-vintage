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
 * extended all references should be to TurbineRolePermission
 */
public abstract class BaseTurbineRolePermission extends BaseObject
    implements org.apache.fulcrum.intake.Retrievable
{
    /** The Peer class */
    private static final TurbineRolePermissionPeer peer =
        new TurbineRolePermissionPeer();

        
    /** The value for the roleId field */
    private Integer roleId;
      
    /** The value for the permissionId field */
    private Integer permissionId;
  
    
    /**
     * Get the RoleId
     *
     * @return Integer
     */
    public Integer getRoleId()
    {
        return roleId;
    }

                              
    /**
     * Set the value of RoleId
     *
     * @param v new value
     */
    public void setRoleId(Integer v) throws TorqueException
    {
    
                  if (!ObjectUtils.equals(this.roleId, v))
              {
            this.roleId = v;
            setModified(true);
        }
    
                          
                if (aTurbineRole != null && !ObjectUtils.equals(aTurbineRole.getRoleId(), v))
                {
            aTurbineRole = null;
        }
      
              }
  
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
    
                          
                if (aTurbinePermission != null && !ObjectUtils.equals(aTurbinePermission.getPermissionId(), v))
                {
            aTurbinePermission = null;
        }
      
              }
  
      
    
                  
    
        private TurbineRole aTurbineRole;

    /**
     * Declares an association between this object and a TurbineRole object
     *
     * @param v TurbineRole
     * @throws TorqueException
     */
    public void setTurbineRole(TurbineRole v) throws TorqueException
    {
            if (v == null)
        {
                    setRoleId((Integer) null);
                  }
        else
        {
            setRoleId(v.getRoleId());
        }
                aTurbineRole = v;
    }

                                            
    /**
     * Get the associated TurbineRole object
     *
     * @return the associated TurbineRole object
     * @throws TorqueException
     */
    public TurbineRole getTurbineRole() throws TorqueException
    {
        if (aTurbineRole == null && (!ObjectUtils.equals(this.roleId, null)))
        {
                          aTurbineRole = TurbineRolePeer.retrieveByPK(SimpleKey.keyFor(this.roleId));
              
            /* The following can be used instead of the line above to
               guarantee the related object contains a reference
               to this object, but this level of coupling
               may be undesirable in many circumstances.
               As it can lead to a db query with many results that may
               never be used.
               TurbineRole obj = TurbineRolePeer.retrieveByPK(this.roleId);
               obj.addTurbineRolePermissions(this);
            */
        }
        return aTurbineRole;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey.  e.g.
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
           */
    public void setTurbineRoleKey(ObjectKey key) throws TorqueException
    {
      
                        setRoleId(new Integer(((NumberKey) key).intValue()));
                  }
    
    
                  
    
        private TurbinePermission aTurbinePermission;

    /**
     * Declares an association between this object and a TurbinePermission object
     *
     * @param v TurbinePermission
     * @throws TorqueException
     */
    public void setTurbinePermission(TurbinePermission v) throws TorqueException
    {
            if (v == null)
        {
                    setPermissionId((Integer) null);
                  }
        else
        {
            setPermissionId(v.getPermissionId());
        }
                aTurbinePermission = v;
    }

                                            
    /**
     * Get the associated TurbinePermission object
     *
     * @return the associated TurbinePermission object
     * @throws TorqueException
     */
    public TurbinePermission getTurbinePermission() throws TorqueException
    {
        if (aTurbinePermission == null && (!ObjectUtils.equals(this.permissionId, null)))
        {
                          aTurbinePermission = TurbinePermissionPeer.retrieveByPK(SimpleKey.keyFor(this.permissionId));
              
            /* The following can be used instead of the line above to
               guarantee the related object contains a reference
               to this object, but this level of coupling
               may be undesirable in many circumstances.
               As it can lead to a db query with many results that may
               never be used.
               TurbinePermission obj = TurbinePermissionPeer.retrieveByPK(this.permissionId);
               obj.addTurbineRolePermissions(this);
            */
        }
        return aTurbinePermission;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey.  e.g.
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
           */
    public void setTurbinePermissionKey(ObjectKey key) throws TorqueException
    {
      
                        setPermissionId(new Integer(((NumberKey) key).intValue()));
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
              fieldNames.add("RoleId");
              fieldNames.add("PermissionId");
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
          if (name.equals("RoleId"))
        {
                return getRoleId();
            }
          if (name.equals("PermissionId"))
        {
                return getPermissionId();
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
          if (name.equals(TurbineRolePermissionPeer.ROLE_ID))
        {
                return getRoleId();
            }
          if (name.equals(TurbineRolePermissionPeer.PERMISSION_ID))
        {
                return getPermissionId();
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
                return getRoleId();
            }
              if (pos == 1)
        {
                return getPermissionId();
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
          save(TurbineRolePermissionPeer.getMapBuilder()
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
                    TurbineRolePermissionPeer.doInsert((TurbineRolePermission) this, con);
                    setNew(false);
                }
                else
                {
                    TurbineRolePermissionPeer.doUpdate((TurbineRolePermission) this, con);
                }
            }

                      alreadyInSave = false;
        }
      }


                                                
  
    private final SimpleKey[] pks = new SimpleKey[2];
    private final ComboKey comboPK = new ComboKey(pks);
    
    /**
     * Set the PrimaryKey with an ObjectKey
     *
     * @param key
     */
    public void setPrimaryKey(ObjectKey key) throws TorqueException
    {
        SimpleKey[] keys = (SimpleKey[]) key.getValue();
        SimpleKey tmpKey = null;
                      setRoleId(new Integer(((NumberKey)keys[0]).intValue()));
                        setPermissionId(new Integer(((NumberKey)keys[1]).intValue()));
              }

    /**
     * Set the PrimaryKey using SimpleKeys.
     *
         * @param Integer roleId
         * @param Integer permissionId
         */
    public void setPrimaryKey( Integer roleId, Integer permissionId)
        throws TorqueException
    {
            setRoleId(roleId);
            setPermissionId(permissionId);
        }

    /**
     * Set the PrimaryKey using a String.
     */
    public void setPrimaryKey(String key) throws TorqueException
    {
        setPrimaryKey(new ComboKey(key));
    }
  
    /**
     * returns an id that differentiates this object from others
     * of its class.
     */
    public ObjectKey getPrimaryKey()
    {
              pks[0] = SimpleKey.keyFor(getRoleId());
                  pks[1] = SimpleKey.keyFor(getPermissionId());
                  return comboPK;
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
      public TurbineRolePermission copy() throws TorqueException
    {
        return copyInto(new TurbineRolePermission());
    }
  
    protected TurbineRolePermission copyInto(TurbineRolePermission copyObj) throws TorqueException
    {
          copyObj.setRoleId(roleId);
          copyObj.setPermissionId(permissionId);
  
                    copyObj.setRoleId((Integer)null);
                              copyObj.setPermissionId((Integer)null);
            
        
        return copyObj;
    }

    /**
     * returns a peer instance associated with this om.  Since Peer classes
     * are not to have any instance attributes, this method returns the
     * same instance for all member of this class. The method could therefore
     * be static, but this would prevent one from overriding the behavior.
     */
    public TurbineRolePermissionPeer getPeer()
    {
        return peer;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("TurbineRolePermission:\n");
        str.append("RoleId = ")
           .append(getRoleId())
           .append("\n");
        str.append("PermissionId = ")
           .append(getPermissionId())
           .append("\n");
        return(str.toString());
    }
}
