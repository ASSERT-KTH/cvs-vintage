package org.tigris.scarab.services.security;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.fulcrum.Service;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.TurbineServices;

/**
 * This class provides access to security properties
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ScarabSecurity.java,v 1.20 2003/02/04 11:26:03 jon Exp $
 */
public class ScarabSecurity 
    extends BaseService
    implements Service
{
    /** The name of the service */
    public static final String SERVICE_NAME = "ScarabSecurity";

    public static final String TOOL_KEY = 
        "services.PullService.tool.request.security";

    private static final String SCREEN_PREFIX = "screen.";
    private static final String ACTION_PREFIX = "action.";

    private static final String MAP_PREFIX = "map.";

    /**
     * String used to indicate that an Action module does not require
     * a permission.
     */
    public static final String NONE = "None";

    /** 
     * Specifies that a User is valid as an assignee for an issue.
     */
    public static final String ISSUE__EDIT = 
        getService().getPermissionImpl("Issue__Edit");

    /** 
     * Specifies that a User is allowed to enter an issue.
     */
    public static final String ISSUE__ENTER = 
        getService().getPermissionImpl("Issue__Enter");

    /** 
     * Specifies that a User is allowed to view an issue.
     */
    public static final String ISSUE__VIEW = 
        getService().getPermissionImpl("Issue__View");

    /** 
     * Specifies that a User is allowed to search for issues.
     */
    public static final String ISSUE__SEARCH = 
        getService().getPermissionImpl("Issue__Search");

    /** 
     * Specifies that a User is allowed to move or copy an issue.
     */
    public static final String ISSUE__MOVE = 
        getService().getPermissionImpl("Issue__Move");

    /** 
     * Specifies that a User is allowed to search for issues.
     */
    public static final String ISSUE__ATTACH = 
        getService().getPermissionImpl("Issue__Attach");

    /** 
     * Specifies that a User is allowed to search for issues.
     */
    public static final String ISSUE__ASSIGN = 
        getService().getPermissionImpl("Issue__Assign");

    /** 
     * Specifies that a User is allowed to approve a query.
     */
    public static final String ITEM__APPROVE = 
        getService().getPermissionImpl("Item__Approve");

    /** 
     * Specifies that a User is allowed to delete a query.
     */
    public static final String ITEM__DELETE = 
        getService().getPermissionImpl("Item__Delete");

    /** 
     * Specifies that a User is allowed to edit preferences.
     */
    public static final String USER__EDIT_PREFERENCES =         
        getService().getPermissionImpl("User__Edit_Preferences");

    /** 
     * Specifies that a User is allowed to administer a domain.
     */
    public static final String DOMAIN__ADMIN = 
        getService().getPermissionImpl("Domain__Admin");

    /** 
     * Specifies that a User is allowed to edit a domain.
     */
    public static final String DOMAIN__EDIT = 
        getService().getPermissionImpl("Domain__Edit");

    /** 
     * Specifies that a User is allowed to modify a project.
     */
    public static final String MODULE__EDIT = 
        getService().getPermissionImpl("Module__Edit");

    /** 
     * Specifies that a User is allowed to configure a project.
     */
    public static final String MODULE__CONFIGURE = 
        getService().getPermissionImpl("Module__Configure");

    /** 
     * Specifies that a User is allowed to add a project.
     */
    public static final String MODULE__ADD = 
        getService().getPermissionImpl("Module__Add");

    /**
     * User with this permission is allowed to approve roles requested
     * by other users.
     */
    public static final String USER__APPROVE_ROLES = 
        getService().getPermissionImpl("User__Approve_Roles");

    /** 
     * Specifies that a User is allowed to set up voting policies.
     */
    public static final String VOTE__MANAGE = 
        getService().getPermissionImpl("Vote__Manage");


    private Configuration props;
    private List allPermissions;

    public ScarabSecurity()
    {
    }

    public void init()
    {
        props = getConfiguration();
        setInit(true);
    }

    /**
     * Used in getAllPermissionsImpl to build the list
     */
    private void addPerm(List perms, String perm)
    {
        if (perm != null && perm.length() > 0 && !perms.contains(perm))
        {
            perms.add(perm);
        }
    }

    protected List getAllPermissionsImpl()
    {
        if (allPermissions == null) 
        {
            List tmpPerms = new ArrayList();
            addPerm(tmpPerms, ScarabSecurity.ISSUE__EDIT);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__ENTER);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__SEARCH);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__VIEW);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__ASSIGN);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__ATTACH);
            addPerm(tmpPerms, ScarabSecurity.ISSUE__MOVE);
            addPerm(tmpPerms, ScarabSecurity.ITEM__APPROVE);
            addPerm(tmpPerms, ScarabSecurity.ITEM__DELETE);
            addPerm(tmpPerms, ScarabSecurity.DOMAIN__ADMIN);
            addPerm(tmpPerms, ScarabSecurity.DOMAIN__EDIT);
            addPerm(tmpPerms, ScarabSecurity.MODULE__EDIT);
            addPerm(tmpPerms, ScarabSecurity.MODULE__CONFIGURE);
            addPerm(tmpPerms, ScarabSecurity.MODULE__ADD);
            addPerm(tmpPerms, ScarabSecurity.USER__EDIT_PREFERENCES);
            addPerm(tmpPerms, ScarabSecurity.USER__APPROVE_ROLES);
            addPerm(tmpPerms, ScarabSecurity.VOTE__MANAGE);
            allPermissions = tmpPerms;
        }
        
        return allPermissions;
    }

    protected String getScreenPermissionImpl(String screen)
    {
        String t = screen.replace(',','.');
        return getPermissionImpl(props.getString(SCREEN_PREFIX + t));
    }

    protected String getActionPermissionImpl(String action)
    {
        String perm = null;
        String property = props.getString(ACTION_PREFIX + action);
        if (NONE.equals(property)) 
        {
            perm = NONE;
        }
        else 
        {
            perm = getPermissionImpl(property);
        }
        return perm;
    }

    protected String getPermissionImpl(String permConstant)
    {
        return props.getString(MAP_PREFIX + permConstant);
    }


    // *******************************************************************
    // static accessors
    // *******************************************************************

    public static List getAllPermissions()
    {
        return getService().getAllPermissionsImpl();
    }

    public static String getScreenPermission(String screen)
    {
        return getService().getScreenPermissionImpl(screen);
    }

    public static String getActionPermission(String action)
    {
        return getService().getActionPermissionImpl(action);
    }

    /*
    public static String getPermission(String task)
    {
        return getService().getPermissionImpl(task);
    }
    */

    public static Configuration getProps()
    {
        return getService().getConfiguration();
    }

    /**
     * Gets the <code>LocalizationService</code> implementation.
     *
     * @return the LocalizationService implementation.
     */
    protected static final ScarabSecurity getService()
    {
        return (ScarabSecurity) TurbineServices.getInstance()
                .getService(ScarabSecurity.SERVICE_NAME);
    }

}
