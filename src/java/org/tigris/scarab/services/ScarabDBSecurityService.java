package org.tigris.scarab.services;


/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.*;

import org.apache.turbine.services.security.impl.db.DBSecurityService;
import org.apache.turbine.services.security.util.GroupSet;
import org.apache.turbine.services.security.util.PermissionSet;
import org.apache.turbine.services.security.util.RoleSet;
import org.apache.turbine.services.security.util.DataBackendException;
import org.apache.turbine.services.security.util.EntityExistsException;
import org.apache.turbine.services.security.util.UnknownEntityException;
import org.apache.turbine.services.security.util.TurbineSecurityException;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.util.Log;

import org.apache.turbine.services.security.entity.Group;
// import org.apache.turbine.om.security.*;
import org.apache.turbine.services.db.om.Persistent;

import org.tigris.scarab.util.*;
import org.tigris.scarab.om.ModulePeer;
import org.tigris.scarab.services.module.ModuleEntity;


/**
 * Implementation of turbine's SecurityService to account for the ScarabModule
 * being the Group implementation.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ScarabDBSecurityService.java,v 1.5 2001/07/17 21:40:31 jon Exp $
 */
public class ScarabDBSecurityService extends DBSecurityService
{
    /**
     * Retrieve a set of Groups that meet the specified Criteria.
     *
     * @param a Criteria of Group selection.
     * @return a set of Groups that meet the specified Criteria.
     */
    public GroupSet getGroups( Criteria criteria )
        throws DataBackendException
    {
        // only supporting getting all modules through this method.
        if ( criteria.size() != 0 ) 
        {
            throw new DataBackendException(
                "Partial selection of groups not implemented");
        }
        /*
        Criteria dbCriteria = new Criteria();
        Iterator keys = criteria.keySet().iterator();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            dbCriteria.put(GroupPeer.getColumnName(key), criteria.get(key));
        }
        */
        Vector groups = new Vector(0);
        try
        {
            groups = ModulePeer.doSelect(criteria);
        }
        catch(Exception e)
        {
            throw new DataBackendException("getGroups(Criteria) failed", e);
        }

        return new GroupSet(groups);
    }



    /**
     * Stores Group's attributes. The Groups is required to exist in the system.
     *
     * @param group The Group to be stored.
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public void saveGroup( Group group )
        throws DataBackendException, UnknownEntityException
    {
        boolean groupExists = false;
        try
        {
            if ( !((Persistent)group).isNew() ) 
            {
                group.save();
            }
        }
        catch(Exception e)
        {
            throw new DataBackendException("saveGroup(Group) failed" ,e);
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
    }



    /**
     * Retrieves a new Group. It creates
     * a new Group based on the Services Group implementation. It does not
     * create a new Group in the system though. Use create for that.
     *
     * @param groupName The name of the Group to be retrieved.
     */
    public Group getNewGroup( String groupName )
    {
        throw new RuntimeException("getNewGroup NOT implemented.");
            /*    
        ModuleEntity module = ModuleManager.getInstance();
        module.setName(groupName);
        return (Group) module;
            */
    }


    /**
     * Creates a new group with specified attributes.
     *
     * @param group the object describing the group to be created.
     * @return a new Group object that has id set up properly.
     * @throws DataBackendException if there was an error accessing the data backend.
     * @throws EntityExistsException if the group already exists.
     */
    public synchronized Group addGroup( Group group ) 
        throws DataBackendException, EntityExistsException
    {
        boolean groupExists = false;
        try
        {
            lockExclusive();
            if ( ((Persistent)group).isNew() ) 
            {
                group.save();
                // add the group to system-wide cache
                getAllGroups().add(group);
                unlockExclusive();
                return group;
            }
        }
        catch(Exception e)
        {
            throw new DataBackendException("addGroup(Group) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        // the only way we could get here without return/throw tirggered
        // is that the groupExists was true.
        throw new EntityExistsException("Group '" + group + 
            "' already exists");
    }


    /**
     * Removes a Group from the system.
     *
     * @param the object describing group to be removed.
     * @throws DataBackendException if there was an error accessing the 
     * data backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public synchronized void removeGroup( Group group )
        throws DataBackendException, UnknownEntityException
    {
        try
        {
            lockExclusive();
            if ( !((Persistent)group).isNew() ) 
            {
                ((ModuleEntity)group).setDeleted(true);
                group.save();
                getAllGroups().remove(group);
            }
        }
        catch(Exception e)
        {
            Log.error("Failed to delete a Group");
            Log.error(e);
            throw new DataBackendException("removeGroup(Group) failed", e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
    }


    /**
     * Renames an existing Group.
     *
     * @param the object describing the group to be renamed.
     * @param name the new name for the group.
     * @throws DataBackendException if there was an error accessing the 
     * data backend.
     * @throws UnknownEntityException if the group does not exist.
     */
    public synchronized void renameGroup( Group group, String name )
        throws DataBackendException, UnknownEntityException
    {
        throw new DataBackendException("rename is not supported");

        /* this stuff is cut-n-paste
        boolean groupExists = false;
        try
        {
            lockExclusive();
            groupExists = checkExists(group);
            if(groupExists)
            {
                ((SecurityObject)group).setName(name);
                Criteria criteria = GroupPeer.buildCriteria(group);
                GroupPeer.doUpdate(criteria);
                return;
            }
        }
        catch(Exception e)
        {
            throw new DataBackendException("renameGroup(Group,String)" ,e);
        }
        finally
        {
            unlockExclusive();
        }
        throw new UnknownEntityException("Unknown group '" + group + "'");
        */
    }

    /**
     * Determines if the <code>Group</code> exists in the security system.
     *
     * @param group a <code>Group</code> value
     * @return true if the group exists in the system, false otherwise
     * @throws DataBackendException when more than one Group with 
     *         the same name exists.
     * @throws Exception, a generic exception.
     */
    protected boolean checkExists(Group group)
        throws DataBackendException, Exception
    {
        return ModulePeer.checkExists(group);
    }
}
