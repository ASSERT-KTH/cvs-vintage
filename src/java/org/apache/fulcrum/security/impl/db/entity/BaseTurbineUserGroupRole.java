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
 * extended all references should be to TurbineUserGroupRole
 */
public abstract class BaseTurbineUserGroupRole extends BaseObject
    implements org.apache.fulcrum.intake.Retrievable
{
    /** The Peer class */
    private static final TurbineUserGroupRolePeer peer =
        new TurbineUserGroupRolePeer();

        
    /** The value for the userId field */
    private Integer userId;
      
    /** The value for the groupId field */
    private Integer groupId;
      
    /** The value for the roleId field */
    private Integer roleId;
  
    
    /**
     * Get the UserId
     *
     * @return Integer
     */
    public Integer getUserId()
    {
        return userId;
    }

                              
    /**
     * Set the value of UserId
     *
     * @param v new value
     */
    public void setUserId(Integer v) throws TorqueException
    {
    
                  if (!ObjectUtils.equals(this.userId, v))
              {
            this.userId = v;
            setModified(true);
        }
    
                          
                if (aTurbineUser != null && !ObjectUtils.equals(aTurbineUser.getUserId(), v))
                {
            aTurbineUser = null;
        }
      
              }
  
    /**
     * Get the GroupId
     *
     * @return Integer
     */
    public Integer getGroupId()
    {
        return groupId;
    }

                              
    /**
     * Set the value of GroupId
     *
     * @param v new value
     */
    public void setGroupId(Integer v) throws TorqueException
    {
    
                  if (!ObjectUtils.equals(this.groupId, v))
              {
            this.groupId = v;
            setModified(true);
        }
    
                          
                if (aTurbineGroup != null && !ObjectUtils.equals(aTurbineGroup.getGroupId(), v))
                {
            aTurbineGroup = null;
        }
      
              }
  
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
  
      
    
                  
    
        private TurbineUser aTurbineUser;

    /**
     * Declares an association between this object and a TurbineUser object
     *
     * @param v TurbineUser
     * @throws TorqueException
     */
    public void setTurbineUser(TurbineUser v) throws TorqueException
    {
            if (v == null)
        {
                    setUserId((Integer) null);
                  }
        else
        {
            setUserId(v.getUserId());
        }
                aTurbineUser = v;
    }

                                            
    /**
     * Get the associated TurbineUser object
     *
     * @return the associated TurbineUser object
     * @throws TorqueException
     */
    public TurbineUser getTurbineUser() throws TorqueException
    {
        if (aTurbineUser == null && (!ObjectUtils.equals(this.userId, null)))
        {
                          aTurbineUser = TurbineUserPeer.retrieveByPK(SimpleKey.keyFor(this.userId));
              
            /* The following can be used instead of the line above to
               guarantee the related object contains a reference
               to this object, but this level of coupling
               may be undesirable in many circumstances.
               As it can lead to a db query with many results that may
               never be used.
               TurbineUser obj = TurbineUserPeer.retrieveByPK(this.userId);
               obj.addTurbineUserGroupRoles(this);
            */
        }
        return aTurbineUser;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey.  e.g.
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
           */
    public void setTurbineUserKey(ObjectKey key) throws TorqueException
    {
      
                        setUserId(new Integer(((NumberKey) key).intValue()));
                  }
    
    
                  
    
        private TurbineGroup aTurbineGroup;

    /**
     * Declares an association between this object and a TurbineGroup object
     *
     * @param v TurbineGroup
     * @throws TorqueException
     */
    public void setTurbineGroup(TurbineGroup v) throws TorqueException
    {
            if (v == null)
        {
                    setGroupId((Integer) null);
                  }
        else
        {
            setGroupId(v.getGroupId());
        }
                aTurbineGroup = v;
    }

                                            
    /**
     * Get the associated TurbineGroup object
     *
     * @return the associated TurbineGroup object
     * @throws TorqueException
     */
    public TurbineGroup getTurbineGroup() throws TorqueException
    {
        if (aTurbineGroup == null && (!ObjectUtils.equals(this.groupId, null)))
        {
                          aTurbineGroup = TurbineGroupPeer.retrieveByPK(SimpleKey.keyFor(this.groupId));
              
            /* The following can be used instead of the line above to
               guarantee the related object contains a reference
               to this object, but this level of coupling
               may be undesirable in many circumstances.
               As it can lead to a db query with many results that may
               never be used.
               TurbineGroup obj = TurbineGroupPeer.retrieveByPK(this.groupId);
               obj.addTurbineUserGroupRoles(this);
            */
        }
        return aTurbineGroup;
    }

    /**
     * Provides convenient way to set a relationship based on a
     * ObjectKey.  e.g.
     * <code>bar.setFooKey(foo.getPrimaryKey())</code>
     *
           */
    public void setTurbineGroupKey(ObjectKey key) throws TorqueException
    {
      
                        setGroupId(new Integer(((NumberKey) key).intValue()));
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
               obj.addTurbineUserGroupRoles(this);
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
              fieldNames.add("UserId");
              fieldNames.add("GroupId");
              fieldNames.add("RoleId");
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
          if (name.equals("UserId"))
        {
                return getUserId();
            }
          if (name.equals("GroupId"))
        {
                return getGroupId();
            }
          if (name.equals("RoleId"))
        {
                return getRoleId();
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
          if (name.equals(TurbineUserGroupRolePeer.USER_ID))
        {
                return getUserId();
            }
          if (name.equals(TurbineUserGroupRolePeer.GROUP_ID))
        {
                return getGroupId();
            }
          if (name.equals(TurbineUserGroupRolePeer.ROLE_ID))
        {
                return getRoleId();
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
                return getUserId();
            }
              if (pos == 1)
        {
                return getGroupId();
            }
              if (pos == 2)
        {
                return getRoleId();
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
          save(TurbineUserGroupRolePeer.getMapBuilder()
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
                    TurbineUserGroupRolePeer.doInsert((TurbineUserGroupRole) this, con);
                    setNew(false);
                }
                else
                {
                    TurbineUserGroupRolePeer.doUpdate((TurbineUserGroupRole) this, con);
                }
            }

                      alreadyInSave = false;
        }
      }


                                                                      
  
    private final SimpleKey[] pks = new SimpleKey[3];
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
                      setUserId(new Integer(((NumberKey)keys[0]).intValue()));
                        setGroupId(new Integer(((NumberKey)keys[1]).intValue()));
                        setRoleId(new Integer(((NumberKey)keys[2]).intValue()));
              }

    /**
     * Set the PrimaryKey using SimpleKeys.
     *
         * @param Integer userId
         * @param Integer groupId
         * @param Integer roleId
         */
    public void setPrimaryKey( Integer userId, Integer groupId, Integer roleId)
        throws TorqueException
    {
            setUserId(userId);
            setGroupId(groupId);
            setRoleId(roleId);
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
              pks[0] = SimpleKey.keyFor(getUserId());
                  pks[1] = SimpleKey.keyFor(getGroupId());
                  pks[2] = SimpleKey.keyFor(getRoleId());
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
      public TurbineUserGroupRole copy() throws TorqueException
    {
        return copyInto(new TurbineUserGroupRole());
    }
  
    protected TurbineUserGroupRole copyInto(TurbineUserGroupRole copyObj) throws TorqueException
    {
          copyObj.setUserId(userId);
          copyObj.setGroupId(groupId);
          copyObj.setRoleId(roleId);
  
                    copyObj.setUserId((Integer)null);
                              copyObj.setGroupId((Integer)null);
                              copyObj.setRoleId((Integer)null);
            
        
        return copyObj;
    }

    /**
     * returns a peer instance associated with this om.  Since Peer classes
     * are not to have any instance attributes, this method returns the
     * same instance for all member of this class. The method could therefore
     * be static, but this would prevent one from overriding the behavior.
     */
    public TurbineUserGroupRolePeer getPeer()
    {
        return peer;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append("TurbineUserGroupRole:\n");
        str.append("UserId = ")
           .append(getUserId())
           .append("\n");
        str.append("GroupId = ")
           .append(getGroupId())
           .append("\n");
        str.append("RoleId = ")
           .append(getRoleId())
           .append("\n");
        return(str.toString());
    }
}
