package org.tigris.scarab.security;

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

import java.util.List;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.ScarabUser;

/**
 * A security interface to Turbine/Helm/... security mechanisms.
 * Constants for permissions should be grouped here as well.
 *
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ScarabSecurity.java,v 1.10 2001/09/06 20:57:42 elicia Exp $
*/
public interface ScarabSecurity
{
    public static final String TOOL_KEY = 
        "services.PullService.tool.request.security";

    /** 
     * Specifies that a User is valid as an assignee for an issue.
     */
    public static final String ISSUE__EDIT = "Issue | Edit";

    /** 
     * Specifies that a User is allowed to enter an issue.
     */
    public static final String ISSUE__ENTER = "Issue | Enter";

    /** 
     * Specifies that a User is allowed to approve a query.
     */
    public static final String QUERY__APPROVE = "Query | Approve";


    /**
     * Determine if a user has a permission within a module.
     *
     * @param permission a <code>String</code> permission value, which should
     * be a constant in this interface.
     * @param user a <code>ScarabUser</code> value
     * @param module a <code>ModuleEntity</code> value
     * @return true if the permission exists for the user within the
     * given module, false otherwise
     */
    public boolean hasPermission(String permission, 
                                 ScarabUser user, ModuleEntity module);

    /**
     * Get a list of <code>ScarabUser</code>'s that have the given
     * permission in the given module.
     *
     * @param permission a <code>String</code> value
     * @param module a <code>ModuleEntity</code> value
     * @return a <code>List</code> of <code>ScarabUser</code>'s
     */
    public ScarabUser[] getUsers(String permission, ModuleEntity module);

    /**
     * Get a list of <code>ModuleEntity</code>'s that where a user has
     * the permissions given.
     *
     * @param user a <code>ScarabUser</code> value
     * @param permission a <code>String</code> value
     * @return a <code>ModuleEntity[]</code> value
     */
    public ModuleEntity[] getModules(ScarabUser user, String permission);

    /**
     * Get a list of <code>ModuleEntity</code>'s that where a user has
     * at least one of the permissions given.
     *
     * @param user a <code>ScarabUser</code> value
     * @param permissions a <code>String[]</code> value
     * @return a <code>ModuleEntity[]</code> value
     */
    public ModuleEntity[] getModules(ScarabUser user, String[] permissions);

    public List getRoles(ScarabUser user, ModuleEntity module) throws Exception;
}    


